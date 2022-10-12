package org.iutools.morphemedict;

import ca.nrc.dtrc.stats.FrequencyHistogram;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Given a list of MorphWordExamples, balance them by their roots
 */
public class ExamplesRootsBalancer {

	private Integer maxExamples = 100;
	private List<MorphWordExample> initialExamples = null;
	private List<MorphWordExample> balancedExamples = null;
	private FrequencyHistogram<String> balancedRootFreqs = null;
	private long lowestBalancedRootFreq = 0;
	private long highestBalancedRootFreq = Integer.MIN_VALUE;

	public ExamplesRootsBalancer(List<MorphWordExample> _examples, Integer _maxExamples) {
		this.initialExamples = _examples;
		if (_maxExamples != null) {
			this.maxExamples = _maxExamples;
		}
	}

	public List<MorphWordExample> balance() {
		Logger logger = LogManager.getLogger("org.iutools.morphemedict.ExamplesRootsBalancer.balance");
		logger.trace("Invoked with " + initialExamples.size() + " examples");
		balancedExamples = new ArrayList<MorphWordExample>();
		balancedRootFreqs = new FrequencyHistogram<String>();
		Deque<MorphWordExample> remaining = new ArrayDeque<MorphWordExample>();
		remaining.addAll(initialExamples);
		boolean keepGoing = true;
		while (keepGoing) {
			if (logger.isTraceEnabled()) {
				logger.trace("Starting next pass; Size of: initialExamples="+initialExamples.size()+
					", balancedExamples="+balancedExamples.size()+
					", remaining="+remaining.size());
			}
			if (remaining.isEmpty()) {
				keepGoing = false;
				break;
			}
			Deque<MorphWordExample> skipped = new ArrayDeque<MorphWordExample>();
			boolean atLeastOneAdded = false;
			for (MorphWordExample example: remaining) {
				logger.trace("  Looking at example with word="+example.word+", root="+example.root);
				if (wouldWorsenBalance(example)) {
					logger.trace("    Skipping example for now");
					skipped.add(example);
				} else {
					logger.trace("    Adding example to balanced list");
					addToBalancedList(example);
					atLeastOneAdded = true;
				}
				if (balancedExamples.size() >= maxExamples) {
					keepGoing = false;
					break;
				}
			}
			remaining = skipped;
			if (!atLeastOneAdded) {
				// all of the remaining examples made balance worse.
				// So at least, add the first one
				addToBalancedList(remaining.pop());
			}
		}
		return balancedExamples;
	}

	private void addToBalancedList(MorphWordExample example) {
		balancedExamples.add(example);
		balancedRootFreqs.updateFreq(example.root);
		lowestBalancedRootFreq = balancedRootFreqs.min();
		highestBalancedRootFreq = balancedRootFreqs.max();
	}

	private boolean wouldWorsenBalance(MorphWordExample example) {
		Boolean answer = null;
		long exampleRootFreq = balancedRootFreqs.frequency(example.root);
		if (exampleRootFreq == highestBalancedRootFreq) {
			// BAdding this example to the balanced list would increase the gap
			// between highest and lowest root frequency even further
			answer = true;
		}

		if (answer == null) {
			answer = false;
		}
		return answer;
	}

	private boolean isPerfectlyBalanced() {
		Boolean answer = false;
		if (balancedExamples.isEmpty() ||
			lowestBalancedRootFreq == highestBalancedRootFreq) {
			answer = true;
		}
		return answer;
	}
}
