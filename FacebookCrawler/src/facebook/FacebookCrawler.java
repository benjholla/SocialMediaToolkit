package facebook;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.http.ParseException;

import db.graph.FacebookGraph;

import facebook.models.AuthenticatedFacebookAccount;
import facebook.utilities.FriendUtilities;

public class FacebookCrawler {
	
	public static void bfs(Long id, AuthenticatedFacebookAccount fb) throws IOException, ParseException {
		System.out.println("\n\n------------------------------" + id + "-------------------------------\n\n");
		
		// get friend id's of user
		LinkedList<Long> friends = new LinkedList<Long>();
		friends.addAll(FriendUtilities.getFriendsIDs(id, fb));
		
		for(Long friendId : friends){
			System.out.println(friendId);
		}
		
		for(Long friendID : friends){
			
			System.out.println("\n\n------------------------------" + friendID + "-------------------------------\n\n");
			
			LinkedList<Long> extendedFriends = new LinkedList<Long>();
			extendedFriends.addAll(FriendUtilities.getFriendsIDs(friendID, fb));
			
			for(Long extendedFriendID : extendedFriends){
				System.out.println(extendedFriendID);
			}
		}
	}  
	
	public static void search(long origin, long target, AuthenticatedFacebookAccount perspective) throws ParseException, IOException {
		if(origin == target) {
			return;  // well that was easy...
		}
		
		LinkedList<Long> searchedNodes = new LinkedList<Long>();
		LinkedList<Long> nodesToSearch = new LinkedList<Long>();
		
		// no need to search the first node...
		searchedNodes.addLast(origin);
		
		// expand the origin node
		Collection<Long> friends = FriendUtilities.getFriendsIDs(origin, perspective, true);
		
		// add each friend that is not already searched
		for(Long friend : friends){
			if(target == friend){
				// found target, target is a direct friend of origin
				System.out.println("Success!  Target and origin are first degree friends.");
				return;
			} else if(!searchedNodes.contains(friend)){
				// not the target and we haven't searched it, add it to the queue to search
				nodesToSearch.addLast(friend);
			}
		}
		
		// target is not a direct friend, lets start searching deeper than first degree connections
		// while we still have nodes to expand and while we haven't found the target node
		boolean searching = true;
		while(nodesToSearch.size() > 0 && searching){
			friends = FriendUtilities.getFriendsIDs(nodesToSearch.getFirst(), perspective, true);
			// add each friend that is not already searched
			for(Long friend : friends){
				if(target == friend){
					// found target, target is a direct friend of origin
					System.out.println("Success! Target is a friend of " + nodesToSearch.getFirst());
					return;
				} else if(!searchedNodes.contains(friend)){
					// not the target and we haven't searched it, add it to the queue to search
					nodesToSearch.addLast(friend);
				}
			}
			searchedNodes.addLast(nodesToSearch.getFirst());
			nodesToSearch.removeFirst();
		}
		
		// Check to see if we ended on favorable conditions
		if(searching){
			System.out.println("Couldn't find a path to target node from origin " + origin);
		} else {
			System.out.println("Success!");
		}
	}
	
	public static Collection<Long> findNeighboringFriendsOfTarget(long origin, long target, AuthenticatedFacebookAccount perspective) throws ParseException, IOException {
		
		LinkedList<Long> results = new LinkedList<Long>();
		
		// get the origin friends minus the actual target (we don't really care about the target since we know origin and target are friends already)
		Collection<Long> originFriends = FriendUtilities.getFriendsIDs(origin, perspective, false);
		originFriends.remove(target);
		
		for(Long friend : originFriends){
			if(FriendUtilities.getFriendsIDs(friend, perspective, false).contains(target)){
				results.add(friend);
			}
		}
		
		return results;
	}
	
	public static Long mapPathToTarget(long origin, long target, AuthenticatedFacebookAccount perspective, Collection<Long> blockedNodes, FacebookGraph graph) throws ParseException, IOException {
		
		// go ahead and add the origin and target nodes to the graph
		graph.addNode(origin);
		graph.addNode(target);
		
		if(origin == target) {
			return null;  // well that was easy...
		}
		
		LinkedList<Long> searchedNodes = new LinkedList<Long>();
		LinkedList<Long> nodesToSearch = new LinkedList<Long>();
		
		// count blocked nodes as nodes that we already searched
		if(blockedNodes != null){
			searchedNodes.addAll(blockedNodes);
		}
		
		// no need to search the first node...
		searchedNodes.addLast(origin);
		
		// expand the origin node
		Collection<Long> friends = FriendUtilities.getFriendsIDs(origin, perspective, true);
		
		// add friends of origin to the graph
		for(Long friend : friends){
			graph.addNode(friend);
			graph.addRelationship(origin, friend, FacebookGraph.RelationshipTypes.FRIEND);
		}
		
		// add each friend that is not already searched
		for(Long friend : friends){
			if(target == friend){
				// found target, target is a direct friend of origin
				System.out.println("Success!  Target and origin are first degree friends.");
				return origin;
			} else if(!searchedNodes.contains(friend)){
				// not the target and we haven't searched it, add it to the queue to search
				nodesToSearch.addLast(friend);
			}
		}
		
		// target is not a direct friend, lets start searching deeper than first degree connections
		// while we still have nodes to expand and while we haven't found the target node
		boolean searching = true;
		while(nodesToSearch.size() > 0 && searching){
			friends = FriendUtilities.getFriendsIDs(nodesToSearch.getFirst(), perspective, true);
			
			// add friends of node to the graph
			for(Long friend : friends){
				graph.addNode(friend);
				graph.addRelationship(nodesToSearch.getFirst(), friend, FacebookGraph.RelationshipTypes.FRIEND);
			}
			
			// add each friend that is not already searched
			for(Long friend : friends){
				if(target == friend){
					// found target, target is a direct friend of origin
					System.out.println("Success! Target is a friend of " + nodesToSearch.getFirst());
					return nodesToSearch.getFirst();
				} else if(!searchedNodes.contains(friend)){
					// not the target and we haven't searched it, add it to the queue to search
					nodesToSearch.addLast(friend);
				}
			}
			searchedNodes.addLast(nodesToSearch.getFirst());
			nodesToSearch.removeFirst();
		}
		
		// Check to see if we ended on favorable conditions
		if(searching){
			System.out.println("Couldn't find a path to target node from origin " + origin);
		} else {
			System.out.println("Success!");
		}
		
		return null;
	}

}
