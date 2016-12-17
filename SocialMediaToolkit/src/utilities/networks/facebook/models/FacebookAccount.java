package utilities.networks.facebook.models;
import java.io.IOException;
import java.util.Collection;

import json.JSONException;
import json.JSONObject;

import org.apache.http.ParseException;

import utilities.networks.facebook.crawler.FriendUtilities;

/**
 *
 * @author Ben Holland
 */
public class FacebookAccount {
    
    private long id = ((long)-1);
    private String name = "";
    private String firstName = "";
    private String lastName = "";
    private String username = "";
    private String link = "";
    private String gender = "";
    private String locale = "";
    
    // lazy loading
    private boolean loaded = false;
    
    public FacebookAccount(long id) throws IOException {
    	this.id = id;
    	loadByID(id);
		loaded = true;
    }
    
    public FacebookAccount(long id, boolean lazyLoad) throws IOException {
    	this.id = id;
    	if(!lazyLoad){
    		loadByID(id);
    		loaded = true;
    	}
    }
    
    public FacebookAccount(String username) throws IOException {
    	this.username = username;
    	loadByUsername(username);
		loaded = true;
    }
    
    public FacebookAccount(String username, boolean lazyLoad) throws IOException {
    	this.username = username;
    	if(!lazyLoad){
    		loadByUsername(username);
    		loaded = true;
    	}
    }
    
    private void loadByID(long id) throws IOException{
    	String name = "";
        String firstName = "";
        String lastName = "";
        String username = "";
        String link = "";
        String gender = "";
        String locale = "";
        
        String response = utilities.http.HttpInterface.get("https://graph.facebook.com/" + id);
        
        try {
        	JSONObject json = new JSONObject(response);
            
            name = ((String)json.get("name"));
            firstName = ((String)json.get("first_name"));
            lastName = ((String)json.get("last_name"));
            gender = ((String)json.get("gender"));
            locale = ((String)json.get("locale"));
            
            // at this point we don't know if this account has a vanity name, 
            // so make the assumption that it does, and if we break something
            // then default to the assumption that it doesn't
            
            try {
                // try to get the account vanity name
                username = ((String)json.get("username"));
            } catch (Exception ex){
                // no vanity name, get account link
                username = "";
                link = ((String)json.get("link"));
            }
        }
        catch(JSONException e){
        	throw new IllegalArgumentException("Could not retrieve account details, check identifier.");
        }
        
        this.id = id;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.locale = locale;
        this.username = username;
        this.link = link;
    }
    
    public void loadByUsername(String username) throws IOException {
    	long id = 0;
        String name = "";
        String firstName = "";
        String lastName = "";
        String gender = "";
        String locale = "";
        String link = "";
        
        String response = utilities.http.HttpInterface.get("https://graph.facebook.com/" + username);
        
        try {
        	JSONObject json = new JSONObject(response);
	        id = Long.parseLong(((String)json.get("id")));
	        name = ((String)json.get("name"));
	        firstName = ((String)json.get("first_name"));
	        lastName = ((String)json.get("last_name"));
	        gender = ((String)json.get("gender"));
	        locale = ((String)json.get("locale"));
	        username = ((String)json.get("username"));
        }
        catch(JSONException e){
        	throw new IllegalArgumentException("Could not retrieve account details, check identifier.");
        }
        
        this.username = username;
        this.id = id;
        this.name = name;
        this.firstName = firstName;
        this.lastName = lastName;
        this.gender = gender;
        this.locale = locale;
        this.link = link;
    }
    
    public void load() throws IOException {
    	if(!loaded){
    		if(id == ((long)-1)){
    			loadByUsername(username);
    		} else {
    			loadByID(id);
    		}
    	}
    }
    
    public long getID() throws IOException {
    	if(!loaded) load();
        return id;
    }
    
    public String getName() throws IOException {
    	if(!loaded) load();
        return name;
    }
    
    public String getFirstName() throws IOException {
    	if(!loaded) load();
        return firstName;
    }
    
    public String getLastName() throws IOException {
    	if(!loaded) load();
        return lastName;
    }
    
    public String getUsername() throws IOException {
    	if(!loaded) load();
        return username;
    }
    
    public boolean hasVanityUsername() throws IOException {
    	if(!loaded) load();
        return username.equals("");
    }
    
    public String getLink() throws IOException {
    	if(!loaded) load();
        return link.equals("") ? ("http://www.facebook.com/" + username) : link;
    }
    
    public String getMobileLink() throws IOException {
    	if(!loaded) load();
        return link.equals("") ? ("http://m.facebook.com/" + username) : link.replace("http://www.facebook.com", "http://m.facebook.com");
    }
    
    public String getGender() throws IOException {
    	if(!loaded) load();
        return gender;
    }
    
    public String getLocale() throws IOException {
    	if(!loaded) load();
        return locale;
    }
    
    public String getCurrentProfilePicture() throws IOException {
    	if(!loaded) load();
        return "http://graph.facebook.com/" + id + "/picture?type=normal";
    }
    
    public String getCurrentProfilePicture(String type) throws IllegalArgumentException, IOException {
    	if(!loaded) load();
    	// Supported types: small, normal, large, square
    	if(type.equalsIgnoreCase("small") || type.equalsIgnoreCase("normal") || type.equalsIgnoreCase("large") || type.equalsIgnoreCase("square")){
            return "http://graph.facebook.com/" + id + "/picture?type=" + type;
    	} else {
    		throw new IllegalArgumentException("Supported types: small, normal, large, square");
    	}
    }
    
    @Override
    public String toString(){
    	if(loaded){
    		try {
    			return "[" + name + ", " + id + ", " + gender + ", " +  getLink() + "]";
    		} catch (Exception e){
    			return "[" + name + ", " + id + ", " + gender +  "]";
    		}
        } else {
        	try {
    			load();
    			try {
        			return "[" + name + ", " + id + ", " + gender + ", " + getLink() + "]";
        		} catch (Exception e){
        			return "[" + name + ", " + id + ", " + gender +  "]";
        		}
    		} catch (Exception e){
    			if(id == ((long)-1)){
        			return "[" + username + "]";
        		} else {
        			return "[" + id + "]";
        		}
    		}
        }
    }
    
    public Collection<FacebookAccount> getFriends(AuthenticatedFacebookAccount account) throws ParseException, IOException {
		return FriendUtilities.getFriendsAccounts(getID(), account);
	}
    
    public Collection<FacebookAccount> getFriends(AuthenticatedFacebookAccount account, boolean logging) throws ParseException, IOException {
    	System.out.println("Getting friends of " + getID());
		return FriendUtilities.getFriendsAccounts(getID(), account, logging);
	}
}
