import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Scanner;

import db.graph.FacebookGraph;
import facebook.FacebookCrawler;
import facebook.models.AuthenticatedFacebookAccount;

public class Test {


	/*
	public static void main(String[] args) throws ClientProtocolException, IOException {
		// Mozilla/5.0 (compatible; MSIE 7.0; Windows 2000)
		// Nick = 680836756
		
		// login user
		DefaultHttpClient httpClient = FacebookCrawler.login("benholland@fribblesoft.com", "REDACTED");
		
		// get friend id's of user
		LinkedList<String> friends = new LinkedList<String>();
		friends.addAll(FacebookCrawler.getFriends("714214742", httpClient));
		
		System.out.println("\n\n-------------------------------------------------------------\n\n");
		
		// print friend ids
		for(String id : friends){
			System.out.println(id);
		}
		
		//bfs("680836756", httpClient);
		
		
		// close http client
		httpClient.getConnectionManager().shutdown();
	}
	*/
	
	
	public static void main(String[] args) throws IllegalArgumentException, IOException {
		
		// 2^32 = 4294967296
		//        100001897045891
		
		/*
		AuthenticatedFacebookAccount ben = new AuthenticatedFacebookAccount("benholland@fribblesoft.com", "REDACTED");
		for(FacebookAccount account : ben.getFriends(true)){
			System.err.println(account);
			for(FacebookAccount subAccount : account.getFriends(ben, true)){
				System.err.println("     " + subAccount);
			}
		}
		*/
		
		AuthenticatedFacebookAccount rsage = new AuthenticatedFacebookAccount("robin.sage@safetyonthe.net", "REDACTED");
		
		// Origin: Tim Prince - 722408800
		// Target: Ben Kallal - 16916982
		
		// Nick Holland - 680836756
		
		// open a connection to the graph database
		FacebookGraph graph = FacebookGraph.getInstance(true);
		
		// map a path to the target from the orgin and save results to the database
		Long result = FacebookCrawler.mapPathToTarget(Long.parseLong("100003524434527"), Long.parseLong("100003374880352"), rsage, null, graph);
		System.out.println("Friend: " + result);
		
		// repeat the process with a set of blocked nodes that contains the previous result
		Collection<Long> blockedNodes = new LinkedList<Long>();
		blockedNodes.add(result);
		result = FacebookCrawler.mapPathToTarget(Long.parseLong("100003524434527"), Long.parseLong("100003374880352"), rsage, blockedNodes, graph);
		System.out.println("Friend: " + result);
		
		System.out.println("Press enter key to quit.");
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		
		// shutdown graph database nicely
		graph.shutdown();
		
		// kill off any rouge processes
		System.exit(0);
		
		
		// FriendUtilities.getFriendsIDs(Long.parseLong("100003524434527"), rsage, true);
		
		
		
		
		
		// System.out.println(ben);
		
		//System.out.println(SamplingUtilities.getRandomPublicAccount(ben, true));
		
	}

}
