package ca.pirurvik.iutools.spellchecker;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.Pair;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.json.PrettyPrinter;

public class SpellCheckerEvaluator {
	
	SpellChecker checker = null;
	
	Set<SpellCheckerExample> truePos = new HashSet<SpellCheckerExample>();
	Set<SpellCheckerExample> falsePos = new HashSet<SpellCheckerExample>();
	Set<SpellCheckerExample> trueNeg = new HashSet<SpellCheckerExample>();
	Set<SpellCheckerExample> falseNeg = new HashSet<SpellCheckerExample>();
	
	Map<SpellCheckerExample,Integer> correctSpellingRank = new HashMap<SpellCheckerExample,Integer>();
	Map<SpellCheckerExample,Pair<Integer,List<String>>> examplesWithBadRank = 
			new HashMap<SpellCheckerExample,Pair<Integer,List<String>>>();
	
	private double sumRank = 0.0;
	private int totalNonNullRank = 0;
	private int totalExamplesWithKnownCorrection = 0;	

	private Integer verbosity = 0;
	
	public SpellCheckerEvaluator() throws StringSegmenterException, FileNotFoundException, SpellCheckerException, ConfigException {
		init_SpellCheckerEvaluator(null);
	}
	
	public SpellCheckerEvaluator(SpellChecker _checker) throws StringSegmenterException, FileNotFoundException, SpellCheckerException, ConfigException {
		init_SpellCheckerEvaluator(_checker);
	}

	public void init_SpellCheckerEvaluator(SpellChecker _checker) throws StringSegmenterException, FileNotFoundException, SpellCheckerException, ConfigException {
		if (_checker == null) {
			_checker = new SpellChecker();
		}
		this.checker = _checker;
	}
	
	public void onNewExample(SpellCheckerExample example, 
			Boolean assumesCorrectionsLoadeInDic) throws SpellCheckerException {
		if (verbosity > 0) {
			System.out.print("\nProcessing example "+example.toString()+"\n");
		}
		
		Set<String> correctForms = example.acceptableCorrections;
		SpellingCorrection gotCorrection = checker.correctWord(example.wordToCheck);
		
		boolean shouldHaveBeenCorrect = !example.misspelled;
		
		if (gotCorrection.wasMispelled) {
			// Positive cases (i.e. where SpellChecker found an error)
			//
			if (!shouldHaveBeenCorrect) {
				onTruePositive(example, gotCorrection, 
						assumesCorrectionsLoadeInDic);
			} else {
				onFalsePositive(example);
			}
		} else {
			// Negative cases (i.e. where SpellChecker did NOT find an error)
			if (shouldHaveBeenCorrect) {
				onTrueNegative(example);
			} else {
				onFalseNegative(example);
			}
		};
	}
	
	private void evaluateCheckerSuggestions(SpellCheckerExample example, 
			SpellingCorrection gotCorrection, 
			Boolean assumesCorrectionsLoadeInDict) {
		
		if (!assumesCorrectionsLoadeInDict && 
				example.maxRankNOTAssumingInDict != null &&
				example.maxRankNOTAssumingInDict == -1) {
			// We don't expect this example to find the correction.
			// So don't validate its rank
			return;
		}
		
		if (!example.acceptableCorrections.isEmpty()) {
			Integer rank = null;
			List<String> suggestions = new ArrayList<String>();
			for (ScoredSpelling cand: gotCorrection.getScoredPossibleSpellings()) {
				suggestions.add(cand.spelling);
			}
			
			if (verbosity > 1) {
				System.out.println("   Got suggestions: "+
						String.join(",", suggestions));
			}
			
			for (int ii=0; ii < suggestions.size(); ii++) {
				if (example.acceptableCorrections.contains(suggestions.get(ii))) {
					rank = ii + 1;
					break;
				}
			}
						
			boolean rankBad = false;
			Integer maxRank = example.maxRankAssumingInDict;
			if (!assumesCorrectionsLoadeInDict) {
				// We are evaluating the examples WITHOUT having first ensured that 
				// the correct spellings are known to the dictionary.
				// The max rank expectations may be different in that situation
				//
				if (example.maxRankNOTAssumingInDict != null) {
					maxRank = example.maxRankNOTAssumingInDict;
				}
			}
	
			if (rank == null) {
				if (maxRank != null && maxRank > 0) {
					rankBad = true;
				}
			} else {
				if (maxRank == null || maxRank < 0 ||
						rank > maxRank) {
					rankBad = true;
				}
			}
			if (rankBad) {
				addExampleWithBadRank(example, rank, gotCorrection.scoredCandidates);
			}
	
			if (rank != null) {
				sumRank += 1.0 * rank;
				totalNonNullRank++;
			}
			
			if (verbosity > 1) {
				System.out.println("   Got rank="+rank+" (exp max: "+maxRank+")");
			}
			
			correctSpellingRank.put(example, rank);
		}
	}

	private void addExampleWithBadRank(SpellCheckerExample example, 
			Integer rank, List<ScoredSpelling> scoredSpellings) {
		
		List<String> suggestions = new ArrayList<String>();
		for (ScoredSpelling aSpelling: scoredSpellings) {
			suggestions.add(aSpelling.toString());
		}
		
		examplesWithBadRank.put(example, Pair.of(rank,suggestions));
	}


	private void onTruePositive(SpellCheckerExample example, 
			SpellingCorrection gotCorrection, 
			Boolean assumesCorrectionsLoadeInDic) {
		
		if (verbosity > 1) {
			System.out.println("   True Positive: input word was deemed mis-spelled as it should have");
		}		
		truePos.add(example);
		evaluateCheckerSuggestions(example, gotCorrection, assumesCorrectionsLoadeInDic);		
	}
	
	private void onFalsePositive(SpellCheckerExample example) {
		if (verbosity > 1) {
			System.out.println("   False Positive: input word was deemed mis-spelled but it should NOT have");
		}		
		falsePos.add(example);
	}

	private void onTrueNegative(SpellCheckerExample example) {
		if (verbosity > 1) {
			System.out.println("   True Negative: input word was deemed correctly spelled as it should have");
		}		
		trueNeg.add(example);
	}

	private void onFalseNegative(SpellCheckerExample example) {
		if (verbosity > 1) {
			System.out.println("   False Negative: input word was deemed correctly spelled but it should NOT have");
		}				
		falseNeg.add(example);
	}
	
	List<Pair<Integer,Integer>> correctSpellingRankHistogram() {
		Map<Integer,Integer> frequencies = new HashMap<Integer,Integer>();
		
		Integer maxRank = new Integer(-1);
		
		// Calculate frequency for all ranks
		//
		for (SpellCheckerExample example: correctSpellingRank.keySet()) {
			Integer rank = correctSpellingRank.get(example);
			if (rank != null && rank > maxRank) {
				maxRank = rank;
			}
			if (!frequencies.containsKey(rank)) {
				frequencies.put(rank, new Integer(0));
			}
			frequencies.put(rank, frequencies.get(rank) +1);
		}
		
		
		// Fill histogram entries for ranks with 0 frequency
		//
		for (int ii=0; ii <= maxRank; ii++) {
			Integer rank = new Integer(ii);
			if (!frequencies.containsKey(rank)) {
				frequencies.put(rank, new Integer(0));
			}
		}
		
		List<Pair<Integer,Integer>> histogram = new ArrayList<Pair<Integer,Integer>>();
		
		// Created a sorted list of ranks (excluding the null rank, as it is not
		// sorable)
		//
		List<Integer> ranksSorted = new ArrayList<Integer>();
		for (Integer aRank: frequencies.keySet()) {
			if (aRank != null) {
				ranksSorted.add(aRank);
			}
		}
		Collections.sort(ranksSorted);
		ranksSorted.add(null);
		
		for (Integer rank: ranksSorted) {
			Integer freq = 0;
			if (frequencies.containsKey(rank)) {
				freq = frequencies.get(rank);
			}
			histogram.add(Pair.of(rank, freq));
		}
		
		return histogram;
	}
	
	List<Pair<Integer,Double>> correctSpellingRankHistogramRelative() {
		List<Pair<Integer,Integer>> absoluteHistogram = correctSpellingRankHistogram();
		List<Pair<Integer,Double>> relativeHistogram = new ArrayList<Pair<Integer,Double>>();

		int numExamples = 0;
		for (Pair<Integer,Integer> absHistEntry: absoluteHistogram) {
			numExamples += absHistEntry.getSecond();
		}
		
		for (Pair<Integer,Integer> absHistEntry: absoluteHistogram) {
			Double relFreq = new Double(absHistEntry.getSecond() * 1.0 / numExamples);
			relativeHistogram.add(Pair.of(absHistEntry.getFirst(), relFreq));
		}
		
		return relativeHistogram;
	}
	
	public Double averageRank() {
		Double avg = null;
		if (totalNonNullRank > 0) {
			avg = sumRank / totalNonNullRank;
		}
				
		return avg;
	}

	public void setVerbose(Integer _verbosity) {
		this.verbosity  = _verbosity;		
	}
	
	public Double falsePositiveRate() {
		Double rate = 0.0;
		int inTotal = totalExamples();
		if (inTotal > 0) {
			rate = 1.0 * falsePos.size() / inTotal;
		}
		
		return rate;
	}

	public Double falseNegativeRate() {
		Double rate = 0.0;
		int inTotal = totalExamples();
		if (inTotal > 0) {
			rate = 1.0 * falseNeg.size() / inTotal;
		}
		
		return rate;
	}

	public int totalExamples() {
		int total = truePos.size() + falsePos.size() +
						trueNeg.size() + falseNeg.size();
		return total;
	}
}
