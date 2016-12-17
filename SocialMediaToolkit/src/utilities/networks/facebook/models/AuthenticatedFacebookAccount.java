package utilities.networks.facebook.models;
import java.io.IOException;
import java.util.Collection;

import org.apache.http.ParseException;
import org.apache.http.impl.client.DefaultHttpClient;

import utilities.networks.facebook.crawler.FriendUtilities;
import utilities.networks.facebook.crawler.LoginUtilities;

/**
 * @author Ben Holland
 */
public class AuthenticatedFacebookAccount extends FacebookAccount {
	
	private DefaultHttpClient httpClient = null;
	private String email = "";
	private String password = "";
	
	public AuthenticatedFacebookAccount(String email, String password) throws IOException, IllegalArgumentException {
		super(LoginUtilities.validateCredentials(email, password, false));
		this.email = email;
		this.password = password;
	} 
	
	public AuthenticatedFacebookAccount(String email, String password, boolean logging) throws IOException, IllegalArgumentException {
		super(LoginUtilities.validateCredentials(email, password, logging));
		this.email = email;
		this.password = password;
	} 
	
	public boolean isAuthenticated(){
		return httpClient != null;
	}
	
	public void authenticate() throws IllegalArgumentException, IOException {
		if(isAuthenticated()){
			return;
		}
		httpClient = LoginUtilities.login(email, password);
	}
	
	public void unauthenticate(){
		httpClient.getConnectionManager().shutdown();
		httpClient = null;
	}
	
	public DefaultHttpClient getHttpClient(){
		return httpClient;
	}
	
	public Collection<FacebookAccount> getFriends() throws ParseException, IOException{
		return FriendUtilities.getFriendsAccounts(getID(), this);
	}
	
	public Collection<FacebookAccount> getFriends(boolean logging) throws ParseException, IOException{
		return FriendUtilities.getFriendsAccounts(getID(), this, logging);
	}
	
}
