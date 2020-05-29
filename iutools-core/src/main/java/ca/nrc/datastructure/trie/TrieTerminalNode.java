package ca.nrc.datastructure.trie;

import java.util.HashMap;
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

	@Override
	public HashMap<String, TrieNode> getChildren() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrieNode[] getChildrenNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setChildren(HashMap<String, TrieNode> _children) {
		// TODO Auto-generated method stub
		
	}
}
