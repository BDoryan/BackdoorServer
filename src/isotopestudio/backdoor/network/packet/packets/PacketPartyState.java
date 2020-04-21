package isotopestudio.backdoor.network.packet.packets;

import isotopestudio.backdoor.core.party.PartyState;
import isotopestudio.backdoor.network.client.GameClient;
import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;

public class PacketPartyState extends Packet {

	public PacketPartyState() {
		super(PARTY_STATE);
	}

	public PacketPartyState(PartyState partyState) {
		super(PARTY_STATE, partyState.toString());
	}
	
	@Override
	public Packet clone() {
		return new PacketPartyState();
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
	public void process(GameClient client) {
		if(partyState == PartyState.START) {
			client.startParty();	
		} else {
			client.stopParty();
		}
	}

	@Override
	public void process(GameServer gameServer, GameServerClient server) {
		
	}
}
