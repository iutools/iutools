package ca.nrc.datastructure.trie;

public class StringSegmenterException extends Exception {
	
	public StringSegmenterException(Exception e) { super(e); }

	public StringSegmenterException(String mess) { super (mess); }

	public StringSegmenterException(String mess, Exception e) { super(mess, e); }

}
