package ca.nrc.datastructure.trie;

import java.util.Map;

public abstract class TrieNode_Base {
	
    public String[] keys = new String[] {};
    protected boolean isWord = false;
    protected long frequency = 0;
    
    public abstract void addChild(String key, TrieNode_Base node);
	
	public abstract Map<String,TrieNode_Base> getChildren();

	public abstract String toString();
	
	public abstract TrieNode_Base getMostFrequentTerminal();
	
	// TODO: Could it be made concrete?
	public abstract TrieNode_InMemory[] getMostFrequentTerminals(int n);	
		
	public abstract TrieNode_Base[] getAllTerminals();
	
	public abstract void addSurfaceForm(String form);
	
	// TODO: Could it be made concrete?
	public abstract long getFrequency();	
	
	// TODO: Could it be made concrete?
	public abstract void incrementFrequency();
}
