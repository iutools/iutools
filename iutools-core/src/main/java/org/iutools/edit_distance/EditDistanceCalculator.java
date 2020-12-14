package org.iutools.edit_distance;

public interface EditDistanceCalculator {
	
	public double distance(String s1, String s2) throws EditDistanceCalculatorException;

}
