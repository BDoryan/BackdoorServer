package isotopestudio.backdoor.network.packet.packets;

import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

public class PacketPlayerLoginFailed extends Packet {

	public PacketPlayerLoginFailed() {
		super(LOGIN_FAILED);
	}

	public PacketPlayerLoginFailed(int disconnect_id) {
		super(LOGIN_FAILED, disconnect_id);
	}

	@Override
	public Packet clone() {
		return new PacketPlayerLoginFailed();
	}

	private int disconnect_id;
	
	public int getDisconnectID() {
		return disconnect_id;
	}

	@Override
	public void read() {
		this.disconnect_id = readInt();
	}

	@Override
	public void process(GameServer server, GameServerClient client) {
	}
}
