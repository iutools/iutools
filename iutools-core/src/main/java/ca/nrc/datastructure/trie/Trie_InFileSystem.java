package ca.nrc.datastructure.trie;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.nrc.string.StringUtils;

public class Trie_InFileSystem extends Trie {
	
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
	public TrieNode getNode(String[] keys) throws TrieException {
		File nodeFile = file4node(keys);
		
		ObjectMapper mapper = new ObjectMapper();	
		TrieNode node = null;
		try {
			node = mapper.readValue(nodeFile, TrieNode.class);
		} catch (IOException e) {
			throw new TrieException(
					"Unable to read TrieNode from file "+nodeFile, e);
		}
		
		File[] children = nodeFile.getParentFile().listFiles(File::isDirectory);
		for (File aChildDir: children) {
			String child = aChildDir.getName();
			node.children.put(child, null);
		}
		
		return node;
	}
	
	@Override
	public TrieNode add(String[] wordKeys, String word) throws TrieException {
		
		TrieNode node = new TrieNode(wordKeys, word, true);
		saveNode(node);
		incrementAllAncestorFrequencies(wordKeys);
		
		return node;
	}

	private void incrementAllAncestorFrequencies(String[] nodeKeys) throws TrieException {
		List<String> ancestorKeys = new ArrayList<String>();
		for (String aKey: nodeKeys) {
			ancestorKeys.add(aKey);
			incrementNodeFrequency(ancestorKeys);
		}
	}

	private void incrementNodeFrequency(List<String> nodeKeys) throws TrieException {
		TrieNode node = getNode(nodeKeys);
		node.frequency++;
		saveNode(node);		
	}

	private void saveNode(TrieNode node) throws TrieException {
		File nodeFile = file4node(node);
		try {
			new ObjectMapper().writeValue(nodeFile, node);
		} catch (IOException e) {
			throw new TrieException(
				"Could not save node: "+String.join(", ", node.keys), e);
		}
		
	}

	private void ensureNodeExists(List<String> ancestorKeys) throws TrieException {
		File nodeFile = file4node(ancestorKeys);
		File nodeDir = nodeFile.getParentFile();
		if (!nodeDir.exists()) {
			nodeDir.mkdir();
		}
		if (!nodeFile.exists()) {
			try {
				new ObjectMapper().writeValue(nodeFile, new TrieNode());
			} catch (IOException e) {
				throw new TrieException(
					"Unable to create node file for keys: "+
					StringUtils.join(ancestorKeys.iterator()), 
					e);
			}
		}
	}
	
	@Override
	public TrieNode[] getAllTerminals(String[] segments) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public TrieNode getRoot() {
		TrieNode root = null;
//		root = getNode(new String[0]);
		return root;
	}

	@Override
	public String toJSON() {
		// TODO Auto-generated method stub
		return null;
	}
	
	private File file4node(TrieNode node) throws TrieException {
		String[] keys = null;
		if (node.isTerminal()) {
			keys = appendTerminalKey(node.keys);
		} else {
			keys = node.keys;
		}
		return file4node(keys);
	}
	
	private File file4node(String[] keys) throws TrieException {
		
		keys = escapeKeys(keys);
		
		File nodeFile = 
			new File(rootDir, 
					 String.join(File.separator, keys)+
					 File.separator+"data.json");
		
		if (!nodeFile.exists()) {
			try {
				nodeFile.getParentFile().mkdirs();
				TrieNode node = new TrieNode(keys);
				new ObjectMapper().writeValue(nodeFile, node);
			} catch (IOException e) {
				throw new TrieException(
					"Unable to create node file for keys: "+
					String.join(", ", keys), e);
			}
		}
		
		return nodeFile;
	}
	
	private File file4node(List<String> ancestorKeys) throws TrieException {
		return file4node(ancestorKeys.toArray(new String[ancestorKeys.size()]));
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
	public long getFrequency(String[] segments) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	protected void collectAllTerminals(TrieNode node, List<TrieNode> collected) {
		// TODO Auto-generated method stub
		
	}
	
	protected String[] appendTerminalKey(String[] origKeys) throws TrieException {
		String[] extendedKeys = new String[origKeys.length + 1];
		for (int ii=0; ii < origKeys.length; ii++) {
			extendedKeys[ii] = origKeys[ii];
		}
		extendedKeys[extendedKeys.length-1] = "$";
		
		return extendedKeys;
	}
	
	protected String[] escapeKeys(String[] origKeys) throws TrieException {
		String[] escaped = new String[origKeys.length];
		for (int ii=0; ii < origKeys.length; ii++) {
			String key = origKeys[ii];
			if (ii == origKeys.length-1 && 
				(key.equals("$") || key.equals("\\"))) {
				// Leave the last key alone if it's the 'termina' key
				//
				escaped[ii] = origKeys[ii];
				continue;
			}
			
			try {
				escaped[ii] = URLEncoder.encode(
					origKeys[ii], StandardCharsets.UTF_8.toString());
			} catch (UnsupportedEncodingException e) {
				throw new TrieException("Could not escape key "+origKeys[ii], e);
			}
		}
		
		if (escaped.length > 0 && escaped[escaped.length-1].equals("\\")) {
			escaped[escaped.length-1] = "$";
		}
		
		return escaped;
	}
	
	protected String[] unescapeKeys(String[] origKeys) throws TrieException {
		String[] escaped = new String[origKeys.length];
		for (int ii=0; ii < origKeys.length; ii++) {
			try {
				escaped[ii] = URLDecoder.decode(
					origKeys[ii], StandardCharsets.UTF_8.toString());
			} catch (UnsupportedEncodingException e) {
				throw new TrieException("Could not unescape key "+origKeys[ii], e);
			}
		}
		return escaped;
	}
}
