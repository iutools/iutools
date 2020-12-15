package org.iutools.datastructure.trie;

public class RW_TrieNodeException extends Exception {
	public RW_TrieNodeException(String mess, Exception e) {
		super(mess, e);
	}
	public RW_TrieNodeException(String mess) {
		super(mess);
	}
	public RW_TrieNodeException(Exception e) {
		super(e);
	}
}
