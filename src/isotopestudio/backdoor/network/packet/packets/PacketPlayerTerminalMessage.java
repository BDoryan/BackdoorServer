package isotopestudio.backdoor.network.packet.packets;

import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

public class PacketPlayerTerminalMessage extends Packet {

	public PacketPlayerTerminalMessage() {
		super(PLAYER_TERMINAL_MESSAGE);
	}

	public PacketPlayerTerminalMessage(String message) {
		super(PLAYER_TERMINAL_MESSAGE, message);	
	}

	@Override
	public Packet clone() {
		return new PacketPlayerTerminalMessage();
	}
	
	private String message;

	public String getMessage() {
		return message;
	}
	
	@Override
	public void read() {
		this.message = readString();
	}

	@Override
	public void process(GameServer gameServer, GameServerClient server) {
	}
}
