package libraries.testing;

import graph.GraphLinker;
import graph.generator.GraphGenerator;
import graph.operations.Deanonymizer;
import graph.operations.Seeds;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class GenerateAssistedNetworkMappingFramework {

	static enum TestNetworkRelationshipTypes implements RelationshipType
    {
    	ACCOUNT_A,
    	CONNECTION_A,
    	ACCOUNT_B,
    	CONNECTION_B,
    	INTERNETWORK_LINK
    }

	public static void main(String[] args) {
		
		int numNodes = 1000;
		String database = "test-reference-network";	
		String networkAPrefix = "facebook";
		String networkBPrefix = "twitter";
		
		GraphGenerator.createWattsStrogatzGraph(database, numNodes, 130, (2.0/10.0));
		
		GraphGenerator.createSubgraphs(database, numNodes, (1.0/2.0),
								       networkAPrefix, TestNetworkRelationshipTypes.ACCOUNT_A, TestNetworkRelationshipTypes.CONNECTION_A,
								       networkBPrefix, TestNetworkRelationshipTypes.ACCOUNT_B, TestNetworkRelationshipTypes.CONNECTION_B);
		
		/*
		// make some noise!!! woot! its a party!
		NoiseGenerator.addRandomEdges(database, networkAPrefix, TestNetworkRelationshipTypes.CONNECTION_A, .10);
		NoiseGenerator.addRandomEdges(database, networkBPrefix, TestNetworkRelationshipTypes.CONNECTION_B, .10);
		NoiseGenerator.removeRandomEdges(database, networkAPrefix, TestNetworkRelationshipTypes.CONNECTION_A, .10);
		NoiseGenerator.removeRandomEdges(database, networkBPrefix, TestNetworkRelationshipTypes.CONNECTION_B, .10);
		
		// make 25% of the Facebook nodes private
		NoiseGenerator.randomlyPrivatizeNodes(database, networkAPrefix, .25);
		*/

		System.out.println("Discovering seeds...");
		
		AbstractGraphDatabase graphDB = new EmbeddedGraphDatabase(database);
		Node root = graphDB.getNodeById(0);
	    
	    LinkedList<Node> networkA = new LinkedList<Node>();
	    LinkedList<Node> networkB = new LinkedList<Node>();
	    
	    for(Relationship relationship : root.getRelationships(TestNetworkRelationshipTypes.ACCOUNT_A)){
	    	// connection is from node to root, so the start node is actually the node of interest
	    	Node node = relationship.getEndNode();
	    	networkA.add(node);
	    }
	    
	    for(Relationship relationship : root.getRelationships(TestNetworkRelationshipTypes.ACCOUNT_B)){
	    	// connection is from node to root, so the start node is actually the node of interest
	    	Node node = relationship.getEndNode();
	    	networkB.add(node);
	    }
	    
	    System.out.println("networkA: " + networkA.size());
	    System.out.println("networkB: " + networkB.size());
	    
	    int numSeeds = 100;
	    System.out.println("Num Seeds = " + numSeeds);
	    
	    Map<Node,Node> seedMap = Seeds.discoverSeeds(networkA, networkB, numSeeds);
		
	    System.out.println("Discovered " + seedMap.keySet().size() + " seeds.");
	    System.out.println("Successfully mapped (seed map): " + scoreMapping(graphDB, seedMap) * 100 + " % of " + seedMap.size() + " mappings.");
	    
	    Map<Node,Node> mapping = Deanonymizer.deanonymize(networkA, TestNetworkRelationshipTypes.CONNECTION_A, networkB, TestNetworkRelationshipTypes.CONNECTION_B, seedMap, .8);
	    
	    System.out.println("Successfully mapped (deanonymized): " + scoreMapping(graphDB, mapping) * 100 + " % of " + mapping.size() + " mappings.");
	    
	    /*
	    // filter out all private nodes from the mapping
	    LinkedList<Node> toRemove = new LinkedList<Node>();
	    for(Node key : mapping.keySet()){
	    	if(key.hasProperty("private")){
	    		toRemove.add(key);
	    	}
	    }
	    for(Node node : toRemove){
	    	mapping.remove(node);
	    }
	    
	    System.out.println("Successfully mapped (deanonymized [private nodes removed]): " + scoreMapping(graphDB, mapping) * 100 + " % of " + mapping.size() + " mappings.");
		*/
	    
	    System.out.println("Linking...");
	    Map<Integer,Integer> linkerMapping = convertMap(mapping, networkAPrefix, networkBPrefix); 
	    
	    graphDB.shutdown();
	    
	    GraphLinker linker = new GraphLinker(database);
	    for(Entry<Integer,Integer> pair : linkerMapping.entrySet()){
	    	linker.addRelationship(networkAPrefix, "" + pair.getKey(), networkBPrefix,"" + pair.getValue(), TestNetworkRelationshipTypes.INTERNETWORK_LINK);
	    }
	    linker.shutdown();

	    System.out.println("Finished generating test network.");
		
	    ////////////////////////////////////////////////////////////////////////////
	    // Start assisted network mapping
	    
	    
	    
	}
	
	private static Map<Integer,Integer> convertMap(Map<Node,Node> mapping, String networkAPrefix, String networkBPrefix){
		Map<Integer,Integer> result = new HashMap<Integer,Integer>();
		for(Entry<Node,Node> pair : mapping.entrySet()){
			int left = Integer.parseInt((String)pair.getKey().getProperty(networkAPrefix + "_id"));
    		int right = Integer.parseInt((String)pair.getValue().getProperty(networkBPrefix + "_id"));
	    	result.put(left, right);
	    }
		return result;
	}
	
	private static double scoreMapping(AbstractGraphDatabase graphDB, Map<Node,Node> mapping){
		int counter = 0;
		for(Node key : mapping.keySet()){
			String a = (String)key.getProperty(GraphGenerator.IDENTIFIER);
			String b = (String)mapping.get(key).getProperty(GraphGenerator.IDENTIFIER);
			if(a.equals(b)){
				counter++;
			}
		}
		return ((double)counter / (double)mapping.size());
	}

}
