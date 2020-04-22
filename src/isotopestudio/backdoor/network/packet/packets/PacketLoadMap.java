package isotopestudio.backdoor.network.packet.packets;

import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

public class PacketLoadMap extends Packet {

	public PacketLoadMap() {
		super(MAP_LOAD);
	}

	public PacketLoadMap(String json) {
		super(MAP_LOAD, json.length(), json);
	}

	@Override
	public Packet clone() {
		return new PacketLoadMap();
	}

	private int json_length;
	private String json;
	
	public int getJsonLength() {
		return json_length;
	}

	public String getJson() {
		return json;
	}

	@Override
	public void read() {
		this.json_length = readInt();
		this.json = readString();
	}

	@Override
	public void process(GameServer server, GameServerClient client) {
	}
}
