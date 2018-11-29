package ca.nrc.datastructure.trie;

/* blah */

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;


public class Trie_IUMorphemeWithSegmenterClassname {

    private long size;
    private String segmenterclassname;
    private TrieNode_IUMorpheme root;

    public Trie_IUMorphemeWithSegmenterClassname(String _segmenterclassname, TrieNode_IUMorpheme _root, long _size) {
    	this.size = _size;
    	this.root = _root;
    	this.segmenterclassname = _segmenterclassname;
	}
    
    public Trie_IUMorpheme toTrie() throws TrieException {
    	try {
    	Class<?> clazz = Class.forName(segmenterclassname);
    	Constructor<?> ctor = clazz.getConstructor();
    	StringSegmenter segmenter = (StringSegmenter) ctor.newInstance(new Object[] { });
    	return new Trie_IUMorpheme(segmenter,root,size);
    	} catch (Exception e) {
    		throw new TrieException("",e);
    	}
    }

    public long getSize() {
    	return size;
    }
}
