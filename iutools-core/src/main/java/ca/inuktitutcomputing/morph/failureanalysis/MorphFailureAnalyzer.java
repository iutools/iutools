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
	
	Map<String,ProblematicNGram> ngramStats = 
		new HashMap<String,ProblematicNGram>();

	public MorphFailureAnalyzer() {
		initMorphFailureAnalyzer();
	}

	private void initMorphFailureAnalyzer() {
	}
	
	public void addWord(String word, boolean analyzesSuccessfully) {
		Set<String> ngrams = 
			new NgramCompiler()
				.setMin(minNgramLen)
				.setMax(maxNgramLen)
				.compile(word);
	
		if (analyzesSuccessfully) {
			onSuccesfulWord(word, ngrams);
		} else {
			onFailingWord(word, ngrams);
		}
	}
	
	private void onFailingWord(String word, Set<String> ngrams) {
		for (String aNgram: ngrams) {
			ProblematicNGram stats = statsForNGram(aNgram);
			stats.numFailures++;
			stats.addFailureExample(word);
		}
	}

	private void onSuccesfulWord(String word, Set<String> ngrams) {
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
				(ProblematicNGram p1, ProblematicNGram p2) ->
					p2.getFailSucceedRatio()
						.compareTo(p1.getFailSucceedRatio());
		} else {
			comparator = 
				(ProblematicNGram p1, ProblematicNGram p2) ->
					p2.getNumFailures()
						.compareTo(p1.getNumFailures());
			
		}
				
		return comparator;
	}

	public void analyseFailures() {
		// TODO Auto-generated method stub
		
	}

	public MorphFailureAnalyzer setMinNgramLen(int minLen) {
		this.minNgramLen = minLen;
		return this;
	}

	public MorphFailureAnalyzer setMaxNgramLen(int maxLen) {
		this.maxNgramLen = maxLen;
		return this;
	}
}
