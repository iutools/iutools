package ca.nrc.datastructure.trie;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.config.IUConfig;
import ca.nrc.config.ConfigException;
import ca.nrc.string.StringUtils;

public class NodeTracer {
	
	public static void trace(Logger tLogger, TrieNode node, String message) 
		throws TrieException {
		trace(tLogger, node, message, null);
	}
	
	public static void trace(Logger tLogger, TrieNode node, String message,
			String specificNodesRegex) 
		throws TrieException {
		if (shouldTrace(tLogger, node, specificNodesRegex)) {
			tLogger.trace("Node:\n"+node+"\n"+message);
		}
	}

	public static boolean shouldTrace(Logger tLogger, TrieNode node) 
		throws TrieException {
		return shouldTrace(tLogger, node, null);
	}
	
	public static boolean shouldTrace(Logger tLogger, TrieNode node, 
			String nodesToTraceRegex) throws TrieException {
		
		boolean trace = false;
		if (tLogger.isTraceEnabled()) {
			String nodeKeys = String.join("", node.keys);
			if (nodesToTraceRegex == null) {
				try {
					nodesToTraceRegex = new IUConfig().nodesToTraceRegex();
				} catch (ConfigException e) {
					throw new TrieException(e);
				}
			}
			trace = true;
			if  (nodesToTraceRegex != null) {
				trace = (nodeKeys.matches(nodesToTraceRegex));
			}
		}
		
		return trace;
	}
	
	static String printKeys(List<TrieNode> nodes) {
		List<String> joinedKeys = new ArrayList<String>();
		for (TrieNode node: nodes) {
			joinedKeys.add(String.join("", node.keys));
		}
		
		String pretty = StringUtils.join(joinedKeys.iterator(), ",");
		
		return pretty;
	}
}
