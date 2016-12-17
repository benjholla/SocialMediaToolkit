package libraries.testing;

import graph.ProprietaryGraph;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Map.Entry;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class TestNormalization {

	public static enum NetworkRelationshipTypes implements RelationshipType
    {
    	TWITTER_ACCOUNT,
    	FOLLOWS,
    	FACEBOOK_ACCOUNT,
    	FRIEND
    }
	
	public static void main(String[] args) {
	
		ProprietaryGraph graph = new ProprietaryGraph("test2", "facebook", NetworkRelationshipTypes.FACEBOOK_ACCOUNT);
		graph.addNode("1");
		graph.addNode("2");
		graph.addNode("3");
		graph.addNode("5");
		graph.addRelationship("1", "2", NetworkRelationshipTypes.FRIEND);
		graph.addRelationship("2", "1", NetworkRelationshipTypes.FRIEND);
		graph.addRelationship("3", "5", NetworkRelationshipTypes.FRIEND);
		graph.addRelationship("5", "3", NetworkRelationshipTypes.FRIEND);
		graph.addRelationship("2", "5", NetworkRelationshipTypes.FRIEND);
		graph.addRelationship("5", "2", NetworkRelationshipTypes.FRIEND);
		graph.shutdown();
		
	}
		/*
		ProprietaryGraph graph = new ProprietaryGraph("test", "twitter", NetworkRelationshipTypes.TWITTER_ACCOUNT);
		graph.addNode("1");
		graph.addNode("2");
		graph.addNode("3");
		graph.addNode("4");
		graph.addNode("5");
		graph.addNode("6");
		
		graph.addRelationship("1", "2", NetworkRelationshipTypes.FOLLOWS);
		graph.addRelationship("2", "1", NetworkRelationshipTypes.FOLLOWS);
		
		graph.addRelationship("3", "5", NetworkRelationshipTypes.FOLLOWS);
		graph.addRelationship("5", "3", NetworkRelationshipTypes.FOLLOWS);
		
		graph.addRelationship("2", "5", NetworkRelationshipTypes.FOLLOWS);
		graph.addRelationship("5", "2", NetworkRelationshipTypes.FOLLOWS);
		
		graph.addRelationship("4", "1", NetworkRelationshipTypes.FOLLOWS);
		graph.addRelationship("2", "4", NetworkRelationshipTypes.FOLLOWS);
		graph.addRelationship("6", "4", NetworkRelationshipTypes.FOLLOWS);
		graph.addRelationship("5", "6", NetworkRelationshipTypes.FOLLOWS);
		graph.addRelationship("2", "6", NetworkRelationshipTypes.FOLLOWS);
		graph.addRelationship("2", "3", NetworkRelationshipTypes.FOLLOWS);
		*/
		
		/*
		 * normalize_twitter_to_facebook(lgraph){
		 * foreach node in lgraph.nodes
		 *   foreach outgoing in node.outgoing_edges
		 *     if(outgoing.end.outgoing_edges.contains(node))
		 *       if(!marked_edges.contains(node, outgoing.end))
		 *         if(!rgraph.contains(node))
		 *           rgraph.nodes.add(node)
		 *         if(!rgraph.contains(outgoing.end))
		 *           rgraph.nodes.add(outgoing.end)
		 *         rgraph.edges.add(node,outgoing.end)
		 *       marked_edges.add(outgoing.end,node)
		 *  return rgraph
		 * }
		 */
		
		/*
		ArrayList<Entry<Integer,Integer>> markedEdges = new ArrayList<Entry<Integer,Integer>>();
		
		for(int i=1; i<=6; i++){
			Node node = graph.getNode("" + i);
			for(Relationship outgoing : node.getRelationships(NetworkRelationshipTypes.FOLLOWS, Direction.OUTGOING)){
				if(contains(outgoing.getEndNode().getRelationships(NetworkRelationshipTypes.FOLLOWS, Direction.OUTGOING),node)){
					if(!markedEdges.contains(new AbstractMap.SimpleEntry<Integer,Integer>(i,Integer.parseInt((String) outgoing.getEndNode().getProperty("twitter_id"))))){
						System.out.println(i + " and " + outgoing.getEndNode().getProperty("twitter_id") + " are friends.");
						markedEdges.add(new AbstractMap.SimpleEntry<Integer,Integer>(Integer.parseInt((String) outgoing.getEndNode().getProperty("twitter_id")),i));
					}
					
				}
			}
		}
		
		graph.shutdown();
		
	}

	/*
	public static boolean contains(Iterable<Relationship> outgoingRelationships, Node node){
		for(Relationship edge : outgoingRelationships){
			if(edge.getEndNode().equals(node)){
				return true;
			}
		}
		return false;
	}
	*/
	
}
