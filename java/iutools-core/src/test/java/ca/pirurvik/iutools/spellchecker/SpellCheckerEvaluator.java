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
	
	public SpellCheckerEvaluator() throws StringSegmenterException, FileNotFoundException, SpellCheckerException, ConfigException {
		checker = new SpellChecker();
		checker.setDictionaryFromCorpus();
	}

	
	public void onNewExample(SpellCheckerExample example) throws SpellCheckerException {
		Set<String> correctForms = example.acceptableCorrections;
		SpellingCorrection gotCorrection = checker.correctWord(example.wordToCheck);
		
		boolean shouldHaveBeenCorrect = (correctForms == null);
		
		if (gotCorrection.wasMispelled) {
			// Positive cases (i.e. where SpellChecker found an error)
			//
			if (!shouldHaveBeenCorrect) {
				onTruePositive(example, gotCorrection);
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
	
	

	private void evaluateCheckerSuggestions(SpellCheckerExample example, SpellingCorrection gotCorrection) {
		Integer rank = null;
		List<String> suggestions = gotCorrection.getPossibleSpellings();
		for (int ii=0; ii < suggestions.size(); ii++) {
			if (example.acceptableCorrections.contains(suggestions.get(ii))) {
				rank = ii;
				break;
			}
		}
		
		boolean rankBad = false;
		if (rank == null) {
			if (example.expMaxRank >= 0) {
				rankBad = true;
			}
		} else {
			if (rank > example.expMaxRank) {
				rankBad = true;
			}
		}
		if (rankBad) {
			addExampleWithBadRank(example, rank, suggestions);
		}

		if (rank != null) {
			sumRank += 1.0 * rank;
			totalNonNullRank++;
		}
		
		correctSpellingRank.put(example, rank);
	}

	private void addExampleWithBadRank(SpellCheckerExample example, 
			Integer rank, List<String> suggestions) {
		examplesWithBadRank.put(example, Pair.of(rank,suggestions));
	}


	private void onTruePositive(SpellCheckerExample example, SpellingCorrection gotCorrection) {
		truePos.add(example);
		evaluateCheckerSuggestions(example, gotCorrection);		
	}
	
	private void onFalsePositive(SpellCheckerExample example) {
		falsePos.add(example);
	}

	private void onTrueNegative(SpellCheckerExample example) {
		trueNeg.add(example);
	}


	private void onFalseNegative(SpellCheckerExample example) {
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
	
}
