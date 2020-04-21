package isotopestudio.backdoor.network.server.command.commands;

import isotopestudio.backdoor.network.player.NetworkedPlayer;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.command.ICommand;

public class InfoCommand implements ICommand {

	@Override
	public void handle(String[] args) {
		System.out.println("Players online: "+GameServer.gameServer.getPlayers().size());
		for(NetworkedPlayer player : GameServer.gameServer.getPlayers()) {
			System.out.println(" - "+player.getUsername());
			System.out.println("   > "+player.getUUID());
			System.out.println("   > "+player.getTeam().toString());
			System.out.println("   > "+player.getPing());
			System.out.println("");
		}
	}

	@Override
	public String getCommand() {
		return "info";
	}

	@Override
	public String getDescription() {
		return "This command gives you information about the connected players.";
	}
}
