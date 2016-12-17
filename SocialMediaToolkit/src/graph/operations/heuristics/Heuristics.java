package graph.operations.heuristics;


public class Heuristics {

	// this metric is taken from the assumption that many usernames 
	// are the same names as emails minus the domain, for AOL.com
	// the average length is 18.7 minus 7 characters for "aol.com"
	// which results in an average username of 12.7
	// src = http://www.eph.co.uk/resources/email-address-length-faq/#emailavglength
	private static final double AVERAGE_USERNAME_LENGTH = 12.7;
	
	// just sort of an intuitive guess here based on some data at 
	// http://www.eph.co.uk/resources/email-address-length-faq/
	private static final double AVERAGE_MINIMUM_USERNAME_LENGTH = 6;
	
	/**
	 * Computes a username similarity heuristic score
	 * @param a Username A
	 * @param b Username B
	 * @return The heuristic score ranging from 0 to unbounded where 0 is no heuristic similarity.
	 * This heuristic is based on stemming, string length (under the assumption that short usernames are too
	 * common or likely to overlap, and longer usernames are less likely and should therefore produce are larger score).
	 */
	public static double usernameScore(String a, String b){
		if(a.equalsIgnoreCase(b) && a.length() >= AVERAGE_MINIMUM_USERNAME_LENGTH){
			// this is the basic case for most identity linking softwares
			return 1.0;
		} else if(a.length() >= AVERAGE_MINIMUM_USERNAME_LENGTH || b.length() >= AVERAGE_MINIMUM_USERNAME_LENGTH){
			if(a.contains(b)){
				return 1 + (b.length() / AVERAGE_USERNAME_LENGTH);
			}
			else if(b.contains(a)){
				return 1 + (a.length() / AVERAGE_USERNAME_LENGTH);
			} 
		}
		// we could add another case for fuzzy string matching here, but for now just ignore other cases
		return 0;
	}
	
	/**
	 * Computes a email similarity heuristic score
	 * @param a Username A
	 * @param b Username B
	 * @return The heuristic score ranging from 0 to 1, where 0 is unequivalent and 1 is equivalent
	 */
	public static double emailScore(String a, String b){
		if(a.equalsIgnoreCase(b)){
			return 1.0;
		}
		// could possibly add fuzzy email comparison here but, not implementing at this time
		return 0;
	}
	
	public static double uniqueIDScore(String a, String b){
		if(a.equalsIgnoreCase(b)){
			return 10.0;
		}
		return 0;
	}
	
	public static double locationScore(String a, String b){
		return 0;
	}
	
}
