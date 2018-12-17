package ca.nrc.datastructure.trie;

import java.util.ArrayList;
import java.util.Arrays;

/* blah */

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.Gson;


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
    
	public TrieNode add(String[] segments) throws TrieException {
        TrieNode trieNode = root;
        if (trieNode == null)
            throw new TrieException("Can't add to a null root.");
        if (segments == null)
            return null; // null means the segmenter was not able to segment a word
        

        Logger logger = Logger.getLogger("Trie.add");
        logger.debug("segments: "+Arrays.toString(segments));
        int iseg = 0;
        while (iseg < segments.length) {
        	String segment = segments[iseg];
            Set<String> childs = trieNode.getChildren().keySet();
            // if the current char is not in the keys, add it
            if (!childs.contains(segment)) {
                insertNode(trieNode, segment);
            }
			// if this is the last char, indicate this is a word
			if (iseg == segments.length - 1) {
				TrieNode terminalNode = getChild(trieNode, segment);
				terminalNode.setIsWord(true);
				terminalNode.incrementFrequency();
				// REMOVED: size++; // for each new word
				return terminalNode;
			}
            // current char is in the keys, or it was not and has just been added and is not the last char
            trieNode = getChild(trieNode, segment);
            trieNode.incrementFrequency();
            iseg++;
        }
        return null;
		
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
	
	public long getFrequency(String[] segments) {
		TrieNode node = this.getNode(segments);
		if (node != null)
			return node.getFrequency();
		else
			return 0;
	}
	
	public TrieNode[] getAllTerminals() {
		return root.getAllTerminals();
	}
	
	public TrieNode[] getAllTerminals(String[] segments) {
		TrieNode node = this.getNode(segments);
		return node.getAllTerminals();
	}
	
	// --------------------- PRIVATE------------------------------

    private TrieNode getChild(TrieNode trieNode, String string) {
        return (TrieNode) trieNode.getChildren().get(string);
    }

    private TrieNode insertNode(TrieNode trieNode, String string) {
        if (trieNode.getChildren().containsKey(string)) {
            return null;
        }
        ArrayList<String> keys = new ArrayList<String>(Arrays.asList(trieNode.keys));
        keys.add(string);
        TrieNode nextNode = new TrieNode(keys.toArray(new String[] {}));
        trieNode.getChildren().put(string, nextNode);
        return nextNode;
    }




}
