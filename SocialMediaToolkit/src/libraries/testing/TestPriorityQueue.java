package libraries.testing;

import java.util.Comparator;
import java.util.PriorityQueue;

public class TestPriorityQueue {
	
	public static void main(String args[]){
		
		Comparator<WeightedTNode> comparator = new WeightedTNodeComparator();
		PriorityQueue<WeightedTNode> pq = new PriorityQueue<WeightedTNode>(1,comparator);
		
		pq.add(new WeightedTNode("a", 0));
		System.out.println(pq);
		
		pq.add(new WeightedTNode("b", 0));
		System.out.println(pq);
		
		pq.add(new WeightedTNode("c", 1));
		System.out.println(pq);
		
		pq.add(new WeightedTNode("d", 1));
		System.out.println(pq);
		
		pq.remove(new WeightedTNode("a", 0));
		pq.add(new WeightedTNode("a", 1));
		
		System.out.println(pq);
		
		
	}
	
}
