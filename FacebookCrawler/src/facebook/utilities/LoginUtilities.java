package facebook.utilities;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

public class LoginUtilities {

	public static Long validateCredentials(String email, String password, boolean logging) throws IOException, IllegalArgumentException {
		
		if(logging) System.out.println("Logging in as: " + email);
		
		DefaultHttpClient httpClient = new DefaultHttpClient();

		if(logging) System.out.println("\nGET http://www.facebook.com/login.php");
		HttpGet httpget = new HttpGet("http://www.facebook.com/login.php");

		HttpResponse response = httpClient.execute(httpget);
		HttpEntity entity = response.getEntity();
		
		if(logging) System.out.println("Response Status: " + response.getStatusLine());
		
		if (entity != null) {
		    entity.consumeContent();
		}
		
		if(logging) System.out.println("Cookies (before login):");
		List<Cookie> cookies = httpClient.getCookieStore().getCookies();
		
		if(logging) {
			if (cookies.isEmpty()) {
			    System.out.println("N/A");
			} else {
			    for (int i = 0; i < cookies.size(); i++) {
			        System.out.println("- " + cookies.get(i).toString());
			    }
			}
		}

		if(logging) System.out.println("\nPOST http://www.facebook.com/login.php");
		HttpPost httpost = new HttpPost("http://www.facebook.com/login.php");

		// set values to post in login form
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair("email", email));
		nvps.add(new BasicNameValuePair("pass", password));

		httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		response = httpClient.execute(httpost);
		entity = response.getEntity();

		if(logging) System.out.println("Response Status: " + response.getStatusLine());
		
		if (entity != null) {
		    entity.consumeContent();
		}

		if(logging) System.out.println("Cookies (after login):");
		cookies = httpClient.getCookieStore().getCookies();
		boolean loginSuccess = false;
		String userID = "";
		
		if (cookies.isEmpty()) {
			if(logging) System.out.println("N/A");
		} else {
		    for (int i = 0; i < cookies.size(); i++) {
		    	if(cookies.get(i).getName().equals("c_user")){
		    		loginSuccess = true;
		    		userID = cookies.get(i).getValue();
		    	}
		    	if(logging) System.out.println("- " + cookies.get(i).toString());
		    }
		}
		
		httpClient.getConnectionManager().shutdown();
		
		if(loginSuccess){
			if(logging) System.out.println("\nSuccesfully logged in as " + email + " [ID: " + userID + "]");
			return Long.parseLong(userID);
		} else {
			if(logging) System.err.print("\nError: Could not log in as " + email + ".  Invalid credentials.");
			throw new IllegalArgumentException("Invalid Credentials.");
		}
	}
	
	public static DefaultHttpClient login(String email, String password) throws IOException, IllegalArgumentException {
		return login(email, password, "", false);
	}
	
	public static DefaultHttpClient login(String email, String password, boolean logging) throws IOException, IllegalArgumentException {
		return login(email, password, "", logging);
	}
	
	public static DefaultHttpClient login(String email, String password, String userAgent) throws IOException, IllegalArgumentException {
		return login(email, password, userAgent, false);
	}
	
	public static DefaultHttpClient login(String email, String password, String userAgent, boolean logging) throws IOException, IllegalArgumentException {
		if(logging) System.out.println("Logging in as: " + email);
		
		DefaultHttpClient httpClient = new DefaultHttpClient();

		if(logging) System.out.println("\nGET http://www.facebook.com/login.php");
		HttpGet httpget = new HttpGet("http://www.facebook.com/login.php");
		if(!userAgent.equals("")){
			httpget.setHeader("User-Agent", userAgent);
		}
		HttpResponse response = httpClient.execute(httpget);
		HttpEntity entity = response.getEntity();
		
		if(logging) System.out.println("Response Status: " + response.getStatusLine());
		if (entity != null) {
		    entity.consumeContent();
		}
		
		if(logging) System.out.println("Cookies (before login):");
		
		List<Cookie> cookies = httpClient.getCookieStore().getCookies();
		
		if(logging) {
			if (cookies.isEmpty()) {
			    System.out.println("N/A");
			} else {
			    for (int i = 0; i < cookies.size(); i++) {
			        System.out.println("- " + cookies.get(i).toString());
			    }
			}
		}

		if(logging) System.out.println("\nPOST http://www.facebook.com/login.php");
		HttpPost httpost = new HttpPost("http://www.facebook.com/login.php");
		if(!userAgent.equals("")){
			httpost.setHeader("User-Agent", userAgent);
		}

		// set values to post in login form
		List <NameValuePair> nvps = new ArrayList <NameValuePair>();
		nvps.add(new BasicNameValuePair("email", email));
		nvps.add(new BasicNameValuePair("pass", password));

		httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));
		response = httpClient.execute(httpost);
		entity = response.getEntity();

		if(logging) System.out.println("Response Status: " + response.getStatusLine());
		if (entity != null) {
		    entity.consumeContent();
		}

		if(logging) System.out.println("Cookies (after login):");
		cookies = httpClient.getCookieStore().getCookies();
		boolean loginSuccess = false;
		String userID = "";
		if (cookies.isEmpty()) {
			if(logging) System.out.println("N/A");
		} else {
		    for (int i = 0; i < cookies.size(); i++) {
		    	if(cookies.get(i).getName().equals("c_user")){
		    		loginSuccess = true;
		    		userID = cookies.get(i).getValue();
		    	}
		    	if(logging) System.out.println("- " + cookies.get(i).toString());
		    }
		}
		
		if(loginSuccess){
			if(logging) System.out.println("\nSuccesfully logged in as " + email + " [ID: " + userID + "]");
			return httpClient;
		} else {
			if(logging) System.err.print("\nError: Could not log in as " + email + ".  Check credentials.");
			throw new IllegalArgumentException("Invalid Credentials.");
		}
	}
	
}
