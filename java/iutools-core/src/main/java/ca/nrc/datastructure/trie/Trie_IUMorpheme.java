package ca.nrc.datastructure.trie;

/* blah */

import java.util.HashMap;
import java.util.Set;

import com.google.gson.Gson;


public class Trie_IUMorpheme extends Trie {
	
	//protected TrieNode_IUMorpheme root;
	
    public Trie_IUMorpheme() {
    	super(new StringSegmenter_IUMorpheme());
    	root = new TrieNode_IUMorpheme();
	}
    
    public Trie_IUMorpheme(StringSegmenter _segmenter, TrieNode _root, long _size) {
    	super(_segmenter,_root,_size);
    }
    	
    public TrieNode add(String string) throws TrieException {
        TrieNode_IUMorpheme trieNode = (TrieNode_IUMorpheme) root;
        if (trieNode == null || string == null)
            return null;

        String[] segments = null;
        try {
        	boolean fullAnalysis = true;
        	segments = this.segmenter.segment(string, fullAnalysis);
        } catch (Exception exc){
        	throw new TrieException("("+exc.getClass().getName()+") "+"Could not decompose word into its parts: "+string, exc);
        }
        int iseg = 0;
        while (iseg < segments.length) {
        	String segment = segments[iseg];
        	String[] segmentParts = segment.split(":");
        	String segmentSurfaceForm = segmentParts[0].substring(1);
        	String segmentMorphemeId = "{"+segmentParts[1];
            Set<String> childs = trieNode.getChildren().keySet();
            // if the current char is not in the keys, add it
            if (!childs.contains(segmentMorphemeId)) {
                TrieNode_IUMorpheme insertedNode = insertNode(trieNode, segmentMorphemeId, segmentSurfaceForm);
                // if this is the last char, indicate this is a word
                if (iseg == segments.length - 1) {
                	TrieNode terminalNode = getChild(trieNode, segmentMorphemeId);
                    terminalNode.setIsWord(true);
                    terminalNode.incrementFrequency();
                    size++; // for each new word
                    return terminalNode;
                }
            }
            // current char is in the keys, or it was not and has just been added and is not the last char
            trieNode = (TrieNode_IUMorpheme) getChild(trieNode, segmentMorphemeId);
            trieNode.incrementFrequency();
            
            if (iseg==segments.length-1) {
            	if (!trieNode.isWord())
            		trieNode.setIsWord(true);
                size++; // for each new word
                return trieNode;
            }
            iseg++;
        }
        return null;
		
	}

    /*
     * @param keys array of String of the form "{taku/1v}"
     */
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
	
    /*
     * @param keys array of String of the form "taku"
     */
    public TrieNode_IUMorpheme getNodeInSurface(String[] surfaceForms) {
        HashMap<String,TrieNode_IUMorpheme> children = ((TrieNode_IUMorpheme) root).getChildrenInSurface();
        TrieNode_IUMorpheme trieNode = null;
        for (int i = 0; i < surfaceForms.length; i++) {
            String key = surfaceForms[i];
            if (children.containsKey(key)) {
                trieNode = children.get(key);
                children = trieNode.getChildrenInSurface();
            } else 
            	return null;
        }
        return trieNode;
    }
	
    /*
     * @param key String of the form "umialiuq" (partial surface form of a word)
     */
    public TrieNode_IUMorpheme getNodeBySurfaceForm(String surfaceForm) {
        return __getNodeBySurfaceForm((TrieNode_IUMorpheme) root,surfaceForm);
    }
	
    private TrieNode_IUMorpheme __getNodeBySurfaceForm(TrieNode_IUMorpheme node, String target) {
        TrieNode_IUMorpheme trieNode = null;
        HashMap<String,TrieNode_IUMorpheme> children = node.getChildrenInSurface();
        String[] childrenSurfaceFormKeys = (String[]) children.keySet().toArray(new String[]{});
        for (int ic=0; ic<childrenSurfaceFormKeys.length; ic++) {
        	String surfaceFormKey = (String)childrenSurfaceFormKeys[ic];
        	TrieNode_IUMorpheme childNode = children.get(surfaceFormKey);
        	String childNodeSurfaceForm = childNode.getSurfaceForm();
        	if (childNodeSurfaceForm.equals(target))
        		return childNode;
        	if (target.indexOf(childNodeSurfaceForm)==0) {
        		return __getNodeBySurfaceForm(childNode,target);
        	}
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
	
    /*public String toJSON() {
		Gson gson = new Gson();
		String json = gson.toJson(trieWithoutSegmenter());
		return json;
    }*/
    
    public TrieWithSegmenterClassname trieWithoutSegmenter() {
    	TrieWithSegmenterClassname trieWithoutSegmenter = new TrieWithSegmenterClassname(segmenter.getClass().getName(),root,size);
		return trieWithoutSegmenter;
	}
	
	
	// --------------------- PRIVATE------------------------------

    private TrieNode getChild(TrieNode trieNode, String string) {
        return (TrieNode) trieNode.getChildren().get(string);
    }

    private TrieNode_IUMorpheme insertNode(TrieNode_IUMorpheme trieNode, String morphemeId, String segmentSurfaceForm) {
        if (trieNode.getChildren().containsKey(morphemeId)) {
            return null;
        }
        TrieNode_IUMorpheme nextNode = new TrieNode_IUMorpheme(trieNode.getText() + morphemeId, trieNode.getSurfaceForm() + segmentSurfaceForm);
        trieNode.getChildren().put(morphemeId, nextNode);
        trieNode.getChildrenInSurface().put(segmentSurfaceForm, nextNode);
        return nextNode;
    }


}
