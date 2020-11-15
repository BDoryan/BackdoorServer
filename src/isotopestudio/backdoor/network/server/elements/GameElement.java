package isotopestudio.backdoor.network.server.elements;

import java.security.SecureRandom;
import java.util.ArrayList;

import isotopestudio.backdoor.core.elements.GameElementType;
import isotopestudio.backdoor.core.team.Team;
import isotopestudio.backdoor.network.packet.packets.PacketSendElementData;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.party.TeamManager;
import isotopestudio.backdoor.network.server.player.NetworkedPlayer;

public class GameElement extends isotopestudio.backdoor.core.elements.GameElement {

    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String NUMBER = "0123456789";
    
    private static final String DATA_FOR_RANDOM_STRING = CHAR_LOWER + NUMBER;
    private static SecureRandom random = new SecureRandom();
    
    public static String generateKey() {
    	int length = 5;
        if (length < 1) throw new IllegalArgumentException();

        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {

			// 0-62 (exclusive), random returns 0-61
            int rndCharAt = random.nextInt(DATA_FOR_RANDOM_STRING.length());
            char rndChar = DATA_FOR_RANDOM_STRING.charAt(rndCharAt);

            sb.append(rndChar);

        }

        return sb.toString();
    }
	
	private transient ArrayList<NetworkedPlayer> connected = new ArrayList<NetworkedPlayer>();
	
	public GameElement(GameElementType type, String name, isotopestudio.backdoor.core.team.Team team) {
		super(type, name, team);
	}

	public void connect(NetworkedPlayer player) {
		connected.add(player);
		if (!getTeamConnected().contains(player.getTeam()))
			getTeamConnected().add(player.getTeam());
		updateNetworkedData();
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
		updateNetworkedData();
	}

	public ArrayList<NetworkedPlayer> getConnected() {
		return connected;
	}
	
	@Override
	public void setFirewall(int firewall) {
		super.setFirewall(firewall);
		updateNetworkedData();
	}
	
	@Override
	public void setFirewallMax(int firewall_max) {
		super.setFirewallMax(firewall_max);
		updateNetworkedData();
	}
	
	@Override
	public void setProtected(boolean protected_) {
		super.setProtected(protected_);
		updateNetworkedData();
	}
	
	@Override
	public void setOffline(boolean offline) {
		if(offline) {
			disconnectAll();
		}
		super.setOffline(offline);
		updateNetworkedData();
	}
	
	public void updateNetworkedData() {
		GameServer.gameServer.sendAll(new PacketSendElementData(this));
	}
	
	public void attack(Team team) {
		if(isProtected()) {
			return;
		}
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
		updateNetworkedData();
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

	public void addPoint(Team team, int amount) {
		int points = getTeamPoint(team) + amount;
		if(points >= getMaxPoints()) {
			points = getMaxPoint();
		}
		setTeamPoint(team, points);
		updateNetworkedData();
	}

	public void removePoint(Team team, int amount) {
		int points = getTeamPoint(team) - amount;
		if(points < 0) {
			points = 0;
		}
		setTeamPoint(team, points);
		updateNetworkedData();
	}
}
