package ca.nrc.datastructure.trie;

/* blah */

import java.util.HashMap;
import java.util.Set;

import com.google.gson.Gson;


public class Trie {

    private long size;
    private StringSegmenter segmenter;
    private TrieNode root;
    
    
    public Trie(StringSegmenter _segmenter) {
    	this.segmenter = _segmenter;
    	root = new TrieNode();
	}
    
    public Trie(StringSegmenter _segmenter, TrieNode _root, long _size) {
    	this.segmenter = _segmenter;
    	this.root = _root;
    	this.size = _size;
	}
    
    public String toJSON() {
		Gson gson = new Gson();
		String json = gson.toJson(trieWithoutSegmenter());
		return json;
    }
    
    private TrieWithSegmenterClassname trieWithoutSegmenter() {
    	TrieWithSegmenterClassname trieWithoutSegmenter = new TrieWithSegmenterClassname(segmenter.getClass().getName(),root,size);
		return trieWithoutSegmenter;
	}

	public TrieNode getRoot() {
    	return this.root;
    }
    
    public long getSize() {
    	return size;
    }

	public TrieNode add(String string) throws TrieException {
        TrieNode trieNode = root;
        if (trieNode == null || string == null)
            return null;

        
        String[] segments = null;
        try {
        	segments = this.segmenter.segment(string);
        } catch (Exception exc){
        	throw new TrieException("("+exc.getClass().getName()+") "+"Could not decompose word into its parts: "+string, exc);
        }
        int counter = 0;
        while (counter < segments.length) {
            Set<String> childs = trieNode.getChildren().keySet();
            // if the current char is not in the keys, add it
            if (!childs.contains(segments[counter])) {
                insertNode(trieNode, segments[counter]);
                // if this is the last char, indicate this is a word
                if (counter == segments.length - 1) {
                	TrieNode terminalNode = getChild(trieNode, segments[counter]);
                    terminalNode.setIsWord(true);
                    terminalNode.incrementFrequency();
                    size++; // for each new word
                    return terminalNode;
                }
            }
            // current char is in the keys, or has been added and is not the last char
            trieNode = getChild(trieNode, segments[counter]);
            trieNode.incrementFrequency();
            if (trieNode.getText().equals(string) && !trieNode.isWord()) {
                trieNode.setIsWord(true);
                size++; // for each new word
                return trieNode;
            }
            counter++;
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
