package ca.nrc.datastructure.trie;

import java.util.Arrays;

/* blah */

import java.util.HashMap;
import java.util.Set;

import org.apache.log4j.Logger;

import com.google.gson.Gson;


public class Trie {

    protected long size = 0;
    protected TrieNode root;
    
    
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
    	return size;
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
				size++; // for each new word
				logger.debug("size: "+size);
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
	
	public TrieNode getMostFrequentTerminal(String[] segments) {
		TrieNode node = this.getNode(segments);
		TrieNode mostFrequentTerminalNode = node.getMostFrequentTerminal();
		return mostFrequentTerminalNode;
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
        TrieNode nextNode = new TrieNode(trieNode.getText() + string);
        trieNode.getChildren().put(string, nextNode);
        return nextNode;
    }


}
