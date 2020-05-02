package isotopestudio.backdoor.network.server.party;

import java.util.ArrayList;
import java.util.List;

import doryanbessiere.isotopestudio.api.IsotopeStudioAPI;
import doryanbessiere.isotopestudio.commons.GsonInstance;
import doryanbessiere.isotopestudio.commons.lang.LangMessage;
import isotopestudio.backdoor.core.elements.GameElementLink;
import isotopestudio.backdoor.core.elements.GameElementType;
import isotopestudio.backdoor.core.map.MapData;
import isotopestudio.backdoor.core.party.PartyState;
import isotopestudio.backdoor.core.player.Player;
import isotopestudio.backdoor.core.team.Team;
import isotopestudio.backdoor.network.packet.packets.PacketEndParty;
import isotopestudio.backdoor.network.packet.packets.PacketLoadMap;
import isotopestudio.backdoor.network.packet.packets.PacketPartyState;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerConnectToElement;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerDisconnectCurrentElement;
import isotopestudio.backdoor.network.packet.packets.PacketPlayerTerminalLangMessage;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.elements.GameElement;
import isotopestudio.backdoor.network.server.player.NetworkedPlayer;

public class Party {

	private GameServer server;
	private MapData mapData;

	public Party(GameServer server, MapData mapData) {
		this.server = server;
		this.mapData = mapData;
	}

	private boolean started = false;

	public void start() {
		server.sendAll(new PacketLoadMap(GsonInstance.instance().toJson(GameServer.getMap())));
		server.sendAll(new PacketPartyState(PartyState.START));

		for (NetworkedPlayer player : server.getPlayers()) {
			((GameElement)server.getParty().getMap().getTeamServer(player.getTeam())).connect(player);
		}
		started = true;
	}

	public void stop() {
		server.sendAll(new PacketPartyState(PartyState.STOP));
		ArrayList<NetworkedPlayer> players = new ArrayList<>();
		players.addAll(server.getPlayers());
		for (NetworkedPlayer player : players) {
			player.disconnection("game_finish");
		}
		started = false;
	}

	public void attack(NetworkedPlayer player, String adress) {
		GameElement gameElement = getEntity(player, adress);
		if (gameElement != null) {
			gameElement.attack(player.getTeam());
		}
	}

	public boolean connect(NetworkedPlayer player, String address) {
		GameElement entity_target = null;
		if ((entity_target = getEntity(player, address)) != null) {
			if (player.getMoney() > entity_target.getConnectPrice()) {
				entity_target.connect(player);
				player.setTargetAddress(address);
				player.addMoney(25);
				player.sendPacket(new PacketPlayerConnectToElement(address));
				player.sendPacket(new PacketPlayerTerminalLangMessage(
						new LangMessage("server_you_are_now_connected_to_the_server", "%address%", address)));
				return true;
			} else {
				player.setTargetAddress(null);
				player.sendPacket(new PacketPlayerConnectToElement(null));
				player.sendPacket(
						new PacketPlayerTerminalLangMessage(new LangMessage("server_you_dont_have_enough_money")));
				return false;
			}
		} else {
			player.setTargetAddress(null);
			player.sendPacket(new PacketPlayerConnectToElement(null));
			player.sendPacket(new PacketPlayerTerminalLangMessage(
					new LangMessage("you_do_not_have_access_to_the_server", "%address%", address)));
			return false;
		}

	}

	public void disconnect(NetworkedPlayer player) {
		GameElement entity_target = null;
		if ((entity_target = getEntity(player, player.getTargetAddress())) != null) {
			player.setTargetAddress(null);
			player.sendPacket(new PacketPlayerDisconnectCurrentElement());
			player.sendPacket(
					new PacketPlayerTerminalLangMessage(new LangMessage("server_you_have_been_disconnected")));
			entity_target.disconnect(player);
		}
		player.setTargetAddress(null);
	}

	public GameElement getEntity(NetworkedPlayer player, String adress) {
		alreadyScanned.clear();
		return scanEntity(player, null, (GameElement)getMap().getTeamServer(player.getTeam()), adress);
	}

	private ArrayList<String> alreadyScanned = new ArrayList<String>();

	private GameElement scanEntity(NetworkedPlayer player, GameElement from, GameElement to, String adress) {
		alreadyScanned.add(to.getUUID().toString());
		/**
		 * La bouche r�cup�re toutes les connexions du GameElement puis elle liste une
		 * par une pour v�rifier si leur adress correspond ou pas � celle recherche
		 * 
		 * V�rification de voir que le destinataire n'est pas le composant pr�c�dent
		 * 
		 * Si oui alors on retourne le GameElement
		 * 
		 * Sinon on fait poursuivre le code
		 * 
		 */
		for (GameElementLink link : to.getLinks()) {
			GameElement gameElement = (GameElement) GameElement.CACHE.get(link.getTo());
			if (from != null && from == gameElement) {
				continue;
			}
			if (gameElement.getAddress().equalsIgnoreCase(adress)) {
				return gameElement;
			}
		}

		/**
		 * La boucle r�cup�re aussi toutes les connexions du GameElement puis elle liste
		 * les GameElement (destination)
		 * 
		 * V�rification de voir que le destinataire n'est pas le composant pr�c�dent
		 * 
		 * V�rifier que le le composant destinataire n'a pas d�j� �t� scann� pour �viter
		 * une boucle infini --> StackOverflawError tmtc
		 * 
		 * Si le gameElement appartient � l'�quipe du joueur il peut lancer une
		 * recherche dans celui-ci puis si la recherche trouve un �l�ment on retourne
		 * celle-ci
		 * 
		 */
		GameElement found = null;
		for (GameElementLink link : to.getLinks()) {
			GameElement gameElement = (GameElement) GameElement.CACHE.get(link.getTo());
			if (from != null && from == gameElement) {
				continue;
			}
			if (gameElement.getTeam() == player.getTeam()) {
				GameElement from_ =(GameElement)  GameElement.CACHE.get(link.getFrom());
				if (alreadyScanned.contains(gameElement.getUUID().toString()))
					continue;

				found = scanEntity(player, from_, gameElement, adress);
				if (found != null)
					return found;
			}
		}
		return null;
	}

	private ArrayList<String> scan_gameelement = new ArrayList<String>();

	public List<GameElement> getNodes(Team team){
		scan_gameelement.clear();
		return scanNodes((GameElement)getMap().getTeamServer(team), team);
	}

	public List<GameElement> scanNodes(GameElement from, Team team){
		scan_gameelement.add(from.getAddress());
		ArrayList<GameElement> teamElements = new ArrayList<GameElement>();
		for (GameElementLink link : from.getLinks()) {
			GameElement to =(GameElement)  GameElement.CACHE.get(link.getTo());
			if (scan_gameelement.contains(to.getAddress()))
				continue;
			if (to.getTeam() == team) {
				teamElements.add(to);
			}
			teamElements.addAll(scanNodes(to, team));
		}
		return teamElements;
	}
	
	/*
	 * WARNING:
	 * 
	 * Faire faut faire attention au probl�me de synchronisation:
	 * - Pourrais �tre patch avec un syst�me de paquets avec un syst�me de queue
	 * 
	 */
	private ArrayList<String> linked_scan = new ArrayList<String>();

	public boolean isLinked(GameElement element, Team team) {
		linked_scan.clear();
		return searchLinked(element, team);
	}

	public boolean searchLinked(GameElement from, Team team) {
		linked_scan.add(from.getAddress());
		for (GameElementLink link : from.getLinks()) {
			GameElement to =(GameElement)  GameElement.CACHE.get(link.getTo());
			if (linked_scan.contains(to.getAddress()))
				continue;
			if (to.getTeam() == team) {
				if (searchLinked(to, team)) {
					return true;
				}
				if(to.getType() == GameElementType.SERVER && to.getTeam() == team) {
					return true;
				}
			}
		}
		return false;
	}

	public boolean isStarted() {
		return started;
	}

	public GameServer getServer() {
		return server;
	}

	public MapData getMap() {
		return mapData;
	}

	public void lose(Team team) {
		Team winner = null;
		for (Team teams : Team.values()) {
			if (teams != team)
				winner = teams;
		}
		if (winner != null) {
			for (Player player : GameServer.gameServer.getPlayers()) {
				((NetworkedPlayer) player).sendPacket(new PacketEndParty(winner));
			}
		}
		System.exit(IsotopeStudioAPI.EXIT_CODE_EXIT);
	}

	public GameElement getNodeByAddress(String targetAddress) {
		for(isotopestudio.backdoor.core.elements.GameElement element : getMap().getElements().values()) {
			if(element.getAddress().equals(targetAddress)) {
				return (GameElement) element;
			}
		}
		return null;
	}
}
