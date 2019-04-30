package ca.inuktitutcomputing.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import ca.inuktitutcomputing.script.TransCoder;
import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.Pair;
import ca.nrc.datastructure.trie.TrieNode;

import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

public class QueryExpander {
	
	public CompiledCorpus compiledCorpus;
	public int numberOfReformulations = 5;

	
	public QueryExpander() throws QueryExpanderException {
		initialize(null);
	}

	public QueryExpander(CompiledCorpus _compiledCorpus) throws QueryExpanderException {
		this.compiledCorpus = _compiledCorpus;
		initialize(_compiledCorpus);
	}
	
	private void initialize(CompiledCorpus _compiledCorpus) throws QueryExpanderException {
		if (_compiledCorpus == null) {
			try {
				_compiledCorpus = CompiledCorpusRegistry.getCorpus();
			} catch (ConfigException | CompiledCorpusRegistryException e) {
				throw new QueryExpanderException("Problem creating a QueryExpander with default pre-compiled corpus", e);
			}
		}
		this.compiledCorpus = _compiledCorpus;
	}
	
	/**
	 * 
	 * @param word String - an inuktitut word
	 * @return String[] An array of the most frequent inuktitut words related to the input word
	 * @throws Exception
	 */
	public QueryExpansion[] getExpansions(String word)  {
    	Logger logger = Logger.getLogger("QueryExpander.getExpansions");
		logger.debug("word: "+word);
		QueryExpansion[] expansionsArr = new QueryExpansion[] {};
				
		String[] segments;
		ArrayList<QueryExpansion> mostFrequentTerminalsForWord;
		
		try {
			segments = this.compiledCorpus.getSegmenter().segment(word);
		} catch (Exception e) {
			segments = null;
		}
		
		if (segments !=null && segments.length >0) {
			logger.debug("segments: "+segments.length);
			TrieNode node = this.compiledCorpus.trie.getNode(segments);	
			if (node==null)
				mostFrequentTerminalsForWord = new ArrayList<QueryExpansion>();
			else
				mostFrequentTerminalsForWord = getNMostFrequentForms(node,this.numberOfReformulations,word,new ArrayList<QueryExpansion>());
			logger.debug("mostFrequentTerminalsForWord: "+mostFrequentTerminalsForWord.size());
			ArrayList<QueryExpansion> expansions = __getExpansions(mostFrequentTerminalsForWord, segments, word);
			logger.debug("expansions: "+expansions.size());
			
			expansions = possiblyConvertToSyllabic(word, expansions);
			
			expansionsArr =  expansions.toArray(new QueryExpansion[] {});
		}
		
		return expansionsArr;
	}
	
	private ArrayList<QueryExpansion> possiblyConvertToSyllabic(String word, ArrayList<QueryExpansion> expansions) {
		boolean inputIsLatin = Pattern.compile("[a-zA-Z]").matcher(word).find();
		if (!inputIsLatin) {
			for (int ii=0; ii < expansions.size(); ii++) {
				expansions.get(ii).word = TransCoder.romanToUnicode(expansions.get(ii).word);
			}
			
		}
		return expansions;
	}
	
	
	public ArrayList<QueryExpansion> __getExpansions(ArrayList<QueryExpansion> mostFrequentTerminalsForReformulations, String[] segments, String word) {
		Logger logger = Logger.getLogger("QueryExpander.__getExpansions");
		logger.debug("nb. segments : "+segments.length);
		logger.debug("nb. most frequent : "+mostFrequentTerminalsForReformulations.size());
		
		if (segments.length == 0 || mostFrequentTerminalsForReformulations.size() == this.numberOfReformulations) {
			return mostFrequentTerminalsForReformulations;
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
				ArrayList<QueryExpansion> mostFrequentTerminalsForNode = getNMostFrequentForms(node,
					this.numberOfReformulations - mostFrequentTerminalsForReformulations.size(),
					word, mostFrequentTerminalsForReformulations);
				
				ArrayList<QueryExpansion> newMostFrequentTerminalsForReformulations = new ArrayList<QueryExpansion>();
				newMostFrequentTerminalsForReformulations.addAll(mostFrequentTerminalsForReformulations);
				newMostFrequentTerminalsForReformulations.addAll(mostFrequentTerminalsForNode);
				return __getExpansions(newMostFrequentTerminalsForReformulations, segmentsBack1, word);
			} else
				return __getExpansions(mostFrequentTerminalsForReformulations, segmentsBack1, word);
		}
	}
	
	public ArrayList<QueryExpansion> getNMostFrequentForms(TrieNode node, int n, String word, ArrayList<QueryExpansion> exclusions) {
		Logger logger = Logger.getLogger("QueryExpander.getNMostFrequentForms");
		ArrayList<String> listOfExclusions = new ArrayList<String>();
		for (int i=0; i<exclusions.size(); i++)
			listOfExclusions.add(exclusions.get(i).word);
		TrieNode[] terminals = node.getAllTerminals();
		ArrayList<Object[]> forms= new ArrayList<Object[]>();
		for (TrieNode terminal : terminals) {
			HashMap<String,Long> surfaceForms = terminal.getSurfaceForms();
			if (surfaceForms.size()==0) {
				surfaceForms = new HashMap<String,Long>();
				surfaceForms.put(terminal.getSurfaceForm(), new Long(terminal.getFrequency()));
			}
			for (String surfaceForm : surfaceForms.keySet().toArray(new String[] {}))
				if ( !listOfExclusions.contains(surfaceForm))
					forms.add(new Object[] {surfaceForm,surfaceForms.get(surfaceForm),terminal.keys});
		}
		Object[][] listForms = forms.toArray(new Object[][] {});
		for (int i=0; i<listForms.length; i++)
			logger.debug(listForms[i][0]+" ("+listForms[i][1]+")");
	    Arrays.sort(listForms, (Object[] o1, Object[] o2) -> {
    			String word1 = (String)o1[0];
    			String word2 = (String)o2[0];
	        	Long o1Freq = (Long)o1[1];
	        	Long o2Freq = (Long)o2[1];
	        	if (o1Freq.compareTo(o2Freq)==0) {
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
	        		return o2Freq.compareTo(o1Freq);
	        }
	    );
	    ArrayList<QueryExpansion> mostFrequentForms = new ArrayList<QueryExpansion>();
	    int max = listForms.length>n? n : listForms.length;
	    for (int i=0; i<max; i++) {
	    	// TODO: morphemes
	    	String surfaceForm = (String)listForms[i][0];
	    	String[] morphemes = (String[])listForms[i][2];
	    	long frequency = ((Long)listForms[i][1]).longValue();
	    	mostFrequentForms.add(new QueryExpansion(surfaceForm,morphemes,frequency));
	    }
		return mostFrequentForms;
	}


}
