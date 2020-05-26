package isotopestudio.backdoor.network.packet.packets;

import java.util.Random;

import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;
import isotopestudio.backdoor.network.server.elements.GameElement;

public class PacketPlayerAttackElement extends Packet {

	public PacketPlayerAttackElement() {
		super(ATTACK_ELEMENT);
	}

	public PacketPlayerAttackElement(String command) {
		super(ATTACK_ELEMENT, command);
	}

	@Override
	public Packet clone() {
		return new PacketPlayerAttackElement();
	}
	
	private String command;

	public String getCommand() {
		return command;
	}
	
	@Override
	public void read() {
		command = readString();
	}

	@Override
	public void process(GameServer server, GameServerClient client) {
		if(server.getParty() != null && server.getParty().isStarted()) {
			if(client.getTargetAddress() != null) {
				if(client.getCommand().equals(getCommand())) {
					server.getParty().attack(client, client.getTargetAddress());
					client.setCommand(GameElement.generateKey());
					client.sendPacket(new PacketPlayerAttackElement(client.getCommand()));	
				}
			}
		}
	}
}
