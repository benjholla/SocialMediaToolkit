package libraries.testing;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;

import utilities.networks.facebook.crawler.SamplingUtilities;

public class Experiment {

	public static void main(String[] args) throws IllegalArgumentException, IOException, InterruptedException {
		
		Collection<Long> ids = new LinkedList<Long>(); 
		
		while(ids.size() != 1000){
			long x = SamplingUtilities.getRandomPublicAccount(1000);
			System.err.println(x);
			ids.add(x);
		}

		System.out.println("\n\n-------------------------------\n\n");
		
		for(Long id : ids){
			System.out.println(id);
		}
		
	}

}
