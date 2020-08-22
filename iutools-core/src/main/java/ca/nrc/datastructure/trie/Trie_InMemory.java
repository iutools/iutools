package ca.nrc.datastructure.trie;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

public class Trie_InMemory extends Trie {
	
	TrieNode root = new TrieNode();
    
    public Trie_InMemory() {
	}

	@Override
	public void reset() throws TrieException {
		root = new TrieNode();
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
    
	@Override
	public boolean contains(String[] segments) throws TrieException {
		boolean answer = null != node4keys(segments, NodeOption.NO_CREATE);
		return answer;
	}
    
        
    /**
     * Add an entry to the Trie.
     */
	public TrieNode retrieveNode_NoStatsRefresh(String[] keys, NodeOption... options )
			throws TrieException {
		// TODO-June2020: Implement all getNode() entry points at level of parent
		//  Trie class. These methods will check that segments is not null, then 
		//  invoke getNodeAssumingNonNullSegments()
		//
		if (keys == null) {
			keys = new String[] {TrieNode.NULL_SEG};
		}
		
		boolean createIfNotExist = true;
		boolean terminal = false;
		for (NodeOption anOption: options) {
			if (anOption == NodeOption.NO_CREATE) {
				createIfNotExist = false;
			} else if (anOption == NodeOption.TERMINAL) {
				terminal = true;
			}
		}
		
		if (terminal) {
			keys = ensureTerminal(keys);
		}
		
        TrieNode trieNode = root;
        Map<String,TrieNode> children = getRoot().getChildren();        
        if (keys.length > 0) {
	        for (int i = 0; i < keys.length; i++) {
	            String key = keys[i];
	            if (children.containsKey(key)) {
	                trieNode = (TrieNode) children.get(key);
	            } else {
	            	if (createIfNotExist) {
	            		TrieNode newNode = 
	            			new TrieNode(Arrays.copyOfRange(keys, 0, i+1));
	            		trieNode.addChild(key, newNode);
	            		trieNode = newNode;
	            	} else {
		            	trieNode = null;
		            	break;
	            	}
	            }
                children = trieNode.getChildren();
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
		TrieNode node = this.node4keys((rootSegment+" "+newCumulativeKeys).split(" "));
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
    
    public String toJSON() {
		Gson gson = new Gson();
		String json = gson.toJson(this);
		return json;
    }

	@Override
	public void saveNode(TrieNode node) throws TrieException {
		// Nothing to do. For an InMemory trie, the nodes are 
		// always up to date in the memory.
		
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
