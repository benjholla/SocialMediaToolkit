package libraries.testing;

import graph.operations.Seeds;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class Deanonymize {

	public static void main(String[] args) {
		
		System.out.println("Discovering seeds...");
		
		AbstractGraphDatabase graphDB = new EmbeddedGraphDatabase("primary-test");
		Node root = graphDB.getNodeById(0);
	    
	    LinkedList<Node> networkA = new LinkedList<Node>();
	    LinkedList<Node> networkB = new LinkedList<Node>();	
	   /*
		for(Node node : root.getAllNodes()){
			for(Relationship rel : node.getRelationships()){
				if(rel.getType().toString().equals("A")){
					networkA.add(node);
				} else {
					networkB.add(node);
				}
			}
		}
		*/
	    
	    System.out.println("networkA: " + networkA.size());
	    System.out.println("networkB: " + networkB.size());
	    
	    int numSeeds = 100;
	    System.out.println("Num Seeds = " + 1);
	    
	    
	    Map<String,Node> idMap = new HashMap<String,Node>();
	    for(Node node : networkA){
	    	idMap.put((String)node.getProperty("id"), node);
	    }
		
	    /*
	    System.out.println("Discovered " + seedMap.keySet().size() + " seeds.");
	    System.out.println("Successfully mapped (seed map): " + scoreMapping(graphDB, seedMap) * 100 + " % of " + seedMap.size() + " mappings.");
	    
	    Map<Node,Node> mapping = Deanonymizer.deanonymize(networkA, TestNetworkRelationshipTypes.CONNECTION_A, networkB, TestNetworkRelationshipTypes.CONNECTION_B, seedMap, .8);
	    
	    System.out.println("Successfully mapped (deanonymized): " + scoreMapping(graphDB, mapping) * 100 + " % of " + mapping.size() + " mappings.");
	    
	    System.out.println("Linking...");
	    Map<Integer,Integer> linkerMapping = convertMap(mapping, networkAPrefix, networkBPrefix); 
	    
	    graphDB.shutdown();
	    
	    GraphLinker linker = new GraphLinker(database);
	    for(Entry<Integer,Integer> pair : linkerMapping.entrySet()){
	    	linker.addRelationship(networkAPrefix, "" + pair.getKey(), networkBPrefix,"" + pair.getValue(), TestNetworkRelationshipTypes.INTERNETWORK_LINK);
	    }
	    linker.shutdown();
	    */

	    System.out.println("Finished generating test network.");
		
		
	}

}
