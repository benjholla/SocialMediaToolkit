package graph;

import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;

public class FacebookGraph extends Graph {
	
	// property names
	private static final String FACEBOOK_PLATFORM = "facebook";
    private static final String FACEBOOK_ID_KEY = "facebook_id";
    private static final String FACEBOOK_NAME_KEY = "facebook_name";
    
    public static enum KeyTypes
    {
    	FACEBOOK_ID,
    	FACEBOOK_NAME
    }
    
    public static String getKeyString(KeyTypes key){
    	if(key == KeyTypes.FACEBOOK_ID){
    		return FACEBOOK_ID_KEY;
    	} else {
    		return FACEBOOK_NAME_KEY;
    	}
    }

    public static enum RelationshipTypes implements RelationshipType
    {
    	FACEBOOK_ACCOUNT,
    	FACEBOOK_FRIEND
    }
    
    // single constructor, creates graph and registers shutdown hook
    public FacebookGraph(final String DATABASE)
    {
    	// get an instance of the graph
    	super(DATABASE);
    	
    	// try to get a reference to the platform node
    	Node platform_node = nodeIndex.get(Graph.NETWORK, FACEBOOK_PLATFORM).getSingle();
    	
    	// if platform node does not exist, add it
    	if(platform_node == null){
    		Transaction tx = graphDB.beginTx();
            try
            {
            	// create the platform node
            	Node node = graphDB.createNode();
            	node.setProperty(Graph.NETWORK, FACEBOOK_PLATFORM);
                
                // add connection to root
                Node root = graphDB.getNodeById(0);
                root.createRelationshipTo(node, Graph.PlatformReferenceRelationship.NETWORK_REFERENCE);
  
                // update the node index with the platform
                nodeIndex.add(node,Graph.NETWORK, FACEBOOK_PLATFORM);
                
                tx.success();
            }
            finally
            {
                tx.finish();
            }
    	}
    }
    
    public boolean accountExists(final long id){
    	Node node = nodeIndex.get(FACEBOOK_ID_KEY, ("" + id)).getSingle();
    	return node != null;
    }
    
    public void addAccount(final long id)
    {
    	if(!accountExists(id)){
    		Transaction tx = graphDB.beginTx();
            try
            {
            	Node node = graphDB.createNode();
                node.setProperty(FACEBOOK_ID_KEY, ("" + id));
                nodeIndex.add(node, FACEBOOK_ID_KEY, ("" + id));
                
                // add facebook account relationship
                Node platform_node = nodeIndex.get(Graph.NETWORK, FACEBOOK_PLATFORM).getSingle();
                platform_node.createRelationshipTo(node, RelationshipTypes.FACEBOOK_ACCOUNT);
                tx.success();
            }
            finally
            {
                tx.finish();
            }
    	}
    }
    
    public void addAccountProperty(final long id, final KeyTypes key, final String value){
    	Node node = nodeIndex.get(FACEBOOK_ID_KEY, ("" + id)).getSingle();
    	if(node != null){
    		Transaction tx = graphDB.beginTx();
            try
            {
                node.setProperty(getKeyString(key), value);
                tx.success();
            }
            finally
            {
                tx.finish();
            }
    	}
    }
    
    public void addAccountProperty(final long id, final String property, final String value){
    	Node node = nodeIndex.get(FACEBOOK_ID_KEY, ("" + id)).getSingle();
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
    
    public boolean friendshipExists(final long id_A, final long id_B){
    	
    	Node nodeA = nodeIndex.get(FACEBOOK_ID_KEY, ("" + id_A)).getSingle();
    	Node nodeB = nodeIndex.get(FACEBOOK_ID_KEY, ("" + id_B)).getSingle();
    	
    	boolean relationshipExists = false;
    	
    	if(nodeA != null && nodeB != null){
    		Iterable<Relationship> relationships = nodeA.getRelationships(RelationshipTypes.FACEBOOK_FRIEND);
    		for(Relationship relationship : relationships){
    			if(relationship.getStartNode().equals(nodeA) && relationship.getEndNode().equals(nodeB)){
    				relationshipExists = true;
    			}
    		}
    	}
    	
    	return relationshipExists;
    }
    
    public void addFriendship(final long id_A, final long id_B){
    	Node nodeA = nodeIndex.get(FACEBOOK_ID_KEY, ("" + id_A)).getSingle();
    	Node nodeB = nodeIndex.get(FACEBOOK_ID_KEY, ("" + id_B)).getSingle();
    	if(nodeA != null && nodeB != null){
    		if(!friendshipExists(id_A, id_B)){
    			Transaction tx = graphDB.beginTx();
                try
                {
                	nodeA.createRelationshipTo(nodeB, RelationshipTypes.FACEBOOK_FRIEND);
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
