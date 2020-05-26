package isotopestudio.backdoor.network.server.map;

import isotopestudio.backdoor.core.elements.GameElementType;
import isotopestudio.backdoor.core.map.MapData;
import isotopestudio.backdoor.core.team.Team;
import isotopestudio.backdoor.network.server.elements.GameElement;

public class ServerMapData {

	public static MapData mapDefault() {
		MapData mapData = new MapData("default");

		GameElement red_server= new GameElement(GameElementType.SERVER, "red_server", Team.RED);

		GameElement node_entity_1 = new GameElement(GameElementType.NODE, "node1", null);
		GameElement node_entity_2 = new GameElement(GameElementType.NODE, "node2", null);
		
		GameElement node_entity_3 = new GameElement(GameElementType.NODE, "node3", null);
		GameElement node_entity_4 = new GameElement(GameElementType.NODE, "node4", null);
		GameElement node_entity_5 = new GameElement(GameElementType.NODE, "node5", null);
		
		GameElement node_entity_6 = new GameElement(GameElementType.NODE, "node6",  null);
		GameElement node_entity_7 = new GameElement(GameElementType.NODE, "node7", null);
		GameElement node_entity_8 = new GameElement(GameElementType.NODE, "node8", null);
		
		GameElement node_entity_9 = new GameElement(GameElementType.NODE, "node9", null);
		GameElement node_entity_10 = new GameElement(GameElementType.NODE, "node10", null);
		
		GameElement blue_server= new GameElement(GameElementType.SERVER, "blue_server", Team.BLUE);

		mapData.team_servers.put(Team.RED, red_server);
		mapData.team_servers.put(Team.BLUE, blue_server);

		red_server.link(node_entity_1);
		red_server.link(node_entity_2);

		node_entity_1.link(node_entity_4);
		node_entity_1.link(node_entity_5);

		node_entity_2.link(node_entity_4);
		node_entity_2.link(node_entity_3);

		node_entity_3.link(node_entity_6);
		node_entity_3.link(node_entity_7);

		node_entity_4.link(node_entity_6);
		node_entity_4.link(node_entity_8);

		node_entity_5.link(node_entity_7);
		node_entity_5.link(node_entity_8);

		node_entity_6.link(node_entity_10);

		node_entity_7.link(node_entity_9);
		node_entity_7.link(node_entity_10);

		node_entity_8.link(node_entity_9);

		node_entity_9.link(blue_server);
		node_entity_10.link(blue_server);
		
		mapData.getElements().put(red_server.getName(), red_server);
		
		mapData.getElements().put(node_entity_1.getName(), node_entity_1);
		mapData.getElements().put(node_entity_2.getName(), node_entity_2);
		mapData.getElements().put(node_entity_3.getName(), node_entity_3);
		mapData.getElements().put(node_entity_4.getName(), node_entity_4);
		mapData.getElements().put(node_entity_5.getName(), node_entity_5);
		mapData.getElements().put(node_entity_6.getName(), node_entity_6);
		mapData.getElements().put(node_entity_7.getName(), node_entity_7);
		mapData.getElements().put(node_entity_8.getName(), node_entity_8);
		mapData.getElements().put(node_entity_9.getName(), node_entity_9);
		mapData.getElements().put(node_entity_10.getName(), node_entity_10);
		
		mapData.getElements().put(blue_server.getName(), blue_server);
		
		return mapData;
	}
}
