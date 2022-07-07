package org.iutools.datastructure.trie;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.iutools.config.IUConfig;
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
		if (node != null) {
			trace(tLogger, node.keys, message, specificNodesRegex);
		}
	}

	public static void trace(Logger tLogger, String[] nodeKeys, String message,
							 String specificNodesRegex)
			throws TrieException {
		if (shouldTrace(tLogger, nodeKeys, specificNodesRegex)) {
			String key = "null";
			if (nodeKeys != null) {
			    key = StringUtils.join(nodeKeys, ",");
            }
			tLogger.trace("\n  Node: "+key+"\n  "+message);
		}
	}

	public static boolean shouldTrace(Logger tLogger, TrieNode node) 
		throws TrieException {
		return shouldTrace(tLogger, node, null);
	}
	
	public static boolean shouldTrace(Logger tLogger, TrieNode node, 
			String nodesToTraceRegex) throws TrieException {
		return shouldTrace(tLogger, node.keys, nodesToTraceRegex);
	}

	public static boolean shouldTrace(Logger tLogger, String[] nodeKeys,
				String nodesToTraceRegex) throws TrieException {

			boolean trace = false;
		if (tLogger.isTraceEnabled()) {
			String key = String.join("", nodeKeys);
			if (nodesToTraceRegex == null) {
				try {
					nodesToTraceRegex = new IUConfig().nodesToTraceRegex();
				} catch (ConfigException e) {
					throw new TrieException(e);
				}
			}
			trace = true;
			if  (nodesToTraceRegex != null) {
				trace = (key.matches(nodesToTraceRegex));
			}
		}
		
		return trace;
	}
	
	static String printKeys(List<TrieNode> nodes) {
		List<String> joinedKeys = new ArrayList<String>();
		for (TrieNode node: nodes) {
			joinedKeys.add(String.join("", node.keys));
		}
		
		String pretty;
		pretty = StringUtils.join(joinedKeys.iterator(), ",");

		return pretty;
	}
}
