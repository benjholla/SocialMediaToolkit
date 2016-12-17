package utilities.networks.facebook.crawler;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.htmlparser.jericho.Element;
import net.htmlparser.jericho.Source;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import utilities.networks.facebook.models.AuthenticatedFacebookAccount;
import utilities.networks.facebook.models.FacebookAccount;

public class FriendUtilities {

	public static Collection<FacebookAccount> getFriendsAccounts(Long ID, AuthenticatedFacebookAccount fb) throws IOException, ParseException {
		return getFriendsAccounts(ID, fb, false);
	}
	
	public static Collection<FacebookAccount> getFriendsAccounts(Long ID, AuthenticatedFacebookAccount fb, boolean logging) throws IOException, ParseException {
		Collection<Long> ids = getFriendsIDs(ID, fb, logging);
		Collection<FacebookAccount> accounts = new LinkedList<FacebookAccount>();
		for(Long id : ids){
			accounts.add(new FacebookAccount(id));
		}
		return accounts;
	}
	
	public static Collection<Long> getFriendsIDs(Long ID, AuthenticatedFacebookAccount fb) throws IOException, ParseException {
		return getFriendsIDs(ID, fb, false);
	}
	
	public static Collection<Long> getFriendsIDs(Long ID, AuthenticatedFacebookAccount fb, boolean logging) throws IOException, ParseException {
		if(!fb.isAuthenticated()){
			fb.authenticate();
		}
		
		// parsing information from an account that is the same as the authenticated  
		// facebook account is a bit trickier, so figure out if we need to be cautious
		boolean self = fb.getID() == ID ? true : false;
		
		
		
		DefaultHttpClient httpClient = fb.getHttpClient();
		if (logging) System.out.println("\nGET http://m.facebook.com/friends/?id=" + ID);
		HttpGet httpget = new HttpGet("http://m.facebook.com/friends/?id=" + ID);
		HttpResponse response = httpClient.execute(httpget);
		HttpEntity entity = response.getEntity();
		
		if (logging) System.out.println("Response Status: " + response.getStatusLine());
		
		// for each link look for an id and add to the collection
		// also look for the next load more friends link or end (more friends link is missing)
		LinkedList<Long> friends = new LinkedList<Long>();
		
		String html = EntityUtils.toString(entity);

		// parse out friend ID's into friends collection and return link to next set of friends if link exists
		String moreFriendsLink = parseFriends(html, friends, self);
		
		while(!moreFriendsLink.equals("")){
			// repeat steps for more friends link
			if (logging) System.out.println("\nGET " + moreFriendsLink);
			httpget = new HttpGet(moreFriendsLink);
			response = httpClient.execute(httpget);
			entity = response.getEntity();
			if (logging) System.out.println("Response Status: " + response.getStatusLine());
			html = EntityUtils.toString(entity);
			moreFriendsLink = parseFriends(html, friends, self);
		}
		return friends;
	}
	
	// start helper functions
	
	// this function should exclude reccommended friends and recent contacts if the authenticated 
	// facebook account is looking at itself (when self variable is true)
	// Edit: updated to remove support for recentlyContacted because recentlyContact represents actual friends...bug or feature we will find out later...
	private static String parseFriends(String html, LinkedList<Long> friends, boolean self) throws MalformedURLException {
		
		// just parse out the ids of each friend, each friend has a url like /a/mobile/friends/add_friend.php?id=<ID HERE>&amp blah blah
		// use Jericho html parser to get relevant information
		Source source = new Source(html);
		
		// int recentlyContactedLocation = -1;
		int peopleYouMayKnowLocation = -1;
		if(self){
			
			List<Element> divs = source.getAllElements("div");
			for(Element div : divs){
				String text = div.getTextExtractor().toString();
				if(text.equalsIgnoreCase("People You May Know")){
					peopleYouMayKnowLocation = div.getBegin();
				} 
				//else if(text.equalsIgnoreCase("Recently Contacted")){
				//	recentlyContactedLocation = div.getBegin();
				//}
			}
			
			if( /* recentlyContactedLocation == -1 || */ peopleYouMayKnowLocation == -1){
				// if its not in divs it might be in the even simplier mobile interface, which just uses strong element tags
				List<Element> strongs = source.getAllElements("strong");
				for(Element strong : strongs){
					String text = strong.getTextExtractor().toString();
					if(text.equalsIgnoreCase("People You May Know")){
						peopleYouMayKnowLocation = strong.getBegin();
					}
					//else if(text.equalsIgnoreCase("Recently Contacted")){
					//	recentlyContactedLocation = strong.getBegin();
					//}
				}
			}
		}
		
		// friend ID's are all stored in anchor links, so grab all the links then we can examine them
		List<Element> links = source.getAllElements("a");
		
		String moreFriendsLink = "";
		for(Element element : links){
			String text = element.getTextExtractor().toString();
			String link = element.getAttributeValue("href");
			
			// This is good debug information, but it gets a bit noisy
			// System.out.println("Examining Link: " + (!text.trim().equals("") ? "[ " + text + " ] - " : "") + link);
			
			// if there are more friends grab that link, and save it for later
			if(text.equals("See More Friends")){
				moreFriendsLink = link;
			}
			
			URL url;
			// convert link to url, assume absolute url then default to relative url with base url of http://m.facebook.com
			try {
				url = new URL(link);
			} catch(Exception e1){
				// maybe its not a relative url
				try{
					url = new URL(new URL("http://m.facebook.com"), link);
				} catch (Exception e2) {
					// this isn't really an html link...its something like an email or a telephone link, just skip it
					continue;
				}
			}
			
			// parse out url parameters
			Map<String, String> params = getQueryMap(url);
			
			Set<String> keys = params.keySet();  
			for (String key : keys)  
			{  
				// get all the user id's we see
			   if(key.equals("id")){
				   String idString = params.get(key);
				   // let's just do a little extra validation and make sure its a numeric value
				   try{
					   Long id = Long.parseLong(idString);
					   
					   // is this actually a friend? There is a special case for viewing your own account
					   // is link in the People You May Know or Recently Contacted category? 
					   // if so we don't care about this, just ignore it
					   
					   int linkLocation = element.getBegin();
					   
					   //if(self && (recentlyContactedLocation != -1) && linkLocation > recentlyContactedLocation){
					   //   // System.out.println("Skipping Link (After Recently Contacted): " + (!text.trim().equals("") ? "[ " + text + " ] - " : "") + link);
					   //	   continue;
					   //}
						
					   if(self && (peopleYouMayKnowLocation != -1) && linkLocation > peopleYouMayKnowLocation){
						   // This is good debug information, but it gets a bit noisy
						   // System.out.println("Skipping Link (After People You May Know): " + (!text.trim().equals("") ? "[ " + text + " ] - " : "") + link);
						   continue;
					   }
					   
					   // if we haven't seen this id before add it to the collection
					   if(!friends.contains(id)){
						   friends.add(id);
					   }
				   } catch (Exception e){
					   // nope its not...just ignore it...
				   }
			   }
			}  
		}
		
		try{
			return getAbsoluteURL(moreFriendsLink, "http://m.facebook.com");
		} catch (Exception e){
			return "";
		}
	}
	
	private static String getAbsoluteURL(String relativeURL, String baseURL) throws MalformedURLException {
		if(relativeURL.equals("")){
			return "";
		} else {
			URL url;
			// convert link to url, assume absolute url then default to relative url with base url of http://m.facebook.com
			try {
				url = new URL(relativeURL);
			} catch(Exception e){
				// maybe its not a relative url
				try{
					url = new URL(new URL(baseURL), relativeURL);
				} catch (Exception e2) {
					// this isn't really an html link...its something like an email or a telephone link, just return null string
					return "";
				}
			}
			return url.toString();
		}
	}
	
	private static Map<String, String> getQueryMap(URL url)  
	{  
		String query = url.getQuery();
	    String[] params = query.split("&");  
	    Map<String, String> map = new HashMap<String, String>();  
	    for (String param : params)  
	    {  
	    	try{
	    		String name = param.split("=")[0];  
	    		String value = param.split("=")[1];
	    		map.put(name, value);  
	    	} catch (Exception e){
	    		// malformed url params, just ignore
	    	}
	        
	    }  
	    return map;  
	}
	
}
