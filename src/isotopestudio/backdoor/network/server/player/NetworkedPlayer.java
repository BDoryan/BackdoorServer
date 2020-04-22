package isotopestudio.backdoor.network.server.player;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

import isotopestudio.backdoor.core.player.Player;
import isotopestudio.backdoor.core.team.Team;
import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.packet.PacketListener;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerDisconnect;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerMoneyUpdate;

public abstract class NetworkedPlayer extends Player {

	private Socket socket;
	private DataOutputStream output;
	private DataInputStream input;

	public void initialize(Socket socket, DataInputStream input, DataOutputStream output) {
		this.socket = socket;
		this.output = output;
		this.input = input;
	}

	public ArrayList<PacketListener> packetListeners = new ArrayList<>();

	public ArrayList<PacketListener> getPacketListeners() {
		return packetListeners;
	}

	/**
	 * 
	 * This methode is called when you decide to send a packet to the server
	 * 
	 * @param packet
	 */
	public void sendPacket(Packet packet) {
		try {
			byte[] data = packet.toData().getBytes();
			write(data);
			//System.out.println("[Packet] send -> "+packet.toData());
			if (!getPacketListeners().isEmpty()) {
				for (PacketListener listener : getPacketListeners()) {
					listener.sended(packet);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/***
	 * 
	 * This method is called when you decide to write a message to the server
	 * 
	 * @param bytes
	 * @throws IOException
	 */
	public void write(byte[] bytes) throws IOException {
		//output.writeInt(bytes.length);
		output.writeUTF(new String(bytes));
		output.flush();
	}

	/**
	 *
	 * This method is called when you want to read the last message (puts the thread
	 * on hold if it is still not received).
	 *
	 * @return
	 * @throws IOException
	 */
	public byte[] read() throws IOException {
		/*int length = input.readInt();
		byte[] read = new byte[length];
		int totalRead = 0;
		boolean end = false;
	    while(!end) {
	        int bytesRead = input.read(read);
			if (bytesRead == -1) {
				disconnect("connection_lost");
				return null;
			} else {
				totalRead+=bytesRead;
				if(totalRead > length) {
					end = true;
					break;
				}
			}
	    }*/
		/*
		boolean reading = true;
		while(reading) {
			int i = input.read(read, 0, length);	
			if (i == -1) {
				disconnect("connection_lost");
				return null;
			}
		}*/
		return input.readUTF().getBytes();
	}

	/**
	 * 
	 * This method is called when you decide to close the connection.
	 * 
	 * @throws IOException
	 */
	public void close() {
		try {
			if(!socket.isClosed())
			socket.close();
			input.close();
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * This method is called when you decide to read the next packet
	 * 
	 * @return
	 * @throws IOException
	 */
	public Packet readPacket() throws IOException {
		Packet packet = Packet.parsePacket(new String(read()));
		//System.out.println("[Packet] receive -> "+packet.toData());
		return packet;
	}

	/**
	 * 
	 * This methid is called when you decide to close the networked connection
	 * 
	 * @param reason
	 */
	public abstract void disconnect(String reason);
	
	private boolean connected = false;
	
	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public boolean isConnected() {
		return connected;
	}
	
	/**
	 * 
	 * 
	 * 
	 * @param reason
	 */
	public void disconnection(String reason) {
		sendPacket(new PacketPlayerDisconnect(getUUID(), reason));
		disconnect(reason);
	}

	/**
	 * 
	 * This method is called when you decide to put a thread on hold in order to
	 * receive a packet
	 * 
	 * @param packet
	 * @param time_left
	 * 
	 * @return Return true if he received the package and false if he did not.
	 * 
	 */
	public abstract boolean waitPacket(Packet packet, long timeout);

	// I use AtomicLong for synchronize the value
	private AtomicLong ping;

	public long getPing() {
		if (this.ping == null)
			this.ping = new AtomicLong();
		return ping.get();
	}

	public void setPing(long ping) {
		if (this.ping == null)
			this.ping = new AtomicLong();
		this.ping.set(ping);
	}
	
	private String target_address;
	
	public void setTargetAddress(String target_address) {
		this.target_address = target_address;
	}
	
	public String getTargetAddress() {
		return target_address;
	}
	
	private String command;
	
	public String getCommand() {
		return command;
	}
	
	public void setCommand(String command) {
		this.command = command;
	}
	
	@Override
	public void setMoney(int money) {
		super.setMoney(money);
		sendPacket(new PacketPlayerMoneyUpdate(this));
	}
	
	@Override
	public void addMoney(int money) {
		super.addMoney(money);
		sendPacket(new PacketPlayerMoneyUpdate(this));
	}
	
	@Override
	public void removeMoney(int money) {
		super.removeMoney(money);
		sendPacket(new PacketPlayerMoneyUpdate(this));
	}
	
	public DataInputStream getInput() {
		return input;
	}

	public DataOutputStream getOutput() {
		return output;
	}

	public Socket getSocket() {
		return socket;
	}
}
