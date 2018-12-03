package ca.nrc.datastructure.trie;

/* blah */

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;


public class TrieWithSegmenterClassname {

    private long size;
    private String segmenterclassname;
    private TrieNode root;
    
    
    public TrieWithSegmenterClassname(String _segmenterclassname, TrieNode _root, long _size) {
    	this.size = _size;
    	this.root = _root;
    	this.segmenterclassname = _segmenterclassname;
	}
    
    /*public Trie toTrie() throws Exception {
    	Class<?> clazz = Class.forName(segmenterclassname);
    	Constructor<?> ctor = clazz.getConstructor();
    	StringSegmenter segmenter = (StringSegmenter) ctor.newInstance(new Object[] { });
    	return new Trie(segmenter,root,size);
    }*/
    
    public long getSize() {
    	return size;
    }

}
