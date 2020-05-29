package ca.nrc.datastructure.trie;

public abstract class Trie_Base {

    public abstract long getNbOccurrences();

    public abstract long getSize();

	public abstract TrieNode getNode(String[] keys);
	
	public abstract TrieNode add(String[] partsSequence, String word) 
		throws TrieException;
	
	public abstract TrieNode[] getAllTerminals(String[] segments);
	
	public abstract TrieNode getRoot();
	
	public abstract String toJSON();
}
