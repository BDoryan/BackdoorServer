package isotopestudio.backdoor.network.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import doryanbessiere.isotopestudio.commons.GsonInstance;
import isotopestudio.backdoor.core.elements.GameElement;
import isotopestudio.backdoor.core.elements.GameElementType;
import isotopestudio.backdoor.core.map.MapData;
import isotopestudio.backdoor.core.party.Party;
import isotopestudio.backdoor.core.player.Player;
import isotopestudio.backdoor.core.team.Team;
import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.packet.PacketListener;
import isotopestudio.backdoor.network.packet.packets.PacketLoadMap;
import isotopestudio.backdoor.network.packet.packets.PacketPing;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerDisconnect;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerKick;
import isotopestudio.backdoor.network.player.NetworkedPlayer;
import isotopestudio.backdoor.network.server.command.ICommand;

public class GameServer extends Thread {

	public static GameServer gameServer;

	public static void main(String[] args) {
		ICommand.listenJavaConsole().start();
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if(GameServer.mapData == null)return;
				for (GameElement node : GameServer.mapData.getElements().values()) {
					if(node.getType() == GameElementType.SERVER)continue;
					if(node.getTeam() == null)continue;
					for (NetworkedPlayer player : node.getTeam().getPlayers()) {
						player.addMoney(5);
					}
				}
			}
		}, 0, 5000);
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if(GameServer.mapData == null)return;
				for (GameElement server : GameServer.mapData.getTeamServers().values()) {
					for (Player player : server.getTeam().getPlayers()) {
						player.addMoney(1);
					}
				}
			}
		}, 0, 10000);
		gameServer = new GameServer(66);
		gameServer.start();
	}

	private int port;

	public GameServer(int port) {
		this.port = port;
	}

	private ArrayList<GameServerClient> clients = new ArrayList<GameServerClient>();
	private ArrayList<NetworkedPlayer> players = new ArrayList<NetworkedPlayer>();

	public ArrayList<GameServerClient> getClients() {
		return clients;
	}

	public ArrayList<NetworkedPlayer> getPlayers() {
		return players;
	}

	private ServerSocket serverSocket;

	@Override
	public void run() {
		try {
			serverSocket = new ServerSocket(port);
			System.out.println("Game server is online on port -> " + port);
			new Thread(new Runnable() {

				long ms = 1000; // 1 second

				@Override
				public void run() {
					System.out.println("Starting the slow connection checker...");
					while (!serverSocket.isClosed()) {
						try {
							Thread.sleep(ms);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						for (NetworkedPlayer player : getPlayers()) {
							player.sendPacket(new PacketPing(System.currentTimeMillis()));
						}
					}
				}
			}).start();
			System.out.println("Waiting player...");
			while (!serverSocket.isClosed()) {
				Socket socket = serverSocket.accept();
				System.out.println(
						"Socket connected -> " + socket.getInetAddress().getHostAddress() + ":" + socket.getPort());
				GameServerClient gameServerClient = new GameServerClient(this, socket);
				clients.add(gameServerClient);
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						if (clients.contains(gameServerClient) && !gameServerClient.isConnected()) {
							gameServerClient.disconnect("identification took too long!");
							System.out.println("Socket kicked -> " + socket.getInetAddress().getHostAddress() + ":"
									+ socket.getPort());
						}
					}
				}, 2000);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			this.serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void sendAll(Packet packet) {
		for (NetworkedPlayer player : getPlayers()) {
			player.sendPacket(packet);
		}
	}

	public static class GameServerClient extends NetworkedPlayer {

		private GameServer gameServer;
		private Socket socket;

		public GameServerClient(GameServer gameServer, Socket socket) {
			try {
				this.gameServer = gameServer;
				this.socket = socket;
				DataInputStream input = new DataInputStream(socket.getInputStream());
				DataOutputStream output = new DataOutputStream(socket.getOutputStream());

				initialize(socket, input, output);

				receiver().start();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		private Player player;

		public void setPlayer(Player player) {
			this.player = player;
		}

		public Player getPlayer() {
			return player;
		}

		public Thread receiver() {
			Thread thread = new Thread(new Runnable() {
				@Override
				public void run() {
					setConnected(true);
					while (isConnected()) {
						try {
							Packet packet = readPacket();
							gameServer.readPacket(packet, GameServerClient.this);
						} catch (IOException e) {
							if (!isConnected())
								return;
							PacketPlayerDisconnect.disconnect(gameServer, GameServerClient.this, "connection_lost");
							break;
						}
					}
				}
			});
			return thread;
		}

		@Override
		public boolean waitPacket(Packet packet_target, long timeout) {
			AtomicBoolean received = new AtomicBoolean(false);
			PacketListener listener = new PacketListener() {
				@Override
				public void sended(Packet packet) {
				}

				@Override
				public void received(Packet packet) {
					if (packet.getId() == packet_target.getId()) {
						received.set(true);
					}
				}
			};
			getPacketListeners().add(listener);
			long timeleft = System.currentTimeMillis() + timeout;
			while (!received.get()) {
				if (timeleft - System.currentTimeMillis() < 0) {
					// Timeout
					break;
				}
			}
			getPacketListeners().remove(listener);
			return received.get();
		}

		public void disconnect(String reason) {
			gameServer.getPlayers().remove(this);
			for (NetworkedPlayer player : gameServer.getPlayers()) {
				player.sendPacket(new PacketPlayerDisconnect(getUUID(), reason));
			}
			disconnected();
			setConnected(false);
			close();
		}

		public void disconnected() {
			if (getTeam() != null)
				getTeam().leave(this);
		}

		public void kick(String reason) {
			sendPacket(new PacketPlayerKick(reason));
			PacketPlayerDisconnect.disconnect(gameServer, this, reason);
		}

		public void sendMap(MapData mapData) {
			PacketLoadMap packetLoadMap = new PacketLoadMap(GsonInstance.instance().toJson(mapData));
			sendPacket(packetLoadMap);
		}

		@Override
		public void setPing(long ping) {
			super.setPing(ping);
		}

		public Socket getSocket() {
			return socket;
		}

		@Override
		public void close() {
			gameServer.getClients().remove(this);
			if (gameServer.getPlayers().contains(this))
				gameServer.getPlayers().remove(this);
			super.close();
		}
	}

	public boolean isFull() {
		boolean is_full = true;
		for (Team team : Team.values()) {
			if (team.getPlayers().size() < team.getMaxPlayers()) {
				is_full = false;
			}
		}
		return is_full;
	}

	public synchronized void readPacket(Packet packet, GameServerClient client) {
		try {
			if (packet != null) {
				packet.read();
				packet.process(gameServer, client);
				if (packet != null) {
					if (!client.getPacketListeners().isEmpty()) {
						for (PacketListener listener : client.getPacketListeners()) {
							listener.received(packet);
						}
					}
				}
			}
		} catch (Exception e) {
			if (!client.isConnected())
				return;
			// packet error and not connection lost
			e.printStackTrace();
		}
	}

	private Party party;

	public Party getParty() {
		return party;
	}

	public void startParty() {
		if (party == null) {
			party = new Party(this, GameServer.getMap());
			party.start();
		}
	}

	public void stopParty() {
		if (party != null && party.isStarted()) {
			party.stop();
			party = null;
		}
	}

	private static MapData mapData = null;

	public static MapData getMap() {
		if (mapData == null) {
			mapData = MapData.mapDefault();
		}
		return mapData;
	}
}
