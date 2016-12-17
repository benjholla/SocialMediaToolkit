package utilities.math;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Jaccard {

	public static <T> double jaccardIndex(Collection<T> a, Collection<T> b){
		Set<T> setA = new HashSet<T>(a);
		Set<T> setB = new HashSet<T>(b);
		Set<T> numerator = Sets.intersection(setA, setB);
		Set<T> denominator = Sets.union(setA, setB);
		return ((double) numerator.size()) / ((double) denominator.size());
	}
	
}
