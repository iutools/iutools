package org.iutools.morphemedict;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.nrc.debug.Debug;
import ca.nrc.json.PrettyPrinter;
import org.iutools.corpus.*;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.iutools.linguisticdata.LinguisticData;
import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.linguisticdata.MorphemeException;
import org.iutools.morph.*;
import org.iutools.morph.r2l.DecompositionState.DecompositionExpression;
import org.iutools.morph.MorphologicalAnalyzerException;
import org.iutools.morph.r2l.MorphologicalAnalyzer_R2L;

public class MorphemeDictionary {
	
	protected CompiledCorpus corpus = null;
	protected int nbWordsToBeDisplayed = 20;
	protected int maxNbInitialCandidates = 100;
	static LinguisticData _linguisticData = null;
	
	public MorphemeDictionary() throws MorphemeDictionaryException {
		Logger tLogger = LogManager.getLogger("org.iutools.morphemesearcher.MorphemeDictionary.constructor");
		try {
			useCorpus(new CompiledCorpusRegistry().getCorpus());
		} catch (IOException | CompiledCorpusRegistryException | CompiledCorpusException e) {
			throw new MorphemeDictionaryException(e);
		}
	}

	protected LinguisticData linguisticData() {
		if (_linguisticData == null) {
			_linguisticData = LinguisticData.getInstance();
		}
		return _linguisticData;
	}

	public void useCorpus(CompiledCorpus _corpus) throws IOException {
		corpus = _corpus;
	}
	
	public void setNbDisplayedWords(int n) {
		this.nbWordsToBeDisplayed = n;
	}

	public List<MorphDictionaryEntry> search(String partialMorpheme) throws MorphemeDictionaryException {
		return search(partialMorpheme, 100);
	}

	public List<MorphDictionaryEntry> search(String partialMorpheme, Integer maxExamples) throws MorphemeDictionaryException {
		Logger tLogger = LogManager.getLogger("org.iutools.morphemesearcher.MorphemeDictionary.wordsContainingMorpheme");
		tLogger.trace("partialMorpheme= "+partialMorpheme);

		List<MorphDictionaryEntry> morphEntries = new ArrayList<MorphDictionaryEntry>();

		// Get all morphemes whose canonical form contains the input partial
		// morpheme
		Set<String> morphemeIDs = canonicalMorphemesContaining(partialMorpheme);

		for (String morphemeID: morphemeIDs) {
			List<MorphWordExample> examplesOneMorpheme = bestExamplesForMorphID(morphemeID, maxExamples);

			try {
				MorphDictionaryEntry entry = new MorphDictionaryEntry(morphemeID, examplesOneMorpheme);
				morphEntries.add(entry);
			} catch (MorphemeException e) {
				throw new MorphemeDictionaryException(e);
			}
		}

		return morphEntries;
	}

	public List<MorphWordExample> bestExamplesForMorphID(String morphemeID, Integer maxExamples) throws MorphemeDictionaryException {
		Logger logger = LogManager.getLogger("org.iutools.morphemedict.MorphemeDictionary.bestExamplesForMorphID");
		List<MorphWordExample> examples = wordExamples4morpheme(morphemeID);
		if (logger.isTraceEnabled()) {
			logger.trace("initial examples for morphemeID="+morphemeID+":");
			for (MorphWordExample example: examples) {
				logger.trace("  "+example.word+" (score: "+example.getScore()+")");
			}
		}
		examples = sortExamplesByScore(examples);
		examples = balanceExamplesByRoots(examples, maxExamples);
		return examples;
	}

	/**
	 * Sort word examples for a same morpheme by their score.
	 */
	private List<MorphWordExample> sortExamplesByScore(List<MorphWordExample> examples) {
		Collections.sort(examples, (e1, e2) ->
		{
			int cmp = Double.compare(e2.getScore(), e1.getScore());
			if (cmp == 0) {
				// If both morphemes score, favour the one that is shortest
				cmp = Integer.compare(e1.word.length(), e2.word.length());
			}
			return cmp;
		}
		);
		return examples;
	}

	private List<MorphWordExample> wordExamples4morpheme(String morphemeID) throws MorphemeDictionaryException {
		List<MorphWordExample> examples = new ArrayList<MorphWordExample>();
		try {
			List<WordInfo> winfos = corpus.wordsContainingMorpheme(morphemeID, 100, "frequency:desc");
			for (WordInfo aWinfo: winfos) {
				examples.add(new MorphWordExample(aWinfo, morphemeID));
			}

		} catch (CompiledCorpusException e) {
			throw new MorphemeDictionaryException(e);
		}
		return examples;
	}

	private List<MorphDictionaryEntry> sortMorphemeDictEntries(List<MorphDictionaryEntry> morphEntries) {
		Collections.sort(morphEntries, (e1, e2) ->
			{
				int cmp = Integer.compare(e2.words.size(), e1.words.size());
				if (cmp == 0) {
					// If both morphemes have same number of example words, favour
					// the one that is shortest
					cmp = Integer.compare(e1.morphemeWithId.length(), e2.morphemeWithId.length());
				}
				return cmp;
			}
		);
		return morphEntries;
	}


	private Set<String> canonicalMorphemesContaining(String partialMorpheme) {
		Pattern pattMorph = Pattern.compile("^("+partialMorpheme+"[^\\/]*\\/)");
		Set<String> canonicals = new HashSet<String>();
		for (String morphID: linguisticData().allMorphemeIDs()) {
			Matcher matcher = pattMorph.matcher(morphID);
			if (matcher.find()) {
				canonicals.add(morphID);
			}
		}
		return canonicals;
	}


	public static Comparator<MorphWordExample> ScoredExamplesComparator = new Comparator<MorphWordExample>() {

		public int compare(MorphWordExample s1, MorphWordExample s2) {
			if (s1.getScore() < s2.getScore())
				return 1;
			else if (s1.getScore() > s2.getScore())
				return -1;
			else
				return 0;
	   }};	

	public class WordFreqComparator implements Comparator<MorphWordExample> {
	    @Override
	    public int compare(MorphWordExample a, MorphWordExample b) {
	    	if (a.getScore() > b.getScore())
	    		return -1;
	    	else if (a.getScore() < b.getScore())
				return 1;
	    	else 
	    		return a.word.compareToIgnoreCase(b.word);
	    }
	}

	/** Change the order of a list of word examples, so that the examples
	 * with the same root are balanced across the list.
	 * This avoids a situation where most of the examples at the top of the
	 * list have the same root.
	 */
	protected List<MorphWordExample> balanceExamplesByRoots(
		List<MorphWordExample> wordExamples, Integer maxExamples) {
		if (maxExamples == null) {
			maxExamples = 100;
		}
		List<MorphWordExample> balancedList = new ArrayList<MorphWordExample>();
		Deque<MorphWordExample> remaining = new ArrayDeque<MorphWordExample>();
		remaining.addAll(wordExamples);
		Map<String,Integer> rootCounts = new HashMap<String,Integer>();
		int highestRootCount = 0;
		int lowestRootCount = 0;
		boolean keepGoing = true;

		while (keepGoing) {
			Deque<MorphWordExample> skippedExamples = new ArrayDeque<MorphWordExample>();
			Set<String> skippedRoots = new HashSet<String>();
			// Inspect each of the remaining examples, and either add it to
			// the balanced list, or to a list of examples that are skipped for now
			while (!remaining.isEmpty()) {
				MorphWordExample currExample = remaining.pop();
				String currRoot = currExample.root;
				if (!rootCounts.containsKey(currRoot)) {
					rootCounts.put(currRoot, 0);
				}
				Integer currRootCount = rootCounts.get(currRoot);
				if (currRootCount == highestRootCount && lowestRootCount < highestRootCount) {
					// The list is not balanced at the moment and adding this example
					// would decrease balance even further. So skip it.
					skippedExamples.add(currExample);
					skippedRoots.add(currRoot);
				} else {
					// This example does not decrease balance. So add it to the
					// balanced list.
					balancedList.add(currExample);
					currRootCount++;
					rootCounts.put(currRoot, currRootCount);
					if (currRootCount > highestRootCount) {
						highestRootCount = currRootCount;
					}
				}
				if (balancedList.size() >= maxExamples) {
					keepGoing = false;
					break;
				}
			}
			Collection<Integer> rootCountVals = rootCounts.values();
			if (!rootCountVals.isEmpty()) {
				lowestRootCount = Collections.min(rootCounts.values());
			}
			remaining = skippedExamples;
			if (remaining.isEmpty()) {
				keepGoing = false;
			} else if (skippedRoots.size() <= 1) {
				// All the skipped examples have the same root. So
				// add them to the balanced list
				balancedList.addAll(skippedExamples);
				keepGoing = false;
			}
		}

		return balancedList;
	}
}


