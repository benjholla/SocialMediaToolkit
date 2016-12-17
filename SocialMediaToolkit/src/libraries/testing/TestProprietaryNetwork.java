package libraries.testing;
import graph.GraphLinker;
import graph.ProprietaryGraph;

import org.neo4j.graphdb.RelationshipType;

public class TestProprietaryNetwork {

	public static enum NetworkRelationshipTypes implements RelationshipType
    {
    	RED_NODE,
    	RED_RELATIONSHIP,
    	BLUE_NODE,
    	BLUE_RELATIONSHIP,
    	GREEN_NODE,
    	GREEN_RELATIONSHIP,
    	EQUIVALENT
    }
	
	public static void main(String[] args) {
		
		String database = "graph-db";
		
		ProprietaryGraph redGraph = new ProprietaryGraph(database, "red", NetworkRelationshipTypes.RED_NODE);
		redGraph.addNode("1R");
		redGraph.addNode("2R");
		redGraph.addNode("3R");
		redGraph.addNode("4R");
		redGraph.addRelationship("1R", "2R", NetworkRelationshipTypes.RED_RELATIONSHIP);
		redGraph.addRelationship("1R", "4R", NetworkRelationshipTypes.RED_RELATIONSHIP);
		redGraph.addRelationship("4R", "2R", NetworkRelationshipTypes.RED_RELATIONSHIP);
		redGraph.addRelationship("2R", "3R", NetworkRelationshipTypes.RED_RELATIONSHIP);
		redGraph.shutdown();
		
		ProprietaryGraph blueGraph = new ProprietaryGraph(database, "blue", NetworkRelationshipTypes.BLUE_NODE);
		blueGraph.addNode("1B");
		blueGraph.addNode("2B");
		blueGraph.addNode("3B");
		blueGraph.addNode("4B");
		blueGraph.addNode("5B");
		blueGraph.addRelationship("1B", "2B", NetworkRelationshipTypes.BLUE_RELATIONSHIP);
		blueGraph.addRelationship("2B", "3B", NetworkRelationshipTypes.BLUE_RELATIONSHIP);
		blueGraph.addRelationship("3B", "4B", NetworkRelationshipTypes.BLUE_RELATIONSHIP);
		blueGraph.addRelationship("3B", "5B", NetworkRelationshipTypes.BLUE_RELATIONSHIP);
		blueGraph.shutdown();
		
		ProprietaryGraph greenGraph = new ProprietaryGraph(database, "green", NetworkRelationshipTypes.GREEN_NODE);
		greenGraph.addNode("1G");
		greenGraph.addNode("2G");
		greenGraph.addNode("3G");
		greenGraph.addRelationship("1G", "2G", NetworkRelationshipTypes.GREEN_RELATIONSHIP);
		greenGraph.shutdown();
		
		GraphLinker linker = new GraphLinker(database);
		linker.addRelationship("blue", "1B", "red", "1R", NetworkRelationshipTypes.EQUIVALENT);
		linker.addRelationship("blue", "3B", "red", "2R", NetworkRelationshipTypes.EQUIVALENT);
		linker.addRelationship("blue", "4B", "green", "3G", NetworkRelationshipTypes.EQUIVALENT);
		linker.addRelationship("red", "1R", "green", "1G", NetworkRelationshipTypes.EQUIVALENT);
		linker.shutdown();
		
		// kill off any rouge processes
		System.exit(0);
	}

}