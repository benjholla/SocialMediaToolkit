package graph.operations;

import graph.operations.heuristics.Heuristics;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.neo4j.graphdb.Node;

public class Seeds {

	private static double THRESHOLD = .9;
	
	public static Map<Node,Node> discoverSeeds(Collection<Node> a, Collection<Node> b){
		Map<Node,Node> results = new HashMap<Node,Node>();
		for(Node node : a){
			Node match = findBestMatch(node, b, THRESHOLD);
			if(match != null){
				results.put(node, match);
				b.remove(match);
			}
		}
		return results;
	}
	
	public static Map<Node,Node> discoverSeeds(Collection<Node> a, Collection<Node> b, int maxSeeds){
		Map<Node,Node> results = new HashMap<Node,Node>();
		for(Node node : a){
			Node match = findBestMatch(node, b, THRESHOLD);
			if(match != null){
				results.put(node, match);
				b.remove(match);
				if(results.size() == maxSeeds){
					return results;
				}
			}
		}
		return results;
	}
	
	private static Node findBestMatch(Node node, Collection<Node> candidates, final double threshold){
		Node result = null;
		double matchScore = 0;
		for(Node candidate : candidates){
			double match =  match(node, candidate);
			if(match > matchScore && match >= threshold){
				result = candidate;
			}
		}
		return result;
	}
	
	// so far this just checks username and email
	private static double match(Node a, Node b){
		double result = 0;
		
		String usernameA = "";
		String usernameB = "";
		String emailA = "";
		String emailB = "";
		
		String identifierA = "";
		String identifierB = "";
		
		// just use a little fuzzy logic to see what we have for properties for node a
		for(String propertyKey : a.getPropertyKeys()){
			// username property
			if(propertyKey.toLowerCase().contains("username")){
				usernameA = (String) a.getProperty(propertyKey);
			}
			// email property
			else if(propertyKey.toLowerCase().contains("email")){
				emailA = (String) a.getProperty(propertyKey);
			}
			// identifier property
			else if(propertyKey.toLowerCase().contains("identifier")){
				identifierA = (String) a.getProperty(propertyKey);
			}
		}
		
		// just use a little fuzzy logic to see what we have for properties for node b
		for(String propertyKey : b.getPropertyKeys()){
			// username property
			if(propertyKey.toLowerCase().contains("username")){
				usernameB = (String) b.getProperty(propertyKey);
			}
			// email property
			else if(propertyKey.toLowerCase().contains("email")){
				emailB = (String) b.getProperty(propertyKey);
			}
			// identifier property
			else if(propertyKey.toLowerCase().contains("identifier")){
				identifierB = (String) b.getProperty(propertyKey);
			}
		}
		
		// calculate score for common node properties
		
		if(!usernameA.equals("") && !usernameB.equals("")){
			result += Heuristics.usernameScore(usernameA, usernameB);
		}
		
		if(!emailA.equals("") && !emailB.equals("")){
			result += Heuristics.emailScore(emailA, emailB);
		}
		
		/*
		if(!identifierA.equals("") && !identifierB.equals("")){
			result += Heuristics.uniqueIDScore(identifierA, identifierB);
		}
		*/
		
		return result;
	}
	
}
