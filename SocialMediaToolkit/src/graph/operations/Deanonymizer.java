package graph.operations;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;

import utilities.math.Statistics;

public class Deanonymizer {
	
	/**
	 * Returns a mapping between nodes that are common in both graphs
	 * @param lgraph Left Graph (a List of nodes for a graph)
	 * @param ltype Left graph edge type
	 * @param rgraph Right Graph (a List of nodes for another graph)
	 * @param rtype Right graph edge type
	 * @param mapping A seed mapping set of confident Node to Node mappings.  
	 * This set must contain at least one mapping, but more mappings will yield better results.
	 * @param theta The fixed eccentricity threshold used to define the tradeoff between yield and accuracy
	 * @param maxIteration The maximum number of iterations to attempt.  
	 * Note: The algorithm attempts to correct early poor assumptions with multiple passes.
	 * @return A mapping of node to node structural equivalents
	 */
	public static Map<Node,Node> deanonymize(List<Node> lgraph, 
											 RelationshipType ltype, 
										     List<Node> rgraph, 
											 RelationshipType rtype, 
											 Map<Node,Node> mapping,
										     double theta, 
											 int maxIteration){
		
		int iterationCounter = 1;

		if(mapping.size() == 0){
			throw new IllegalArgumentException("Mapping must contain at least one pair.");
		}
		
		System.out.println("Deanonymizing...");
		
		double lastSize = 0;
		boolean eccentricityFail = true;
		
		// this is just a simple 2 pass iteration
		while(mapping.size() != lastSize || eccentricityFail && iterationCounter <= maxIteration){
			// System.out.println("New Iteration.");
			lastSize = mapping.size();
			eccentricityFail = false;
			for(Node lnode : lgraph){
				if(!mapping.containsKey(lnode)){
					
					// compute scores for lnode
					Map<Node,Double> lScores = matchScores(lgraph, ltype, rgraph, rtype, mapping, lnode);
					double eccentricity = Statistics.eccentricity(doubleCollectionToDoubleArray(lScores.values()));
					if(eccentricity < theta){
						// System.out.println("lscores: Eccentricity < theta (" + eccentricity + " < " + theta + ")");
						eccentricityFail = true;
						continue;
					}
					
					// pick node from rgraph where scores equals max score of lnodes
					Node rnode = bestScore(lScores);
					
					// compute scores for rnode
					Map<Node,Double> rScores = matchScores(rgraph, rtype, lgraph, ltype, invert(mapping), rnode);
					eccentricity = Statistics.eccentricity(doubleCollectionToDoubleArray(rScores.values()));
					if(eccentricity < theta){
						// System.out.println("rscores: Eccentricity < theta (" + eccentricity + " < " + theta + ")");
						eccentricityFail = true;
						continue;
					}
					
					// pick node from lgraph where scores equals max score of rnodes
					Node reverseMatch = bestScore(rScores);
					
					if(reverseMatch != null && lnode != null){
						// check for reverse match
						if(!reverseMatch.equals(lnode)){
							// System.out.println("No reverse match.");
							continue;
						}
						// System.out.println("New mapping found.");
						mapping.put(lnode, rnode);
					}
					
				} else {
					// System.out.println("Already Mapped.");
				}
			}
			System.out.println("Finished iteration: " + iterationCounter);
			iterationCounter++;
		}

		return mapping;
	}
	
	/**
	 * Returns a mapping between nodes that are common in both graphs
	 * @param lgraph Left Graph (a List of nodes for a graph)
	 * @param ltype Left graph edge type
	 * @param rgraph Right Graph (a List of nodes for another graph)
	 * @param rtype Right graph edge type
	 * @param mapping A seed mapping set of confident Node to Node mappings.  
	 * This set must contain at least one mapping, but more mappings will yield better results.
	 * @param theta The starting eccentricity threshold used to define the tradeoff between yield and accuracy
	 * @param convergenceRate A value between 0 and 1 that describes the factor by which to reduce theta after each iteration
	 * @param maxIteration The maximum number of iterations to attempt.  
	 * Note: The algorithm attempts to correct early poor assumptions with multiple passes.
	 * @return A mapping of node to node structural equivalents
	 */
	public static Map<Node,Node> deanonymize(List<Node> lgraph, 
											 RelationshipType ltype, 
											 List<Node> rgraph, 
											 RelationshipType rtype, 
											 Map<Node,Node> mapping, 
											 double theta, 
											 double convergenceRate, 
											 int maxIteration){
		
		int iterationCounter = 1;

		if(mapping.size() == 0){
			throw new IllegalArgumentException("Mapping must contain at least one pair.");
		}
		
		System.out.println("Deanonymizing...");
		
		double lastSize = 0;
		boolean eccentricityFail = true;
		
		while(mapping.size() != lastSize || eccentricityFail && iterationCounter <= maxIteration){
			// System.out.println("New Iteration.");
			lastSize = mapping.size();
			eccentricityFail = false;
			for(Node lnode : lgraph){
				if(!mapping.containsKey(lnode)){
					
					// compute scores for lnode
					Map<Node,Double> lScores = matchScores(lgraph, ltype, rgraph, rtype, mapping, lnode);
					double eccentricity = Statistics.eccentricity(doubleCollectionToDoubleArray(lScores.values()));
					if(eccentricity < theta){
						// System.out.println("lscores: Eccentricity < theta (" + eccentricity + " < " + theta + ")");
						eccentricityFail = true;
						continue;
					}
					
					// pick node from rgraph where scores equals max score of lnodes
					Node rnode = bestScore(lScores);
					
					// compute scores for rnode
					Map<Node,Double> rScores = matchScores(rgraph, rtype, lgraph, ltype, invert(mapping), rnode);
					eccentricity = Statistics.eccentricity(doubleCollectionToDoubleArray(rScores.values()));
					if(eccentricity < theta){
						// System.out.println("rscores: Eccentricity < theta (" + eccentricity + " < " + theta + ")");
						eccentricityFail = true;
						continue;
					}
					
					// pick node from lgraph where scores equals max score of rnodes
					Node reverseMatch = bestScore(rScores);
					
					if(reverseMatch != null && lnode != null){
						// check for reverse match
						if(!reverseMatch.equals(lnode)){
							// System.out.println("No reverse match.");
							continue;
						}
						// System.out.println("New mapping found.");
						mapping.put(lnode, rnode);
					}
					
				} else {
					// System.out.println("Already Mapped.");
				}
			}
			System.out.println("Finished iteration: " + iterationCounter + " - Theta: " + theta);
			iterationCounter++;
			theta = theta * convergenceRate;
		}
		
		return mapping;
	}

	/**
	 * Returns a mapping between nodes that are common in both graphs.  By default this method makes at least two iterative passes.
	 * @param lgraph Left Graph (a List of nodes for a graph)
	 * @param ltype Left graph edge type
	 * @param rgraph Right Graph (a List of nodes for another graph)
	 * @param rtype Right graph edge type
	 * @param mapping A seed mapping set of confident Node to Node mappings.  
	 * This set must contain at least one mapping, but more mappings will yield better results.
	 * @param theta The fixed eccentricity threshold used to define the tradeoff between yield and accuracy
	 * @return A mapping of node to node structural equivalents
	 */
	public static Map<Node,Node> deanonymize(List<Node> lgraph, 
											 RelationshipType ltype, 
											 List<Node> rgraph, 
											 RelationshipType rtype, 
											 Map<Node,Node> mapping, 
											 double theta){
		
		int iterationCounter = 1;

		if(mapping.size() == 0){
			throw new IllegalArgumentException("Mapping must contain at least one pair.");
		}
		
		System.out.println("Deanonymizing...");
		
		double lastSize = 0;
		boolean eccentricityFail = true;
		
		// this is just a simple 2 pass iteration
		while(mapping.size() != lastSize || eccentricityFail && iterationCounter <= 2){
			// System.out.println("New Iteration.");
			lastSize = mapping.size();
			eccentricityFail = false;
			for(Node lnode : lgraph){
				if(!mapping.containsKey(lnode)){
					
					// compute scores for lnode
					Map<Node,Double> lScores = matchScores(lgraph, ltype, rgraph, rtype, mapping, lnode);
					double eccentricity = Statistics.eccentricity(doubleCollectionToDoubleArray(lScores.values()));
					if(eccentricity < theta){
						// System.out.println("lscores: Eccentricity < theta (" + eccentricity + " < " + theta + ")");
						eccentricityFail = true;
						continue;
					}
					
					// pick node from rgraph where scores equals max score of lnodes
					Node rnode = bestScore(lScores);
					
					// compute scores for rnode
					Map<Node,Double> rScores = matchScores(rgraph, rtype, lgraph, ltype, invert(mapping), rnode);
					eccentricity = Statistics.eccentricity(doubleCollectionToDoubleArray(rScores.values()));
					if(eccentricity < theta){
						// System.out.println("rscores: Eccentricity < theta (" + eccentricity + " < " + theta + ")");
						eccentricityFail = true;
						continue;
					}
					
					// pick node from lgraph where scores equals max score of rnodes
					Node reverseMatch = bestScore(rScores);
					
					if(reverseMatch != null && lnode != null){
						// check for reverse match
						if(!reverseMatch.equals(lnode)){
							// System.out.println("No reverse match.");
							continue;
						}
						// System.out.println("New mapping found.");
						mapping.put(lnode, rnode);
					}
					
				} else {
					// System.out.println("Already Mapped.");
				}
			}
			System.out.println("Finished iteration: " + iterationCounter);
			iterationCounter++;
		}

		return mapping;
	}
	
	// start helper functions
	
	/**
	 * Returns a map of scores for respective nodes.  
	 * Credit for this algorithm goes to Arvind Narayanan and Vitaly Shmatikov described in the paper De-anonymizing Social Networks
	 * @param lgraph Left Graph (a List of nodes for a graph)
	 * @param ltype Left graph edge type
	 * @param rgraph Right Graph (a List of nodes for another graph)
	 * @param rtype Right graph edge type
	 * @param mapping The current mapping between nodes of lgraph and rgraph
	 * @param lnode A node from lgraph to be evaluated
	 * @return A mapping of node to score values
	 */
	private static Map<Node,Double> matchScores(List<Node> lgraph, RelationshipType ltype, List<Node> rgraph, RelationshipType rtype, Map<Node,Node> mapping, Node lnode){
		Map<Node,Double> scores = initializeScores(rgraph);

		if(lnode != null){
			// incoming relationships
			Iterable<Relationship> lgraphEdges = lnode.getRelationships(ltype, Direction.INCOMING);
			if(lgraphEdges != null){
				for(Relationship lgraphEdge : lgraphEdges){
					
					Node lnbr = lgraphEdge.getStartNode();
					if(!mapping.containsKey(lnbr)){
						continue;
					}
					
					Node rnbr = mapping.get(lnbr);
					
					for(Relationship rgraphEdge : rnbr.getRelationships(rtype, Direction.OUTGOING)){
						
						Node rnode = rgraphEdge.getEndNode();
						if(mapping.containsValue(rnode)){
							continue;
						}
						
						scores.put(rnode, (scores.get(rnode) + (1 / Math.pow(Structure.nodeDegree(rnode, rtype, Direction.INCOMING), 0.5))));
					}
				}
			}
			
			// outgoing relationships
			lgraphEdges = lnode.getRelationships(ltype, Direction.OUTGOING);
			if(lgraphEdges != null){
				for(Relationship lgraphEdge : lgraphEdges){
					
					Node lnbr = lgraphEdge.getEndNode();
					if(!mapping.containsKey(lnbr)){
						continue;
					}
					
					Node rnbr = mapping.get(lnbr);
					
					for(Relationship rgraphEdge : rnbr.getRelationships(rtype, Direction.INCOMING)){
						
						Node rnode = rgraphEdge.getStartNode();
						if(mapping.containsValue(rnode)){
							continue;
						}
						
						scores.put(rnode, (scores.get(rnode) + (1 / Math.pow(Structure.nodeDegree(rnode, rtype, Direction.OUTGOING), 0.5))));
					}
				}
			}
		}
		
		return scores;
	}
	
	/**
	 * Inverts a map
	 * @param map A Map object with a key set of K and value set of V
	 * @return A map object with a key set of V and a value set of K
	 */
	private static <V, K> Map<V, K> invert(Map<K, V> map) {
	    Map<V, K> inv = new HashMap<V, K>();
	    for (Entry<K, V> entry : map.entrySet())
	        inv.put(entry.getValue(), entry.getKey());
	    return inv;
	}

	/**
	 * Iterates a Node/Double map pair set and returns the Node with the highest double value
	 * @param scores A map with a key set of Nodes and a value set of Doubles
	 * @return Returns the Node with the highest value
	 */
	private static Node bestScore(Map<Node,Double> scores){
		Node node = null;
		double bestScore = 0;
		for(Entry<Node, Double> entry : scores.entrySet()){
			if(entry.getValue() > bestScore){
				bestScore = entry.getValue();
				node = entry.getKey();
			}
		}
		return node;
	}
	
	/**
	 * Returns a mapping of Node to Double score values initialized to zero
	 * @param graph A list of Nodes in the graph to evaluate
	 * @return An initialization of node to double mappings all initialized to 0.0
	 */
	private static Map<Node,Double> initializeScores(List<Node> graph){
		Map<Node,Double> result = new HashMap<Node,Double>();
		for(Node node : graph){
			result.put(node, 0.0);
		}
		return result;
	}
	
	/**
	 * Converts a collection of Double elements to an array of double primitives
	 * @param collection A collection of Doubles
	 * @return An array of double primitives
	 */
	private static double[] doubleCollectionToDoubleArray(Collection<Double> collection){
		double[] result = new double[collection.size()];
		Iterator<Double> iter = collection.iterator();
		for(int i=0; i<result.length; i++){
			result[i] = iter.next();
		}
		return result;
	}
}
