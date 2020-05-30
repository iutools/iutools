package ca.nrc.datastructure.trie;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.Ignore;

public class Trie_InFileSystem extends Trie_Base {
	
	File rootDir = null;

	public Trie_InFileSystem(File _rootDir) {
		this.rootDir = _rootDir;
	}

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

	@Override
	public TrieNode[] getAllTerminals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrieNode[] getAllTerminals(TrieNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrieNode getMostFrequentTerminal() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrieNode getMostFrequentTerminal(TrieNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrieNode getMostFrequentTerminal(String[] segments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrieNode[] getMostFrequentTerminals(int n) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrieNode[] getMostFrequentTerminals(int n, String[] segments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrieNode[] getMostFrequentTerminals(String[] segments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrieNode[] getMostFrequentTerminals() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrieNode[] getMostFrequentTerminals(Integer n, TrieNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrieNode[] getMostFrequentTerminals(Integer n, TrieNode node, TrieNode[] exclusions) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getMostFrequentSequenceForRoot(String rootKey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected TrieNode getMostFrequentTerminalFromMostFrequentSequenceForRoot(String rootSegment) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected TrieNode getParentNode(TrieNode node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected TrieNode getParentNode(String[] keys) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getFrequency(String[] segments) {
		// TODO Auto-generated method stub
		return 0;
	}
}
