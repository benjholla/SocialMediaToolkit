package libraries.testing;

import graph.generator.GraphGenerator;

import org.neo4j.graphdb.RelationshipType;

public class Test {

	static enum TestNetworkRelationshipTypes implements RelationshipType
    {
    	ACCOUNT_A,
    	CONNECTION_A,
    	ACCOUNT_B,
    	CONNECTION_B,
    	INTERNETWORK_LINK
    }
	public static void main(String[] args) {
		
		String database = "test-platform";	
		
		int numNodes = 15;
		
		String networkAPrefix = "twitter";
		String networkBPrefix = "facebook";
		
		GraphGenerator.createWattsStrogatzGraph(database, numNodes, 4, (2.0/5.0));
		
		GraphGenerator.createSubgraphs(database, numNodes, (1.0/3.0),
			       networkAPrefix, TestNetworkRelationshipTypes.ACCOUNT_A, TestNetworkRelationshipTypes.CONNECTION_A,
			       networkBPrefix, TestNetworkRelationshipTypes.ACCOUNT_B, TestNetworkRelationshipTypes.CONNECTION_B);
		
	    System.out.println("Finished.");
	    
		// kill off any rouge processes
		System.exit(0);	
	}

}
