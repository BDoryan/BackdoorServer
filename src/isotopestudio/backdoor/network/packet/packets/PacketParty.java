package isotopestudio.backdoor.network.packet.packets;

import isotopestudio.backdoor.core.party.PartyState;
import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

public class PacketParty extends Packet {

	public PacketParty() {
		super(PARTY_STATE);
	}

	public PacketParty(PartyState partyState) {
		super(PARTY_STATE, partyState.toString());
	}
	
	@Override
	public Packet clone() {
		return new PacketParty();
	}

	private PartyState partyState;
	
	public PartyState getPartyState() {
		return partyState;
	}
	
	@Override
	public void read() {
		this.partyState = PartyState.parse(readString());
	}

	@Override
	public void process(GameServer gameServer, GameServerClient server) {
		
	}
}
