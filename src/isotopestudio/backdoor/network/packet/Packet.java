package isotopestudio.backdoor.network.packet;

import isotopestudio.backdoor.network.packet.packets.PacketEndParty;
import isotopestudio.backdoor.network.packet.packets.PacketLoadMap;
import isotopestudio.backdoor.network.packet.packets.PacketPartyState;
import isotopestudio.backdoor.network.packet.packets.PacketPing;
import isotopestudio.backdoor.network.packet.packets.PacketPingReply;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerAttackElement;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerBuyGameScript;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerConnectToElement;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerConnected;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerDisconnect;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerDisconnectCurrentElement;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerKick;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerLogin;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerLoginFailed;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerMoneyUpdate;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerScriptsUpdate;
import isotopestudio.backdoor.network.packet.packets.PacketSendElementData;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

public abstract class Packet {

	public static final char SEPARATOR = '/';
	public static final int PING = 1;
	public static final int PING_REPLY = 2;
	public static final int LOGIN = 3;
	public static final int LOGIN_FAILED = 4;
	public static final int DISCONNECT = 5;
	public static final int KICK = 6;
	public static final int MAP_LOAD = 7;
	public static final int PARTY_STATE = 9;
	public static final int CONNECT_ENTITY = 10;
	public static final int DISCONNECT_ENTITY = 11;
	public static final int ELEMENT_DATA = 12;
	public static final int ATTACK_ELEMENT = 13;
	public static final int END_PARTY = 14;
	public static final int PLAYER_MONEY_UPDATE = 15;
	public static final int PLAYER_CONNECTED = 17;
	public static final int PLAYER_BUY_GAME_SCRIPT = 18;
	public static final int PLAYER_SCRIPTS_UPDATE = 19;

	public static Packet[] packets = new Packet[] { null,
			new PacketPing(), 
			new PacketPingReply(),
			new PacketPlayerLogin(),
			new PacketPlayerLoginFailed(),
			new PacketPlayerDisconnect(),
			new PacketPlayerKick(), 
			new PacketLoadMap(),
			null,
			new PacketPartyState(),
			new PacketPlayerConnectToElement(),
			new PacketPlayerDisconnectCurrentElement(),
			new PacketSendElementData(),
			new PacketPlayerAttackElement(),
			new PacketEndParty(),
			new PacketPlayerMoneyUpdate(),
			null,
			new PacketPlayerConnected(),
			new PacketPlayerBuyGameScript(),
			new PacketPlayerScriptsUpdate()
	};

	public static Packet parsePacket(String data) {
		String[] datas = data.split(SEPARATOR + "");
		int packet_id = Integer.parseInt(datas[0].trim());
		if (packet_id == 0) {
			System.err.println("Invalid packet");
			return null;
		}

		if (packet_id > packets.length) {
			System.err.println("Invalid packet (packet_id invalid)");
			return null;
		}

		Packet Packet = packets[packet_id].clone();
		Packet.id = packet_id;
		Packet.datas = datas;
		return Packet;
	}

	private int id;
	public String[] datas;

	public Packet(int id, Object... datas) {
		super();
		this.id = id;
		this.datas = new String[datas.length + 1];
		this.datas[0] = id + "";
		for (int i = 1; i < datas.length + 1; i++) {
			String data = datas[i - 1] != null ? datas[i - 1] + "" : "null";
			this.datas[i] = data.replace(SEPARATOR+"", "%20");
		}
	}

	public String toData() {
		String data = this.id + "";
		if (this.datas.length > 1) {
			for (int i = 1; i < this.datas.length; i++) {
				data += SEPARATOR + this.datas[i];
			}
		}
		return data;
	}

	public int getId() {
		return id;
	}

	public abstract Packet clone();

	public abstract void read();

	public abstract void process(GameServer gameServer, GameServerClient server);

	// for skip the first information (packet id)
	int read_index = 1;

	public String readString() {
		String data = datas[read_index];
		read_index++;
		String data_string = data.trim().replace("%20", SEPARATOR+"");
		if(data_string.equalsIgnoreCase("null"))
			return null;
		return data_string;
	}

	public Boolean readBoolean() {
		return Boolean.parseBoolean(readString());
	}

	public Integer readInt() {
		return Integer.parseInt(readString());
	}

	public Long readLong() {
		return Long.parseLong(readString());
	}

	public Float readFloat() {
		return Float.parseFloat(readString());
	}

	public Double readDouble() {
		return Double.parseDouble(readString());
	}

	public static Packet get(int id) {
		return packets[id];
	}
}
