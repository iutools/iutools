package ca.nrc.datastructure.trie;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import ca.inuktitutcomputing.utilities.StopWatch;

public class Trie_InFileSystem extends Trie {
	
	File rootDir = null;
	
	protected ObjectWriter nodeWriter = null;
	
	public Trie_InFileSystem(File _rootDir) {
		this.rootDir = _rootDir;
	}
	
	@Override
	public TrieNode getRoot() throws TrieException {
		return getNode(new String[0]);
	}

	@Override
	public TrieNode getNode(String[] keys, NodeOption... options) throws TrieException {
		Logger tLogger = Logger.getLogger("ca.nrc.datastructure.trie.Trie_InFileSystem.getNode");
		
		// TODO-June2020: Implement all getNode() entry points at level of parent
		//  Trie class. These methods will check that segments is not null, then 
		//  invoke getNodeAssumingNonNullSegments()
		//
		if (keys == null) {
			keys = new String[] {TrieNode.NULL_SEG};
		}
		
		TrieNode node = null;
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("looking for keys="+String.join(",", keys));
		}
		File nodeFile = file4node(keys, options);
		tLogger.trace("nodeFile="+nodeFile);
		if (nodeFile != null) {
		
			ObjectMapper mapper = new ObjectMapper();	
			node = readNodeFile(nodeFile);
			
			File[] children = nodeFile.getParentFile().listFiles(File::isDirectory);
			for (File aChildDir: children) {
				String child = aChildDir.getName();
				child = unescapeKey(child);
				node.children.put(child, null);
			}
		}
		
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("returning node=\n"+node);;
		}
		
		return node;
	}
	
		
	protected TrieNode readNodeFile(File nodeFile) throws TrieException {
		TrieNode node;
		try {
			node = new ObjectMapper().readValue(nodeFile, TrieNode.class);
		} catch (IOException e) {
			throw new TrieException(e);
		}
		return node;
	}

	public void saveNode(TrieNode node) throws TrieException {
		File nodeFile = file4node(node);
		writeNodeFile(node, nodeFile);
	}

	protected void writeNodeFile(TrieNode node, File nodeFile) throws TrieException {
		Logger tLogger = Logger.getLogger("ca.nrc.datastructure.trie.Trie_InFileSystem.writeNodeFile");
		long start = 0;
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("Writing node to file.\n   Node["+String.join(",", node.keys)+"] --> "+nodeFile);
			start = StopWatch.nowMSecs();
		}
		try {
			getNodeWriter().writeValue(nodeFile, node);
		} catch (IOException e) {
			throw new TrieException(e);
		}
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("Writing took "+StopWatch.elapsedMsecsSince(start)+" msecs");
		}
		
	}

	private File file4node(TrieNode node) throws TrieException {
		String[] keys = node.keys;
		return file4node(keys);
	}
	
	private File file4node(String[] keys) throws TrieException {
		return file4node(keys, new NodeOption[0]);
	}
	
	protected File file4node(String[] keys, NodeOption... options) throws TrieException {
		boolean createIfNotExist = true;
		boolean terminal = false;
		for (NodeOption anOption: options) {
			if (anOption == NodeOption.NO_CREATE) {
				createIfNotExist = false;
			} else if (anOption == NodeOption.TERMINAL) {
				terminal = true;
			}
		}
		
		if (terminal) {
			keys = ensureTerminal(keys);
		}
		String[] escapedKeys = escapeKeys(keys);
		
		File nodeFile = 
			new File(rootDir, 
					 String.join(File.separator, escapedKeys)+
					 File.separator+"node.json");
		
		if (!nodeFile.exists()) {
			if (!createIfNotExist) {
				nodeFile = null;
			} else {
				nodeFile.getParentFile().mkdirs();
				TrieNode node = new TrieNode(keys);
				writeNodeFile(node, nodeFile);
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
		extendedKeys[extendedKeys.length-1] = TrieNode.TERMINAL_SEG;
		
		return extendedKeys;
	}
	
	protected String[] escapeKeys(String[] origKeys) throws TrieException {
		String[] escaped = new String[origKeys.length];
		for (int ii=0; ii < origKeys.length; ii++) {
			String key = origKeys[ii];
			if (ii == origKeys.length-1 && 
				(key.equals(TrieNode.TERMINAL_SEG) || key.equals("\\"))) {
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
			escaped[escaped.length-1] = TrieNode.TERMINAL_SEG;
		}
		
		return escaped;
	}
	
	protected String[] unescapeKeys(String[] origKeys) throws TrieException {
		String[] escaped = new String[origKeys.length];
		for (int ii=0; ii < origKeys.length; ii++) {
			escaped[ii] = unescapeKey(origKeys[ii]);
		}
		return escaped;
	}
	
	protected String unescapeKey(String origKey) throws TrieException {
		String escaped = null;
		try {
			escaped = URLDecoder.decode(
					origKey, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			throw new TrieException(e);
		}
		return escaped;
	}

	@Override
	public boolean contains(String[] segments) throws TrieException {
		File nodeFile = file4node(segments, NodeOption.NO_CREATE);
		boolean answer = (nodeFile != null && nodeFile.exists());
		return answer;
	}
	
	// JSon writer that filters some TrieNode properties which should not be 
	// written to the node's json file.
	//
	@JsonIgnore
	protected ObjectWriter getNodeWriter() {
		if (nodeWriter == null) 
		{
			SimpleBeanPropertyFilter propsFilter = SimpleBeanPropertyFilter
				.serializeAllExcept("children");
			FilterProvider filters = new SimpleFilterProvider()
				      .addFilter("TrieNodeFilter", propsFilter);
			nodeWriter = new ObjectMapper().writer(filters);
		}
		
		return nodeWriter;
	}	
}
