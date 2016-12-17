package utilities.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

public class HttpInterface {
	
	public static String get(String address) throws IOException {
		StringBuilder response = new StringBuilder();
		URL url = new URL(address);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
        String responsePartial;
        while ((responsePartial = in.readLine()) != null) 
        {
          response.append(responsePartial);
        }
        in.close();
		return response.toString();
	}

}
