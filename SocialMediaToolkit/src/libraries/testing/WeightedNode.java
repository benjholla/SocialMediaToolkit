package libraries.testing;

import org.neo4j.graphdb.Node;

public class WeightedNode {

	private Node node;
	private int priority;
	
	public WeightedNode(Node node, int priority){
		this.node = node;
		this.priority = priority;
	}
	
	public Node getNode(){
		return node;
	}
	
	public int getPriority(){
		return priority;
	}
	
	@Override
	public String toString(){
		return node.toString();
	}
	
	@Override
	public boolean equals(Object obj){
		return ((WeightedNode)obj).getNode().equals(node) && ((WeightedNode)obj).getPriority() == priority;
	}
	
}
