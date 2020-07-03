package ca.nrc.datastructure.trie.visitors;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.apache.log4j.Logger;

import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.string.StringUtils;

public  class VisitorFindMostFrequentTerminals extends TrieNodeVisitor {

	public Set<TrieNode> mostFrequentLst = new HashSet<TrieNode>();
	private long lowestFreqAmongTop = -1;
	private TrieNode topWithLowestFreq = null;
	private int n = 5;
	private HashSet<TrieNode> exclusions = new HashSet<TrieNode>();
	
	public VisitorFindMostFrequentTerminals(Integer n, TrieNode[] exclusions) {
		init_VisitorFindMostFrequentTerminals(n, exclusions);
	}

	private void init_VisitorFindMostFrequentTerminals(Integer _n, TrieNode[] _exclusions) {
		if (_n != null) {
			n = _n;
		}
		if (_exclusions != null) {
			for (TrieNode aNode: exclusions) {
				this.exclusions.add(aNode);
			}
		}
	}

	
	public TrieNode[] mostFrequentTerminals() {
		TrieNode[] terminals = new TrieNode[mostFrequentLst.size()];
		int ii = 0;
		for (TrieNode aNode: mostFrequentLst) {
			terminals[ii] = aNode;
			ii++;
		}
		
	    Arrays.sort(terminals, new Comparator<TrieNode>() {
	        @Override
	        public int compare(TrieNode o1, TrieNode o2) {
	        	if (o1.getFrequency() == o2.getFrequency())
	        		return 0;
	            return o1.getFrequency() < o2.getFrequency()? 1 : -1;
	        }
	    });
	    
	    return terminals;
	}

	@Override
	public void visitNode(TrieNode node) throws TrieException {
		if (node.isTerminal() && node.getFrequency() > lowestFreqAmongTop) {
			mostFrequentLst.add(node);
			pruneMostFrequentLst();
		}
	}

	private void pruneMostFrequentLst() {
		Logger tLogger = Logger.getLogger("ca.nrc.datastructure.trie.visitors.VisitorFindMostFrequentTerminals.pruneMostFrequentLst");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("n="+n+", lowestFreqAmongTop="+lowestFreqAmongTop+
				", mostFrequentLst="+
				StringUtils.join(mostFrequentLst.iterator(), ","));
		}
		if (mostFrequentLst.size() > n) {
			Long lowestFreq = null;
			TrieNode nodeWithLowestFreq = null;
			Long secondLowestFreq = null;
			
			// Find the nodes with the lowest and second lowest frequency
			//
			for (TrieNode aNode: mostFrequentLst) {
				long nodeFreq = aNode.getFrequency();
				tLogger.trace(
					"Looking at aNode="+aNode+"; "+
					"nodeFreq="+nodeFreq+", lowestFreq="+lowestFreq+
					", secondLowestFreq="+secondLowestFreq);
				if (lowestFreq == null || nodeFreq < lowestFreq) {
					secondLowestFreq = lowestFreq;
					lowestFreq = nodeFreq;
					nodeWithLowestFreq = aNode;
				} else {
					if (secondLowestFreq == null || nodeFreq <= secondLowestFreq) {
						secondLowestFreq = nodeFreq;
					}
				}
				tLogger.trace(
					"After processing node: lowestFreq="+lowestFreq+
					", secondLowestFreq="+secondLowestFreq);
				
			}
			

			tLogger.trace("About to remove node with lowest freq; "+
				"lowestFreq="+lowestFreq+", secondLowestFreq="+secondLowestFreq+
				", nodeWithLowestFreq="+nodeWithLowestFreq);

			// Remove the node with lowest frequency, and set the second 
			// lowest frequency as the new lowest
			mostFrequentLst.remove(nodeWithLowestFreq);
			if (n > 1) {
				lowestFreqAmongTop = secondLowestFreq;
			} else {
				lowestFreqAmongTop = 
					mostFrequentLst.iterator().next().getFrequency();
			}
		}
		
		tLogger.trace("Returning");
		
		return;
	}
}