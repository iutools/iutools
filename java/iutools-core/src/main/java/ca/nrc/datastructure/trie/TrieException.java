package ca.nrc.datastructure.trie;

public class TrieException extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public TrieException(String mess, Exception exc) {
		super(mess, exc);
//		super(mess+"; "+exc.getMessage());
	}

	public TrieException(String mess) {
		super(mess);
	}
}
