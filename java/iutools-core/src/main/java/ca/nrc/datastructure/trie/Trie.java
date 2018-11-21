package ca.nrc.datastructure.trie;

import java.util.HashMap;
import java.util.Set;


public class Trie {

    private StringSegmenter segmenter;
    private TrieNode root;
    private long size;
    
    
    public Trie(StringSegmenter _segmenter) {
    	this.segmenter = _segmenter;
        root = new TrieNode();
    }
    
    public TrieNode getRoot() {
    	return this.root;
    }
    
    public long getSize() {
    	return size;
    }

	public boolean add(String string) {
        TrieNode trieNode = root;
        if (trieNode == null || string == null)
            return false;

        String[] segments = this.segmenter.segment(string);
        int counter = 0;
        while (counter < segments.length) {
            Set<String> childs = trieNode.getChildren().keySet();
            // if the current char is not in the keys, add it
            if (!childs.contains(segments[counter])) {
                insertNode(trieNode, segments[counter]);
                // if this is the last char, indicate this is a word
                if (counter == segments.length - 1) {
                    getChild(trieNode, segments[counter]).setIsWord(true);
                    getChild(trieNode, segments[counter]).incrementFrequency();
                    size++; // for each new word
                    return true;
                }
            }
            // current char is in the keys, or has been added and is not the last char
            trieNode = getChild(trieNode, segments[counter]);
            trieNode.incrementFrequency();
            if (trieNode.getText().equals(string) && !trieNode.isWord()) {
                trieNode.setIsWord(true);
                size++; // for each new word
                return true;
            }
            counter++;
        }
        return false;
		
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
