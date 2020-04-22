package isotopestudio.backdoor.network.server.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import isotopestudio.backdoor.core.elements.GameElementType;
import isotopestudio.backdoor.core.player.Player;
import isotopestudio.backdoor.core.team.Team;
import isotopestudio.backdoor.network.packet.packets.PacketSendElementData;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.party.TeamManager;
import isotopestudio.backdoor.network.server.player.NetworkedPlayer;

public class GameElement extends isotopestudio.backdoor.core.elements.GameElement {

	public static final String[] fake_commands = new String[] { "su", "mysql", "redis", "nano", "getinfo", "mkdir", "chmod",
			"cd", "grep", "install", "update", "write", "bufferedwriter", "scanner", "ls", "rm", "reboot", "su",
			"sizeof", "size", "apt-get", "constructor", "log", "hostserver", "client", "port", "disconnect" };
	
	private transient ArrayList<NetworkedPlayer> connected = new ArrayList<NetworkedPlayer>();
	
	public GameElement(GameElementType type, String name, isotopestudio.backdoor.core.team.Team team) {
		super(type, name, team);
	}

	public void connect(NetworkedPlayer player) {
		connected.add(player);
		if (!getTeamConnected().contains(player.getTeam()))
			getTeamConnected().add(player.getTeam());
		GameServer.gameServer.sendAll(new PacketSendElementData(this));
		if (getType() == GameElementType.SERVER)
			return;
		player.removeMoney(50);
	}

	public void disconnect(NetworkedPlayer player) {
		connected.remove(player);
		getTeamConnected().remove(player.getTeam());
		for (NetworkedPlayer player_ : getConnected()) {
			if (player_.getTeam() == player.getTeam()) {
				getTeamConnected().add(player.getTeam());
				break;
			}
		}
		GameServer.gameServer.sendAll(new PacketSendElementData(this));
	}

	public ArrayList<NetworkedPlayer> getConnected() {
		return connected;
	}

	public void attack(Team team) {
		if (getType() == GameElementType.SERVER) {
			if (team == getTeam()) {
				int point = getTeamPoint(getTeam()) + 1;
				setTeamPoint(getTeam(), point > getMaxPoints() ? getMaxPoints() : point);
			} else {
				int point = getTeamPoint(getTeam()) - 1;
				setTeamPoint(getTeam(), point < 0 ? 0 : point);
			}

			if (getTeamPoint(getTeam()) <= 0) {
				GameServer.gameServer.getParty().lose(getTeam());
				GameServer.gameServer.getParty().stop();
			}
		} else if (getType() == GameElementType.NODE) {
			int point = getTeamPoint(team) + 1;
			point = point > getMaxPoints() ? getMaxPoints() : point;
			point = point < 0 ? 0 : point;
			
			setTeamPoint(team, point);

			/**
			 * Quand le noeud est neutre on rajoute un point � chaque joueur
			 * 
			 * Quand le noeud appartient � une �quipe on fait un syst�me de duel: quand la
			 * team qui a le serveur attack enl�ve un point � l'adversaire
			 * 
			 */

			if (getTeam() != null) {
				if(getTeam() == team) // L'�quipe poss�de d�j� le serveur
					return;
				for (Team team_ : Team.values()) {
					if (team_ != team) {
						int team_point = getTeamPoint(team_) - 1;
						if (team_point < 0)
							team_point = 0;
						setTeamPoint(team_, team_point);
					}
				}
			}

			if (getTeamPoint(team) >= getMaxPoints()) {
				if (getTeam() == null) {
					setTeam(team);
					for (Team team_ : Team.values()) {
						if (team_ != team) {
							setTeamPoint(team_, 0);
						} else {
							setTeamPoint(team_, getMaxPoints());
						}
					}
					setLinked(true);
					/*
					 * - On r�cup�re tous les noeuds de l'�quipe
					 * - On v�rifie si le noeud est d�fini comme 'linked'
					 * - Si ce n'est pas le cas alors qu'il est d�sormais 'linked' on lui d�fini sa variable et on le met � jours pour
					 * les joueurs
					 */
					for (GameElement node : GameServer.gameServer.getParty().getNodes(getTeam())) {
						if (!node.isLinked() && GameServer.gameServer.getParty().isLinked(node, getTeam())) {
							node.setLinked(true);
							GameServer.gameServer.sendAll(new PacketSendElementData(node));
						}
					}
				} else {
					if (getTeam() == team)
						return;

					for (Team team_ : Team.values()) {
						setTeamPoint(team_, 0);
					}
					setLinked(false);

					Team old_team = getTeam();

					setTeam(null);

					/*
					 * 
					 * - On r�cup�re tous les noeuds de l'�quipe
					 * - On v�rifie si le noeud n'est pas 'linked' on lui d�fini sa variable et on envoie l'informations aux joueurs
					 * et on d�connecte l'�quipe 
					 */
					for (GameElement node : GameServer.gameServer.getParty().getNodes(old_team)) {
						if (!GameServer.gameServer.getParty().isLinked(node, old_team)) {
							node.setLinked(false);
							GameServer.gameServer.sendAll(new PacketSendElementData(node));
							node.disconnectTeam(old_team);
						}
					}

					/*
					 * - On r�cup�re les joueurs de l'ancienne �quipe
					 * - On v�rifie si ils sont connect�s sur une noeuds/serveurs et si il n'est plus 'linked'
					 * - On d�connecte l'�quipe
					 */
					ArrayList<NetworkedPlayer> players = new ArrayList<NetworkedPlayer>();
					players.addAll(TeamManager.getPlayers(old_team));
					for (NetworkedPlayer player : players) {
						if (player.getTargetAddress() != null) {
							GameElement node = GameServer.gameServer.getParty().getNodeByAddress(player.getTargetAddress());
							if (/*node.getTeam() != player.getTeam() && */!GameServer.gameServer.getParty().isLinked(node, old_team)) {
								node.disconnect(player);
							}
						}
					}
				}
				disconnectAll();
			}
		}
		GameServer.gameServer.sendAll(new PacketSendElementData(this));
	}

	public void disconnectAll() {
		ArrayList<NetworkedPlayer> connected = new ArrayList<NetworkedPlayer>();
		connected.addAll(getConnected());
		for (NetworkedPlayer player : connected) {
			GameServer.gameServer.getParty().disconnect(player);
		}
	}

	public void disconnectTeam(Team team) {
		ArrayList<NetworkedPlayer> connected = new ArrayList<NetworkedPlayer>();
		for (NetworkedPlayer player : connected) {
			if (player.getTeam() == team) {
				GameServer.gameServer.getParty().disconnect(player);	
			}
		}
	}
}
