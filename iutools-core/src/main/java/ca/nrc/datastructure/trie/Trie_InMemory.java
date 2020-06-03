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


public class Trie_InMemory extends Trie {
	
	TrieNode root = new TrieNode();
    
    public Trie_InMemory() {
	}
    
	@Override
	public TrieNode getRoot() throws TrieException {
		return root;
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
        TrieNode trieNode = getRoot();
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
        trieNode.surfaceForm = word; //***
        trieNode.isWord = true;
        return trieNode; //***		
	}

	public TrieNode getNode(String[] keys) throws TrieException {
        TrieNode trieNode = null;
        if (keys.length == 0) {
        	trieNode = root;
        } else {
	        Map<String,TrieNode> children = getRoot().getChildren();
	        for (int i = 0; i < keys.length; i++) {
	            String key = keys[i];
	            if (children.containsKey(key)) {
	                trieNode = (TrieNode) children.get(key);
	                children = trieNode.getChildren();
	            } else {
	            	return null;
	            }
	        }
        }
        return trieNode;
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
