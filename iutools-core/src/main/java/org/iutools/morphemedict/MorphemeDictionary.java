package org.iutools.morphemedict;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.iutools.corpus.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.iutools.elasticsearch.GenericESException;
import org.iutools.linguisticdata.LinguisticData;
import org.iutools.linguisticdata.Morpheme;
import org.iutools.linguisticdata.MorphemeException;
import org.iutools.linguisticdata.index.MorphemeIndex;

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
		return search(partialMorpheme, (String)null, (String)null, (Integer)null);
	}

	public List<MorphDictionaryEntry> search(String partialMorpheme, int maxExamples) throws MorphemeDictionaryException {
		return search(partialMorpheme, (String)null, (String)null, maxExamples);
	}

	public List<MorphDictionaryEntry> search(String partialMorpheme, String grammar, String meaning) throws MorphemeDictionaryException {
		return search(partialMorpheme, grammar, meaning, (Integer)null);
	}

	public List<MorphDictionaryEntry> search(String partialMorpheme, String grammar, String meaning, Integer maxExamples) throws MorphemeDictionaryException {
		Logger tLogger = LogManager.getLogger("org.iutools.morphemesearcher.MorphemeDictionary.wordsContainingMorpheme");
		tLogger.trace("partialMorpheme= "+partialMorpheme);

		if (maxExamples == null) {
			maxExamples = 20;
		}

		List<MorphDictionaryEntry> morphEntries = new ArrayList<MorphDictionaryEntry>();

		// Get all morphemes that match the search criteria
		List<Morpheme> morphemes = null;
		try {
			morphemes = matchingMorphemes(partialMorpheme, grammar, meaning);
		} catch (GenericESException e) {
			throw new MorphemeDictionaryException(e);
		}

		Set<String> morphemeIDs = null;
		for (Morpheme morpheme: morphemes) {
			String morphemeID = morpheme.id;
			List<MorphWordExample> examplesOneMorpheme = bestExamplesForMorphID(morpheme.id, maxExamples);

			try {
				MorphDictionaryEntry entry = new MorphDictionaryEntry(morphemeID, examplesOneMorpheme);
				morphEntries.add(entry);
			} catch (MorphemeException e) {
				throw new MorphemeDictionaryException(e);
			}
		}

		return morphEntries;
	}

	private List<Morpheme> matchingMorphemes(String canonicalForm, String grammar, String meaning) throws GenericESException {
		MorphemeIndex index = new MorphemeIndex();
		List<Morpheme> hits = index.searchMorphemes(canonicalForm, grammar, meaning);
		return hits;
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
		Collections.sort(examples, new MorphExampleComparator());
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

	public class MorphExampleComparator implements Comparator<MorphWordExample> {
	    @Override
	    public int compare(MorphWordExample a, MorphWordExample b) {
	    	int comp = Double.compare(b.getScore(), a.getScore());
	    	if (comp == 0) {
	    		// If the two examples have the same score, favour the one that
				// is shorter.
	    		comp = Integer.compare(a.word.length(), b.word.length());
			}
	    	if (comp == 0) {
	    		// If the two words have same score and length, compare using
				// alphabetical order
				comp = a.word.compareToIgnoreCase(b.word);
			}
	    	return comp;
	    }
	}

	/** Change the order of a list of word examples, so that the examples
	 * with the same root are balanced across the list.
	 * This avoids a situation where most of the examples at the top of the
	 * list have the same root.
	 */
	protected List<MorphWordExample> balanceExamplesByRoots(
		List<MorphWordExample> wordExamples, Integer maxExamples) {
		Logger logger = LogManager.getLogger("org.iutools.morphemedict.MorphemeDictionary.balanceExamplesByRoots");
		
		List<MorphWordExample> balanced = new ExamplesRootsBalancer(wordExamples, maxExamples).balance();
		return balanced;
	}
}


