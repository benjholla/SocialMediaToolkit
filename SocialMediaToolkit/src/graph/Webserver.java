package graph;

import java.util.Scanner;

import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.Config;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.server.WrappingNeoServerBootstrapper;

public class Webserver {

	public static void main(String[] args) {
		String database = "demo";
		AbstractGraphDatabase graphDB = new EmbeddedGraphDatabase(database, MapUtil.stringMap(Config.ENABLE_REMOTE_SHELL, "true"));
		WrappingNeoServerBootstrapper webServer = new WrappingNeoServerBootstrapper(graphDB);
        webServer.start(); 
        System.out.println("Press enter key to quit.");
		Scanner scanner = new Scanner(System.in);
		scanner.nextLine();
		graphDB.shutdown();
		System.exit(0);
	}

}
