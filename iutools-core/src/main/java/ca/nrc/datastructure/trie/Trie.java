package ca.nrc.datastructure.trie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

import ca.nrc.json.PrettyPrinter;

public abstract class Trie {
	
    protected TrieNode root = new TrieNode();
	
	public abstract TrieNode getNode(String[] keys) throws TrieException;
	
	public abstract TrieNode add(String[] partsSequence, String word) 
		throws TrieException;
	
	protected abstract void collectAllTerminals(TrieNode node, 
			List<TrieNode> collected); 
		
	public TrieNode getRoot() {
    	return this.root;
    }
	
    public long getSize() {
    	return getAllTerminals().length;
    }
    
	public TrieNode getNode(List<String> keys) throws TrieException {
		return getNode(keys.toArray(new String[keys.size()]));
	}

	public TrieNode[] getAllTerminals() {
		TrieNode[] allTerminals = getAllTerminals(root);
		return allTerminals;
	}
	
	public TrieNode[] getAllTerminals(String[] segments) throws TrieException {
		TrieNode node = this.getNode(segments);
		TrieNode[] allTerminals = null;
		if (node==null)
			allTerminals = new TrieNode[0];
		else
			allTerminals = getAllTerminals(node);
		
		return allTerminals;
	}
	
	public TrieNode[] getAllTerminals(TrieNode node) {
		List<TrieNode> allTerminalsLst = 
			new ArrayList<TrieNode>();
			
		collectAllTerminals(node, allTerminalsLst);
		
		return allTerminalsLst.toArray(new TrieNode[allTerminalsLst.size()]);
	}
	
        
    public long getNbOccurrences() {
    	TrieNode[] terminals = getAllTerminals();
    	long nbOccurrences = 0;
    	for (TrieNode terminal : terminals) {
    		nbOccurrences += terminal.getFrequency();
    	}
    	return nbOccurrences;
    }
    
	public TrieNode getMostFrequentTerminal() {
		return getMostFrequentTerminal(root);
	}
    
	public TrieNode getMostFrequentTerminal(TrieNode node) {
		TrieNode mostFrequent = null;
		TrieNode[] terminals = getMostFrequentTerminals(1, node, null);
		if (terminals != null && terminals.length > 0) {
			mostFrequent = terminals[0];
		}
		return mostFrequent;
	}
	
	public TrieNode getMostFrequentTerminal(String[] segments) throws TrieException {
		TrieNode node = getNode(segments);
		return getMostFrequentTerminal(node);
	}
	
	public TrieNode[] getMostFrequentTerminals(int n) {
		return getMostFrequentTerminals(n, root, null);
	}	
	
	public TrieNode[] getMostFrequentTerminals(int n, String[] segments) throws TrieException {
		TrieNode node = getNode(segments);
		return getMostFrequentTerminals(n, node, null);
	}
	
	public TrieNode[] getMostFrequentTerminals(String[] segments) throws TrieException {
		TrieNode node = getNode(segments);
		return getMostFrequentTerminals(null, node, null);
	}

	public TrieNode[] getMostFrequentTerminals() {
		return getMostFrequentTerminals(null, root, null);
	}
	
	public TrieNode[] getMostFrequentTerminals(
			Integer n, TrieNode node) {
		return getMostFrequentTerminals(n, node, null);
	}

	public TrieNode[] getMostFrequentTerminals(
			Integer n, TrieNode node, 
			TrieNode[] exclusions) {
		if (exclusions == null) {
			exclusions = new TrieNode[0];
		}
		TrieNode[] terminals = getAllTerminals(node);
		for (TrieNode nodeToExclude : exclusions)
			terminals = (TrieNode[]) ArrayUtils.removeElement(terminals, nodeToExclude);
	    Arrays.sort(terminals, new Comparator<TrieNode>() {
	        @Override
	        public int compare(TrieNode o1, TrieNode o2) {
	        	if (o1.getFrequency() == o2.getFrequency())
	        		return 0;
	            return o1.getFrequency() < o2.getFrequency()? 1 : -1;
	        }
	    });
	    TrieNode[] mostFrequentTerminals;
	    if (n > terminals.length) {
	    	mostFrequentTerminals = Arrays.copyOfRange(terminals, 0, terminals.length);
	    } else {
	    	mostFrequentTerminals = Arrays.copyOfRange(terminals, 0, n);
	    }
		return mostFrequentTerminals;
	}	
	
	protected TrieNode getMostFrequentTerminalFromMostFrequentSequenceForRoot(String rootSegment) throws TrieException {
		String[] mostFrequentSequence = getMostFrequentSequenceForRoot(rootSegment);
		TrieNode node = this.getNode(mostFrequentSequence);
		TrieNode[] terminals = getAllTerminals(node);
		long max = 0;
		TrieNode mostFrequentTerminal = null;
		for (TrieNode terminal : terminals)
			if (terminal.getFrequency() > max) {
				max = terminal.getFrequency();
				mostFrequentTerminal = terminal;
			}
		return mostFrequentTerminal;
	}
	
    
	protected TrieNode getParentNode(TrieNode node) throws TrieException {
		return this.getParentNode(node.keys);
	}    
	
	protected TrieNode getParentNode(String[] keys) throws TrieException {
		if (keys.length==0)
			return null;
		else
			return this.getNode(Arrays.copyOfRange(keys, 0, keys.length-1));
	}
	
	public long getFrequency(String[] segments) throws TrieException {
		TrieNode node = this.getNode(segments);
		if (node != null)
			return node.getFrequency();
		else
			return 0;
	}
    
    public String toJSON() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		return json;
    }
    
	/**
	 * 
	 * @param String rootKey
	 * @return String[] space-separated keys of the most frequent sequence of morphemes following rootSegment
	 * @throws TrieException 
	 */
	public String[] getMostFrequentSequenceForRoot(String rootKey) throws TrieException {
		Logger logger = Logger.getLogger("CompiledCorpus.getMostFrequentSequenceToTerminals");
		HashMap<String, Long> freqs = new HashMap<String, Long>();
		TrieNode rootSegmentNode = this.getNode(new String[] {rootKey});
		TrieNode[] terminals = getAllTerminals(rootSegmentNode);
		logger.debug("all terminals: "+terminals.length);
		for (TrieNode terminalNode : terminals) {
			//logger.debug("terminalNode: "+PrettyPrinter.print(terminalNode));
			String[] terminalNodeKeys = Arrays.copyOfRange(terminalNode.keys, 1, terminalNode.keys.length);
			freqs = computeFreqs(terminalNodeKeys,freqs,rootKey);
		}
		logger.debug("freqs: "+PrettyPrinter.print(freqs));
		long maxFreq = 0;
		int minLength = 1000;
		String seq = null;
		String[] freqsKeys = freqs.keySet().toArray(new String[] {});
		for (int i=0; i<freqsKeys.length; i++) {
			String freqKey = freqsKeys[i];
			int nbKeys = freqKey.split(" ").length;
			if (freqs.get(freqKey)==maxFreq) {
				if (nbKeys<minLength) {
					maxFreq = freqs.get(freqKey);
					minLength = nbKeys;
					seq = freqKey;
				} 
			} else if (freqs.get(freqKey) > maxFreq) {
				maxFreq = freqs.get(freqKey);
				minLength = nbKeys;
				seq = freqKey;
			}
		}
		return (rootKey+" "+seq).split(" ");
	}    
    
	private HashMap<String, Long> computeFreqs(String[] terminalNodeKeys, HashMap<String, Long> freqs, String rootSegment) throws TrieException {
		return _computeFreqs("",terminalNodeKeys,freqs,rootSegment);
	}

	private HashMap<String, Long> _computeFreqs(String cumulativeKeys, String[] terminalNodeKeys, HashMap<String, Long> freqs, String rootSegment) throws TrieException {
		Logger logger = Logger.getLogger("CompiledCorpus._computeFreqs");
		if (terminalNodeKeys.length==0)
			return freqs;
		logger.debug("cumulativeKeys: '"+cumulativeKeys+"'");
		logger.debug("terminalNodeKeys: '"+String.join("", terminalNodeKeys)+"'\n");
		String key = terminalNodeKeys[0];
		String newCumulativeKeys = (cumulativeKeys + " " + key).trim();
		String[] remKeys = Arrays.copyOfRange(terminalNodeKeys, 1, terminalNodeKeys.length);
		// node of rootSegment + newCumulativeKeys
		TrieNode node = this.getNode((rootSegment+" "+newCumulativeKeys).split(" "));
		long incr = node.getFrequency();
		if (!freqs.containsKey(newCumulativeKeys))
			freqs.put(newCumulativeKeys, new Long(incr));
		//else {
		//	freqs.put(newCumulativeKeys, new Long(freqs.get(newCumulativeKeys).longValue() + incr));
		//}
		freqs = _computeFreqs(newCumulativeKeys, remKeys, freqs, rootSegment);
		return freqs;
	}
}
