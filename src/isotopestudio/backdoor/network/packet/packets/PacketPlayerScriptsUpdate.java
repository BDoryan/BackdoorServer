package isotopestudio.backdoor.network.packet.packets;

import java.util.ArrayList;

import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;
import isotopestudio.backdoor.network.server.player.NetworkedPlayer;

public class PacketPlayerScriptsUpdate extends Packet {

	public PacketPlayerScriptsUpdate() {
		super(PLAYER_SCRIPTS_UPDATE);
	}

	public PacketPlayerScriptsUpdate(NetworkedPlayer player, ArrayList<String> scripts) {
		super(PLAYER_SCRIPTS_UPDATE, scripts);
	}

	@Override
	public Packet clone() {
		return new PacketPlayerScriptsUpdate();
	}

	@Override
	public void read() {
	}
	
	@Override
	public void process(GameServer gameServer, GameServerClient server) {
	}
}
