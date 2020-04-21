package isotopestudio.backdoor.network.packet.packets;

import java.util.Random;

import isotopestudio.backdoor.game.BackdoorGame;
import isotopestudio.backdoor.game.applications.NetworkApplication;
import isotopestudio.backdoor.network.client.GameClient;
import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

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
	public void process(GameClient client) {
		client.setCommand(getCommand());
		BackdoorGame.getGameParty().getGameTerminal().generateCommand(getCommand());
	}

	@Override
	public void process(GameServer server, GameServerClient client) {
		if(server.getParty() != null && server.getParty().isStarted()) {
			if(client.getTargetAddress() != null) {
				if(client.getCommand().equals(getCommand())) {
					server.getParty().attack(client, client.getTargetAddress());
					client.setCommand(NetworkApplication.fake_commands[new Random().nextInt(NetworkApplication.fake_commands.length-1)]);
					client.sendPacket(new PacketPlayerAttackElement(client.getCommand()));	
				}
			}
		}
	}
}
