package ca.nrc.datastructure.trie;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import com.google.gson.Gson;

import ca.nrc.json.PrettyPrinter;

// TODO: Maybe the polymorphism should be in the TrieNode class, NOT in the 
//   Trie class?
//
// In other words, make Trie be a generic class...
//
//   public class Trie<TrieNode> {
//
// and TrieNode will be an abstract class with methods that allow you to 
// get the children, parents etc... from an in memory versus File System
//


public class Trie_InMemory extends Trie_Base {

    protected TrieNode root;
    
    public Trie_InMemory() {
    	root = new TrieNode();
	}
    
    public TrieNode newNode(String[] keys, Boolean isWord) {
    	return new TrieNode(keys, isWord);
    }
    
    public TrieNode newNode() {
    	return newNode(null, null);
    }
    
    public TrieNode newNode(String[] keys) {
    	return newNode(keys, null);
    }
    
    public String toJSON() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		return json;
    }
    
    public static Trie_InMemory fromJSON(String filePath) throws TrieException {
		FileReader jsonFileReader;
		try {
			jsonFileReader = new FileReader(filePath);
		} catch (FileNotFoundException e) {
			throw new TrieException(e);
		}
		Gson gson = new Gson();
		Trie_InMemory trie = gson.fromJson(jsonFileReader, Trie_InMemory.class);
	
		return trie;
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
     * @param partsSequence
     * @param word
     * @return an object of class TrieNode
     * @throws TrieException
     */
    
    // If we assume that this method will not be called with an empty list of segments
    // (the test is done before calling this method),
    // then we can replace the commented lines with the lines with //***
    //
    //
    // TODO: Check that partsSequence is NOT empty. If it is, raise exception
    //
	public TrieNode add(String[] partsSequence, String word) throws TrieException {
        TrieNode trieNode = root;
        Logger logger = Logger.getLogger("Trie.add");
        logger.debug("segments: "+Arrays.toString(partsSequence));
        
        String terminalSegment = "\\";
        ArrayList<String> segmentList = new ArrayList<String>(Arrays.asList(partsSequence));
        segmentList.add(terminalSegment);
        partsSequence = segmentList.toArray(new String[] {});
        logger.debug("segments after adding \\: "+Arrays.toString(partsSequence));
        int iseg = 0;
        while (iseg < partsSequence.length) {
        	String segment = partsSequence[iseg];
            Set<String> childrenKeys = trieNode.getChildren().keySet();
            TrieNode segmentNode = null;
            // if the current segment is not in the keys, add a new node for it
            if (!childrenKeys.contains(segment)) {
                segmentNode = insertNode(trieNode, segment); // where new child is added to the node
            } else {
            	segmentNode = getChild(trieNode, segment);
            }
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
	}

	public TrieNode getNode(String[] keys) {
        Map<String,TrieNode> children = root.getChildren();
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
	
	protected TrieNode getParentNode(TrieNode node) {
		return this.getParentNode(node.keys);
	}
	
	protected TrieNode getParentNode(String[] keys) {
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
		TrieNode[] allTerminals = getAllTerminals(root);
		return allTerminals;
	}
	
	public TrieNode[] getAllTerminals(TrieNode node) {
		List<TrieNode> allTerminalsLst = 
			new ArrayList<TrieNode>();
			
		collectAllTerminals(node, allTerminalsLst);
		
		return allTerminalsLst.toArray(new TrieNode[allTerminalsLst.size()]);
	}

	public TrieNode[] getAllTerminals(String[] segments) {
		TrieNode node = this.getNode(segments);
		TrieNode[] allTerminals = null;
		if (node==null)
			allTerminals = new TrieNode[0];
		else
			allTerminals = getAllTerminals(node);
		
		return allTerminals;
	}

	private void collectAllTerminals(TrieNode node, 
			List<TrieNode> collected) {
		if (node.isWord()) {
			collected.add(node);
		} else {
			for (TrieNode aChild: node.getChildrenNodes()) {
				collectAllTerminals(aChild, collected);
			}
		}
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

	public TrieNode getMostFrequentTerminal(String[] segments) {
		TrieNode node = getNode(segments);
		return getMostFrequentTerminal(node);
	}
	
	public TrieNode[] getMostFrequentTerminals(int n) {
		return getMostFrequentTerminals(n, root, null);
	}

	
	public TrieNode[] getMostFrequentTerminals(int n, String[] segments) {
		TrieNode node = getNode(segments);
		return getMostFrequentTerminals(n, node, null);
	}
	
	public TrieNode[] getMostFrequentTerminals(String[] segments) {
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

	/**
	 * 
	 * @param String rootKey
	 * @return String[] space-separated keys of the most frequent sequence of morphemes following rootSegment
	 */
	public String[] getMostFrequentSequenceForRoot(String rootKey) {
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

	protected TrieNode getMostFrequentTerminalFromMostFrequentSequenceForRoot(String rootSegment) {
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
