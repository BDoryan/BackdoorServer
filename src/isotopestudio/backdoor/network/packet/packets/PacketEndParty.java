package isotopestudio.backdoor.network.packet.packets;

import isotopestudio.backdoor.core.team.Team;
import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

public class PacketEndParty extends Packet {

	public PacketEndParty() {
		super(END_PARTY);
	}

	public PacketEndParty(Team winner) {
		super(END_PARTY, winner.toString());
	}
	
	@Override
	public Packet clone() {
		return new PacketEndParty();
	}

	private Team winner;
	
	public Team getWinner() {
		return winner;
	}
	
	@Override
	public void read() {
		this.winner = Team.get(readString());
	}

	@Override
	public void process(GameServer gameServer, GameServerClient server) {
		
	}
}
