package org.iutools.edit_distance;

public class EditDistanceCalculatorException extends Exception {

	public EditDistanceCalculatorException(String mess, Exception e) {super(mess, e);}
	public EditDistanceCalculatorException(Exception e) {super(e);}
	public EditDistanceCalculatorException(String mess) {super(mess);}
}
