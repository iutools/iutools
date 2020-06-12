package ca.nrc.datastructure.trie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;

import ca.nrc.json.PrettyPrinter;

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
		
	public abstract TrieNode getRoot() throws TrieException;
	
	public abstract TrieNode getNode(String[] keys) throws TrieException;
		
	public abstract boolean contains(String[] segments) throws TrieException;
		
	public abstract TrieNode addExpression(String[] segments, String expression) 
		throws TrieException;
	
	protected String allTerminalsJoined = ";";
	
	public TrieNode add(String[] segments, String expression) 
			throws TrieException {
		addToJoinedTerminals(segments);
		return addExpression(segments, expression);
	}
		
    public long getSize() throws TrieException {
    	return getTerminals().length;
    }
    
	public TrieNode getNode(List<String> keys) throws TrieException {
		return getNode(keys.toArray(new String[keys.size()]));
	}

	public TrieNode[] getTerminals() throws TrieException {
		TrieNode[] allTerminals = getTerminals(getRoot());
		return allTerminals;
	}
	public TrieNode[] getTerminals(String[] segments) throws TrieException {
		return getTerminals(segments, null);
	}
	
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
			TrieNode node = this.getNode(segments);
			if (node==null)
				allTerminals = new TrieNode[0];
			else
				allTerminals = getTerminals(node);
		}
		
		return allTerminals;
	}
	
	public TrieNode[] getTerminalsMatchingNgram(String[] segments) 
			throws TrieException {
		
		List<TrieNode> terminalsLst = new ArrayList<TrieNode>();
		Matcher matcher = joinedTerminalsMatcher(segments, true);
		while (matcher.find()) {
			String terminalStr = matcher.group(1);
			String[] matchSegs = terminalStr.split(",");
			terminalsLst.add(getNode(matchSegs));
		}
		
		return terminalsLst.toArray(new TrieNode[terminalsLst.size()]);	
	}	
	
	public TrieNode[] getTerminals(TrieNode node) throws TrieException {
		List<TrieNode> allTerminalsLst = 
			new ArrayList<TrieNode>();
			
		collectAllTerminals(node, allTerminalsLst);
		
		return allTerminalsLst.toArray(new TrieNode[allTerminalsLst.size()]);
	}
	
	protected void collectAllTerminals(TrieNode node, 
			List<TrieNode> collected) throws TrieException {
		if (node.isTerminal()) {
			collected.add((TrieNode)node);
		} else {
			for (TrieNode aChild: childrenNodes(node)) {
				collectAllTerminals(aChild, collected);
			}
		}
	}
	
    private List<TrieNode> childrenNodes(TrieNode node) throws TrieException {
    	List<TrieNode> children = new ArrayList<TrieNode>();
    	for (String extension: node.childrenSegments()) {
    		TrieNode childNode = getNode(extendSegments(node.keys, extension ));
    		children.add(childNode);
    	}
    	
		return children;
	}

	private String[] extendSegments(String[] orig, String extension) {
		String[] extended = new String[orig.length+1];
		for (int ii=0; ii < orig.length; ii++) {
			extended[ii] = orig[ii];
		}
		extended[extended.length-1] = extension;
		
		return extended;
	}

	public long getNbOccurrences() throws TrieException {
    	TrieNode[] terminals = getTerminals();
    	long nbOccurrences = 0;
    	for (TrieNode terminal : terminals) {
    		nbOccurrences += terminal.getFrequency();
    	}
    	return nbOccurrences;
    }
    
	public TrieNode getMostFrequentTerminal() throws TrieException {
		return getMostFrequentTerminal(getRoot());
	}
    
	public TrieNode getMostFrequentTerminal(TrieNode node) throws TrieException {
		TrieNode mostFrequent = null;
		TrieNode[] terminals = getMostFrequentTerminals(1, node, null);
		if (terminals != null && terminals.length > 0) {
			mostFrequent = terminals[0];
		}
		return mostFrequent;
	}
	
	public TrieNode getMostFrequentTerminal(String[] segments) throws TrieException {
		TrieNode node = getNode(segments);
		return getMostFrequentTerminal(node);
	}
	
	public TrieNode[] getMostFrequentTerminals(int n) throws TrieException {
		return getMostFrequentTerminals(n, getRoot(), null);
	}	
	
	public TrieNode[] getMostFrequentTerminals(int n, String[] segments) throws TrieException {
		TrieNode node = getNode(segments);
		return getMostFrequentTerminals(n, node, null);
	}
	
	public TrieNode[] getMostFrequentTerminals(String[] segments) throws TrieException {
		TrieNode node = getNode(segments);
		return getMostFrequentTerminals(null, node, null);
	}

	public TrieNode[] getMostFrequentTerminals() throws TrieException {
		return getMostFrequentTerminals(null, getRoot(), null);
	}
	
	public TrieNode[] getMostFrequentTerminals(
			Integer n, TrieNode node) throws TrieException {
		return getMostFrequentTerminals(n, node, null);
	}

	public TrieNode[] getMostFrequentTerminals(
			Integer n, TrieNode node, 
			TrieNode[] exclusions) throws TrieException {
		if (exclusions == null) {
			exclusions = new TrieNode[0];
		}
		TrieNode[] terminals = getTerminals(node);
		for (TrieNode nodeToExclude : exclusions)
			terminals = (TrieNode[]) ArrayUtils.removeElement(terminals, nodeToExclude);
	    Arrays.sort(terminals, new Comparator<TrieNode>() {
	        @Override
	        public int compare(TrieNode o1, TrieNode o2) {
	        	if (o1.getFrequency() == o2.getFrequency())
	        		return 0;
	            return o1.getFrequency() < o2.getFrequency()? 1 : -1;
	        }
	    });
	    TrieNode[] mostFrequentTerminals;
	    if (n > terminals.length) {
	    	mostFrequentTerminals = Arrays.copyOfRange(terminals, 0, terminals.length);
	    } else {
	    	mostFrequentTerminals = Arrays.copyOfRange(terminals, 0, n);
	    }
		return mostFrequentTerminals;
	}	
	
	protected TrieNode getMostFrequentTerminalFromMostFrequentSequenceForRoot(String rootSegment) throws TrieException {
		String[] mostFrequentSequence = getMostFrequentSequenceForRoot(rootSegment);
		TrieNode node = this.getNode(mostFrequentSequence);
		TrieNode[] terminals = getTerminals(node);
		long max = 0;
		TrieNode mostFrequentTerminal = null;
		for (TrieNode terminal : terminals)
			if (terminal.getFrequency() > max) {
				max = terminal.getFrequency();
				mostFrequentTerminal = terminal;
			}
		return mostFrequentTerminal;
	}
	
	protected TrieNode getParentNode(TrieNode node) throws TrieException {
		return this.getParentNode(node.keys);
	}    
	
	protected TrieNode getParentNode(String[] keys) throws TrieException {
		if (keys.length==0)
			return null;
		else
			return this.getNode(Arrays.copyOfRange(keys, 0, keys.length-1));
	}
	
	public long getFrequency(String[] segments) throws TrieException {
		TrieNode node = this.getNode(segments);
		if (node != null)
			return node.getFrequency();
		else
			return 0;
	}
        
	/**
	 * 
	 * @param String rootKey
	 * @return String[] space-separated keys of the most frequent sequence of morphemes following rootSegment
	 * @throws TrieException 
	 */
	public String[] getMostFrequentSequenceForRoot(String rootKey) throws TrieException {
		Logger logger = Logger.getLogger("CompiledCorpus.getMostFrequentSequenceToTerminals");
		HashMap<String, Long> freqs = new HashMap<String, Long>();
		TrieNode rootSegmentNode = this.getNode(new String[] {rootKey});
		TrieNode[] terminals = getTerminals(rootSegmentNode);
		logger.debug("all terminals: "+terminals.length);
		for (TrieNode terminalNode : terminals) {
			//logger.debug("terminalNode: "+PrettyPrinter.print(terminalNode));
			String[] terminalNodeKeys = Arrays.copyOfRange(terminalNode.keys, 1, terminalNode.keys.length);
			freqs = computeFreqs(terminalNodeKeys,freqs,rootKey);
		}
		if (logger.isDebugEnabled()) {
			logger.debug("freqs: "+PrettyPrinter.print(freqs));
		}
		long maxFreq = 0;
		int minLength = 1000;
		String seq = null;
		String[] freqsKeys = freqs.keySet().toArray(new String[] {});
		for (int i=0; i<freqsKeys.length; i++) {
			String freqKey = freqsKeys[i];
			int nbKeys = freqKey.split(" ").length;
			if (freqs.get(freqKey)==maxFreq) {
				if (nbKeys<minLength) {
					maxFreq = freqs.get(freqKey);
					minLength = nbKeys;
					seq = freqKey;
				} 
			} else if (freqs.get(freqKey) > maxFreq) {
				maxFreq = freqs.get(freqKey);
				minLength = nbKeys;
				seq = freqKey;
			}
		}
		return (rootKey+" "+seq).split(" ");
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
		TrieNode node = this.getNode((rootSegment+" "+newCumulativeKeys).split(" "));
		long incr = node.getFrequency();
		if (!freqs.containsKey(newCumulativeKeys))
			freqs.put(newCumulativeKeys, new Long(incr));
		//else {
		//	freqs.put(newCumulativeKeys, new Long(freqs.get(newCumulativeKeys).longValue() + incr));
		//}
		freqs = _computeFreqs(newCumulativeKeys, remKeys, freqs, rootSegment);
		return freqs;
	}

	public String[] wordChars(String word) {
		String[] chars = Arrays.copyOf(word.split(""), word.length()+1);
		chars[chars.length-1] = "$";
		return chars;
	}
	
	protected String getAllTerminalJoined() {
		return allTerminalsJoined;
	}
	
	protected void addToJoinedTerminals(String[] segments) {
		Matcher matcher = joinedTerminalsMatcher(segments);
		if (!matcher.find()) {
			allTerminalsJoined += ";"+String.join(",", segments)+";";			
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
}
