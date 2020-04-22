package isotopestudio.backdoor.network.packet.packets;

import doryanbessiere.isotopestudio.commons.lang.LangMessage;
import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

public class PacketPlayerTerminalLangMessage extends Packet {

	public static int LOG_INFO = 0;
	public static int LOG_ERROR = 1;
	public static int LOG_WARNING = 2;

	public PacketPlayerTerminalLangMessage() {
		super(PLAYER_TERMINAL_LANG_MESSAGE);
	}

	public PacketPlayerTerminalLangMessage(int logType, LangMessage langMessage) {
		super(PLAYER_TERMINAL_LANG_MESSAGE, logType, langMessage.toJson());	
	}

	public PacketPlayerTerminalLangMessage(LangMessage langMessage) {
		super(PLAYER_TERMINAL_LANG_MESSAGE, PacketPlayerTerminalLangMessage.LOG_INFO, langMessage.toJson());	
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
	
	public int getLogType() {
		return logType;
	}

	@Override
	public void read() {
		this.logType = readInt();
		this.langMessage = LangMessage.fromJson(readString());
	}

	@Override
	public void process(GameServer gameServer, GameServerClient server) {
	}
}
