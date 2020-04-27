package ca.inuktitutcomputing.morph.failureanalysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.morph.failureanalysis.ProblematicNGram.SortBy;
import ca.pirurvik.iutools.CompiledCorpus;
import ca.pirurvik.iutools.WordInfo;
import ca.pirurvik.iutools.text.ngrams.NgramCompiler;

/**
 * Use this class to identify ngrams that seem to cause words to not be 
 * analyzed by the morphological analyzer.
 * 
 * @author desilets
 *
 */
public class MorphFailureAnalyzer {

	private Integer minNgramLen = 3;
	private Integer maxNgramLen = 10;
	private Pattern pattNgramExclusion = null;
	
	private long totalSuccesses = 0;
	private long totalFailures = 0;
	
	Map<String,ProblematicNGram> ngramStats = 
		new HashMap<String,ProblematicNGram>();

	public MorphFailureAnalyzer() {
		initMorphFailureAnalyzer();
	}

	private void initMorphFailureAnalyzer() {
	}

	public void addWord(String word, boolean analyzesSuccessfully) {
		addWord(word, analyzesSuccessfully, null);
	}
	

	public void addWord(String word, boolean analyzesSuccessfully,
			Long freq) {
		if (!ignoreWord(word)) {		
			Set<String> ngrams = 
				new NgramCompiler()
					.setMin(minNgramLen)
					.setMax(maxNgramLen)
					.compile(word);
		
			if (analyzesSuccessfully) {
				onSuccesfulWord(word, ngrams, freq);
			} else {
				onFailingWord(word, ngrams, freq);
			}
		}
	}
	
	private boolean ignoreWord(String word) {
		boolean ignore = false;
		if (pattNgramExclusion != null &&
				pattNgramExclusion.matcher(word).matches()) {
			ignore = true;
		}
		return ignore;
	}

	private void onFailingWord(String word, Set<String> ngrams, Long freq) {
		totalFailures++;		
		for (String aNgram: ngrams) {
			ProblematicNGram stats = statsForNGram(aNgram);
			
			stats.numFailures++;
			
			if (freq != null) {
				if (stats.failureMass == -1) {
					stats.failureMass = 0;
				}
				stats.failureMass += freq;
			}
			
			stats.addFailureExample(word);
		}
	}

	private void onSuccesfulWord(String word, Set<String> ngrams, Long freq) {
		totalSuccesses++;
		for (String aNgram: ngrams) {
			ProblematicNGram stats = statsForNGram(aNgram);
			stats.numSuccesses++;
			stats.addSuccessExample(word);
		}
	}

	ProblematicNGram statsForNGram(String ngram) {
		if (!ngramStats.containsKey(ngram)) {
			ngramStats.put(ngram, new ProblematicNGram(ngram));
		}
		ProblematicNGram stats = ngramStats.get(ngram);
		
		return stats;
	}

	public List<String> failureExamplesFor(ProblematicNGram problem) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<String> successExamplesFor(ProblematicNGram problem) {
		// TODO Auto-generated method stub
		return null;
	}

	@JsonIgnore
	public List<ProblematicNGram> getProblems(SortBy sortBy) {
		List<ProblematicNGram> problems = new ArrayList<ProblematicNGram>();
		
		Iterator<ProblematicNGram> iter = ngramStats.values().iterator();
		while (iter.hasNext()) {
			ProblematicNGram problem = iter.next();
			if (problem.numFailures > 0) {
				problems.add(problem);
			}
		}		
		
		Comparator<ProblematicNGram> comparator = 
				getProblemComparator(sortBy);
		
		Collections.sort(problems, comparator);
		
		return problems;
	}

	private Comparator<ProblematicNGram> getProblemComparator(SortBy sortBy) {
		Comparator<ProblematicNGram> comparator  = null;
		
		if (sortBy == SortBy.FS_RATIO) {
			comparator = 
				(ProblematicNGram p1, ProblematicNGram p2) -> {
					int comp = p2.getFailSucceedRatio()
						.compareTo(p1.getFailSucceedRatio());
					if (comp == 0) {
						comp = 	p2.getNumFailures()
						.compareTo(p1.getNumFailures());
					}
					return comp;
				};
		} else {
			comparator = 
				(ProblematicNGram p1, ProblematicNGram p2) -> {
					int comp = p2.getNumFailures()
						.compareTo(p1.getNumFailures());
					if (comp == 0) {
						comp = p2.getFailSucceedRatio()
							.compareTo(p1.getFailSucceedRatio());
					}
					return comp;
				};
		}
				
		return comparator;
	}

	public void analyseFailures() {
		computeFSRatios();	
	}

	private void computeFSRatios() {
		for (ProblematicNGram problem: ngramStats.values()) {
			Double fsRatio = Double.MAX_VALUE;
			if (problem.numSuccesses > 0) {
				// Number of failures that contains this ngram, as a ratio
				// of all failures
				//
				Double failFreq = 1.0 * problem.numFailures / totalFailures;

				// Number of sucesses that contains this ngram, as a ratio
				// of all success		
				//
				Double successFreq = 1.0 * problem.numSuccesses / totalSuccesses;
				
				// Failure/Success ratio.
				// Indicates to what extent the ngram tends to be more frequent 
				// in failing words than in successful ones (in relative terms)
				//
				fsRatio = failFreq / successFreq;
			}
			
			problem.setFailureSuccessRatio(fsRatio);
		}
	}

	public MorphFailureAnalyzer setMinNgramLen(int minLen) {
		this.minNgramLen = minLen;
		return this;
	}

	public MorphFailureAnalyzer setMaxNgramLen(int maxLen) {
		this.maxNgramLen = maxLen;
		return this;
	}

	public MorphFailureAnalyzer setExclude(String regexNgramExclusion) {
		this.pattNgramExclusion  = 
			Pattern.compile("^.*"+regexNgramExclusion+".*$");
		return this;
	}
}
