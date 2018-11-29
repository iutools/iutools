package ca.nrc.datastructure.trie;

import java.util.HashMap;

public class TrieNode_IUMorpheme extends TrieNode {
	
	private String surfaceForm = "";
	private HashMap<String,TrieNode_IUMorpheme> childrenInSurface = new HashMap<String,TrieNode_IUMorpheme>();
    
	

    public TrieNode_IUMorpheme() {
    	super();
    }

    public TrieNode_IUMorpheme(String text) {
    	super(text);
    }
    
    public TrieNode_IUMorpheme(String text, String surfaceForm) {
    	super(text);
    	this.surfaceForm = surfaceForm;
    }
    
    public String getSurfaceForm() {
    	return this.surfaceForm;
    }
    
	public HashMap<String,TrieNode_IUMorpheme> getChildrenInSurface() {
		return childrenInSurface;
	}
	
}