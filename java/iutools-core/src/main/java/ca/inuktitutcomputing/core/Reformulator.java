package ca.inuktitutcomputing.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Vector;

import ca.nrc.datastructure.trie.TrieNode;

public class Reformulator {
	
	public CompiledCorpus compiledCorpus;
	public int numberOfReformulations = 5;
	
	public Reformulator(CompiledCorpus _compiledCorpus) {
		this.compiledCorpus = _compiledCorpus;
	}
	
	/**
	 * 
	 * @param word String - an inuktitut word
	 * @return String[] An array of the most frequent inuktitut words related to the input word
	 * @throws Exception
	 */
	public String[] getReformulations(String word) throws Exception {
		String[] segments;
		try {
			TrieNode[] mostFrequentTerminalsForWord;
			segments = this.compiledCorpus.getSegmenter().segment(word);
			if (segments==null)
				return null;
			
			TrieNode node = this.compiledCorpus.trie.getNode(segments);			
			if (node==null)
				mostFrequentTerminalsForWord = new TrieNode[] {};
			else
				mostFrequentTerminalsForWord = node.getMostFrequentTerminals(this.numberOfReformulations);
			
			return __getReformulations(mostFrequentTerminalsForWord, segments);
			
		} catch (Exception e) {
			return null;
		}
	}
	
	public String[] __getReformulations(TrieNode[] mostFrequentTerminalsForReformulations, String[] segments) {
		if (segments.length == 0 || mostFrequentTerminalsForReformulations.length == this.numberOfReformulations) {
			String[] mostFrequentReformulations = new String[mostFrequentTerminalsForReformulations.length];
			for (int i=0; i<mostFrequentTerminalsForReformulations.length; i++)
				mostFrequentReformulations[i] = mostFrequentTerminalsForReformulations[i].getSurfaceForm();
			return mostFrequentReformulations;
		}
		else {
			// back one node
			segments = Arrays.copyOfRange(segments,0,segments.length-1);
			if (segments.length != 0) {
				TrieNode node = this.compiledCorpus.trie.getNode(segments);
				TrieNode[] mostFrequentTerminalsForNode = node.getMostFrequentTerminals(
					this.numberOfReformulations - mostFrequentTerminalsForReformulations.length,
					mostFrequentTerminalsForReformulations);
				ArrayList<TrieNode> newMostFrequentTerminalsForReformulationsAL = new ArrayList<TrieNode>();
				newMostFrequentTerminalsForReformulationsAL.addAll(Arrays.asList(mostFrequentTerminalsForReformulations));
				newMostFrequentTerminalsForReformulationsAL.addAll(Arrays.asList(mostFrequentTerminalsForNode));
				TrieNode[] newMostFrequentTerminalsForReformulations = (TrieNode[])newMostFrequentTerminalsForReformulationsAL.toArray(new TrieNode[] {});
				return __getReformulations(newMostFrequentTerminalsForReformulations, segments);
			} else
				return __getReformulations(mostFrequentTerminalsForReformulations, segments);
		}
	}

}
