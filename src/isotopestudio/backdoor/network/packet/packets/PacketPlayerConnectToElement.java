package isotopestudio.backdoor.network.packet.packets;

import java.util.Random;

import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;
import isotopestudio.backdoor.network.server.elements.GameElement;

public class PacketPlayerConnectToElement extends Packet {

	public PacketPlayerConnectToElement() {
		super(CONNECT_ENTITY);
	}

	public PacketPlayerConnectToElement(String adress) {
		super(CONNECT_ENTITY, adress);
	}
	
	@Override
	public Packet clone() {
		return new PacketPlayerConnectToElement();
	}

	private String adress;
	
	public String getAdress() {
		return adress;
	}
	
	@Override
	public void read() {
		this.adress = readString();
	}

	@Override
	public void process(GameServer server, GameServerClient client) {
		if(server.getParty() != null && server.getParty().isStarted()) {
			if(server.getParty().connect(client, getAdress())){
				client.setCommand(GameElement.generateKey());
				client.sendPacket(new PacketPlayerAttackElement(client.getCommand()));	
			}
		}
	}
}
