package graph.generator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Random;

import org.fluttercode.datafactory.impl.DataFactory;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import utilities.math.Jaccard;

public class GraphGenerator {

	public final static String WATTS_STROGATZ_NETWORK = "watts_strogatz_id";
	public final static String IDENTIFIER = "identifier";
	
	public static enum WattsStrogatzRelationshipTypes implements RelationshipType
    {
    	WATTS_STROGATZ_ACCOUNT,
    	WATTS_STROGATZ_CONNECTION
    }
	
	public static final String COST = "cost";
	
	/*
	static enum TestNetworkRelationshipTypes implements RelationshipType
    {
    	ACCOUNT_A,
    	CONNECTION_A,
    	ACCOUNT_B,
    	CONNECTION_B
    }
	public static void main(String[] args) {
		
		int numNodes = 300;
		
		// create and store a watts strogatz graph database with random identities
		createWattsStrogatzGraph("watts-strogatz-graph", numNodes, 4, .3);
		
		// for a fair test lets say there is a 33% overlap (the Jaccard coefficient) of 100 between groups A and B that each 
		// have 150,000 ids (the first and second halves)
		
		createSubgraphs("watts-strogatz-graph", numNodes, 
				        "twitter", TestNetworkRelationshipTypes.ACCOUNT_A, TestNetworkRelationshipTypes.CONNECTION_A,
				        "facebook", TestNetworkRelationshipTypes.ACCOUNT_B, TestNetworkRelationshipTypes.CONNECTION_B);
		
		// kill off any rouge processes
		System.exit(0);	
	}
	*/
	
	private static Random rnd = new Random();
	public static String randomIdentifier(){
		return new BigInteger(130, rnd).toString(32);
	}
	
	/**
	 * Creates two partially overlapping random networks
	 * @param database The database to save the resulting graphs to
	 * @param totalNodes The total number of nodes equivalent to the union of the two graphs
	 * @param subgraphAPrefix The network prefix for the first subgraph
	 * @param subgraphBPrefix The network prefix for the second subgraph
	 * @param averageConnections The average number connections each node contains (i.e. the node degree).  For Facebook this is about 130 degrees.
	 * @param graphRandomness The probability (value between 0 and 1) that each connection between nodes is randomly chosen
	 */
	public static void createRandomNetworks(String database, int totalNodes, double jaccard,
			                                String subgraphAPrefix, RelationshipType subgraphANodeType, RelationshipType subgraphARelationshipType, 
			                                String subgraphBPrefix, RelationshipType subgraphBNodeType, RelationshipType subgraphBRelationshipType,
			                                int averageConnections, double graphRandomness){
		// create and store a watts strogatz graph database with random identities
		createWattsStrogatzGraph(database, totalNodes, averageConnections, graphRandomness);
		
		// split the master graph with a 1/3 overlap (a 1/3 jaccard coefficient)
		createSubgraphs(database, totalNodes, jaccard, subgraphAPrefix, subgraphANodeType, subgraphARelationshipType, subgraphBPrefix, subgraphBNodeType, subgraphBRelationshipType);
	}
	
	
	/**
	 * Splits a Watts Strograts graph into two subgraphs that each have a 1/3 Jaccard index to the opposing graph
	 * @param database The database to save the resulting graph to
	 * @param numNodes The number of nodes to create
	 * @param subgraphAPrefix The network prefix for the first subgraph
	 * @param subgraphBPrefix The network prefix for the second subgraph
	 */
	public static void createSubgraphs(String database, int numNodes, double jaccard,
			                           String subgraphAPrefix, RelationshipType subgraphANodeType, RelationshipType subgraphARelationshipType,
			                           String subgraphBPrefix, RelationshipType subgraphBNodeType, RelationshipType subgraphBRelationshipType){
		
		System.out.println("Splitting watts-strogatz graph into " + subgraphAPrefix + " and " + subgraphBPrefix + " networks with " + jaccard + " Jaccard Index...");
		
		AbstractGraphDatabase graphDB = new EmbeddedGraphDatabase(database);
	    Index<Node> nodeIndex = graphDB.index().forNodes("nodes");
	    
		// we should randomly partition the graph into two subsets such that the two subsets have a 1/3 overlap
		// step 1 randomly select and remove 100 nodes for each subset
		
		// create an index array to denote the nodes we want to partition
		ArrayList<Integer> index = range(0,numNodes);
		
		// partition the indexes
		ArrayList<Integer> groupA = new ArrayList<Integer>();
		ArrayList<Integer> groupB = new ArrayList<Integer>();
		
		double nonOverlapping = ((1.0 - jaccard) / 2);
		
		int firstThird = (int)((double) numNodes * nonOverlapping);
		int secondThird = (int)((double) numNodes * (nonOverlapping * 2.0));
		Random rnd = new Random();
		
		// partition group a
		for(int i=0; i<firstThird; i++){
			groupA.add(index.remove(rnd.nextInt(index.size())));
		}
		
		// partition group b
		for(int i=firstThird; i<secondThird; i++){
			groupB.add(index.remove(rnd.nextInt(index.size())));
		}
		
		// step 2 randomly select and add the remaining nodes to each group as overlap
		int renamingNodes = index.size();
		for(int i=0; i<renamingNodes; i++){
			int ptr = index.remove(rnd.nextInt(index.size()));
			groupA.add(ptr);
			groupB.add(ptr);
		}
		
		System.out.println("Confirming Jaccard Index = " + Jaccard.jaccardIndex(groupA, groupB));
		
		// step 3, for each index in each group add a corresponding node as a network node in the final graph
		// then copy over all nodes and connections that link to nodes within the same group
	    createSubgraphNodes(graphDB, nodeIndex, subgraphAPrefix, subgraphANodeType, groupA);
	    createSubgraphNodes(graphDB, nodeIndex, subgraphBPrefix, subgraphBNodeType, groupB);

	    // step 4
	    // all nodes are added, now for each node add the connections that 
	    // exist between nodes in the watts strogatz graph and the respective subgraph
	    createSubgraphConnections(graphDB, nodeIndex, subgraphAPrefix, subgraphARelationshipType, groupA);
	    createSubgraphConnections(graphDB, nodeIndex, subgraphBPrefix, subgraphBRelationshipType, groupB);
	    
	    // shut it down, we're done here
	    System.out.println("Finished generating subgraphs.");
    	if(graphDB != null){
    		graphDB.shutdown();
    	}
	}

	private static void createSubgraphNodes(AbstractGraphDatabase graphDB, Index<Node> nodeIndex, String subgraphPrefix, RelationshipType type, ArrayList<Integer> subgraph) {
		
		System.out.println("Creating " + subgraphPrefix + " nodes...");
		
		for(Integer ptr : subgraph){
	    	Node node = nodeIndex.get(WATTS_STROGATZ_NETWORK, "" + ptr).getSingle();
	    	
	    	// add the node to the graph
    		Transaction tx = graphDB.beginTx();
            try
            {
            	Node newNode = graphDB.createNode();
            	
            	newNode.setProperty(subgraphPrefix + "_id", "" + ptr);
                nodeIndex.add(newNode, subgraphPrefix + "_id", "" + ptr);
                
                newNode.setProperty(IDENTIFIER, node.getProperty(IDENTIFIER));
            	
                // TODO:  Don't add everything... make it a little harder...
                
                for(String key : node.getPropertyKeys()){
    	    		// valid keys: first_name, last_name, email_x, username_x
    	    		String value = (String)node.getProperty(key);
    	    		String subgraphProperty = "";
    	    		String subgraphValue = "";
    	    		if(key.equals("first_name")){
    	    			subgraphProperty = subgraphPrefix + "_first_name";
    	    			subgraphValue = value;
    	    		} else if(key.equals("last_name")){
    	    			subgraphProperty = subgraphPrefix + "_last_name";
    	    			subgraphValue = value;
    	    		} else if(key.contains("username_")){
    	    			subgraphProperty = subgraphPrefix + "_username";
    	    			subgraphValue = value;
    	    		} else if(key.contains("email_")){
    	    			subgraphProperty = subgraphPrefix + "_email";
    	    			subgraphValue = value;
    	    		}
    	    		
    	    		newNode.setProperty(subgraphProperty, subgraphValue);
    	    	}

                // add connection to root
                Node root = graphDB.getNodeById(0);
                root.createRelationshipTo(newNode, type);
                
                tx.success();
            }
            finally
            {
                tx.finish();
            }
            
            // System.out.println("Created '" + subgraphPrefix + "' " + ptr);
	    }
	}

	private static void createSubgraphConnections(AbstractGraphDatabase graphDB, Index<Node> nodeIndex, String subgraphPrefix, RelationshipType type, ArrayList<Integer> subgraph) {
		
		System.out.println("Creating " + subgraphPrefix + " connections...");
		
		for(Integer ptr : subgraph){
	    	Node ws_node = nodeIndex.get(WATTS_STROGATZ_NETWORK, "" + ptr).getSingle();

	    	for(Relationship relationship : ws_node.getRelationships(WattsStrogatzRelationshipTypes.WATTS_STROGATZ_CONNECTION)){

	    		int startPoint = Integer.parseInt((String)relationship.getStartNode().getProperty(WATTS_STROGATZ_NETWORK));
	    		int endPoint = Integer.parseInt((String)relationship.getEndNode().getProperty(WATTS_STROGATZ_NETWORK));
	    		
	    		// reorder connections, at this point we don't care about directed orders, so always consider the start point as the ptr value
	    		if(startPoint != ptr){
	    			endPoint = startPoint;
	    			startPoint = ptr;
	    		}
	    		
	    		if(subgraph.contains(endPoint)){
	    			// make connection to end point in subgraph
	    			Node nodeA = nodeIndex.get(subgraphPrefix + "_id", "" + startPoint).getSingle();
	    			Node nodeB = nodeIndex.get(subgraphPrefix + "_id", "" + endPoint).getSingle();
	    			
	    			if(!relationshipExists(type, nodeA, nodeB)){
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
	    			
	    			// System.out.println("Added '" + subgraphPrefix + "' connection: " + startPoint + " to " + endPoint);
	    		}
	    	}
	    	
	    }
	}
	
	
	/**
	 * Creates a Watts Strogatz graph with random ID values.  According to some research the sweet 
	 * spot between a regular and random graph is between .001 and .01
	 * @param database The database to save the resulting graph to
	 * @param numNodes The number of nodes to create
	 * @param degree The average number of neighbors each node should be connected to
	 * @param probability The probability for which to rewire edges
	 */
	public static void createWattsStrogatzGraph(String database, int numNodes, int degree, double probability){
		
		// make sure the degree is feasible
		
		if( 2 * degree > (numNodes - 1) / 2)
		{
			System.err.println("Degree: " + degree + " is infeasible.");
			degree = (numNodes - 1) / 2;
			System.out.println("Using degree: " + degree);
		}
		
		System.out.println("Creating watts-strogatz nodes...");
		
		AbstractGraphDatabase graphDB = new EmbeddedGraphDatabase(database);
	    //WrappingNeoServerBootstrapper webServer = new WrappingNeoServerBootstrapper(graphDB);
        //webServer.start();  // The server is now running,  until we stop it
	    Index<Node> nodeIndex = graphDB.index().forNodes("nodes");
	    
	    // step 0 - create N nodes
		// let's create nodes with realistic values
		for(int i=0; i<numNodes; i++){
			
			ID id = getRandomID(i);
			
			Transaction tx = graphDB.beginTx();
            try
            {
            	Node node = graphDB.createNode();
                node.setProperty(WATTS_STROGATZ_NETWORK, "" + i);
                nodeIndex.add(node, WATTS_STROGATZ_NETWORK, "" + i);
                
                // add connection to root
                Node root = graphDB.getNodeById(0);
                root.createRelationshipTo(node, WattsStrogatzRelationshipTypes.WATTS_STROGATZ_ACCOUNT);

                node.setProperty(IDENTIFIER, randomIdentifier());
                
                node.setProperty("first_name", "" + id.firstName);
                node.setProperty("last_name", "" + id.lastName);
                
                int counter = 1;
                for(String email : id.emails){
                	node.setProperty("email_" + counter++, "" + email);
    			}
                
                counter = 1;
    			for(String username : id.usernames){
    				node.setProperty("username_" + counter++, "" + username);
    			}
                
                tx.success();
            }
            finally
            {
                tx.finish();
            }
            
            // System.out.println("Created node " + i);
		}
	    		
		System.out.println("Forming lattice ring...");
		
		// create a lattice ring
		for (int n = 0; n < numNodes; n++) {
            for(int d = 0; d < degree/2; d++){
            	
            	int nodeAPtr = n;
            	int nodeBPtr = ((n + (degree - d)) % numNodes);

            	Node nodeA = nodeIndex.get(WATTS_STROGATZ_NETWORK, "" + nodeAPtr).getSingle();
    			Node nodeB = nodeIndex.get(WATTS_STROGATZ_NETWORK, "" + nodeBPtr).getSingle();
    			if(nodeAPtr != nodeBPtr && nodeA != null && nodeB != null){
    				 if(!relationshipExists(WattsStrogatzRelationshipTypes.WATTS_STROGATZ_CONNECTION, nodeA, nodeB)){
    					Transaction tx = graphDB.beginTx();
                        try
                        {
                        	Relationship rel = nodeA.createRelationshipTo(nodeB, WattsStrogatzRelationshipTypes.WATTS_STROGATZ_CONNECTION);
                        	rel.setProperty(COST, 1.0);
                            tx.success();
                        }
                        finally
                        {
                            tx.finish();
                        }
                	}
    			}
            }
        }
		
		System.out.println("Rewiring with probability " + probability + "...");
		
		// for every node, take every edge ni,nj with i<j and rewire with probability beta
		Random random = new Random();
		for (int n = 0; n < numNodes; n++) {
			Node node = nodeIndex.get(WATTS_STROGATZ_NETWORK, "" + n).getSingle();
			
			for(Relationship relationship : node.getRelationships(WattsStrogatzRelationshipTypes.WATTS_STROGATZ_CONNECTION)){
				
				int ni = Integer.parseInt((String)relationship.getStartNode().getProperty(WATTS_STROGATZ_NETWORK));
	    		int nj = Integer.parseInt((String)relationship.getEndNode().getProperty(WATTS_STROGATZ_NETWORK));
	    		
	    		// reorder connections, at this point we don't care about directed orders, 
	    		// so always consider the start point as the node value
	    		if(ni != n){
	    			nj = ni;
	    			nj = n;
	    		}
	    		
	    		if(ni < nj){
					double r = random.nextDouble();
					if (r <= probability) {
						while(true){
							int nk = random.nextInt(numNodes);
							// Rewiring is done by replacing (ni, nj) with (ni, nk) where k is chosen 
							// with uniform probability from all possible values that avoid loops (nk != ni) 
							// and link duplication
							if(nk != ni){
								Node nodeI = nodeIndex.get(WATTS_STROGATZ_NETWORK, "" + ni).getSingle();
								Node nodeK = nodeIndex.get(WATTS_STROGATZ_NETWORK, "" + nk).getSingle();
								if(!relationshipExists(WattsStrogatzRelationshipTypes.WATTS_STROGATZ_CONNECTION, nodeI, nodeK)){
									Transaction tx = graphDB.beginTx();
						            try
						            {
						            	relationship.delete();
						            	Relationship rel = nodeI.createRelationshipTo(nodeK, WattsStrogatzRelationshipTypes.WATTS_STROGATZ_CONNECTION);
						            	rel.setProperty(COST, 1.0);
						                tx.success();
						            }
						            finally
						            {
						                tx.finish();
						            }
						            break;
								}
							}
						}
					}
	    		}
			}
		}
		
    	if(graphDB != null){
    		//if(webServer != null){
    			//System.out.println("Webserver Shutdown");
    			//webServer.stop();
    		//}
    		graphDB.shutdown();
    	}
	}
	
	// helper method to create an array with a range of integer values from start to stop
	private static ArrayList<Integer> range(int start, int stop)
	{
	   ArrayList<Integer> result = new ArrayList<Integer>(stop-start);

	   for(int i=0;i<stop-start;i++)
	      result.add(start+i);

	   return result;
	}

	
	// returns the edge is there exists an edge between a and b
	private static Relationship findAdjacentEdge(Node a, Node b){
		for(Relationship relationship : a.getRelationships()){
			for(Node node : relationship.getNodes()){
				if(node.equals(b)){
					return relationship;
				}
			}
		}
		return null;
	}
	
	private static ID getRandomID(int i){
		// a little random goes a long way...
		Random rnd = new Random();
		DataFactory df = new DataFactory();
		String firstName = df.getFirstName().replace("'", "");
		String lastName = df.getLastName().replace("'", "");
		Collection<String> emails = generateEmailsForID(firstName, lastName, rnd.nextInt(2) + 1);
		Collection<String> usernames = generateUsernamesForID(firstName, lastName, rnd.nextInt(2) + 1);
		return new ID(("" + i), firstName, lastName, emails, usernames);
	}
	
	private static Collection<String> generateUsernamesForID(String firstName, String lastName, int num){
		Collection<String> usernames = new LinkedList<String>();
		usernames.add((firstName.charAt(0) + lastName).toLowerCase());
		for(int i=1; i<num; i++){
			usernames.add((firstName.charAt(0) + lastName + new Random().nextInt(99999)).toLowerCase());
		}
		return usernames;
	}

	private static Collection<String> generateEmailsForID(String firstName, String lastName, int num){
		Collection<String> emails = new LinkedList<String>();
		DataFactory df = new DataFactory();
		for(int i=0; i<num; i++){
			emails.add(df.getEmailAddress().toLowerCase());
		}
		return emails;
	}

	private static boolean relationshipExists(RelationshipType type, Node nodeA, Node nodeB){
    	boolean relationshipExists = false;
    	if(nodeA != null && nodeB != null){
    		Iterable<Relationship> relationships = nodeA.getRelationships(type);
    		for(Relationship relationship : relationships){
    			if(relationship.getStartNode().equals(nodeA) && relationship.getEndNode().equals(nodeB)){
    				relationshipExists = true;
    			}
    		}
    	}
    	return relationshipExists;
    }
	
}
