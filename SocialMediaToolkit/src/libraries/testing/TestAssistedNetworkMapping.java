package libraries.testing;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import libraries.testing.GenerateAssistedNetworkMappingFramework.TestNetworkRelationshipTypes;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import utilities.datastructures.AccumulatingPriorityQueue;

public class TestAssistedNetworkMapping {

    static LinkedList<Node> networkA = new LinkedList<Node>();
    static LinkedList<Node> networkB = new LinkedList<Node>();
    
    private static int API_CALLS = 0;
	private static int PRIVATE_NODE_ENCOUNTERS = 0;
	private static int numNodes;
	
	public static void main(String[] args) throws IOException {
		
		String database = "test-reference-network";
		
		AbstractGraphDatabase graphDB = new EmbeddedGraphDatabase(database);
		Node root = graphDB.getNodeById(0);
	    
	    
	    for(Relationship relationship : root.getRelationships(TestNetworkRelationshipTypes.ACCOUNT_A)){
	    	// connection is from node to root, so the start node is actually the node of interest
	    	Node node = relationship.getEndNode();
	    	networkA.add(node);
	    }
	    
	    for(Relationship relationship : root.getRelationships(TestNetworkRelationshipTypes.ACCOUNT_B)){
	    	// connection is from node to root, so the start node is actually the node of interest
	    	Node node = relationship.getEndNode();
	    	networkB.add(node);
	    }
	    
	    // "randomly" sample 500 node from facebook graph
	    
	    Random rnd = new Random();
	    
	    /*
	    LinkedList<Node> samples = new LinkedList<Node>();
	    while(samples.size() < 500){
	    	samples.add(networkA.remove(rnd.nextInt(networkA.size())));
	    }
	    networkA.addAll(samples); // add them back for reference later
	    
	    LinkedList<Node> privateNodes = new LinkedList<Node>();
	    for(Node node : samples){
	    	if(node.hasProperty("private")){
	    		privateNodes.add(node);
	    	}
	    }
	    
	    System.out.println("Private: " + privateNodes.size() + "/" + samples.size());
	    */
	    
	    Node origin = null;
    	Node target = null;
    	
    	// origin outside target network
    	LinkedList<Node> actualFriends = new LinkedList<Node>();
		do {
			actualFriends.clear();
			target = networkA.get(rnd.nextInt(networkA.size()));
			origin = networkA.get(rnd.nextInt(networkA.size()));
			actualFriends.addAll(reveal(target, TestNetworkRelationshipTypes.CONNECTION_A));
		} while(origin == null || target == null || origin.equals(target) || actualFriends.contains(origin));
		
		numNodes = networkA.size();
		
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
	    
        priorityMap(target, origin, TestNetworkRelationshipTypes.CONNECTION_A, TestNetworkRelationshipTypes.CONNECTION_B);
		System.out.println("Finished Priority Mode 5.");
		
		bfs(target, origin, TestNetworkRelationshipTypes.CONNECTION_A);
		System.out.println("Finished BFS.");
		
		dfs(target, origin, TestNetworkRelationshipTypes.CONNECTION_A);
		System.out.println("Finished DFS.");
		
		// unprivatized target
		Transaction tx2 = graphDB.beginTx();
        try
        {
        	target.removeProperty("private");
            tx2.success();
        }
        finally
        {
            tx2.finish();
        }
	    
	    graphDB.shutdown();
	    
	    System.exit(0);
		
	}
	
	public static Set<Node> bfs(Node target, Node origin, RelationshipType networkA) throws IOException {
		
		Set<Node> neigborhood = new HashSet<Node>();
		
		// target is private and has no neighbors
		if(origin != null && target != null && origin.equals(target)){
			return neigborhood;
		}
		
		Set<Node> searched = new HashSet<Node>();
		LinkedList<Node> queue = new LinkedList<Node>();
		
		queue.add(origin);
		
		LinkedList<Node> actualFriends = new LinkedList<Node>();
		actualFriends.addAll(reveal(target, networkA));
		String location = actualFriends.contains(origin) ? "Start Inside Target Network" : "Start Outside Target Network";
		FileWriter fw = new FileWriter(new File("bfs.csv"), true);
		
		// fw.write("BFS API Ratio,BFS Friend Ratio\n");

		while(!queue.isEmpty()){
			Node node = queue.remove();
			
			if(searched.contains(node)){
				continue;
			}
			
			Set<Node> neighbors = explode(node, networkA);
			
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
	
	public static Set<Node> dfs(Node target, Node origin, RelationshipType networkA) throws IOException {
		
		Set<Node> neigborhood = new HashSet<Node>();
		
		// target is private and has no neighbors
		if(origin != null && target != null && origin.equals(target)){
			return neigborhood;
		}
		
		Set<Node> searched = new HashSet<Node>();
		LinkedList<Node> stack = new LinkedList<Node>();
		
		stack.add(origin);
		
		LinkedList<Node> actualFriends = new LinkedList<Node>();
		actualFriends.addAll(reveal(target, networkA));
		String location = actualFriends.contains(origin) ? "Start Inside Target Network" : "Start Outside Target Network";
		FileWriter fw = new FileWriter(new File("dfs.csv"), true);
		// fw.write("DFS API Ratio,BFS Friend Ratio\n");

		while(!stack.isEmpty()){
			Node node = stack.removeLast();
			
			if(searched.contains(node)){
				continue;
			}
			
			Set<Node> neighbors = explode(node, networkA);
			
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
	
	public static Set<Node> priorityMap(Node target, Node origin, RelationshipType networkA, RelationshipType networkB) throws IOException {
		
		int mode = 5;
		Set<Node> neigborhood = new HashSet<Node>();
		
		// target is private and has no neighbors
		if(origin != null && target != null && origin.equals(target)){
			return neigborhood;
		}
		
		Set<Node> searched = new HashSet<Node>();
		
		AccumulatingPriorityQueue<Node> pq = new AccumulatingPriorityQueue<Node>(mode);
		
		pq.enqueue(origin, 0);
		
		LinkedList<Node> actualFriends = new LinkedList<Node>();
		actualFriends.addAll(reveal(target, networkA));
		FileWriter fw = new FileWriter(new File("hunter-seeker.csv"), true);
		
		while(!pq.isEmpty()){
			Node node = pq.dequeue();
			
			if(node == null || searched.contains(node)){
				continue;
			}
			
			Set<Node> neighbors = explode(node, networkA);

			if(neighbors.contains(target)){
				neigborhood.add(node);
				pq.enqueue(neighbors, 1); // add all, increments weight by 1 if already exists
				fw.write(((double)API_CALLS)/numNodes + "," + ((double)neigborhood.size())/actualFriends.size() + "," + pq.getMode() + "\n");
			} else {
				pq.enqueue(neighbors, 0);
			}
			
			
			/*
			Collection<Node> refneighbors = expandNode(node, networkA, networkB);
			
			if(neighbors2.contains(target)){
				neighbors.add(node);
				
				fw.write(((double)API_CALLS)/numNodes + "," + ((double)neigborhood.size())/actualFriends.size() + "," + pq.getMode() + "\n");
			} else {
				
			}
			*/
			
			searched.add(node);
		}
		
		fw.close();
		resetCounters();
		return neigborhood;
	}
	
	public static void resetCounters(){
		// reset
		API_CALLS = 0;
		PRIVATE_NODE_ENCOUNTERS = 0;
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
	
	/*
	private static Collection<Node> expandNode(Node node, RelationshipType networkA, RelationshipType networkB){
		if(node.hasRelationship(TestNetworkRelationshipTypes.INTERNETWORK_LINK)){
			Node opposing = getOpposingNode(node);
			Collection<Node> opposingFriends = getFriends(opposing, TestNetworkRelationshipTypes.CONNECTION_B);
			return getOpposingNodes(opposingFriends);
		}
		return new HashSet<Node>();
	}*/
	
	///////////////////////////////////////////////////
	
	/*
	private static Collection<Node> expandNode(Node node, Node target, RelationshipType networkA, RelationshipType networkB){
		Set<Node> neighborhood = new HashSet<Node>();
		if(node.hasRelationship(TestNetworkRelationshipTypes.INTERNETWORK_LINK)){
			Node opposing = getOpposingNode(node);
			Collection<Node> opposingFriends = getFriends(opposing, TestNetworkRelationshipTypes.CONNECTION_B);
			
			for(Node candidateFriend : getOpposingNodes(opposingFriends)){
				for(Relationship relationship : candidateFriend.getRelationships(TestNetworkRelationshipTypes.CONNECTION_A)){
					if(relationship.getEndNode().equals(target)){
						neighborhood.add(relationship.getStartNode());
					}
					if(relationship.getStartNode().equals(target)){
						neighborhood.add(relationship.getEndNode());
					}
				}
			}
		}
		return neighborhood;
	}
	*/
	
	private static Collection<Node> getOpposingNodes(Collection<Node> nodes){
		LinkedList<Node> result = new LinkedList<Node>();
		for(Node node : nodes){
			Node opposing = getOpposingNode(node);
			if(opposing != null){
				result.add(opposing);
			}
		}
		return result;
	}
	
	private static Node getOpposingNode(Node node){
		Node opposing = null;
		for(Relationship relationship : node.getRelationships(TestNetworkRelationshipTypes.INTERNETWORK_LINK)){
			opposing = relationship.getEndNode();
			if(opposing.equals(node)){
				opposing = relationship.getStartNode();
			}
		}
		return opposing;
	}
	
	private static Collection<Node> getFriends(Node node, RelationshipType network){
		LinkedList<Node> nodes = new LinkedList<Node>();
		if(!node.hasProperty("private")){
			for(Relationship relationship : node.getRelationships(network)){
				if(!relationship.getStartNode().equals(node)){
					nodes.add(relationship.getStartNode());
				}
				if(!relationship.getEndNode().equals(node)){
					nodes.add(relationship.getEndNode());
				}
			}
		}
		return nodes;
	}

}
