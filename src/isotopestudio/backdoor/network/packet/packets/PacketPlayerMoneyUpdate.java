package isotopestudio.backdoor.network.packet.packets;

import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;
import isotopestudio.backdoor.network.server.player.NetworkedPlayer;

public class PacketPlayerMoneyUpdate extends Packet {

	public PacketPlayerMoneyUpdate() {
		super(PLAYER_MONEY_UPDATE);
	}

	public PacketPlayerMoneyUpdate(NetworkedPlayer player) {
		super(PLAYER_MONEY_UPDATE, player.getMoney());
	}

	@Override
	public Packet clone() {
		return new PacketPlayerMoneyUpdate();
	}

	private int money;

	public int getMoney() {
		return money;
	}

	@Override
	public void read() {
		money = readInt();
	}

	@Override
	public void process(GameServer server, GameServerClient client) {
	}
}
