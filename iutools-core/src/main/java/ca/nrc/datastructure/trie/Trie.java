package ca.nrc.datastructure.trie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.inuktitutcomputing.utilities.StopWatch;
import ca.inuktitutcomputing.utilities.StopWatchException;
import ca.nrc.ui.commandline.ProgressMonitor;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.log4j.Logger;

import ca.nrc.datastructure.trie.visitors.TrieNodeVisitor;
import ca.nrc.datastructure.trie.visitors.VisitorFindMostFrequentTerminals;
import ca.nrc.datastructure.trie.visitors.VisitorNodeCounter;

// TODO-June2020: Methods that return a set or list of TrieNodes should
//   instead return an Iterator<TrieNode>, because the list of nodes may be
//   very large so it's better to not assume that they all will be in 
//   memory.
//
//   In the case of the _InFileSystem version, create a class 
//
//      class FSTrieNodeIterator extends Iterator<TrieNode> {
//
//   This class will use FileUtils.iterateFiles() to create a File<Iterator>
//   and it will use that File<Iterator> to iterate through the files for 
//   the corresponding TrieNode
//
//    http://commons.apache.org/proper/commons-io/javadocs/api-2.5/org/apache/commons/io/FileUtils.html#iterateFiles(java.io.File,%20org.apache.commons.io.filefilter.IOFileFilter,%20org.apache.commons.io.filefilter.IOFileFilter)
//


// TODO-June2020: Standardize the vocabulary used for methods and variable names
//   The trie indexes a series of string EXPRESSIONS.
//   Each expression can be decomposed into a sequence of SEGMENTS
//   Segments have
//
//   - ID: Some canonical form that represent the segment
//   - SURFACE FORM: The way that the segment was actually written 
//       in the expression
//
//   Note: The ID and SURFACE FORM can be the same thing, but not necessarily
//

public abstract class Trie {
	
public static enum NodeOption {NO_CREATE, TERMINAL};

	public abstract void reset() throws TrieException;

	// TODO-2020: Rename to rootNode()
	public abstract TrieNode getRoot(boolean ensureUptodateAggrStats) throws TrieException;

	/**
	 * Retrieve the node that corresponds to a particular sequence of keys.
	 * The method does not make any attempt to ensure that the node's aggregate
	 * stats (ex: frequency) are up to date with those of its children.
	 */
	@JsonIgnore
	protected abstract TrieNode retrieveNode_NoStatsRefresh(
		String[] keys, NodeOption... options) throws TrieException;
		
	public abstract boolean contains(String[] segments) throws TrieException;
		
	public abstract void saveNode(TrieNode node) throws TrieException;
			
	protected String allTerminalsJoined = ";";

	private TrieInfo _info = null;

	public TrieInfo info() throws TrieException {
		if (_info == null) {
			_info = new TrieInfo();
			_info.totalTerminals = computeTotalTerminals();
			_info.totalOccurences = computeTotalOccurences();
		}
		return _info;
	}

	/**
	 * Get the node corresponding to a sequence of keys.
	 *
	 * Note that the node's aggregate stats like 'frequency' may NOT be up to
	 * date. To ensure up to date stats, use
	 *
	 *    node4keys(keys, NodeOption.ENSURE_UPTODATE_STATS)
	 */
	@JsonIgnore
	public TrieNode node4keys(List<String> keys) throws TrieException {
		return node4keys(keys.toArray(new String[keys.size()]), new NodeOption[0]);
	}

	/**
	 * Get the node corresponding to a sequence of keys.
	 *
	 * Note that the node's aggregate stats like 'frequency' may NOT be up to
	 * date. To ensure up to date stats, use
	 *
	 *    node4keys(keys, NodeOption.ENSURE_UPTODATE_STATS)
	 */
	@JsonIgnore
	public TrieNode node4keys(String[] keys) throws TrieException {
		return node4keys(keys, new NodeOption[0]);
	}

	/**
	 * Get the node corresponding to a sequence of keys.
	 *
	 * If NodeOption.ENSURE_UPTODATE_STATS is provided, the system will
	 * additionally ensure that the node's aggregate stats like 'frequency' are
	 * up to date.
	 */
	@JsonIgnore
	public TrieNode node4keys(String[] keys, NodeOption... options)
			throws TrieException {
		TrieNode node = retrieveNode_NoStatsRefresh(keys, options);
		if (node != null &&
				node.statsMayNeedRefreshing(info().lastTerminalChangeTime)) {
			recomputeAggregateStats(node);
		}

		return node;
	}

	public TrieNode getRoot() throws TrieException {
		return getRoot(false);
	}

	public TrieNode add(String[] segments, String expression)
			throws TrieException {
		return add(segments, expression, 1);
	}
	
	public TrieNode add(String[] segments, String expression, long freqIncr) 
			throws TrieException {
		Logger tLogger = Logger.getLogger("ca.nrc.datastructure.trie.Trie.add");

		long start = 0;
		TimeUnit unit = TimeUnit.MILLISECONDS;

		TrieInfo info = info();
		info.lastTerminalChangeTime = System.currentTimeMillis();

		if (segments == null) {
			segments = new String[] {TrieNode.NULL_SEG};
		}

		if (tLogger.isTraceEnabled()) {
			try {
				start = StopWatch.now(unit);
			} catch (StopWatchException e) {
				throw new TrieException(e);
			}
			tLogger.trace("segments="+String.join(",", segments));
		}

		addToJoinedTerminals(segments);
		TrieNode node = retrieveNode_NoStatsRefresh(segments, NodeOption.TERMINAL);
		if (node.frequency == 0) {
			// This is a brand new node
			info.totalTerminals++;
		}
		node.updateSurfaceForms(expression, freqIncr);

		node.frequency += freqIncr;
		info.totalOccurences += freqIncr;
		saveNode(node);

		if (tLogger.isTraceEnabled()) {
			try {
				tLogger.trace("COMPLETED segments="+String.join(",", segments)+" in "+StopWatch.elapsedSince(start, unit)+unit.name());
			} catch (StopWatchException e) {
				throw new TrieException(e);
			}
		}

		return node;		
	}

	@JsonIgnore
	public TrieNode[] getTerminals() throws TrieException {
		TrieNode[] allTerminals = getTerminals(getRoot());
		return allTerminals;
	}

	@JsonIgnore
	public TrieNode[] getTerminals(String[] segments) throws TrieException {
		return getTerminals(segments, null);
	}

	@JsonIgnore
	public TrieNode[] getTerminals(String[] segments, Boolean matchStart) 
			throws TrieException {

		if (matchStart == null) {
			matchStart = true;
		}
		
		TrieNode[] allTerminals = new TrieNode[0];
		if (segments.length > 0 && segments[0].equals("^")) {
			matchStart = true;
		}
		
		if (!matchStart) {
			getTerminalsMatchingNgram(segments);
		} else {
			TrieNode node = this.node4keys(segments);
			if (node==null)
				allTerminals = new TrieNode[0];
			else
				allTerminals = getTerminals(node);
		}
		
		return allTerminals;
	}

	@JsonIgnore
	public TrieNode[] getTerminalsMatchingNgram(String[] segments) 
			throws TrieException {
		
		List<TrieNode> terminalsLst = new ArrayList<TrieNode>();
		Matcher matcher = joinedTerminalsMatcher(segments, true);
		while (matcher.find()) {
			String terminalStr = matcher.group(1);
			String[] matchSegs = terminalStr.split(",");
			terminalsLst.add(node4keys(matchSegs));
		}
		
		return terminalsLst.toArray(new TrieNode[terminalsLst.size()]);	
	}

	@JsonIgnore
	public TrieNode[] getTerminals(TrieNode node) throws TrieException {
		List<TrieNode> allTerminalsLst = 
			new ArrayList<TrieNode>();
			
		collectAllTerminals(node, allTerminalsLst);
		
		return allTerminalsLst.toArray(new TrieNode[allTerminalsLst.size()]);
	}
	
	protected void collectAllTerminals(TrieNode node, 
			List<TrieNode> collected) throws TrieException {
		Logger tLogger = Logger.getLogger("ca.nrc.datastructure.trie.Trie.collectAllTerminals");
		
		NodeTracer.trace(tLogger, node, "Upon entry, collected="+
			NodeTracer.printKeys(collected));
		if (node.isTerminal()) {
			collected.add((TrieNode)node);
			NodeTracer.trace(tLogger, node, 
					"node is terminal; collecting it;\nNow, collected="+
					NodeTracer.printKeys(collected));
		} else {
			NodeTracer.trace(
				tLogger, node,
				"node is NOT terminal; traversing children");
			for (TrieNode aChild: childrenNodes(node)) {
				collectAllTerminals(aChild, collected);
			}
		}
		
		if (NodeTracer.shouldTrace(tLogger, node)) {
			NodeTracer.trace(tLogger, node, 
				"Upon exit, collected nodes are: "+
				NodeTracer.printKeys(collected));
		}
	}
	
    private List<TrieNode> childrenNodes(TrieNode node) throws TrieException {
    	Logger tLogger = Logger.getLogger("ca.nrc.datastructure.trie.Trie.childrenNodes");
    	if (tLogger.isTraceEnabled()) {
    		tLogger.trace("node="+node+"\nnode.children.keySet="+node.children.keySet());
    	}
    	List<TrieNode> children = new ArrayList<TrieNode>();
    	for (String extension: node.childrenSegments()) {
    		TrieNode childNode = node4keys(extendSegments(node.keys, extension ));
    		children.add(childNode);
    	}
    	
		return children;
	}

	public static String[] extendSegments(String[] orig, String extension) {
		String[] extended = new String[orig.length+1];
		for (int ii=0; ii < orig.length; ii++) {
			extended[ii] = orig[ii];
		}
		extended[extended.length-1] = extension;
		
		return extended;
	}

	@JsonIgnore
	public TrieNode getMostFrequentTerminal() throws TrieException {
		return getMostFrequentTerminal(getRoot());
	}

	@JsonIgnore
	public TrieNode getMostFrequentTerminal(TrieNode node) throws TrieException {
		TrieNode mostFrequent = null;
		TrieNode[] terminals = getMostFrequentTerminals(1, node, null);
		if (terminals != null && terminals.length > 0) {
			mostFrequent = terminals[0];
		}
		return mostFrequent;
	}

	@JsonIgnore
	public TrieNode getMostFrequentTerminal(String[] segments) throws TrieException {
		TrieNode node = node4keys(segments);
		return getMostFrequentTerminal(node);
	}

	@JsonIgnore
	public TrieNode[] getMostFrequentTerminals(int n) throws TrieException {
		return getMostFrequentTerminals(n, getRoot(), null);
	}

	@JsonIgnore
	public TrieNode[] getMostFrequentTerminals(int n, String[] segments) throws TrieException {
		Logger tLogger = Logger.getLogger("ca.nrc.datastructure.trie.Trie.getMostFrequentTerminals");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("segments="+String.join(",", segments));
		}
		TrieNode node = node4keys(segments);
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("node="+node);
		}
		return getMostFrequentTerminals(n, node, null);
	}

	@JsonIgnore
	public TrieNode[] getMostFrequentTerminals(String[] segments) throws TrieException {
		TrieNode node = node4keys(segments);
		return getMostFrequentTerminals(null, node, null);
	}

	@JsonIgnore
	public TrieNode[] getMostFrequentTerminals() throws TrieException {
		return getMostFrequentTerminals(null, getRoot(), null);
	}

	@JsonIgnore
	public TrieNode[] getMostFrequentTerminals(
			Integer n, TrieNode node) throws TrieException {
		return getMostFrequentTerminals(n, node, null);
	}

	@JsonIgnore
	public TrieNode[] getMostFrequentTerminals(
			Integer n, TrieNode node, 
			TrieNode[] exclusions) throws TrieException {
		
		VisitorFindMostFrequentTerminals visitor = 
			new VisitorFindMostFrequentTerminals(n, exclusions);
		traverseNodes(node, visitor);
		
		return visitor.mostFrequentTerminals();
	}

	@JsonIgnore
	protected TrieNode getParentNode(TrieNode node) throws TrieException {
		return this.getParentNode(node.keys);
	}

	@JsonIgnore
	protected TrieNode getParentNode(String[] keys) throws TrieException {
		TrieNode parent = null;
		if (keys != null && keys.length > 0) {
			parent = this.retrieveNode_NoStatsRefresh(Arrays.copyOfRange(keys, 0, keys.length - 1));
		}
		return parent;
	}

	@JsonIgnore
	public long getFrequency(String[] segments) throws TrieException {
		TrieNode node = this.node4keys(segments);
		if (node != null)
			return node.getFrequency();
		else
			return 0;
	}
    
	private HashMap<String, Long> computeFreqs(String[] terminalNodeKeys, HashMap<String, Long> freqs, String rootSegment) throws TrieException {
		return _computeFreqs("",terminalNodeKeys,freqs,rootSegment);
	}

	private HashMap<String, Long> _computeFreqs(String cumulativeKeys, String[] terminalNodeKeys, HashMap<String, Long> freqs, String rootSegment) throws TrieException {
		Logger logger = Logger.getLogger("CompiledCorpus._computeFreqs");
		if (terminalNodeKeys.length==0)
			return freqs;
		logger.debug("cumulativeKeys: '"+cumulativeKeys+"'");
		logger.debug("terminalNodeKeys: '"+String.join("", terminalNodeKeys)+"'\n");
		String key = terminalNodeKeys[0];
		String newCumulativeKeys = (cumulativeKeys + " " + key).trim();
		String[] remKeys = Arrays.copyOfRange(terminalNodeKeys, 1, terminalNodeKeys.length);
		// node of rootSegment + newCumulativeKeys
		TrieNode node = this.node4keys((rootSegment+" "+newCumulativeKeys).split(" "));
		long incr = node.getFrequency();
		if (!freqs.containsKey(newCumulativeKeys))
			freqs.put(newCumulativeKeys, new Long(incr));
		freqs = _computeFreqs(newCumulativeKeys, remKeys, freqs, rootSegment);
		return freqs;
	}

	public static String[] wordChars(String word) {
		String[] chars = Arrays.copyOf(word.split(""), word.length()+1);
		chars[chars.length-1] = TrieNode.TERMINAL_SEG;
		return chars;
	}
	
	public static String[] ensureTerminal(String[] segments) {
		String[] terminal = null;
		if (segments.length > 0 && 
				segments[segments.length-1].equals(TrieNode.TERMINAL_SEG)) {
			terminal = segments;
		} else {
			terminal = Arrays.copyOf(segments, segments.length+1);
			terminal[terminal.length-1] = TrieNode.TERMINAL_SEG;
		}
		
		return terminal;
	}

	@JsonIgnore
	protected String getAllTerminalJoined() {
		return allTerminalsJoined;
	}
	
	protected void addToJoinedTerminals(String[] segments) {
		Logger tLogger = Logger.getLogger("ca.nrc.datastructure.trie.Trie.addToJoinedTerminals");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("segments="+String.join(",", segments));
		}
		if (segments!= null) {
			Matcher matcher = joinedTerminalsMatcher(segments);
			if (!matcher.find()) {
				allTerminalsJoined += ";"+String.join(",", segments)+";";			
			}
		}

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("EXITING segments="+String.join(",", segments));
		}
	}
	
	protected Matcher joinedTerminalsMatcher(String[] segments) {
		return joinedTerminalsMatcher(segments, false);
	}
	
	private Matcher joinedTerminalsMatcher(String[] segments, boolean partial) {
		String[] segmentsRegexQuoted = new String[segments.length];
		for (int ii=0; ii < segments.length; ii++) {
			segmentsRegexQuoted[ii] = Pattern.quote(segments[ii]);
		}
		String regex = String.join(",", segmentsRegexQuoted);
		if (partial) {
			regex = "[^;]*" + regex + "[^;]*";
		}
		regex = ";(" + regex + ");";
		Matcher matcher = Pattern.compile(regex).matcher(getAllTerminalJoined());
		return matcher;
	}
	
	public void traverseNodes(TrieNodeVisitor visitor) throws TrieException {
		traverseNodes(getRoot(), visitor, null);
	}

	public void traverseNodes(TrieNodeVisitor visitor, boolean onlyTerminals) throws TrieException {
		traverseNodes(getRoot(), visitor, onlyTerminals);
	}
	
	public void traverseNodes(TrieNode node, TrieNodeVisitor visitor) throws TrieException {
		traverseNodes(node, visitor, null);
	}

	public void traverseNodes(TrieNode node, TrieNodeVisitor visitor, 
		Boolean onlyTerminals) 
		throws TrieException {
		Logger tLogger = Logger.getLogger("ca.nrc.datastructure.trie.Trie.traverseNodes");
		if (node != null) {
			if (onlyTerminals == null) {
				onlyTerminals = true;
			}

			if (node.isTerminal()) {
				NodeTracer.trace(tLogger, node,
						"node is terminal; visiting it");
				visitor.visitNode(node);
			} else {
				NodeTracer.trace(
						tLogger, node,
						"node is NOT terminal");
				if (!onlyTerminals) {
					NodeTracer.trace(tLogger, node,
							"visiting non-terminal node");
					visitor.visitNode(node);

				}
				NodeTracer.trace(
						tLogger, node,
						"visiting node children");
				for (TrieNode aChild : childrenNodes(node)) {
					traverseNodes(aChild, visitor, onlyTerminals);
				}
			}
		}

		NodeTracer.trace(tLogger, node, "Exiting");
	}

	public long totalTerminals() throws TrieException {
		long total = info().totalTerminals;
		return total;
	}

	public long totalTerminals(String[] segments) throws TrieException {
		return computeTotalTerminals(segments);
	}

	protected long computeTotalTerminals() throws TrieException {
		return computeTotalTerminals(new String[0]);
	}
	
	protected long computeTotalTerminals(String[] segments) throws TrieException {
		TrieNode node = node4keys(segments, NodeOption.NO_CREATE);
		VisitorNodeCounter visitor = new VisitorNodeCounter();
		traverseNodes(node, visitor, true);
		return visitor.nodesCount;
	}

	public long totalOccurences() throws TrieException {
		long total = info().totalOccurences;
		return total;
	}

	public long totalOccurences(String[] segments) throws TrieException {
		long total = computeTotalOccurences(segments);
		return total;
	}

	protected long computeTotalOccurences() throws TrieException {
		return computeTotalOccurences(new String[0]);
	}
	
	protected long computeTotalOccurences(String[] segments) throws TrieException {
		TrieNode node = node4keys(segments, NodeOption.NO_CREATE);
		VisitorNodeCounter visitor = new VisitorNodeCounter();
		traverseNodes(node, visitor, true);
		return visitor.occurencesCount;
	}

	public void recomputeAggregateStats(String[] keys) throws TrieException {
		TrieNode node = retrieveNode_NoStatsRefresh(keys, NodeOption.NO_CREATE);
		if (node != null) {
			recomputeAggregateStats(node);
		}
	}

	public void recomputeAggregateStats(TrieNode node) throws TrieException {
		recomputeAggregateStats(node, null);
	}

	private void recomputeAggregateStats(TrieNode node, ProgressMonitor progMonitor) throws TrieException {
		Logger tLogger = Logger.getLogger("ca.nrc.datastructure.trie.Trie.recomputeAggregateStats");
		NodeTracer.trace(tLogger, node, "Invoked");

		if (node.isTerminal()) {
			// For a terminal node, there are no desendants to aggregate.
			// Just notify the progress monitor that we have processed
			// one terminal node out of all the trie's terminals.
			//
			NodeTracer.trace(tLogger, node, "Node is terminal");
			if (progMonitor != null) {
				progMonitor.stepCompleted();
			}
		} else {
			// This is a non-terminal node.
			// Aggregate over the node's children.
			//
			List<TrieNode> children = childrenNodes(node);
			NodeTracer.trace(tLogger, node, "Non-terminal node.");

			// First, Compute the aggregate stats of each child
			for (TrieNode aChild: children) {
				if (tLogger.isTraceEnabled()) {
					NodeTracer.trace(tLogger, node, "Computing aggregate of child "+aChild.keysAsString());
				}
				recomputeAggregateStats(aChild, progMonitor);
			}

			// Compute this node's aggregate stats based on
			// the stats of its children
			NodeTracer.trace(tLogger, node, "Done computing aggregate stats of children");
			node.frequency = 0;
			for (TrieNode aChild: children) {
				node.incrementFrequency(aChild.getFrequency());
			}
		}

		node.statsRefeshedOn = System.currentTimeMillis();

		NodeTracer.trace(tLogger, node, "exiting with node.frequency="+node.frequency);
	}

}
