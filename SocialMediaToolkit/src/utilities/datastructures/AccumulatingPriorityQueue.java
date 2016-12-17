package utilities.datastructures;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class AccumulatingPriorityQueue<E> {

	public static void main(String[] args) {
		AccumulatingPriorityQueue<String> pq = new AccumulatingPriorityQueue<String>(1);
		
		pq.enqueue("a", 0);
		System.out.println(pq + " - " + pq.getMode());
		
		pq.enqueue("b", 0);
		System.out.println(pq + " - " + pq.getMode());
		
		pq.enqueue("b", 0);
		System.out.println(pq + " - " + pq.getMode());
		
		pq.enqueue("b", 1);
		System.out.println(pq + " - " + pq.getMode());
		
		System.out.println(pq.dequeue());
		System.out.println(pq + " - " + pq.getMode());
		
		pq.enqueue("d", 0);
		System.out.println(pq + " - " + pq.getMode());
		
		pq.enqueue("e", 2);
		System.out.println(pq + " - " + pq.getMode());
		
		pq.enqueue("f", 0);
		System.out.println(pq + " - " + pq.getMode());
		
		pq.enqueue("a", 0);
		System.out.println(pq + " - " + pq.getMode());
	}

	private ArrayList<Priority<E>> pq = new ArrayList<Priority<E>>();
	private Map<E,Integer> observedFrequency = new HashMap<E,Integer>();
	private boolean isSeeking = true;
	
	private int mode = 1;
	
	public AccumulatingPriorityQueue(int mode){
		this.mode = mode;
	}
	
	public int size(){
		return pq.size();
	}
	
	// assumes the queue is a set
	public void enqueue(E node, int priority) {
		if(observedFrequency.containsKey(node)){
			observedFrequency.put(node, observedFrequency.get(node)+1);
		} else {
			observedFrequency.put(node,1);
		}
		Priority<E> p = new Priority<E>(node, priority);
		if (!pq.contains(p)) {
			pq.add(new Priority<E>(node, priority));
		} else {
			// priority exists, add priority weight if not zero (adding 0 is pointless...)
			if(priority != 0){
				for (int i = 0; i < pq.size(); i++) {
					if (pq.get(i).getNode().equals(node)) {
						p = new Priority<E>(node, pq.get(i).getPrimaryPriority() + priority);
						pq.set(i, p);
					}
				}
			}
		}
		// at this point one item is out of order (or its perfectly sorted)
		// with hunter seeker mod we always need to sort now because adding a zero can move down secondary priority
		insertionSort(pq);
	}
	
	/*
	public void replace(E node, int priority){
		for (int i = 0; i < pq.size(); i++) {
			if (pq.get(i).getNode().equals(node)) {
				Priority<E> p = new Priority<E>(node, priority);
				pq.set(i, p);
			}
		}
		insertionSort(pq);
	}
	
	public void replace(Collection<E> nodes, int priority){
		for(E node : nodes){
			replace(node, priority);
		}
	}
	*/
	
	public String getMode(){
		if(isSeeking){
			return "Seeker";
		} else {
			return "Hunter";
		}
	}
	
	public void enqueue(Collection<E> nodes, int priority){
		for(E node : nodes){
			enqueue(node, priority);
		}
	}
	
	public E dequeue() {
		Priority<E> p = pq.remove(0);
		if(p.getPrimaryPriority() == 0){
			isSeeking = true;
		} else {
			isSeeking = false;
		}
		return p.getNode();
	}
	
	public E peek(){
		return pq.get(0).getNode();
	}
	
	public int peekPrimaryScore(){
		return pq.get(0).getPrimaryPriority();
	}
	
	public int peekSecondaryScore(){
		return pq.get(0).getSecondaryPriority();
	}
	
	public boolean isEmpty(){
		return pq.isEmpty();
	}
	
	// better to use insertion sort instead of this...
	/*
	private void bubbleSort(ArrayList<Priority<E>> pq) {
		Priority<E> temp;
		if (pq.size() > 1) {
			for (int x = 0; x < pq.size(); x++) // bubble sort outer loop
			{
				for (int i=0; i < pq.size() - x - 1; i++){
					if (pq.get(i).getPriority() < pq.get(i + 1).getPriority()) {
						temp = pq.get(i);
						pq.set(i, pq.get(i + 1));
						pq.set(i + 1, temp);
					}
				}
			}
		}
	}
	*/
	
	private void insertionSort(ArrayList<Priority<E>> pq){
		for (int i = 1; i < pq.size(); i++){
			int j = i;
			Priority<E> B = pq.get(i);
			while ((j > 0) && (pq.get(j-1).compareTo(B) < 0)){
				pq.set(j,pq.get(j-1));
				j--;
			}
			pq.set(j,B);
		}
	}
	
	@Override
	public String toString(){
		return pq.toString();
	}

	public class Priority<F> {
		private F node;
		private int priority;

		public Priority(F node, int priority) {
			this.node = node;
			this.priority = priority;
		}

		public F getNode() {
			return node;
		}

		public int compareTo(Priority<E> p){
			
			// high mutual with target primary, high observed count secondary
			if(mode == 1){
				if(getPrimaryPriority() > p.getPrimaryPriority()){
					return 1;
				} else if(getPrimaryPriority() < p.getPrimaryPriority()){
					return -1;
				} else {
					if(getSecondaryPriority() > p.getSecondaryPriority()){
						return 1;
					} else if(getSecondaryPriority() < p.getSecondaryPriority()){
						return -1;
					} else {
						return 0;
					}
				}
			}
			
			
			// only high mutual with target primary
			if(mode == 2){
				if(getPrimaryPriority() > p.getPrimaryPriority()){
					return 1;
				} else if(getPrimaryPriority() < p.getPrimaryPriority()){
					return -1;
				} else {
					return 0;
				}
			}
						
			
			// only high observed count
			if(mode == 3){
				if(getSecondaryPriority() > p.getSecondaryPriority()){
					return 1;
				} else if(getSecondaryPriority() < p.getSecondaryPriority()){
					return -1;
				} else {
					return 0;
				}
			}
			
			
			// high observed count primary, high mutual with target secondary
			// this ones not bad... a consistent 5-10% better than BFS...
			if(mode == 4){
				if(getSecondaryPriority() > p.getSecondaryPriority()){
					return 1;
				} else if(getSecondaryPriority() < p.getSecondaryPriority()){
					return -1;
				} else {
					if(getPrimaryPriority() > p.getPrimaryPriority()){
						return 1;
					} else if(getPrimaryPriority() < p.getPrimaryPriority()){
						return -1;
					} else {
						return 0;
					}
				}
			}
			
			// hunter / seeker mode
			if(mode == 5){
				if(getPrimaryPriority() > p.getPrimaryPriority()){
					return 1;
				} else if(getPrimaryPriority() < p.getPrimaryPriority()){
					return -1;
				} else {
					if(getSecondaryPriority() < p.getSecondaryPriority()){
						return 1;
					} else if(getSecondaryPriority() > p.getSecondaryPriority()){
						return -1;
					} else {
						return 0;
					}
				}
			}
			
			return 0;  // missed a case, just a BFS now
		}
		
		private int getPrimaryPriority() {
			return priority;
		}
		
		private int getSecondaryPriority() {
			return observedFrequency.get(node);
		}

		@Override
		public String toString() {
			return "{" + node.toString() + "," + getPrimaryPriority() + ":" + getSecondaryPriority() + "}";
		}

		@Override
		public boolean equals(Object obj) {
			return ((Priority) obj).getNode().equals(node);
		}
	}

}
