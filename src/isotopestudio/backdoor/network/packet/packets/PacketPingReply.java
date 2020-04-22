package isotopestudio.backdoor.network.packet.packets;

import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

public class PacketPingReply extends Packet {
	
	public PacketPingReply() {
		super(PING_REPLY);
	}
	
	public PacketPingReply(long latency) {
		super(PING_REPLY, latency);
	}

	@Override
	public Packet clone() {
		return new PacketPingReply();
	}

	private long latency;
	
	public long getLatency() {
		return latency;
	}
	
	@Override
	public void read() {
		this.latency = readLong();
	}

	@Override
	public void process(GameServer server, GameServerClient client) {
		client.setPing(latency);
	}
}
