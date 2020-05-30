package ca.nrc.datastructure.trie;

import java.util.Arrays;
import java.util.Comparator;

import org.apache.commons.lang3.ArrayUtils;

public abstract class Trie_Base {

	/////////////////////////////////
	// Method which are DEFINITELY being called
	// from outside of the Trie class
	/////////////////////////////////
	
    public abstract long getNbOccurrences();

    public abstract long getSize();

	public abstract TrieNode getNode(String[] keys);
	
	public abstract TrieNode add(String[] partsSequence, String word) 
		throws TrieException;

	// TODO: Can it be made concrete?
	public abstract TrieNode[] getAllTerminals();
	
	// TODO: Can it be made concrete?
	public abstract TrieNode[] getAllTerminals(String[] segments);
	
	// TODO: Can it be made concrete?
	//   Would have to make collectAllTerminals be an abstract method
	//
	public abstract TrieNode[] getAllTerminals(TrieNode node);
	
	// TODO: Can it be made concrete?
	public abstract TrieNode getMostFrequentTerminal();
	
	// TODO: Can it be made concrete?
	public abstract TrieNode getMostFrequentTerminal(TrieNode node);

	// TODO: Can it be made concrete?
	public abstract TrieNode getMostFrequentTerminal(String[] segments);
	
	// TODO: Can it be made concrete?
	public abstract TrieNode[] getMostFrequentTerminals(int n);

	// TODO: Can it be made concrete?	
	public abstract TrieNode[] getMostFrequentTerminals(int n, String[] segments);
	
	// TODO: Can it be made concrete?	
	public abstract TrieNode[] getMostFrequentTerminals(String[] segments);

	// TODO: Can it be made concrete?	
	public abstract TrieNode[] getMostFrequentTerminals();
	
	// TODO: Can it be made concrete?	
	public abstract TrieNode[] getMostFrequentTerminals(
			Integer n, TrieNode node);
	
	// TODO: Can it be made concrete?	
	public abstract TrieNode[] getMostFrequentTerminals(
			Integer n, TrieNode node, 
			TrieNode[] exclusions);
	
	// TODO: Can it be made concrete?	
	public abstract String[] getMostFrequentSequenceForRoot(String rootKey);
	
		
	public abstract TrieNode getRoot();
	
	public abstract String toJSON();
	
	/////////////////////////////////
	// PROTECTED OR PRIVATE METHODS
	// Can any of those be made concrete??
	/////////////////////////////////
	
	// TODO: Can this be made concrete?
	protected abstract TrieNode getMostFrequentTerminalFromMostFrequentSequenceForRoot(String rootSegment);
		
	protected abstract TrieNode getParentNode(TrieNode node);
	
	protected abstract TrieNode getParentNode(String[] keys);
	
	public abstract long getFrequency(String[] segments);
	
}
