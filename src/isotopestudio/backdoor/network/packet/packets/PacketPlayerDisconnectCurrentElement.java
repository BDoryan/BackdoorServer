package isotopestudio.backdoor.network.packet.packets;

import doryanbessiere.isotopestudio.commons.lang.LangMessage;
import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

public class PacketPlayerDisconnectCurrentElement extends Packet {

	public PacketPlayerDisconnectCurrentElement() {
		super(DISCONNECT_ENTITY);
	}

	@Override
	public Packet clone() {
		return new PacketPlayerDisconnectCurrentElement();
	}
	
	@Override
	public void read() {
	}

	@Override
	public void process(GameServer server, GameServerClient client) {
		if(server.getParty() != null && server.getParty().isStarted()) {
			if(client.getTargetAddress() != null) {
				server.getParty().disconnect(client);
			} else  {
				/*
				server.sendAll(new PacketPlayerTerminalLangMessage(new LangMessage("server_you_are_already_connected_to_element")));
				*/
			}
		}
	}
}
