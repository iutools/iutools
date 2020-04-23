package ca.nrc.datastructure.trie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import ca.nrc.json.PrettyPrinter;

/**
 * Base class for a Trie.
 *
 * This is a data structure that indexes sequences of strings -- referred to as 
 * "segments", seen in a corpus of text.
 *  
 * The segments can be:
 * - tokens in text (word, word separator, etc...)
 * - charaters in a word
 * - a list of morphemes in a word
 * - anyting you want that can be represented as a sequence of strings
 * 
 * @author desilets
 *
 */
public abstract class Trie_Base {
    
    /** Root of the Trie */
	public abstract TrieNode getRoot();
	
    /**
     * Add an entry to the Trie.
     * 
     * @param segments
     * @param word
     * @return Node generated for this sequence of segments
     * @throws TrieException
     */
	public abstract TrieNode add(String[] segments, String word) 
			throws TrieException;
	
	
	/** Total number of nodes in the Trie */
    public abstract long getSize();
    
    /** Not sure how that differs from getSize(). 
     *  Maybe it's the number of terminal nodes?
     *  Or maybe its the total frequency of terminal nodes
     *  seen in a corpus?
     * @return
     */
    public abstract long getNbOccurrences();
    
    /** Frequency of a sequence of segments. */
	public abstract long getFrequency(String[] segments);
    
    /** Node for a sequence of segments */
	public abstract TrieNode getNode(String[] keys);

	// TODO: This should really be an iterator, because the 
	//   array of terminals may be very large.
	
	/** Array of all terminal nodes. */
	public abstract TrieNode[] getAllTerminals();

	// TODO: This should really be an iterator, because the 
	//   array of terminals may be very large.
	
	/** Array of all terminal nodes that start with a sequence of segments. */
	public abstract TrieNode[] getAllTerminals(String[] segments);
	
	/** Most frequent node that starts with a sequence of segments */
	public abstract TrieNode getMostFrequentTerminal(String[] segments);
	
	protected abstract TrieNode getMostFrequentTerminal();	
	
	protected abstract TrieNode[] getNMostFrequentTerminals(String[] split, int i);
	
	protected abstract TrieNode[] getNMostFrequentTerminals(int i);
	
	protected abstract TrieNode getMostFrequentTerminalFromMostFrequentSequenceForRoot(
									String string);

	/**
	 * 
	 * @param String rootKey
	 * @return String[] space-separated keys of the most frequent sequence of morphemes following rootSegment
	 */
	public abstract String[] getMostFrequentSequenceForRoot(String rootKey);

	protected abstract TrieNode getParentNode(String[] strings);

	protected abstract TrieNode getParentNode(TrieNode parent);
	
    public String toJSON() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		return json;
    }



}
