package isotopestudio.backdoor.network.packet.packets;

import doryanbessiere.isotopestudio.commons.lang.LangMessage;
import isotopestudio.backdoor.game.BackdoorGame;
import isotopestudio.backdoor.game.applications.TerminalApplication;
import isotopestudio.backdoor.network.client.GameClient;
import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

public class PacketPlayerTerminalLangMessage extends Packet {

	public PacketPlayerTerminalLangMessage() {
		super(PLAYER_TERMINAL_LANG_MESSAGE);
	}

	public PacketPlayerTerminalLangMessage(int logType, LangMessage langMessage) {
		super(PLAYER_TERMINAL_LANG_MESSAGE, logType, langMessage.toJson());	
	}

	public PacketPlayerTerminalLangMessage(LangMessage langMessage) {
		super(PLAYER_TERMINAL_LANG_MESSAGE, TerminalApplication.LOG_INFO, langMessage.toJson());	
	}

	@Override
	public Packet clone() {
		return new PacketPlayerTerminalLangMessage();
	}
	
	private int logType;
	private LangMessage langMessage;
	
	public LangMessage getLangMessage() {
		return langMessage;
	}

	@Override
	public void read() {
		this.logType = readInt();
		this.langMessage = LangMessage.fromJson(readString());
	}

	@Override
	public void process(GameClient client) {
		if(BackdoorGame.getGameParty() != null) {
			if(TerminalApplication.main != null) {
				TerminalApplication.main.log(logType, langMessage.message());
			}
		}
	}

	@Override
	public void process(GameServer gameServer, GameServerClient server) {
	}
}
