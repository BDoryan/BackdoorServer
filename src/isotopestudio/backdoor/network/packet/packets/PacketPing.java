package isotopestudio.backdoor.network.packet.packets;

import isotopestudio.backdoor.network.client.GameClient;
import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

public class PacketPing extends Packet {
	
	public PacketPing() {
		super(PING);
	}
	
	public PacketPing(long time) {
		super(PING, time);
	}

	@Override
	public Packet clone() {
		return new PacketPing();
	}

	private long time;
	
	public long getTime() {
		return time;
	}
	
	@Override
	public void read() {
		this.time = readLong();
	}

	@Override
	public void process(GameClient client) {
		long latency = System.currentTimeMillis() - getTime();
		client.sendPacket(new PacketPingReply(latency));
		client.setPing(latency);
	}

	@Override
	public void process(GameServer server, GameServerClient client) {
		long latency = System.currentTimeMillis() - getTime();
		client.sendPacket(new PacketPingReply(latency));
		client.setPing(latency);
	}
}
