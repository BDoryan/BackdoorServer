package isotopestudio.backdoor.network.packet.packets;

import isotopestudio.backdoor.core.elements.GameElement;
import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

public class PacketSendElementData extends Packet {

	public PacketSendElementData() {
		super(ELEMENT_DATA);
	}

	public PacketSendElementData(GameElement gameElement) {
		super(ELEMENT_DATA, GameElement.getGson().toJson(gameElement));
	}

	@Override
	public Packet clone() {
		return new PacketSendElementData();
	}

	private String json;

	public String getJson() {
		return json;
	}

	@Override
	public void read() {
		json = readString();
	}

	@Override
	public void process(GameServer server, GameServerClient client) {
	}
}
