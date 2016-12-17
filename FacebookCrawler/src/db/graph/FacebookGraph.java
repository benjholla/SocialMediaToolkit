package db.graph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.server.WrappingNeoServerBootstrapper;

public class FacebookGraph {

	private static final String DB_PATH = "facebook-graph-db";
	
    private static final String ID_KEY = "id";
    private static final String NAME_KEY = "name";
    
    public static enum KeyTypes
    {
        ID,
        NAME
    }
    
    public static String getKeyString(KeyTypes key){
    	if(key == KeyTypes.ID){
    		return ID_KEY;
    	} else {
    		return NAME_KEY;
    	}
    }

    public static enum RelationshipTypes implements RelationshipType
    {
    	FACEBOOK_ACCOUNT,
    	FRIEND
    }
    
    private static AbstractGraphDatabase graphDB = null;
    private static WrappingNeoServerBootstrapper webServer = null;
    private static Index<Node> nodeIndex;
    
    // singleton instance
    private static final FacebookGraph graph = null;
    
    // singleton instance handler
    public static FacebookGraph getInstance(boolean launchWebServer){
    	if(graph == null){
    		return new FacebookGraph(launchWebServer);
    	} else {
    		return graph;
    	}
    }
    
    public void launchWebServer(){
    	if(!isWebServerLaunched()){
    		webServer = new WrappingNeoServerBootstrapper(graphDB);
            webServer.start();  // The server is now running,  until we stop it
    	}
    }
    
    public boolean isWebServerLaunched(){
    	return webServer != null;
    }
    
    // single constructor, creates graph and registers shutdown hook
    private FacebookGraph(boolean launchWebServer)
    {
    	System.out.println("Launching Facebook Graph Database");
    	graphDB = new EmbeddedGraphDatabase(DB_PATH);
    	nodeIndex = graphDB.index().forNodes("nodes");
        
    	// set the root as a facebook platform reference
    	Node root = graphDB.getNodeById(0);
    	if(!root.hasProperty("platform")){
    		Transaction tx = graphDB.beginTx();
            try
            {
            	Node node = graphDB.getNodeById(0);
            	node.setProperty("platform", "FACEBOOK");
                tx.success();
            }
            finally
            {
                tx.finish();
            }
    	}

        if(launchWebServer){
        	webServer = new WrappingNeoServerBootstrapper(graphDB);
            webServer.start();  // The server is now running,  until we stop it
        }
    }
    
    public void registerShutdownHook(){
    	if(graphDB != null){
    		registerShutdownHook(graphDB);
    	}
    }
    
    public boolean nodeExists(final long id){
    	Node node = nodeIndex.get(ID_KEY, ("" + id)).getSingle();
    	return node != null;
    }
    
    public void addNode(final long id)
    {
    	if(!nodeExists(id)){
    		Transaction tx = graphDB.beginTx();
            try
            {
            	Node node = graphDB.createNode();
                node.setProperty(ID_KEY, ("" + id));
                nodeIndex.add(node, ID_KEY, ("" + id));
                
                // add facebook account relationship
                Node root = graphDB.getNodeById(0);
                node.createRelationshipTo(root, RelationshipTypes.FACEBOOK_ACCOUNT);
                tx.success();
            }
            finally
            {
                tx.finish();
            }
    	}
    }
    
    public void addProperty(final long id, final KeyTypes key, final String value){
    	Node node = nodeIndex.get(ID_KEY, ("" + id)).getSingle();
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
    
    public boolean relationshipExists(final long id_A, final long id_B, final RelationshipTypes type){
    	
    	Node nodeA = nodeIndex.get(ID_KEY, ("" + id_A)).getSingle();
    	Node nodeB = nodeIndex.get(ID_KEY, ("" + id_B)).getSingle();
    	
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
    
    public void addRelationship(final long id_A, final long id_B, final RelationshipTypes type){
    	Node nodeA = nodeIndex.get(ID_KEY, ("" + id_A)).getSingle();
    	Node nodeB = nodeIndex.get(ID_KEY, ("" + id_B)).getSingle();
    	if(nodeA != null && nodeB != null){
    		if(!relationshipExists(id_A, id_B, type)){
    			Transaction tx = graphDB.beginTx();
                try
                {
                	nodeA.createRelationshipTo(nodeB, RelationshipTypes.FRIEND);
                    tx.success();
                }
                finally
                {
                    tx.finish();
                }
    		}
    	}
    }
    
    // manual shutdown
    public void shutdown(){
    	System.out.println("Facebook Graph DB Shutdown");
    	if(graphDB != null){
    		if(webServer != null){
    			System.out.println("Webserver Shutdown");
    			webServer.stop();
    		}
    		graphDB.shutdown();
    	}
    }

    // shutdown hook, automatically closes database on VM exit
    private void registerShutdownHook( final GraphDatabaseService graphDb )
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed)
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
            	if(webServer != null){
        			System.out.println("Webserver Shutdown");
        			webServer.stop();
        		}
                graphDB.shutdown();
                System.out.println("Facebook Graph DB Shutdown");
            }
        } );
    }
	
}
