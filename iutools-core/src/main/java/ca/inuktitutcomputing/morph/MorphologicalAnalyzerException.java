package ca.inuktitutcomputing.morph;

public class MorphologicalAnalyzerException extends Exception {
	
	public MorphologicalAnalyzerException(Exception e) { super(e); }

	public MorphologicalAnalyzerException(String mess) { super (mess); }

	public MorphologicalAnalyzerException(String mess, Exception e) { super(mess, e); }

}
