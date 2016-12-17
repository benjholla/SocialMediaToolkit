package graph.operations;

import java.util.ArrayList;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class Structure {
	
	/**
	 * Returns an integer representing the number of neighboring nodes connected to the given node.  
	 * The method by default count both incoming and outgoing connections.
	 * @param node The node to be evaluated
	 * @param type The edge type to consider
	 * @return An integer number representing the node degree.
	 */
	public static int nodeDegree(Node node, RelationshipType type){
		return nodeDegree(node, type, Direction.BOTH);
	}
	
	/**
	 * Returns an integer representing the number of neighboring nodes connected to the given node.
	 * @param node The node to be evaluated
	 * @param type The edge type to consider
	 * @param direction The direction to consider (incoming or outgoing)
	 * @return An integer number representing the node degree.
	 */
	public static int nodeDegree(Node node, RelationshipType type, Direction direction){
		int result = 0;
		for(Relationship relationship : node.getRelationships(direction, type)){
			result++;
		}
		return result;
	}
	
	/**
	 * Returns a vector representing the extended degree of the node (the number of neighbors of each neighboring node of the given node)
	 * @param node The node to evaluate
	 * @param type The edge type to consider
	 * @return An array of integers representing the neighboring node degrees of the given node
	 */
	public static ArrayList<Integer> extendedNodeDegrees(Node node, RelationshipType type){
		ArrayList<Integer> result = new ArrayList<Integer>();
		for(Relationship relationship : node.getRelationships(type)){
			// connection is from node to root, so the start node is actually the node of interest
	    	Node extendedNode = relationship.getStartNode();
	    	result.add(nodeDegree(extendedNode, type));
	    }
		return result;
	}
	
}