package ca.nrc.datastructure.trie;

import java.nio.file.Path;

/**
 * This Trie implementation uses the File System to capture the 
 * hierarchical tree structure.
 * 
 * The advantages of this approach are:
 * - Less memory
 * - Faster too load
 * 
 * @author desilets
 *
 */
public class Trie_FS extends Trie {
	
	public Trie_FS(Path treeRoot) throws TrieException {
		if (!treeRoot.toFile().exists()) {
			
		}
		if (!treeRoot.toFile().isDirectory()) {
			throw new TrieException("Root of FS trie is a file. Root: "+treeRoot);
		}
	}

}
