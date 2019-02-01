package ca.nrc.datastructure.trie;

public class TrieTerminalNode extends TrieNode {
	
	public TrieTerminalNode(String _surfaceForm) {
		super();
		this.surfaceForm = _surfaceForm;
		this.isWord = true;
	}
	
    public TrieNode getMostFrequentTerminal() {
    	return this;
    }

}
