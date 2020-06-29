package isotopestudio.backdoor.network.packet.packets;

import isotopestudio.backdoor.core.gamescript.GameScript;
import isotopestudio.backdoor.core.gamescript.GameScript.GameScripts;
import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;
import isotopestudio.backdoor.network.server.player.NetworkedPlayer;

public class PacketPlayerExecScript extends Packet {

	public PacketPlayerExecScript() {
		super(PLAYER_EXEC_SCRIPT);
	}

	public PacketPlayerExecScript(GameScript gameScript) {
		super(PLAYER_EXEC_SCRIPT, gameScript.getName());
	}

	@Override
	public Packet clone() {
		return new PacketPlayerExecScript();
	}

	private GameScripts gameScript;
	
	@Override
	public void read() {
		gameScript = GameScripts.fromName(readString());
	}
	
	@Override
	public void process(GameServer server, GameServerClient player) {
		if(player.containsScript(gameScript.getGameScript().getName())) {
			player.execScript(gameScript.getGameScript().getName());
		}
	}
}
