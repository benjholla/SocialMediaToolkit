package libraries.testing;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import utilities.math.Sets;



public class TestFilterLogic {

	public static void main(String[] args) {

		double signalToNoiseRatioForTestData = 9.0;
		int numTestDataValues = 1024;

		ArrayList<Integer> signals = range(0,getNumSignals(signalToNoiseRatioForTestData, numTestDataValues));
		ArrayList<Integer> noise = range(getNumSignals(signalToNoiseRatioForTestData, numTestDataValues),getNumSignals(signalToNoiseRatioForTestData, numTestDataValues) + getNumNoise(signalToNoiseRatioForTestData, numTestDataValues));
		
		System.out.println("Test Data SNR: " + signals.size() + "/" + noise.size() + " = " + (double)signals.size() / (double)noise.size());
		System.out.println("Test Data Signals Range = [0.." + (signals.size()-1) + "]");
		System.out.println("Test Data Noise Range = [" + signals.size() + ".." + (signals.size() + noise.size()-1) + "]");
		
		int numCollections = 4;
		int numValuesPerCollection = 512;		
		ArrayList<Set<Integer>> collections = getCollections(signals, noise, numCollections, numValuesPerCollection);
		
		
		
		for(Set<Integer> c : collections){
			System.out.println("Collection: " + "(S[" + signals(c,signals) +"]/N[" + noise(c,signals) + "]: " + signalToNoise(c,signals) + ")");
		}
		System.out.println();
		
		
		System.out.println("--------------------------------------------");

		
		System.out.println("Threshold,Collection SNR,Collection Signals,Collection Noise,Filtered Collection SNR,Filtered Collection Signals,Filtered Collection Noise");
		// System.out.println("Threshold,Collection SNR,Filtered Collection SNR");
		for(double threshold = 0.0; threshold<=numCollections - 1; threshold = threshold + 1.0){
			System.out.print(threshold/(double)numCollections);
			Set<Integer> mask = getFilterMask(collections,(threshold/(double)numCollections));
			for(Set<Integer> c : collections){
				// Set<Integer> filtered = Sets.intersection(mask, c);
				System.out.print(" & A & ");
				System.out.print(signalToNoise(c,signals) + " [" + signals(c,signals) + "/"+ noise(c,signals) + "] & ");
				Set<Integer> filteredCollection = Sets.difference(c, mask);
				System.out.print(signalToNoise(filteredCollection,signals) + " [" + signals(filteredCollection,signals) + "/"+ noise(filteredCollection,signals) + "] \\\\");
				System.out.println();
			}
			System.out.println();
		}
		
		System.out.print("1.0");
		Set<Integer> intersection = Sets.intersection(collections.get(0), Sets.intersection(collections.get(1), Sets.intersection(collections.get(2), collections.get(3))));
		for(Set<Integer> c : collections){
			System.out.print(" & A & ");
			System.out.print(signalToNoise(c,signals) + " [" + signals(c,signals) + "/"+ noise(c,signals) + "] & ");
			Set<Integer> mask = Sets.difference(c, intersection);
			Set<Integer> filteredCollection = Sets.difference(c, mask);
			System.out.print(signalToNoise(filteredCollection,signals) + " [" + signals(filteredCollection,signals) + "/"+ noise(filteredCollection,signals) + "] \\\\");
			System.out.println();
		}
		
		

	}
	
	private static Set<Integer> getFilterMask(ArrayList<Set<Integer>> collections, double percentileThreshold){
		Set<Integer> union = new HashSet<Integer>();
		for(Set<Integer> c : collections){
			union.addAll(c);
		}
		
		Map<Integer,Double> frequency = new HashMap<Integer,Double>();
		for(Integer i : union){
			for(Set<Integer> c : collections){
				if(c.contains(i)){
					if(frequency.containsKey(i)){
						frequency.put(i, frequency.get(i) + 1.0);
					} else {
						frequency.put(i, 1.0);
					}
				}
			}
		}

		Set<Integer> result = new HashSet<Integer>();
		for(Entry<Integer,Double> entry : frequency.entrySet()){
			if((entry.getValue()/(double)collections.size()) <= percentileThreshold){
				result.add((Integer)entry.getKey());
			}
		}
		
		return result;
	}

	private static int getNumSignals(double signalToNoiseRatio, int numSignalsAndNoise){

		int numSignals = 0;
		int numNoise = numSignalsAndNoise;
		
		while((double)numSignals / (double)numNoise < signalToNoiseRatio){
			numSignals++;
			numNoise--;
		}
		
		if(signalToNoiseRatio - ((double)numSignals / (double)numNoise) > signalToNoiseRatio - ((double)(numSignals-1) / (double)(numNoise+1))){
			numSignals--;
			numNoise++;
		}
		
		return numSignals;
	}
	
	private static int getNumNoise(double signalToNoiseRatio, int numSignalsAndNoise){

		int numSignals = 0;
		int numNoise = numSignalsAndNoise;
		
		while((double)numSignals / (double)numNoise < signalToNoiseRatio){
			numSignals++;
			numNoise--;
		}
		
		if(signalToNoiseRatio - ((double)numSignals / (double)numNoise) > signalToNoiseRatio - ((double)(numSignals-1) / (double)(numNoise+1))){
			numSignals--;
			numNoise++;
		}
		
		return numNoise;
	}
	
	private static double signalToNoise(Set<Integer> collection, ArrayList<Integer> signals){
		
		double signalsCount = 0.0;
		double noiseCount = 0.0;
		
		for(Integer i : collection){
			if(signals.contains(i)){
				signalsCount = signalsCount + 1.0;
			} else {
				noiseCount = noiseCount + 1.0;
			}
		}
		
		return signalsCount/noiseCount;
	}
	
	private static int signals(Set<Integer> collection, ArrayList<Integer> signals){
		int signalsCount = 0;
		for(Integer i : collection){
			if(signals.contains(i)){
				signalsCount++;
			}
		}
		return signalsCount;
	}
	
	private static int noise(Set<Integer> collection, ArrayList<Integer> signals){
		int noiseCount = 0;
		for(Integer i : collection){
			if(!signals.contains(i)){
				noiseCount++;
			}
		}
		return noiseCount;
	}
	
	private static ArrayList<Set<Integer>> getCollections(ArrayList<Integer> signals, ArrayList<Integer> noise, int numCollections, int numValuesPerCollection){
		Random rnd = new Random();

		int total = signals.size() + noise.size(); // should be equal to numCollections * numValuesPerCollection
		ArrayList<Integer> uniqueNoise = range(total, total + (noise.size() * (numCollections*2)));
		System.out.println("Test Data Unique Noise Range = [" + total + ".." + (total + uniqueNoise.size()-1) + "]");
		
		int numSignalsPerCollection = getNumSignals((double)signals.size()/(double)noise.size(), numValuesPerCollection);
		int numNoisePerCollection = getNumNoise((double)signals.size()/(double)noise.size(), numValuesPerCollection);
		
		ArrayList<Set<Integer>> result = new ArrayList<Set<Integer>>();

		for(int i=0; i<numCollections; i++){
			
			Set<Integer> collection = new HashSet<Integer>();
			
			int oneThirdSignals = (int)Math.round(numSignalsPerCollection*(1.0/3.0));
			
			// add the core signals
			for(int n=0; n<oneThirdSignals; n++){
				collection.add(signals.get(n));
			}
			
			// add the random signals aside from the core users
			ArrayList<Integer> signalsCopy = new ArrayList<Integer>();
			for(int n=oneThirdSignals; n<signals.size(); n++){
				signalsCopy.add(signals.get(n));
			}
			for(int n=oneThirdSignals; n<numSignalsPerCollection; n++){
				collection.add(signalsCopy.remove(rnd.nextInt(signalsCopy.size())));
			}
			
			int oneThirdNoise = (int)Math.round(numNoisePerCollection*(1.0/3.0));
			
			// add the unique noise
			for(int n=0; n<oneThirdNoise; n++){
				collection.add(uniqueNoise.remove(rnd.nextInt(uniqueNoise.size())));
			}
			
			// add the noise
			ArrayList<Integer> noiseCopy = new ArrayList<Integer>();
			for(int n=oneThirdNoise; n<noise.size(); n++){
				noiseCopy.add(noise.get(n));
			}
			for(int n=oneThirdNoise; n<numNoisePerCollection; n++){
				collection.add(noiseCopy.remove(rnd.nextInt(noiseCopy.size())));
			}
			
			result.add(collection);
		}
		
		return result;
	}
	
	private static ArrayList<Integer> range(int start, int stop)
	{
	   ArrayList<Integer> result = new ArrayList<Integer>(stop-start);

	   for(int i=0;i<stop-start;i++)
	      result.add(start+i);

	   return result;
	}

}
