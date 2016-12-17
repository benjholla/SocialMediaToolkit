package libraries.testing;
import graph.FacebookGraph;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Scanner;

import utilities.networks.facebook.crawler.FacebookCrawler;
import utilities.networks.facebook.models.AuthenticatedFacebookAccount;

public class TestFacebookCrawler {
	
	public static void main(String[] args) throws IllegalArgumentException, IOException {
		
		AuthenticatedFacebookAccount rsage = new AuthenticatedFacebookAccount("robin.sage@safetyonthe.net", "REDACTED");
		
		// open a connection to the graph database
		FacebookGraph graph = new FacebookGraph("demo");
		
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
	}

}