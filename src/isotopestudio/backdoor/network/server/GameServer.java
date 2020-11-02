package isotopestudio.backdoor.network.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import doryanbessiere.isotopestudio.commons.GsonInstance;
import doryanbessiere.isotopestudio.commons.mysql.SQLDatabase;
import isotopestudio.backdoor.core.elements.GameElement;
import isotopestudio.backdoor.core.elements.GameElementType;
import isotopestudio.backdoor.core.gamescript.GameScript;
import isotopestudio.backdoor.core.map.MapData;
import isotopestudio.backdoor.core.player.Player;
import isotopestudio.backdoor.core.server.configuration.GameServerConfiguration;
import isotopestudio.backdoor.core.team.Team;
import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.packet.PacketListener;
import isotopestudio.backdoor.network.packet.packets.PacketLoadMap;
import isotopestudio.backdoor.network.packet.packets.PacketPing;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerDisconnect;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerKick;
import isotopestudio.backdoor.network.server.command.ICommand;
import isotopestudio.backdoor.network.server.gamescripts.GameScriptsManager;
import isotopestudio.backdoor.network.server.map.ServerMapData;
import isotopestudio.backdoor.network.server.party.Party;
import isotopestudio.backdoor.network.server.party.TeamManager;
import isotopestudio.backdoor.network.server.player.NetworkedPlayer;

public class GameServer extends Thread {

	public static String VERSION = null;

	public static GameServer gameServer;
	public static SQLDatabase database;
	
	public static GameServerConfiguration configuration;
	
	public static boolean offcialServer = false;
	
	public static void main(String[] args) {
		try {
			java.io.InputStream is = GameServer.class.getClass().getResourceAsStream("/maven.properties");
			java.util.Properties p = new Properties();
			p.load(is);

			VERSION = p.getProperty("VERSION");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		String jsonConfiguration = "";
		for(int i = 0; i < args.length; i++) {
			jsonConfiguration += " " + args[i];
		}
		
		configuration = GsonInstance.instance().fromJson(jsonConfiguration, GameServerConfiguration.class);

		if (configuration.hasDatabase()) {
			try {
				String host = configuration.getMysqlHost();
				String database = configuration.getMysqlDatabase();
				String username = configuration.getMysqlUsername();
				String password = configuration.getMysqlPassword();

				GameServer.database = new SQLDatabase(host, database, username, password);
				GameServer.database.connect();
				
				offcialServer = true;
			} catch (Exception e) {
				e.printStackTrace();
				System.err.println("Not every argument is recoverable!");
			}
		}

		ICommand.listenJavaConsole().start();
		new Timer().schedule(new TimerTask() {
			@Override
			public void run() {
				if (GameServer.mapData == null)
					return;
				for (GameElement node : GameServer.mapData.getElements().values()) {
					if (node.getType() == GameElementType.SERVER)
						continue;
					if (node.getTeam() == null)
						continue;
					if (!node.isLinked())
						return;
					for (NetworkedPlayer player : TeamManager.getPlayers(node.getTeam())) {
						player.addMoney(5);
					}
				}
			}
		}, 0, 5000);

		TeamManager.init();
		
		/**
		 * Synthax: <team path>=<maximum players> 
		 * 
		 * Example arguments: 
		 * team_red=5 
		 */
		for(Team team : Team.values()) {
				TeamManager.max_players.put(team, configuration.getVersus().getMaximum());
				
				System.out.println("Max players for "+team.toString().toUpperCase()+" is now "+TeamManager.max_players.get(team));
		}

		gameServer = new GameServer(configuration.getPort(), configuration.getPassword());
		gameServer.start();
	}
	
	/**
	 * @return the configuration
	 */
	public static GameServerConfiguration getConfiguration() {
		return configuration;
	}

	public static boolean isOfficialServer() {
		return offcialServer;
	}

	public static SQLDatabase getDatabase() {
		return database;
	}

	private int port;
	private String password;

	public GameServer(int port, String password) {
		this.port = port;
		this.password = password;
	}

	
	/**
	 * @return true if the don't use a password
	 */
	public boolean isPublicServer() {
		return password == null;
	}
	
	/**
	 * @return true if the use a password
	 */
	public boolean isPrivateServer() {
		return password != null;
	}
	
	/**
	 * @return the password
	 */
	public String getPassword() {
		return password;
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
			System.out.println("Initializing game system");
			GameScriptsManager.init();

			serverSocket = new ServerSocket(port);
			System.out.println("Game server (" + VERSION + ") is online on port -> " + port);
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
			TeamManager.removePlayer(getTeam(), this);
			for (NetworkedPlayer player : gameServer.getPlayers()) {
				player.sendPacket(new PacketPlayerDisconnect(getUUID(), reason));
			}
			disconnected();
			setConnected(false);
			close();
		}

		public void disconnected() {
			if (getTeam() != null)
				TeamManager.removePlayer(getTeam(), this);
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

		/**
		 * @param game script name
		 */
		public void execScript(String name) {
			GameElement target = GameServer.gameServer.getParty().getEntity(this, getTargetAddress());
			if (target == null)
				return;
			if (containsScript(name)) {
				GameScript script = null;
				for (GameScript script_ : getScripts()) {
					if (script_.getName().equals(name)) {
						script = script_;
						break;
					}
				}
				script.getExectutor().exec(this, target);
				removeGameScript(script);
			}
		}
	}

	public boolean isFull() {
		return TeamManager.isFull();
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
			mapData = ServerMapData.mapDefault();
		}
		return mapData;
	}

	/**
	 * @return
	 */
	public static String getServerVersion() {
		return VERSION;
	}
}
