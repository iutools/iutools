package ca.inuktitutcomputing.core;

import java.util.ArrayList;
import java.util.Arrays;

import ca.nrc.datastructure.trie.TrieNode;

import org.apache.log4j.Logger;

public class QueryExpander {
	
	public CompiledCorpus compiledCorpus;
	public int numberOfReformulations = 5;
	
	public QueryExpander(CompiledCorpus _compiledCorpus) {
		this.compiledCorpus = _compiledCorpus;
	}
	
	/**
	 * 
	 * @param word String - an inuktitut word
	 * @return String[] An array of the most frequent inuktitut words related to the input word
	 * @throws Exception
	 */
	public QueryExpansion[] getExpansions(String word) throws Exception {
    	Logger logger = Logger.getLogger("QueryExpander.getExpansions");
		logger.debug("word: "+word);
		String[] segments;
//		try {
			TrieNode[] mostFrequentTerminalsForWord;
			try {
				segments = this.compiledCorpus.getSegmenter().segment(word);
			} catch (Exception e) {
				segments = null;
			}
			if (segments==null || segments.length==0) {
				logger.debug("NULL");
				return null;
			}
			logger.debug("segments: "+segments.length);
			TrieNode node = this.compiledCorpus.trie.getNode(segments);	
			if (node==null)
				mostFrequentTerminalsForWord = new TrieNode[] {};
			else
				mostFrequentTerminalsForWord = node.getMostFrequentTerminals(this.numberOfReformulations);
			logger.debug("mostFrequentTerminalsForWord: "+mostFrequentTerminalsForWord.length);
			QueryExpansion[] expansions = __getExpansions(mostFrequentTerminalsForWord, segments);
			logger.debug("expansions: "+expansions.length);
			return expansions;
			
//		} catch (Exception e) {
//			System.err.println(e.getMessage());
//			throw e;
//		}
	}
	
	public QueryExpansion[] __getExpansions(TrieNode[] mostFrequentTerminalsForReformulations, String[] segments) {
		Logger logger = Logger.getLogger("QueryExpander.__getExpansions");
		logger.debug("nb. segments : "+segments.length);
		logger.debug("nb. most frequent : "+mostFrequentTerminalsForReformulations.length);
		
		if (segments.length == 0 || mostFrequentTerminalsForReformulations.length == this.numberOfReformulations) {
			QueryExpansion[] expansions = new QueryExpansion[mostFrequentTerminalsForReformulations.length];
			for (int i=0; i<mostFrequentTerminalsForReformulations.length; i++) {
				QueryExpansion qexp = new QueryExpansion(
						mostFrequentTerminalsForReformulations[i].getSurfaceForm(),
						mostFrequentTerminalsForReformulations[i].keys,
						mostFrequentTerminalsForReformulations[i].getFrequency());
				expansions[i] = qexp;
			}
			return expansions;
		}
		else {
			// back one node
			String[] segmentsBack1 = Arrays.copyOfRange(segments,0,segments.length-1);
			if (segmentsBack1.length != 0) {
				logger.debug("back one segment -- "+String.join(" ", segmentsBack1));
				TrieNode node = this.compiledCorpus.trie.getNode(segmentsBack1);
				if (node==null)
					return __getExpansions(mostFrequentTerminalsForReformulations, segmentsBack1);
				logger.debug("node: "+node.getKeysAsString());
				TrieNode[] mostFrequentTerminalsForNode = node.getMostFrequentTerminals(
					this.numberOfReformulations - mostFrequentTerminalsForReformulations.length,
					mostFrequentTerminalsForReformulations);
				ArrayList<TrieNode> newMostFrequentTerminalsForReformulationsAL = new ArrayList<TrieNode>();
				newMostFrequentTerminalsForReformulationsAL.addAll(Arrays.asList(mostFrequentTerminalsForReformulations));
				newMostFrequentTerminalsForReformulationsAL.addAll(Arrays.asList(mostFrequentTerminalsForNode));
				TrieNode[] newMostFrequentTerminalsForReformulations = (TrieNode[])newMostFrequentTerminalsForReformulationsAL.toArray(new TrieNode[] {});
				return __getExpansions(newMostFrequentTerminalsForReformulations, segmentsBack1);
			} else
				return __getExpansions(mostFrequentTerminalsForReformulations, segmentsBack1);
		}
	}

}
