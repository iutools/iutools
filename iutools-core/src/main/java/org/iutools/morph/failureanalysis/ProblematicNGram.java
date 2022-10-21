package org.iutools.morph.failureanalysis;

import java.util.*;

import org.apache.commons.lang3.tuple.Pair;

import ca.nrc.string.StringUtils;
import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.WordInfo;
import org.iutools.sql.CloseableIterator;

public class ProblematicNGram {
	
	public static final int MAX_EXAMPLES = 20;

	public static enum SortBy {
		FS_RATIO_THEN_FAILURES, FS_RATIO_THEN_LENGTH_THEN_N_FAILURES, N_FAILURES};
	
	public String ngram = null;
	public long numFailures = 0;
	public long numSuccesses = 0;
	public long failureMass = -1;

	public List<Pair<String,Long>> failureExamples =
		new ArrayList<Pair<String,Long>>();
	Pair<String,Long> failureWithSmallestFreq =
		Pair.of("", new Long(-1));

	public List<Pair<String,Long>> successExamples =
		new ArrayList<Pair<String,Long>>();
	Pair<String,Long> successWithSmallestFreq =
		Pair.of("", new Long(-1));


	private String examplesComputedForCorpus = null;

	private Double failureSuccessRatio = null;

	public ProblematicNGram(String _ngram) {
		initProblematicNGram(_ngram);
	}

	private void initProblematicNGram(String _ngram) {
		this.ngram = _ngram;
	}

	public void addFailureExample(String word) {
		addFailureExample(word, null);
	}

	public void addFailureExample(String word, Long freq) {
		if (freq == null) {
			freq = new Long(-1);
		}
		Pair<String,Long> currFailure = Pair.of(word, freq);
		boolean currHasLowestFreq =
			(failureWithSmallestFreq == null ||
				freq < failureWithSmallestFreq.getRight());

 		if (failureExamples.size() < MAX_EXAMPLES) {
			failureExamples.add(currFailure);
		} else {
			// Too many examples. Insert this one if its frequency
			// is higher than the lowes-frequency example
			//
			if (!currHasLowestFreq) {
				failureExamples.remove(failureWithSmallestFreq);
				failureExamples.add(currFailure);
			}
		}
		failureWithSmallestFreq =
			exampleWithSmallestFreq(failureExamples);

	}

	public void addSuccessExample(String word) {
		addSuccessExample(word, null);
	}

	public void addSuccessExample(String word, Long freq) {
		if (freq == null) {
			freq = new Long(-1);
		}
		Pair<String,Long> currSuccess = Pair.of(word, freq);
		boolean currHasLowestFreq =
			(successWithSmallestFreq == null ||
				freq < successWithSmallestFreq.getRight());

		if (successExamples.size() < MAX_EXAMPLES) {
			successExamples.add(currSuccess);
		} else {
			// Too many examples. Insert this one if its frequency
			// is higher than the lowes-frequency example
			//
			if (!currHasLowestFreq) {
				successExamples.remove(successWithSmallestFreq);
				successExamples.add(currSuccess);
			}
		}
		successWithSmallestFreq =
			exampleWithSmallestFreq(failureExamples);
	}

	private Pair<String, Long> exampleWithSmallestFreq(
		List<Pair<String,Long>> examples) {
		Pair<String,Long> lowestFreqExample = null;
		for (Pair<String, Long> anExample: examples) {
			if (lowestFreqExample == null ||
				anExample.getRight() < lowestFreqExample.getRight()) {
				lowestFreqExample = anExample;
			}
		}
		return lowestFreqExample;
	}

	public Long getNumFailures() {
		return numFailures;
	}
	
	public void setFailureSuccessRatio(Double ratio) {
		this.failureSuccessRatio = ratio;
	}	
	
	public Double getFailSucceedRatio() {
		return this.failureSuccessRatio;
	}	

	public String toCSV() {

		String strFailures =
			StringUtils.join(failureExamples.iterator(), "; ")
			.replaceAll(",", "=");
		String strSuccesses =
			StringUtils.join(successExamples.iterator(), "; ")
			.replaceAll(",", "=");

		String csv =
			ngram+","+
			getFailSucceedRatio()+","+getNumFailures()+","+failureMass+","+
			strFailures+","+strSuccesses;
		return csv;
	}

	public static String csvHeaders() {
		String headers = 
			"ngram,fs_ratio,n_failures,failure_mass,failure_examples,success_examples";
		return headers;
	}

	public void computeExamples(CompiledCorpus corpus) throws MorphFailureAnalyzerException {
		try {
			String corpusName = corpus.getIndexName();
			if (examplesComputedForCorpus == null ||
				!examplesComputedForCorpus.equals(corpusName)) {
				Set<String> fields = new HashSet<String>();
				Collections.addAll(fields, new String[] {"word", "frequency"});

				CloseableIterator<WordInfo> iter =
					corpus.wordInfosContainingNgram(this.ngram, fields);
				final int MAX_TO_LOOK_AT = 1000;
				int wordCounter = 0;
				while (iter.hasNext() && wordCounter < MAX_TO_LOOK_AT) {
					wordCounter++;
					WordInfo winfo = iter.next();
					String word = winfo.word;
					long freq = winfo.frequency;
					if (winfo.decompositionsSample != null &&
						winfo.decompositionsSample.length > 0) {
						successExamples.add(Pair.of(word, freq));
					} else {
						failureExamples.add(Pair.of(word, freq));
					}
				}
				this.successExamples = pickMostFrequentExamples(this.successExamples);
				this.failureExamples = pickMostFrequentExamples(this.failureExamples);
			}
		} catch (CompiledCorpusException e) {
			throw new MorphFailureAnalyzerException(e);
		}
	}

	private List<Pair<String, Long>> pickMostFrequentExamples(
		List<Pair<String, Long>> examplesWithFreq) {
		List<String> picked = new ArrayList<String>();

		examplesWithFreq.sort((a, b) -> {
			int comp = b.getRight().compareTo(a.getRight());
			return comp;
		});

		examplesWithFreq = examplesWithFreq.subList(
			0, Math.min(examplesWithFreq.size(), MAX_EXAMPLES));


		return examplesWithFreq;
	}

	List<String> failedWords() {
		List<String> words = new ArrayList<String>();
		for (Pair<String,Long> anExample:failureExamples) {
			words.add(anExample.getLeft());
		}
		return words;
	}

	List<String> successfulWords() {
		List<String> words = new ArrayList<String>();
		for (Pair<String,Long> anExample:successExamples) {
			words.add(anExample.getLeft());
		}
		return words;
	}
}
