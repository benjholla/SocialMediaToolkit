package facebook.utilities;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import facebook.models.AuthenticatedFacebookAccount;
import facebook.models.FacebookAccount;

public class SamplingUtilities {

	public static FacebookAccount getRandomPublicAccount(AuthenticatedFacebookAccount fb, boolean logging) throws ClientProtocolException, IOException{
		if(!fb.isAuthenticated()){
			fb.authenticate();
		}
		
		DefaultHttpClient httpClient = fb.getHttpClient();
		
		Random rnd = new Random();
		
		long guess = Math.abs(rnd.nextLong() % ((long)Math.pow(2, 32)) - 1);
		boolean guessing = true;
		
		while(guessing){
			if (logging) System.out.println("\nGET http://m.facebook.com/" + guess);
			HttpGet httpget = new HttpGet("http://m.facebook.com/" + guess);
			HttpResponse response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
			
			if (logging) System.out.println("Response Status: " + response.getStatusLine());
			
			// for each link look for an id and add to the collection
			// also look for the next load more friends link or end (more friends link is missing)
			LinkedList<Long> friends = new LinkedList<Long>();
			
			String html = EntityUtils.toString(entity);
			
			if(html.contains("<title>Page Not Found</title>")){
				guess = Math.abs(rnd.nextLong() % ((long)Math.pow(2, 32)) - 1);
			} else {
				guessing = false;
			}
		}
		
		return new FacebookAccount(guess);
	}
	
}
