package libraries.testing;


public class WeightedTNode {

	private String node;
	private int priority;
	
	public WeightedTNode(String node, int priority){
		this.node = node;
		this.priority = priority;
	}
	
	public String getNode(){
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
		// return obj.equals(node);
		return ((WeightedTNode)obj).getNode().equals(node) && ((WeightedTNode)obj).getPriority() == priority;
	}
	
}
