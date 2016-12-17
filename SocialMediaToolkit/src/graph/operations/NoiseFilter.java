package graph.operations;

import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

public class NoiseFilter {

	public static Set<Node> intersectionFilter(Collection<Node> source, Collection<Node> filter, RelationshipType internetworkRelationshipType){
		Set<Node> result = new TreeSet<Node>();
	    for (Node x : source)
	      if (contains(source, x, internetworkRelationshipType))
	        result.add(x);
	    return result;
	}
	
	public static Set<Node> intersectionFilters(Collection<Node> source, Collection<Collection<Node>> filters, RelationshipType internetworkRelationshipType){
		Set<Node> result = new TreeSet<Node>();
		result.addAll(source);
	    for(Collection<Node> filter : filters){
	    	result = intersectionFilter(result, filter, internetworkRelationshipType);
	    }
	    return result;
	}
	
	private static boolean isInternetworked(Node a, Node b, RelationshipType type){
		for(Relationship relationship : a.getRelationships(type)){
			if(relationship.getStartNode().equals(b) || relationship.getEndNode().equals(b)){
				return true;
			}
		}
		return false;
	}
	
	private static boolean contains(Collection<Node> a, Node b, RelationshipType internetworkRelationshipType){
		for(Node node : a){
			if(isInternetworked(node, b, internetworkRelationshipType)){
				return true;
			}
		}
		return false;
	}
	
}
