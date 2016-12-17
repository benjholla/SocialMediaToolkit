package graph;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

public class ProprietaryGraph extends Graph {
    
	private String NETWORK = "";
	private String NETWORK_ID_KEY = "";
	private RelationshipType NETWORK_ACCOUNT;
	
    // single constructor, creates graph and registers shutdown hook
    public ProprietaryGraph(final String DATABASE, final String NETWORK, final RelationshipType NETWORK_ACCOUNT)
    {
    	// get an instance of the graph
    	super(DATABASE);
    	
    	if(NETWORK == null || NETWORK.equals("")){
    		throw new IllegalArgumentException("Network must be defined.");
    	}
    	
    	this.NETWORK = NETWORK;
    	this.NETWORK_ID_KEY = NETWORK + "_id";
    	this.NETWORK_ACCOUNT = NETWORK_ACCOUNT;
    	
    	// try to get a reference to the platform node
    	Node platform_node = nodeIndex.get(Graph.NETWORK, NETWORK).getSingle();
    	
    	// if platform node does not exist, add it
    	if(platform_node == null){
    		Transaction tx = graphDB.beginTx();
            try
            {
            	// create the platform node
            	Node node = graphDB.createNode();
            	node.setProperty(Graph.NETWORK, NETWORK);
                
                // add connection to root
                Node root = graphDB.getNodeById(0);
                root.createRelationshipTo(node, Graph.PlatformReferenceRelationship.NETWORK_REFERENCE);
  
                // update the node index with the platform
                nodeIndex.add(node,Graph.NETWORK, NETWORK);
                
                tx.success();
            }
            finally
            {
                tx.finish();
            }
    	}
    }
    
    public boolean nodeExists(final String id){
    	Node node = nodeIndex.get(NETWORK_ID_KEY, id).getSingle();
    	return node != null;
    }
    
    public Node getNode(final String id){
    	return nodeIndex.get(NETWORK_ID_KEY, id).getSingle();
    }
    
    public void addNode(final String id)
    {
    	if(!nodeExists(id)){
    		Transaction tx = graphDB.beginTx();
            try
            {
            	Node node = graphDB.createNode();
                node.setProperty(NETWORK_ID_KEY, id);
                nodeIndex.add(node, NETWORK_ID_KEY, id);
                
                // add account relationship
                Node platform_node = nodeIndex.get(Graph.NETWORK, NETWORK).getSingle();
                
                platform_node.createRelationshipTo(node, NETWORK_ACCOUNT);
                tx.success();
            }
            finally
            {
                tx.finish();
            }
    	}
    }
    
    public void addProperty(final String id, final String property, final String value){
    	Node node = nodeIndex.get(NETWORK_ID_KEY, id).getSingle();
    	if(node != null){
    		Transaction tx = graphDB.beginTx();
            try
            {
                node.setProperty(property, value);
                tx.success();
            }
            finally
            {
                tx.finish();
            }
    	}
    }
    
    public boolean relationshipExists(final String id_A, final String id_B, final RelationshipType type){
    	
    	Node nodeA = nodeIndex.get(NETWORK_ID_KEY, id_A).getSingle();
    	Node nodeB = nodeIndex.get(NETWORK_ID_KEY, id_B).getSingle();
    	
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
    
    public void addRelationship(final String id_A, final String id_B, final RelationshipType type){
    	Node nodeA = nodeIndex.get(NETWORK_ID_KEY, id_A).getSingle();
    	Node nodeB = nodeIndex.get(NETWORK_ID_KEY, id_B).getSingle();
    	if(nodeA != null && nodeB != null){
    		if(!relationshipExists(id_A, id_B, type)){
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
    	}
    }
	
}
