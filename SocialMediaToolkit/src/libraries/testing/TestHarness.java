package libraries.testing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.neo4j.graphalgo.CommonEvaluators;
import org.neo4j.graphalgo.CostEvaluator;
import org.neo4j.graphalgo.GraphAlgoFactory;
import org.neo4j.graphalgo.PathFinder;
import org.neo4j.graphalgo.WeightedPath;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipExpander;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.kernel.Traversal;

import utilities.datastructures.AccumulatingPriorityQueue;
import utilities.datastructures.AccumulatingPriorityQueue.Priority;

public class TestHarness {
	
	private static int API_CALLS = 0;
	private static int PRIVATE_NODE_ENCOUNTERS = 0;
	
	private static final String database = "watts-strogatz";
	private static int numNodes;
	private static RelationshipType type = null;
	
	public static void main(String[] args) throws IOException {
		
		AbstractGraphDatabase graphDB = new EmbeddedGraphDatabase(database);
		
		LinkedList<Node> network = new LinkedList<Node>();
		// get network nodes
		for(Node node : graphDB.getAllNodes()){
			network.add(node);
		}
		network.remove();
		
		// get type
		// type = ImportGraph.RelationshipTypes.UNDIRECTED;
		for(Node node : graphDB.getAllNodes()){
			for(Relationship relationship : node.getRelationships()){
				type = relationship.getType();
				if(type != null){
					break;
				}
			}
			if(type != null){
				break;
			}
		}
		
		
		numNodes = network.size();
		int nodesToPrivatize = (int) (Math.round(((double)network.size()) * .75));
		Random rnd = new Random();
		
        for(int i=1; i<=10; i++){
        	Node origin = null;
        	Node target = null;
        	
        	// origin outside target network
        	LinkedList<Node> actualFriends = new LinkedList<Node>();
    		do {
    			actualFriends.clear();
    			target = network.get(rnd.nextInt(network.size()));
    			origin = network.get(rnd.nextInt(network.size()));
    			actualFriends.addAll(reveal(target, type));
    		} while(origin == null || target == null || origin.equals(target) || actualFriends.contains(origin));
    		
    		// privatize target
    		Transaction tx = graphDB.beginTx();
            try
            {
            	target.setProperty("private", "true");
                tx.success();
            }
            finally
            {
                tx.finish();
            }
            
            
            LinkedList<Node> privateNodes = new LinkedList<Node>();
            while(privateNodes.size() + 1 < nodesToPrivatize){ // +1, count the target node too
            	Node nodeToPrivatize = null;
        		do {
        			nodeToPrivatize = network.get(rnd.nextInt(network.size()));
        		} while(nodeToPrivatize == null || origin.equals(nodeToPrivatize) || target.equals(nodeToPrivatize) || privateNodes.contains(nodeToPrivatize)); // can't be the target or the origin
        		Transaction tx2 = graphDB.beginTx();
                try
                {
                	nodeToPrivatize.setProperty("private", "true");
                    tx2.success();
                }
                finally
                {
                    tx2.finish();
                }
                privateNodes.add(nodeToPrivatize);
            }
            
            System.err.println("Privatized: " + (privateNodes.size() + 1) + " / " + network.size());
    		
            System.out.println("Pass: " + i);
            
            // collect data for origin outside network
    		collectData(origin, target);
    		
    		// keep target the same, pick an origin inside target network
    		// origin outside target network
    		do {
    			origin = actualFriends.get(rnd.nextInt(actualFriends.size()));
    		} while(origin == null || target == null || origin.equals(target) || !actualFriends.contains(origin));
    		
    		// collect data for origin inside network
    		collectData(origin, target);

    		// unprivatized target
    		Transaction tx2 = graphDB.beginTx();
            try
            {
            	target.removeProperty("private");
            	for(Node node : privateNodes){
            		node.removeProperty("private");
            	}
                tx2.success();
            }
            finally
            {
                tx2.finish();
            }
            
        }
        
		graphDB.shutdown();
	    
	    System.exit(0);
	}

	private static void collectData(Node origin, Node target) throws IOException {
		//System.out.println("Origin: " + origin);
		//System.out.println("Target: " + target);
		//System.out.println("Origin in target neighborhood: " + reveal(target, type).contains(origin));
		/*
		priorityMap(target, origin, type, 1);
		System.out.println("Finished Priority Mode 1.");
		priorityMap(target, origin, type, 2);
		System.out.println("Finished Priority Mode 2.");
		priorityMap(target, origin, type, 3);
		System.out.println("Finished Priority Mode 3.");
		priorityMap(target, origin, type, 4);
		System.out.println("Finished Priority Mode 4.");
		*/
		
		priorityMap(target, origin, type, 5);
		//System.out.println("Finished Priority Mode 5.");
		
		/*
		bfs(target, origin, type);
		System.out.println("Finished BFS.");
		dfs(target, origin, type);
		System.out.println("Finished DFS.");
		*/
	}
	
	public static void resetCounters(){
		// reset
		API_CALLS = 0;
		PRIVATE_NODE_ENCOUNTERS = 0;
	}
	
	public static Set<Node> priorityMap(Node target, Node origin, RelationshipType network, int mode) throws IOException {
		
		Set<Node> neigborhood = new HashSet<Node>();
		
		// target is private and has no neighbors
		if(origin != null && target != null && origin.equals(target)){
			return neigborhood;
		}
		
		Set<Node> searched = new HashSet<Node>();
		
		AccumulatingPriorityQueue<Node> pq = new AccumulatingPriorityQueue<Node>(mode);
		
		pq.enqueue(origin, 0);
		
		LinkedList<Node> actualFriends = new LinkedList<Node>();
		actualFriends.addAll(reveal(target, network));
		String location = actualFriends.contains(origin) ? "Start Inside Target Neighborhood" : "Start Outside Target Neighborhood";
		FileWriter fw = new FileWriter(new File("Variation " + mode + " - " + database + " - " + location + ".csv"), true);
		// fw.write("Priority " + mode + " API Ratio,Priority " + mode + " Friend Ratio\n");
		
		Map<Node,Integer> suspects = new HashMap<Node,Integer>();
		
		while(!pq.isEmpty()){
			Node node = pq.dequeue();
			
			if(node == null || searched.contains(node)){
				continue;
			}
			
			Set<Node> neighbors = explode(node, network);
			
			if(neighbors.contains(target)){
				neigborhood.add(node);
				pq.enqueue(neighbors, 1); // add all, increments weight by 1 if already exists
				
				for(Node suspect : neighbors){
					if(suspects.containsKey(suspect)){
						suspects.put(node, suspects.get(suspect) + 1);
					} else {
						if(!searched.contains(suspect)){
							suspects.put(suspect, 1);
						}
					}
				}
				
				if(suspects.containsKey(node)){
					suspects.remove(node);
				}
				
				if(mode == 5){
					fw.write(((double)API_CALLS)/numNodes + "," + ((double)neigborhood.size())/actualFriends.size() + "," + pq.getMode() + "\n");
				} else {
					fw.write(((double)API_CALLS)/numNodes + "," + ((double)neigborhood.size())/actualFriends.size() + /* "," + pq.getMode() + */ "\n");
				}
				
			} else {
				pq.enqueue(neighbors, 0);
			}
			
			searched.add(node);
		}
		
		// just for good measure strip out the searched nodes
		for(Node n : neigborhood){
			if(suspects.containsKey(n)){
				suspects.remove(n);
			}
		}
		
		
		Set<Integer> thresholds = new HashSet<Integer>();
		for(Integer val : suspects.values()){
			thresholds.add(val);
		}
		
		for(Integer i : thresholds){
			int suspectsCorrect = 0;
			for(Node suspect : suspects.keySet()){
				if(actualFriends.contains(suspect) && suspects.get(suspect) <= i){
					suspectsCorrect++;
				}
			}
			System.err.println("Threshold: " + i + " , Suspects Correct: " + suspectsCorrect + " , Suspects: " + suspects.size() + " , Verified: " + neigborhood.size() + " , Actual Friends: " + actualFriends.size());
		}
		
		
		
		fw.close();
		resetCounters();
		return neigborhood;
	}

	
	public static Set<Node> bfs(Node target, Node origin, RelationshipType network) throws IOException {
		
		Set<Node> neigborhood = new HashSet<Node>();
		
		// target is private and has no neighbors
		if(origin != null && target != null && origin.equals(target)){
			return neigborhood;
		}
		
		Set<Node> searched = new HashSet<Node>();
		LinkedList<Node> queue = new LinkedList<Node>();
		
		queue.add(origin);
		
		LinkedList<Node> actualFriends = new LinkedList<Node>();
		actualFriends.addAll(reveal(target, type));
		String location = actualFriends.contains(origin) ? "Start Inside Target Network" : "Start Outside Target Network";
		FileWriter fw = new FileWriter(new File("BFS " + database + " - " + location + ".csv"), true);
		
		// fw.write("BFS API Ratio,BFS Friend Ratio\n");

		while(!queue.isEmpty()){
			Node node = queue.remove();
			
			if(searched.contains(node)){
				continue;
			}
			
			Set<Node> neighbors = explode(node, network);
			
			if(neighbors.contains(target)){
				neigborhood.add(node);
				fw.write(((double)API_CALLS)/numNodes + "," + ((double)neigborhood.size())/actualFriends.size() + "\n");
			}
			
			queue.addAll(neighbors);
			searched.add(node);
		}
		
		fw.close();
		resetCounters();
		return neigborhood;
	}
	
	public static Set<Node> dfs(Node target, Node origin, RelationshipType network) throws IOException {
		
		Set<Node> neigborhood = new HashSet<Node>();
		
		// target is private and has no neighbors
		if(origin != null && target != null && origin.equals(target)){
			return neigborhood;
		}
		
		Set<Node> searched = new HashSet<Node>();
		LinkedList<Node> stack = new LinkedList<Node>();
		
		stack.add(origin);
		
		LinkedList<Node> actualFriends = new LinkedList<Node>();
		actualFriends.addAll(reveal(target, type));
		String location = actualFriends.contains(origin) ? "Start Inside Target Network" : "Start Outside Target Network";
		FileWriter fw = new FileWriter(new File("DFS " + database + " - " + location + ".csv"), true);
		// fw.write("DFS API Ratio,BFS Friend Ratio\n");

		while(!stack.isEmpty()){
			Node node = stack.removeLast();
			
			if(searched.contains(node)){
				continue;
			}
			
			Set<Node> neighbors = explode(node, network);
			
			if(neighbors.contains(target)){
				neigborhood.add(node);
				fw.write(((double)API_CALLS)/numNodes + "," + ((double)neigborhood.size())/actualFriends.size() + "\n");
			}
			
			stack.addAll(neighbors);
			searched.add(node);
		}
		
		fw.close();
		resetCounters();
		return neigborhood;
	}
	
	private static Set<Node> explode(Node node, RelationshipType network){
		API_CALLS++;
		Set<Node> nodes = new HashSet<Node>();
		if(!node.hasProperty("private")){
			for(Relationship relationship : node.getRelationships(network)){
				if(!relationship.getStartNode().equals(node)){
					nodes.add(relationship.getStartNode());
				}
				if(!relationship.getEndNode().equals(node)){
					nodes.add(relationship.getEndNode());
				}
			}
		} else {
			PRIVATE_NODE_ENCOUNTERS++;
		}
		return nodes;
	}

	private static Set<Node> reveal(Node node, RelationshipType network){
		Set<Node> nodes = new HashSet<Node>();
		for(Relationship relationship : node.getRelationships(network)){
			if(!relationship.getStartNode().equals(node)){
				nodes.add(relationship.getStartNode());
			}
			if(!relationship.getEndNode().equals(node)){
				nodes.add(relationship.getEndNode());
			}
		}
		return nodes;
	}
	
	// finds the shortest path that starts at a start node and ends at an end node
	private static Set<Node> shortestPath(Node start, Node end){
		// path finder
		RelationshipExpander expander = Traversal.expanderForTypes(type, Direction.BOTH);
	    CostEvaluator<Double> costEvaluator = CommonEvaluators.doubleCostEvaluator("cost");
	    PathFinder<WeightedPath> dijkstraPathFinder = GraphAlgoFactory.dijkstra(expander, costEvaluator);
	    
	    // print shortest path
	    Set<Node> pathSet = new HashSet<Node>();
	    WeightedPath path = dijkstraPathFinder.findSinglePath(start, end);
        for (Node node : path.nodes())
        {
            pathSet.add(node);
        }
        return pathSet;
	}
	
	/*
	// calculate the average shortest path using a 10% sampling
	double averageShortestPath = 0.0;
	int sampleSize = (int)((1.0/10.0)*network.size());
	for(int i=0; i<sampleSize; i++){
		Node target = network.get(rnd.nextInt(network.size()));
		LinkedList<Node> actualFriends = new LinkedList<Node>();
		actualFriends.addAll(reveal(target, type));
		Node origin = network.get(rnd.nextInt(network.size()));
		
		while(origin.equals(target)){
			target = network.get(rnd.nextInt(network.size()));
			// target changed, update actual friends
			actualFriends = new LinkedList<Node>();
			actualFriends.addAll(reveal(target, type));
			origin = actualFriends.get(rnd.nextInt(actualFriends.size()));
		}
		
		int numNodesInShortestPath = shortestPath(origin, target).size(); 
		System.out.println("Num nodes in shortest path (" + i + "): " + numNodesInShortestPath);
		
		// minus one because in a path there is no edge between the start and the end
		int numEdgesInShortestPath = numNodesInShortestPath - 1;
		
		averageShortestPath += numEdgesInShortestPath;
	}
	averageShortestPath = averageShortestPath / sampleSize;
	
	System.out.println("Optimal API Calls");
	for(int i=1; i<=130; i++){
		System.out.println(averageShortestPath*i);
	}
	*/
	
}
