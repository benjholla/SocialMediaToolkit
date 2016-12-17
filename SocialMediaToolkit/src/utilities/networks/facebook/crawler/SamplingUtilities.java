package utilities.networks.facebook.crawler;

import java.io.IOException;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import utilities.networks.facebook.models.AuthenticatedFacebookAccount;

public class SamplingUtilities {

	/*
	public static long getRandomPublicAccount(AuthenticatedFacebookAccount fb, boolean logging) throws ClientProtocolException, IOException{
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
			
			String html = EntityUtils.toString(entity);
			
			if(html.contains("<title>Page Not Found</title>")){
				guess = Math.abs(rnd.nextLong() % ((long)Math.pow(2, 32)) - 1);
			} else {
				guessing = false;
			}
		}
		
		return guess;
	}
	*/
	
	
	public static long getRandomPublicAccount(long delay) throws ClientProtocolException, IOException, InterruptedException{
		DefaultHttpClient httpClient = new DefaultHttpClient();
		
		Random rnd = new Random();
		
		long guess = Math.abs(rnd.nextLong() % ((long)Math.pow(2, 32)) - 1);
		boolean guessing = true;
		
		while(guessing){
			
			Thread.sleep(delay);
			
			// http://m.facebook.com/profile.php?id=636292580
			HttpGet httpget = new HttpGet("http://graph.facebook.com/" + guess);
			HttpResponse response = httpClient.execute(httpget);
			HttpEntity entity = response.getEntity();
			
			String html = EntityUtils.toString(entity);
			
			if(response.getStatusLine().toString().contains("Forbidden") || response.getStatusLine().toString().contains("Bad Request")
					|| html.contains("false") || html.contains("Unsupported get request") || html.contains("Some of the aliases you requested do not exist") || html.contains("An access token is required to request this resource.")){
				guess = Math.abs(rnd.nextLong() % ((long)Math.pow(2, 32)) - 1);
			} else {
				guessing = false;
			}
			
		}
		
		return guess;
	}
	
}
