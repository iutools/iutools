package ca.nrc.datastructure.trie;

public abstract class Trie_Base {

    public abstract long getNbOccurrences();

    public abstract long getSize();

	public abstract TrieNode_InMemory getNode(String[] keys);
	
	public abstract TrieNode_InMemory add(String[] partsSequence, String word) 
		throws TrieException;
	
	public abstract TrieNode_InMemory[] getAllTerminals(String[] segments);
	
	public abstract TrieNode_InMemory getRoot();
	
	public abstract String toJSON();
}
