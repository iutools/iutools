package ca.nrc.datastructure.trie;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.nrc.string.StringUtils;

public class Trie_InFileSystem extends Trie {
	
	File rootDir = null;

	public Trie_InFileSystem(File _rootDir) {
		this.rootDir = _rootDir;
	}
	@Override
	public TrieNode getRoot() throws TrieException {
		return getNode(new String[0]);
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
		String[] extendedKeys = appendTerminalKey(wordKeys);
		TrieNode node = getNode(extendedKeys);
		if (node == null) {
			node = new TrieNode(wordKeys);
		}
		node.addSurfaceForm(word);
		node.frequency++;
		saveNode(node);
		updateAncestors(extendedKeys);
		
		return node;
	}

	private void updateAncestors(String[] nodeKeys) throws TrieException {
		updateAncestors(Arrays.asList(nodeKeys));
	}
	
	private void updateAncestors(List<String> nodeSegments) throws TrieException {
		if (nodeSegments.size() > 0) {
			List<String> parentSegments = 
					nodeSegments.subList(0, nodeSegments.size()-1);
			
			TrieNode parentNode = getNode(parentSegments);
			parentNode.frequency++;
			
			String childSegment = nodeSegments.get(nodeSegments.size()-1);
			parentNode.addChild(childSegment, getNode(nodeSegments));
			
			saveNode(parentNode);
			
			updateAncestors(parentSegments);
		} else {
			int x = 1;
		}
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

	private File file4node(TrieNode node) throws TrieException {
		String[] keys = node.keys;
		return file4node(keys);
	}
	
	private File file4node(String[] keys) throws TrieException {
		
		String[] escapedKeys = escapeKeys(keys);
		
		File nodeFile = 
			new File(rootDir, 
					 String.join(File.separator, escapedKeys)+
					 File.separator+"node.json");
		
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
	public long getFrequency(String[] segments) throws TrieException {
		long freq = getNode(segments).frequency;
		return freq;
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
