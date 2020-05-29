package ca.nrc.datastructure.trie;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class Trie_InFileSystem extends Trie_Base {
	
	File rootDir = null;

	@Override
	public long getNbOccurrences() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public TrieNode getNode(String[] keys) {
		return new TrieNode(keys);
	}

	@Override
	public TrieNode add(String[] partsSequence, String word) throws TrieException {
		File nodeFile = file4node(partsSequence);
		try {
			Files.createDirectories(nodeFile.toPath());
		} catch (IOException e) {
			throw new TrieException(e);
		}
		
		
		
		return null;
	}

	@Override
	public TrieNode[] getAllTerminals(String[] segments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrieNode getRoot() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private File file4node(String[] segments) {
		File dir = 
			new File(rootDir, 
					 String.join(File.separator, segments)+
					 File.separator+"data.json");
		return dir;
	}
}
