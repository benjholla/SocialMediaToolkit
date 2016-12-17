package utilities.math;

import org.apache.commons.math.stat.StatUtils;

public class Statistics {

	/**
	 * Calculates percentage error = (absolute(measured - expected) / expected) * 100 %
	 * @param measured The observed value
	 * @param expected The accepted value
	 * @return The percentage error value
	 */
	public static double percentageError(double measured, double expected){
		return (Math.abs(measured - expected) / expected) * 100;
	}
	
	public static double eccentricity(double[] values){
		double max1 = Double.MIN_VALUE;
		double max2 = Double.MIN_VALUE;
		for(int i=0; i<values.length; i++){
			if(values[i] > max1){
				max1 = values[i];
			} 
		}
		for(int i=0; i<values.length; i++){
			if(values[i] > max2 && values[i] != max1){
				max2 = values[i];
			}
		}
		
		double std = standardDeviation(values);
		
		if(std != 0.0){
			return (max1 - max2) / std;
		} else {
			return 0.0;
		}
	}
	
	public static double standardDeviation(double[] values){
		return Math.sqrt(StatUtils.variance(values));
	}
	
	public static double variance(double[] values){
		return StatUtils.variance(values);
	}
	
	public static double mean(double[] values){
		return StatUtils.mean(values);
	}
	
	public static double percentile(double[] values, double percentile){
		return StatUtils.percentile(values, percentile);
	}
}
