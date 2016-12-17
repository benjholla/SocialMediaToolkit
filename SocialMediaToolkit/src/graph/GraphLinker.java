package graph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.Transaction;
import org.neo4j.graphdb.index.Index;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.Config;
import org.neo4j.kernel.EmbeddedGraphDatabase;

public class GraphLinker {

	private static String DB_PATH = "graph-db";
    protected static AbstractGraphDatabase graphDB;
    protected static Index<Node> nodeIndex;
    
    public GraphLinker(String database)
    {
    	DB_PATH = database;
    	if(graphDB == null){
    		graphDB = new EmbeddedGraphDatabase(DB_PATH, MapUtil.stringMap(Config.ENABLE_REMOTE_SHELL, "true"));
    	}
    	nodeIndex = graphDB.index().forNodes("nodes");
    }
    
    public GraphDatabaseService getGraphDatabase(){
    	return graphDB;
    }
    
    public void registerShutdownHook(){
    	if(graphDB != null){
    		registerShutdownHook(graphDB);
    	}
    }
    
    // manual shutdown
    public void shutdown(){
    	System.out.println("Graph Database Shutdown");
    	if(graphDB != null){
    		graphDB.shutdown();
    		nodeIndex = null;
    		graphDB = null;
    	}
    }

    // shutdown hook, automatically closes database on VM exit
    private void registerShutdownHook(final GraphDatabaseService graphDb)
    {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed)
        Runtime.getRuntime().addShutdownHook( new Thread()
        {
            @Override
            public void run()
            {
            	shutdown();
            }
        } );
    }
    
    public boolean relationshipExists(final String networkA, final String id_A, final String networkB, final String id_B, final RelationshipType type){
    	
    	Node nodeA = nodeIndex.get(networkA + "_id", id_A).getSingle();
    	Node nodeB = nodeIndex.get(networkB + "_id", id_B).getSingle();
    	
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
    
    public void addRelationship(final String networkA, final String id_A, final String networkB, final String id_B, final RelationshipType type){
    	Node nodeA = nodeIndex.get(networkA + "_id", id_A).getSingle();
    	Node nodeB = nodeIndex.get(networkB + "_id", id_B).getSingle();
    	if(nodeA != null && nodeB != null){
    		if(!relationshipExists(networkA, id_A, networkB, id_B, type)){
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
