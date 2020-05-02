package isotopestudio.backdoor.network.server.party;

import java.util.ArrayList;
import java.util.HashMap;

import isotopestudio.backdoor.core.team.Team;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.player.NetworkedPlayer;

public class TeamManager {
	
	public static HashMap<Team, ArrayList<NetworkedPlayer>> teams_players = new HashMap<>();
	public static HashMap<Team, Integer> max_players = new HashMap<Team, Integer>();
	
	public static void init() {
		for(Team team : Team.values()) {
			teams_players.put(team, new ArrayList<NetworkedPlayer>());
			max_players.put(team, 1);
		}
	}
	
	public static boolean isFull() {
		int i = 0;
		
		int max = 0;
		for(Team team : Team.values()) {
			i += getPlayers(team).size();
			max += max_players.get(team);
		}
		
		// edited for single connection (developer function)
		max = 1;
		
		if(i < max)
			return false;
		return true;
	}

	public static Team autoJoin(NetworkedPlayer player) {
		for (Team team : Team.values()) {
			if (getPlayers(team).size() < getMaxPlayers(team)) {
				addPlayer(team, player);
				return team;
			}
		}
		return null;
	}
	
	public static void addPlayer(Team team, NetworkedPlayer player) {
		if(hasTeam(player)) {
			throw new IllegalStateException(player.getUsername()+" is already in a team -> team="+player.getTeam());
		}
		teams_players.get(team).add(player);
	}
	
	public static boolean hasTeam(NetworkedPlayer networkedPlayer) {
		if(networkedPlayer.getTeam() != null)
			return true;
		return false;
	}
	
	public static void removePlayer(Team team, NetworkedPlayer player) {
		if(!hasTeam(player)) {
			throw new IllegalStateException(player.getUsername()+" is not in a team -> team="+player.getTeam());
		}
		teams_players.get(team).remove(player);
		if(GameServer.gameServer.getParty().isStarted()) {
			if(teams_players.get(team).size() <= 0) {
				GameServer.gameServer.getParty().lose(team);
			}
		}
	}
	
	public static ArrayList<NetworkedPlayer> getPlayers(Team team){
		return teams_players.get(team);
	}
	
	public static int getMaxPlayers(Team team){
		return max_players.get(team);
	}
}
