package ca.nrc.datastructure.trie;

import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;

import ca.nrc.string.StringUtils;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.Asserter;

public class AssertTrie extends Asserter<Trie> {

	public AssertTrie(Trie _gotObject) {
		super(_gotObject);
	}

	public AssertTrie(Trie _gotObject, String mess) {
		super(_gotObject, mess);
	}

	public AssertTrie terminalsForNodeEqual(String[] segments, String[] expTerminals) 
				throws Exception {
		TrieNode[] allTerminalsNodes = trie().getTerminals(segments);
		Set<String> gotTerminals = new HashSet<String>();
		for (TrieNode node: allTerminalsNodes) {
			gotTerminals.add(node.surfaceForm);
		}
		
		String mess = 
			baseMessage+"\nTerminal nodes were not as expected for ";
		if (segments.length == 0) {
			mess += "root node";
		} else {
			mess += "node with segments: "+String.join(", ", segments);
		}
		
		AssertObject.assertDeepEquals(mess, expTerminals, gotTerminals); 
		
		return this;
	}
	
	public AssertTrie hasTerminals(String[] expTerminals) throws Exception {
		return terminalsForNodeEqual(new String[0], expTerminals);
	}
	
	public AssertTrie hasNbOccurences(long expNbOccurences) throws Exception {
		Assert.assertEquals(
			baseMessage+"\nTotal number of occurences was not as expected", 
			expNbOccurences, trie().getNbOccurrences());
		return this;
	}
	
	public AssertTrie frequencyEquals(String[] segments, long expFreq) 
			throws Exception{
		long gotFreq = trie().getFrequency(segments);
		Assert.assertEquals(
			baseMessage+"\nWrong frequency for segmens: "+
					String.join(", ", segments), 
			expFreq, gotFreq);
		
		return this;
	}
	
	protected Trie trie() {
		return gotObject;
	}

	public AssertTrie mostFrequentTerminalEquals(
		String[] segments, String expMostFrequent) throws Exception {
		if (segments == null) {
			segments = new String[0];
		}
		TrieNode gotMostFrequentNode = trie().getMostFrequentTerminal(segments);
		String gotKeys = String.join("", gotMostFrequentNode.keys);
		String gotMostFrequent = gotKeys.substring(0, gotKeys.length()-1);
		Assert.assertEquals(
			baseMessage+"\nMost frequent terminal not as expected for node: "+
					String.join(",", segments), 
			expMostFrequent, gotMostFrequent);
		return this;
	}

	public AssertTrie mostFrequentTerminalsEqual(
			int n, String[] expMostFrequentTerminals) throws Exception {
		return mostFrequentTerminalsEqual(n, null, expMostFrequentTerminals);
	}
	
	public AssertTrie mostFrequentTerminalsEqual(
			int n, String[] segments, String[] expMostFrequentTerminals) throws Exception {
		if (segments == null) {
			segments = new String[0];
		}
		TrieNode[] gotNodes = trie().getMostFrequentTerminals(n, segments);
		String[] gotMostFrequentTerminals = new String[gotNodes.length];
		for (int ii=0; ii < gotNodes.length; ii++) {
			gotMostFrequentTerminals[ii] = gotNodes[ii].keysAsString(true);
		}
		AssertObject.assertDeepEquals(
			baseMessage+"\n"+n+
				" most frequent terminals not as expected for node=["+
				String.join(",", segments)+"]", 
				expMostFrequentTerminals, gotMostFrequentTerminals);
		
		return this;
	}

	public void terminalsMatchingNGramEqual(String ngram, String[] expTerminals) 
			throws Exception {
		TrieNode[]  gotTerminalNodes = trie().getTerminalsMatchingNgram(ngram.split(""));
		String[] gotTerminals = nodes2stringkeys(gotTerminalNodes);
		AssertObject.assertDeepEquals(
			baseMessage+"\nTerminal nodes matching ngram "+ngram+
			" were not as expected", 
			expTerminals, gotTerminals);
	}
	
	protected String[] nodes2stringkeys(TrieNode[] nodes) {
		String[] stringkeys = new String[nodes.length];
		for (int ii=0; ii < nodes.length; ii++) {
			stringkeys[ii] = nodes[ii].keysAsString();
		}
		return stringkeys;
	}
}
