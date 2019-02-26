package ca.inuktitutcomputing.core;

import java.util.ArrayList;
import java.util.Arrays;

import ca.nrc.datastructure.Pair;
import ca.nrc.datastructure.trie.TrieNode;

import org.apache.commons.lang.ArrayUtils;
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
				mostFrequentTerminalsForWord = getNMostFrequentTerminals(node,this.numberOfReformulations,word,new TrieNode[] {});
			logger.debug("mostFrequentTerminalsForWord: "+mostFrequentTerminalsForWord.length);
			QueryExpansion[] expansions = __getExpansions(mostFrequentTerminalsForWord, segments, word);
			logger.debug("expansions: "+expansions.length);
			return expansions;
			
//		} catch (Exception e) {
//			System.err.println(e.getMessage());
//			throw e;
//		}
	}
	
	public QueryExpansion[] __getExpansions(TrieNode[] mostFrequentTerminalsForReformulations, String[] segments, String word) {
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
					return __getExpansions(mostFrequentTerminalsForReformulations, segmentsBack1, word);
				logger.debug("node: "+node.getKeysAsString());
				TrieNode[] mostFrequentTerminalsForNode = getNMostFrequentTerminals(node,
					this.numberOfReformulations - mostFrequentTerminalsForReformulations.length,
					word,mostFrequentTerminalsForReformulations);
				ArrayList<TrieNode> newMostFrequentTerminalsForReformulationsAL = new ArrayList<TrieNode>();
				newMostFrequentTerminalsForReformulationsAL.addAll(Arrays.asList(mostFrequentTerminalsForReformulations));
				newMostFrequentTerminalsForReformulationsAL.addAll(Arrays.asList(mostFrequentTerminalsForNode));
				TrieNode[] newMostFrequentTerminalsForReformulations = (TrieNode[])newMostFrequentTerminalsForReformulationsAL.toArray(new TrieNode[] {});
				return __getExpansions(newMostFrequentTerminalsForReformulations, segmentsBack1, word);
			} else
				return __getExpansions(mostFrequentTerminalsForReformulations, segmentsBack1, word);
		}
	}
	
	public TrieNode[] getNMostFrequentTerminals(TrieNode node, int n, String word, TrieNode[] exclusions) {
		TrieNode[] terminals = node.getAllTerminals();
		for (TrieNode nodeToExclude : exclusions)
			terminals = (TrieNode[]) ArrayUtils.removeElement(terminals, nodeToExclude);
	    Arrays.sort(terminals, (TrieNode n1, TrieNode n2) -> {
	        	Long o1Freq = n1.getFrequency();
	        	Long o2Freq = n2.getFrequency();
	        	if (o1Freq == o2Freq) {
	        		String word1 = n1.getSurfaceForm();
	        		String word2 = n2.getSurfaceForm();
	        		int word1Length = word1.length();
	        		int word2Length = word2.length();
	        		int diff1WithWord = Math.abs(word1Length-word.length());
	        		int diff2WithWord = Math.abs(word2Length-word.length());
	        		if (diff1WithWord==diff2WithWord) {
	        			return  word1.compareTo(word2);
	        		}
	        		else {
	        			return diff1WithWord > diff2WithWord? 1 : -1;
	        		}
	        	} else
	        		return o1Freq < o2Freq? 1 : -1;
	        }
	    );
	    TrieNode[] mostFrequentTerminals;
	    if (n > terminals.length) {
	    	mostFrequentTerminals = Arrays.copyOfRange(terminals, 0, terminals.length);
	    } else {
	    	mostFrequentTerminals = Arrays.copyOfRange(terminals, 0, n);
	    }
		return mostFrequentTerminals;
	}


}
