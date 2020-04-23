package ca.inuktitutcomputing.morph;

public class MorphInukException extends Exception {
	
	public MorphInukException(Exception e) { super(e); }

	public MorphInukException(String mess) { super (mess); }

	public MorphInukException(String mess, Exception e) { super(mess, e); }

}
