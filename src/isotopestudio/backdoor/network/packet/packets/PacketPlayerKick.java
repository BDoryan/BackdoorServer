package isotopestudio.backdoor.network.packet.packets;

import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

public class PacketPlayerKick extends Packet {

	public PacketPlayerKick() {
		super(KICK);
	}

	public PacketPlayerKick(String reason) {
		super(KICK, reason);
	}

	@Override
	public Packet clone() {
		return new PacketPlayerKick();
	}

	private String reason;

	public String getUUID() {
		return reason;
	}

	@Override
	public void read() {
		this.reason = readString();
	}

	@Override
	public void process(GameServer server, GameServerClient client) {
	}
}
