package isotopestudio.backdoor.network.packet.packets;

import java.io.IOException;
import java.util.UUID;

import org.apache.http.client.ClientProtocolException;

import doryanbessiere.isotopestudio.api.IsotopeStudioAPI;
import doryanbessiere.isotopestudio.api.authentification.AuthClient;
import doryanbessiere.isotopestudio.api.authentification.Response;
import doryanbessiere.isotopestudio.api.authentification.User;
import isotopestudio.backdoor.network.packet.Packet;
import isotopestudio.backdoor.network.server.GameServer;
import isotopestudio.backdoor.network.server.GameServer.GameServerClient;
import isotopestudio.backdoor.network.server.party.TeamManager;
import isotopestudio.backdoor.network.server.player.NetworkedPlayer;

public class PacketPlayerLogin extends Packet {

	public static final int USERNAME_ALREADY_USED = 1;
	public static final int SERVER_FULL = 2;
	public static final int INVALID_AUTHENTICATION_SESSION = 3;
	public static final int USERNAME_CHANGED = 4;
	public static final int INVALID_VERSION = 5;
	public static final int NOT_ALLOWED_TO_LOGIN = 6;
	public static final int WRONG_PASSWORD = 7;

	public PacketPlayerLogin() {
		super(LOGIN);
	}

	public PacketPlayerLogin(User user, String gameVersion) {
		super(LOGIN, user.toJson(), gameVersion);
	}

	@Override
	public Packet clone() {
		return new PacketPlayerLogin();
	}

	private User user;
	private String gameVersion;
	private String password;

	public User getUser() {
		return user;
	}
	
	public String getGameVersion() {
		return gameVersion;
	}
	
	/**
	 * @return the paswword
	 */
	public String getPassword() {
		return password;
	}

	@Override
	public void read() {
		this.user = User.fromJson(readString());
		this.gameVersion = readString();
		this.password = readString();
	}

	@Override
	public void process(GameServer server, GameServerClient client) {
		if(!gameVersion.equals(GameServer.getServerVersion())) {
			client.sendPacket(new PacketPlayerLoginFailed(PacketPlayerLogin.INVALID_VERSION));
			client.disconnect("invalid_version");
			return;
		}
		
		if (!(server.getPlayers().isEmpty())) {
			for (NetworkedPlayer player : server.getPlayers()) {
				if (player.getUsername().equalsIgnoreCase(getUser().getUsername())) {
					client.sendPacket(new PacketPlayerLoginFailed(PacketPlayerLogin.USERNAME_ALREADY_USED));
					client.disconnect("logout");
					return;
				}
			}
		}
		if (server.isFull()) {
			client.sendPacket(new PacketPlayerLoginFailed(PacketPlayerLogin.SERVER_FULL));
			client.disconnect("logout");
			return;
		}
		
		if(server.isPrivateServer() && getPassword().equals(server.getPassword())) {
			client.sendPacket(new PacketPlayerLoginFailed(PacketPlayerLogin.WRONG_PASSWORD));
			client.disconnect("logout");
			return;
		}
		
		AuthClient authClient = new AuthClient(IsotopeStudioAPI.API_URL + "/");
		try {
			Response response = authClient.loginToken(user.getEmail(), user.getToken());
			if (response.getPath().equals("success")) {
				String username = response.getInformations().get("username")+"";
				if(!username.equals(getUser().getUsername())) {
					client.sendPacket(new PacketPlayerLoginFailed(PacketPlayerLogin.USERNAME_CHANGED));
					client.disconnect("logout");
					return;
				}
				if((GameServer.allowedUUIDs != null && GameServer.allowedUUIDs.size() > 0) && !GameServer.allowedUUIDs.contains(response.getInformations().get("uuid"))) {
					client.sendPacket(new PacketPlayerLoginFailed(PacketPlayerLogin.NOT_ALLOWED_TO_LOGIN));
					client.disconnect("logout");
					return;
				}
				
				client.setUsername(username);
				client.setUUID(UUID.fromString(response.getInformations().get("uuid")+""));
				client.setTeam(TeamManager.autoJoin(client));
				
				server.getPlayers().add(client);
				server.sendAll(new PacketPlayerConnected(client.getUsername(), client.getUUID().toString(), client.getTeam()));
				/*
				server.sendAll(new PacketPlayerTerminalLangMessage(new LangMessage("server_player_join_the_server", "%username%", client.getUsername())));
				server.sendAll(new PacketPlayerTerminalLangMessage(new LangMessage("server_player_join_a_team", "%username%", client.getUsername(), "%team%", client.getTeam().getPath())));
				*/
				if(server.isFull()) {
					server.startParty();
				}
			} else {
				client.sendPacket(new PacketPlayerLoginFailed(PacketPlayerLogin.INVALID_AUTHENTICATION_SESSION));
				client.disconnect("logout");
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
