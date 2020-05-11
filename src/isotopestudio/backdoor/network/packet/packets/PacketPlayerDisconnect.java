package isotopestudio.backdoor.network.packet.packets;

import java.util.UUID;

import doryanbessiere.isotopestudio.commons.lang.LangMessage;
import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

public class PacketPlayerDisconnect extends Packet {

	public PacketPlayerDisconnect() {
		super(DISCONNECT);
	}

	public PacketPlayerDisconnect(UUID uuid, String reason) {
		super(DISCONNECT, uuid, reason);
	}

	@Override
	public Packet clone() {
		return new PacketPlayerDisconnect();
	}

	private UUID uuid;
	private String reason;
	
	public String getReason() {
		return reason;
	}

	public UUID getUUID() {
		return uuid;
	}

	@Override
	public void read() {
		this.uuid = UUID.fromString(readString());
		this.reason = readString();
	}

	@Override
	public void process(GameServer server, GameServerClient client) {
		disconnect(server, client, getReason());
	}
	
	public static void disconnect(GameServer server, GameServerClient client, String reason) {
		client.disconnect(reason);
		leftMessage(server, client);
	}
	
	private static void leftMessage(GameServer server, GameServerClient client) {
		/*
		server.sendAll(new PacketPlayerTerminalLangMessage(new LangMessage("server_player_left_the_server", "%username%", client.getUsername())));
		server.sendAll(new PacketPlayerTerminalLangMessage(new LangMessage("server_player_left_a_team", "%username%", client.getUsername(), "%team%", client.getTeam().getPath())));*/
	}
}
