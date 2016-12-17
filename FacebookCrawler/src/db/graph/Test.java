package db.graph;

public class Test {

	public static void main(String[] args) {
		
		/*
			{
			   "id": "100003524434527",
			   "name": "Robin Sage",
			   "first_name": "Robin",
			   "last_name": "Sage",
			   "link": "http://www.facebook.com/people/Robin-Sage/100003524434527",
			   "gender": "female",
			   "locale": "en_US"
			}
			
			{
			   "id": "100003330624979",
			   "name": "Norman Lloyd",
			   "first_name": "Norman",
			   "last_name": "Lloyd",
			   "link": "http://www.facebook.com/people/Norman-Lloyd/100003330624979",
			   "gender": "male",
			   "locale": "en_US"
			}
			
			{
			   "id": "100003321820259",
			   "name": "Hayley Jane",
			   "first_name": "Hayley",
			   "last_name": "Jane",
			   "link": "http://www.facebook.com/people/Hayley-Jane/100003321820259",
			   "gender": "female",
			   "locale": "en_US"
			}
			
			{
			   "id": "100003374880352",
			   "name": "Stuart Fox",
			   "first_name": "Stuart",
			   "last_name": "Fox",
			   "gender": "male",
			   "locale": "en_US"
			}
			
			{
			   "id": "100003490386924",
			   "name": "Patricia Rogers",
			   "first_name": "Patricia",
			   "last_name": "Rogers",
			   "link": "http://www.facebook.com/people/Patricia-Rogers/100003490386924",
			   "gender": "female",
			   "locale": "en_US"
			}
			
			{
			   "id": "100003468429005",
			   "name": "Keith Collin",
			   "first_name": "Keith",
			   "last_name": "Collin",
			   "link": "http://www.facebook.com/people/Keith-Collin/100003468429005",
			   "gender": "male",
			   "locale": "en_US"
			}
		 */
		
		FacebookGraph graph = FacebookGraph.getInstance(true);
		
		// System.out.println(graph.isWebServerLaunched());
		// graph.launchWebServer();
		// System.out.println(graph.isWebServerLaunched());
		
		graph.addNode(Long.parseLong("100003524434527"));
		graph.addNode(Long.parseLong("100003330624979"));
		graph.addRelationship(Long.parseLong("100003524434527"), Long.parseLong("100003330624979"), FacebookGraph.RelationshipTypes.FRIEND);
		
		// System.out.println(graph.nodeExists(Long.parseLong("100003524434527")));
		
		
		
		graph.shutdown();

	}

}
