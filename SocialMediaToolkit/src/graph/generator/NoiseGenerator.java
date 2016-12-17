package graph.generator;

import java.util.ArrayList;
import java.util.Random;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.Config;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class NoiseGenerator {

	/**
	 * Randomly adds an edge between every node and a randomly chosen node with the given probability
	 * @param database
	 * @param networkPrefix
	 * @param type
	 * @param probability
	 */
	public static void addRandomEdges(String database, String networkPrefix, RelationshipType type, double probability){
		System.out.println("Randomly adding edges for each " + networkPrefix + " node with probability " + probability);
		AbstractGraphDatabase graphDB = new EmbeddedGraphDatabase(database, MapUtil.stringMap(Config.ENABLE_REMOTE_SHELL, "true"));
		ArrayList<Integer> networkNodeIndices = getNetworkNodeIndices(graphDB, networkPrefix);
		Index<Node> nodeIndex = graphDB.index().forNodes("nodes");
		Random rnd = new Random();
		
		for(Integer indexA : networkNodeIndices){
			if(rnd.nextDouble() <= probability){
				int indexB = networkNodeIndices.get(rnd.nextInt(networkNodeIndices.size()));
				// avoid self loops
				while(indexA == indexB){
					indexB = networkNodeIndices.get(rnd.nextInt(networkNodeIndices.size()));
				}
				Node nodeA = nodeIndex.get(networkPrefix + "_id", "" + indexA).getSingle();
				Node nodeB = nodeIndex.get(networkPrefix + "_id", "" + indexB).getSingle();
				Transaction tx = graphDB.beginTx();
                try
                {
                	nodeA.createRelationshipTo(nodeB, type);
                    tx.success();
                }
                finally
                {
                    tx.finish();
                }
			}
		}
		graphDB.shutdown();
	}
	
	/**
	 * Examines each edge of each node and deletes the edge with the given probability
	 * @param database
	 * @param networkPrefix
	 * @param type
	 * @param probability
	 */
	public static void removeRandomEdges(String database, String networkPrefix, RelationshipType type, double probability){
		System.out.println("Randomly removing edges for each " + networkPrefix + " node with probability " + probability);
		AbstractGraphDatabase graphDB = new EmbeddedGraphDatabase(database, MapUtil.stringMap(Config.ENABLE_REMOTE_SHELL, "true"));
		ArrayList<Integer> networkNodeIndices = getNetworkNodeIndices(graphDB, networkPrefix);
		Index<Node> nodeIndex = graphDB.index().forNodes("nodes");
		Random rnd = new Random();
		
		for(Integer index : networkNodeIndices){
			Node node = nodeIndex.get(networkPrefix + "_id", "" + index).getSingle();
			for(Relationship relationship : node.getRelationships(type)){
				if(rnd.nextDouble() <= probability){
					Transaction tx = graphDB.beginTx();
	                try
	                {
	                	relationship.delete();
	                    tx.success();
	                }
	                finally
	                {
	                    tx.finish();
	                }
				}
			}
		}
		graphDB.shutdown();
	}
	
	private static ArrayList<Integer> getNetworkNodeIndices(AbstractGraphDatabase graphDB, String networkPrefix){
		ArrayList<Integer> networkNodeIndex = new ArrayList<Integer>();
	    // TODO: use a graph traversal to do this more efficiently...
	    for(Node node : graphDB.getAllNodes()){
	    	if(node.hasProperty(networkPrefix + "_id")){
	    		networkNodeIndex.add(Integer.parseInt((String)node.getProperty(networkPrefix + "_id")));
	    	}
	    }
	    return networkNodeIndex;
	}
	
	
	public static void randomlyPrivatizeNodes(String database, String networkPrefix, double probability){
		System.out.println("Randomly privatizing  each " + networkPrefix + " node with probability " + probability);
		AbstractGraphDatabase graphDB = new EmbeddedGraphDatabase(database, MapUtil.stringMap(Config.ENABLE_REMOTE_SHELL, "true"));
		ArrayList<Integer> networkNodeIndices = getNetworkNodeIndices(graphDB, networkPrefix);
		Index<Node> nodeIndex = graphDB.index().forNodes("nodes");
		Random rnd = new Random();
		
		for(Integer index : networkNodeIndices){
			if(rnd.nextDouble() <= probability){
				Node node = nodeIndex.get(networkPrefix + "_id", "" + index).getSingle();
				Transaction tx = graphDB.beginTx();
                try
                {
                	node.setProperty("private", "true");
                    tx.success();
                }
                finally
                {
                    tx.finish();
                }
			}
		}
		graphDB.shutdown();
	}
	
}
