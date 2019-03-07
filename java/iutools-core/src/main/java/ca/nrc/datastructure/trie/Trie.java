package ca.nrc.datastructure.trie;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import ca.inuktitutcomputing.core.CompiledCorpus;
import ca.nrc.json.PrettyPrinter;


public class Trie {

    protected TrieNode root;
    // REMOVED: protected long size;
    
    
    public Trie() {
    	root = new TrieNode();
	}
    
    public String toJSON() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		return json;
    }
    
	public TrieNode getRoot() {
    	return this.root;
    }
    
    public long getSize() {
    	return getAllTerminals().length;
    }
    
    public long getNbOccurrences() {
    	TrieNode[] terminals = getAllTerminals();
    	long nbOccurrences = 0;
    	for (TrieNode terminal : terminals) {
    		nbOccurrences += terminal.getFrequency();
    	}
    	return nbOccurrences;
    }
    
    /**
     * Add an entry to the Trie.
     * 
     * @param segments
     * @param word
     * @return an object of class TrieNode
     * @throws TrieException
     */
    
    // If we assume that this method will not be called with an empty list of segments
    // (the test is done before calling this method),
    // then we can replace the commented lines with the lines with //***
	public TrieNode add(String[] segments, String word) throws TrieException {
        TrieNode trieNode = root;
//        if (trieNode == null)
//            throw new TrieException("Can't add to a null root.");
//        if (segments == null)
//            return null; // null means the segmenter was not able to segment a word
        
        Logger logger = Logger.getLogger("Trie.add");
        logger.debug("segments: "+Arrays.toString(segments));
        String terminalSegment = "\\";
        ArrayList<String> segmentList = new ArrayList<String>(Arrays.asList(segments));
        segmentList.add(terminalSegment);
        segments = segmentList.toArray(new String[] {});
        logger.debug("segments after adding \\: "+Arrays.toString(segments));
        int iseg = 0;
        while (iseg < segments.length) {
        	String segment = segments[iseg];
            Set<String> childrenKeys = trieNode.getChildren().keySet();
            TrieNode segmentNode = null;
            // if the current segment is not in the keys, add a new node for it
            if (!childrenKeys.contains(segment)) {
                segmentNode = insertNode(trieNode, segment); // where new child is added to the node
            } else {
            	segmentNode = getChild(trieNode, segment);
            }
			// if this is the last segment, indicate this is a word
//			if (iseg == segments.length - 1) {
//				segmentNode.incrementFrequency();
//				segmentNode.surfaceForm = word;
//				segmentNode.isWord = true;
//				return segmentNode;
//			}
            // current segment is in the keys, or it was not and has just been added 
			// and is not the last segment: 
            trieNode = segmentNode;
            trieNode.incrementFrequency();
            iseg++;
        }
        // last segment (\)  = terminal node
        trieNode.addSurfaceForm(word); //***
        trieNode.isWord = true; //***
        return trieNode; //***
//        return null;
		
	}

	public TrieNode getNode(String[] keys) {
        HashMap<String,TrieNode> children = root.getChildren();
        TrieNode trieNode = null;
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            if (children.containsKey(key)) {
                trieNode = (TrieNode) children.get(key);
                children = trieNode.getChildren();
            } else 
            	return null;
        }
        return trieNode;
	}
	
	public TrieNode getParentNode(TrieNode node) {
		return this.getParentNode(node.keys);
	}
	
	public TrieNode getParentNode(String[] keys) {
		if (keys.length==0)
			return null;
		else
			return this.getNode(Arrays.copyOfRange(keys, 0, keys.length-1));
	}
	
	public long getFrequency(String[] segments) {
		TrieNode node = this.getNode(segments);
		if (node != null)
			return node.getFrequency();
		else
			return 0;
	}
	
	// --- ALL TERMINALS
	public TrieNode[] getAllTerminals() {
		return root.getAllTerminals();
	}
	
	public TrieNode[] getAllTerminals(String[] segments) {
		TrieNode node = this.getNode(segments);
		return node.getAllTerminals();
	}
	
	// --- MOST FREQUENT TERMINALS
	public TrieNode getMostFrequentTerminal() {
		return root.getMostFrequentTerminal();
	}
	
	public TrieNode getMostFrequentTerminal(String[] segments) {
		return getNMostFrequentTerminals(segments,1)[0];
	}
	
	public TrieNode[] getNMostFrequentTerminals(int n) {
		return root.getMostFrequentTerminals(n);
	}
	
	public TrieNode[] getNMostFrequentTerminals(String[] segments, int n) {
		TrieNode node = this.getNode(segments);
		return node.getMostFrequentTerminals(n);
	}
	

	/*
	 * 
	 */
	public TrieNode getNReformulations(String[] segments) {
		TrieNode node = this.getNode(segments);
		if (node != null)
			return node;
		for (int subLength=segments.length-1; subLength>0; subLength--) {
			String[] segmentsButLast = Arrays.copyOfRange(segments, 0, subLength);
			node = this.getNode(segmentsButLast);
			if (node != null)
				return node.getMostFrequentTerminal();
		}
		return null;
	}


	/**
	 * 
	 * @param String rootKey
	 * @return String[] space-separated keys of the most frequent sequence of morphemes following rootSegment
	 */
	public String[] getMostFrequentSequenceForRoot(String rootKey) {
		Logger logger = Logger.getLogger("CompiledCorpus.getMostFrequentSequenceToTerminals");
		HashMap<String, Long> freqs = new HashMap<String, Long>();
		TrieNode rootSegmentNode = this.getNode(new String[] {rootKey});
		TrieNode[] terminals = rootSegmentNode.getAllTerminals();
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
	
	private HashMap<String, Long> computeFreqs(String[] terminalNodeKeys, HashMap<String, Long> freqs, String rootSegment) {
		return _computeFreqs("",terminalNodeKeys,freqs,rootSegment);
	}

	private HashMap<String, Long> _computeFreqs(String cumulativeKeys, String[] terminalNodeKeys, HashMap<String, Long> freqs, String rootSegment) {
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

	public TrieNode getMostFrequentTerminalFromMostFrequentSequenceForRoot(String rootSegment) {
		String[] mostFrequentSequence = getMostFrequentSequenceForRoot(rootSegment);
		TrieNode node = this.getNode(mostFrequentSequence);
		TrieNode[] terminals = node.getAllTerminals();
		long max = 0;
		TrieNode mostFrequentTerminal = null;
		for (TrieNode terminal : terminals)
			if (terminal.getFrequency() > max) {
				max = terminal.getFrequency();
				mostFrequentTerminal = terminal;
			}
		return mostFrequentTerminal;
	}


	
	// --------------------- PRIVATE------------------------------

    private TrieNode getChild(TrieNode trieNode, String segment) {
        return (TrieNode) trieNode.getChildren().get(segment);
    }

    private TrieNode insertNode(TrieNode trieNode, String segment) {
      ArrayList<String> keys = new ArrayList<String>(Arrays.asList(trieNode.keys));
      keys.add(segment);
      TrieNode newNode = new TrieNode(keys.toArray(new String[] {}));
      trieNode.addChild(segment, newNode);
      return newNode;
    }
    

}

class NodeFrequencyComparator implements Comparator<TrieNode> {

	@Override
	public int compare(TrieNode o1, TrieNode o2) {
		long o1Freq = o1.getFrequency();
		long o2Freq = o2.getFrequency();
		if ( o1Freq==o2Freq)
			return 0;
		else
			return o1Freq<o2Freq? 1:-1;
	}
	
}
