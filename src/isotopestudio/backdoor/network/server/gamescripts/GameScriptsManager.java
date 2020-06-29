package isotopestudio.backdoor.network.server.gamescripts;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import isotopestudio.backdoor.core.gamescript.GameScript.GameScripts;
import isotopestudio.backdoor.core.gamescript.GameScriptExecutor;
import isotopestudio.backdoor.core.player.Player;
import isotopestudio.backdoor.network.server.player.NetworkedPlayer;

/**
 * @author BESSIERE Doryan
 * @github https://www.github.com/DoryanBessiere/
 */
public class GameScriptsManager {

	public static void init() {
		System.out.println(" - initializing scripts executors");
		GameScripts.ATTACKER.getGameScript().setExectutor(new GameScriptExecutor() {
			@Override
			public Object exec(Object... datas) {
				NetworkedPlayer player = (NetworkedPlayer) datas[0];
				isotopestudio.backdoor.network.server.elements.GameElement target = (isotopestudio.backdoor.network.server.elements.GameElement) datas[1];
				
				new Timer().schedule(new TimerTask() {
					
					int seconds = 0;
					
					@Override
					public void run() {
						if(seconds >= 30)
						{
							cancel();
							return;
						}

						target.attack(player.getTeam());
						seconds+=5;
					}
				}, 0L, 5000L);
				return null;
			}
		});
		GameScripts.ATTACK.getGameScript().setExectutor(new GameScriptExecutor() {
			@Override
			public Object exec(Object... datas) {
				NetworkedPlayer player = (NetworkedPlayer) datas[0];
				isotopestudio.backdoor.network.server.elements.GameElement target = (isotopestudio.backdoor.network.server.elements.GameElement) datas[1];
				
				for(int i = 0; i < 3; i++) {
					target.attack(player.getTeam());
				}
				return null;
			}
		});
		GameScripts.HEALER.getGameScript().setExectutor(new GameScriptExecutor() {
			@Override
			public Object exec(Object... datas) {
				NetworkedPlayer player = (NetworkedPlayer) datas[0];
				isotopestudio.backdoor.network.server.elements.GameElement target = (isotopestudio.backdoor.network.server.elements.GameElement) datas[1];
				

				if(target.getTeam() != player.getTeam())
					return false;
				
				new Timer().schedule(new TimerTask() {
					
					int seconds = 0;
					
					@Override
					public void run() {
						if(seconds >= 30)
						{
							cancel();
							return;
						}

						target.addPoint(player.getTeam(), 1);
						seconds+=5;
					}
				}, 0L, 5000L);
				return null;
			}
		});
		GameScripts.HEAL.getGameScript().setExectutor(new GameScriptExecutor() {
			@Override
			public Object exec(Object... datas) {
				NetworkedPlayer player = (NetworkedPlayer) datas[0];
				isotopestudio.backdoor.network.server.elements.GameElement target = (isotopestudio.backdoor.network.server.elements.GameElement) datas[1];

				if(target.getTeam() != player.getTeam())
					return false;
				
				target.setTeamPoint(player.getTeam(), target.getMaxPoints());
				
				return null;
			}
		});
		GameScripts.SHIELD.getGameScript().setExectutor(new GameScriptExecutor() {
			@Override
			public Object exec(Object... datas) {
				NetworkedPlayer player = (NetworkedPlayer) datas[0];
				isotopestudio.backdoor.network.server.elements.GameElement target = (isotopestudio.backdoor.network.server.elements.GameElement) datas[1];

				if(target.getTeam() != player.getTeam())
					return false;
				
				target.setProtected(true);
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						target.setProtected(false);						
					}
				}, 30000);
				
				return null;
			}
		});
		GameScripts.FIREWALL.getGameScript().setExectutor(new GameScriptExecutor() {
			@Override
			public Object exec(Object... datas) {
				NetworkedPlayer player = (NetworkedPlayer) datas[0];
				isotopestudio.backdoor.network.server.elements.GameElement target = (isotopestudio.backdoor.network.server.elements.GameElement) datas[1];

				if(target.getTeam() != player.getTeam())
					return false;
				
				int firewall = target.getFirewall() + 5;
				if(firewall > target.getFirewallMax()) {
					firewall = target.getFirewallMax();
				}
				target.setFirewall(firewall);
				
				return null;
			}
		});
		GameScripts.KICKALL.getGameScript().setExectutor(new GameScriptExecutor() {
			@Override
			public Object exec(Object... datas) {
				NetworkedPlayer player = (NetworkedPlayer) datas[0];
				isotopestudio.backdoor.network.server.elements.GameElement target = (isotopestudio.backdoor.network.server.elements.GameElement) datas[1];

				if(target.getTeam() != player.getTeam())
					return false;
				
				target.disconnectAll();
				
				return null;
			}
		});
		GameScripts.SHUTDOWN.getGameScript().setExectutor(new GameScriptExecutor() {
			@Override
			public Object exec(Object... datas) {
				NetworkedPlayer player = (NetworkedPlayer) datas[0];
				isotopestudio.backdoor.network.server.elements.GameElement target = (isotopestudio.backdoor.network.server.elements.GameElement) datas[1];

				if(target.getTeam() != player.getTeam())
					return false;
				
				target.setOffline(true);
				new Timer().schedule(new TimerTask() {
					@Override
					public void run() {
						target.setOffline(false);
					}
				}, 30000);
				
				return null;
			}
		});
	}
}
