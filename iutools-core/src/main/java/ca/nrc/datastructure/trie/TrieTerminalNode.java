package ca.nrc.datastructure.trie;

import java.util.Vector;

public class TrieTerminalNode extends TrieNode {
	
	public TrieTerminalNode(String _surfaceForm) {
		super();
		this.surfaceForm = _surfaceForm;
		this.isWord = true;
	}
	
    public TrieTerminalNode getMostFrequentTerminal() {
    	return this;
    }

	@Override
    public String toString() {
        return "[TrieTerminalNode:\n" +
        		"    segments = "+this.getKeysAsString()+"\n"+
        		"    frequency = "+this.frequency+"\n"+
        		"    isWord = "+this.isWord+"\n"+
        		"    surfaceForm = "+this.surfaceForm+"\n"+
        		"    ]";
    }
}
