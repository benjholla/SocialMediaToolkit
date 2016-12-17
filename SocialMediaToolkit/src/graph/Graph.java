package graph;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;
import org.neo4j.helpers.collection.MapUtil;
import org.neo4j.kernel.AbstractGraphDatabase;
import org.neo4j.kernel.Config;
import org.neo4j.kernel.EmbeddedGraphDatabase;
import org.neo4j.server.WrappingNeoServerBootstrapper;

abstract public class Graph {

	public static final String NETWORK = "network";
	public static final String NETWORK_REFERENCE = "network_reference";
	
    public static enum PlatformReferenceRelationship implements RelationshipType
    {
    	NETWORK_REFERENCE
    }
	
	private static String DB_PATH = "graph-db";
	
    protected static AbstractGraphDatabase graphDB;
    protected static Index<Node> nodeIndex;
    
    public Graph(String database)
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
	
}
