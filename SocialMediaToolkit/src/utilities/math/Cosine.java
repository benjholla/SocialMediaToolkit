package utilities.math;

import java.util.LinkedList;
import java.util.List;

public class Cosine {

	
	public static void main(String args[]){
		List<Double> v1 = new LinkedList<Double>();
		v1.add(3.0);
		v1.add(3.0);
		v1.add(5.0);
		
		List<Double> v2 = new LinkedList<Double>();
		v2.add(3.0);
		v2.add(3.0);
		v2.add(5.0);
		
		System.out.println(cosineSimularity(v1, v2));
	}
	
	/**
	 * Cosine Similarity of two vectors.  The Cosine between two vectors x and y is
     * (x*y)/ |x|*|y| where (x*y) is the inner product of x and y, and |x| = sqrt((x*x)) 
     * is the Euclidean norm of x. 
	 * @param a A vector
	 * @param b A vector
	 * @return The cosine similarity of vector a and b.  Range is from -1 to 1 where 1 is most similar
	 * and -1 is least similar.
	 */
	public static double cosineSimularity(List<Double> a, List<Double> b){
		if(a.size() != b.size()){
			return -1;
		} else {
			return dotProduct(a, b) / (normalize(a) * normalize(b));
		}
	}
	
	/**
	 * http://en.wikipedia.org/wiki/Dot_product
	 * @param a A vector
	 * @param b A vector
	 * @return The dot product of vector a and b
	 */
	private static double dotProduct(List<Double> a, List<Double> b){
		int n = a.size();
		double sum = 0;
		for(int i=0; i<n; i++){
			sum += a.get(i) * b.get(i);
		}
		return sum;
	}
	
	/**
	 * http://en.wikipedia.org/wiki/Norm_%28mathematics%29
	 * @param a The vector to normalize
	 * @return The Euclidean Norm
	 */
	private static double normalize(List<Double> a){
		int n = a.size();
		double sum = 0;
		for(int i=0; i<n; i++){
			sum += a.get(i) * a.get(i);
		}
		return Math.sqrt(sum);
	}
	
	// ALTERNATIVE COSINE SIMULARITY IMPLEMENTATION
	// Cosine vector similarity function. The Cosine between two vectors x and y is
    // (x*y)/ |x|*|y| where (x*y) is the inner product of x and y, and 
	// |x| = (x*x) ^ 1/2, the Euclidean norm of x. 
	/*
	public static double cosineSimularity(List<Double> a, List<Double> b){
		int N = ((b.size() < a.size()) ? b.size() : a.size());
	    double dot = 0.0d;
	    double mag1 = 0.0d;
	    double mag2 = 0.0d;
	    for (int n = 0; n < N; n++)
	    {
	        dot += a.get(n) * b.get(n);
	        mag1 += Math.pow(a.get(n), 2);
	        mag2 += Math.pow(b.get(n), 2);
	    }
	    return dot / (Math.sqrt(mag1) * Math.sqrt(mag2));
	}
	*/
	
}
