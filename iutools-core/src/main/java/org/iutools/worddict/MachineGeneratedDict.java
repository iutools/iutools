package org.iutools.worddict;

import ca.nrc.datastructure.CloseableIterator;
import ca.nrc.json.PrettyPrinter;
import ca.nrc.string.StringUtils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.SentencePair;
import org.iutools.concordancer.tm.*;
import org.iutools.corpus.*;
import org.iutools.corpus.elasticsearch.CompiledCorpus_ES;
import org.iutools.datastructure.CloseableIteratorWrapper;
import org.iutools.morph.Decomposition;
import org.iutools.morph.DecompositionException;
import org.iutools.morph.MorphologicalAnalyzerException;
import org.iutools.morph.r2l.MorphologicalAnalyzer_R2L;
import org.iutools.morphrelatives.MorphRelativesFinder;
import org.iutools.morphrelatives.MorphRelativesFinderException;
import org.iutools.morphrelatives.MorphologicalRelative;
import org.iutools.nlp.StopWords;
import org.iutools.nlp.StopWordsException;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoder.*;
import org.iutools.script.TransCoderException;
import org.iutools.utilities.StopWatch;
import org.iutools.utilities.StopWatchException;
import org.iutools.worddict.MDictEntry.*;

import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Machine-generated Dictionary of Inuktitut words.
 *
 * For more info about how to use this class see the DOCUMENTATION TESTS section
 * of IUWordDictTest.
 *
 */
public class MachineGeneratedDict {

	public static enum WhatTerm {ORIGINAL, RELATED}

	private static MachineGeneratedDict _singleton = null;

	/** Maximum number of 'final' translations to be produced*/
	public Integer MAX_TRANSLATIONS = 5;
	/** Maximum number of 'provisional' translations (i.e. before pruning) to be
	 * produced.
	 */
	public Integer MAX_PROVISIONAL_TRANSLATIONS = 10;

	/** Maximum number of sentence pairs to look at when looking for translations */
	public Integer MAX_SENT_PAIRS = 50;

	/** Minimum number of sentence pairs to look at when looking for translations */
	public Integer MIN_SENT_PAIRS = null;

	/** A 'provisional' translation will not be kept unless we have at least that
	 * many sentence pairs that support it.
	 */
	public Integer MIN_REQUIRED_PAIRS_FOR_TRANSLATION = 1;

	public CompiledCorpus corpus = null;

	private static final String TAG = "strong";

	public MachineGeneratedDict() throws MachineGeneratedDictException {
		try {
			corpus = new CompiledCorpusRegistry().getCorpus();
		} catch (Exception e) {
			throw new MachineGeneratedDictException(e);
		}
	}

	public MachineGeneratedDict setMinMaxPairs(Integer min, Integer max) throws MachineGeneratedDictException {
		if (min != null && max != null && max < min) {
			throw new MachineGeneratedDictException(
				"Min number of pairs must be smaller or equal to the max."
			);
		}
		this.MIN_SENT_PAIRS = min;
		this.MAX_SENT_PAIRS = max;
		return this;
	}

	public MachineGeneratedDict setMaxTranslations(Integer max) {
		MAX_TRANSLATIONS = max;
		return this;
	}


	public MDictEntry entry4word(String word) throws MachineGeneratedDictException {
		return entry4word(word, (String)null, (Boolean)null, (Field[])null);
	}

	public MDictEntry entry4word(
		String word, Boolean sortRelatedWords)
		throws MachineGeneratedDictException {
		return entry4word(word, (String)null, sortRelatedWords, (Field[])null);
	}

	public MDictEntry entry4word(String word, String lang)
		throws MachineGeneratedDictException {
		return entry4word(word, lang, (Boolean)null, (Field[])null);
	}

	public MDictEntry entry4word(
		String word, Field... fieldsToPpulate)
		throws MachineGeneratedDictException {
		return entry4word(word, (String)null, (Boolean)null, fieldsToPpulate);
	}

	public MDictEntry entry4word(
		String word, Boolean fullRelatedWordEntries, Field... fieldsToPopulate) throws MachineGeneratedDictException {
		return entry4word(word, (String)null, fullRelatedWordEntries, fieldsToPopulate);
	}

	public MDictEntry entry4word(
		String word, String lang, Boolean fullRelatedWordEntries,
		Field... fieldsToPopulate)
		throws MachineGeneratedDictException {

		if (lang == null) {
			lang = "iu";
		}
		assertIsSupportedLanguage(lang);

		if (fieldsToPopulate == null) {
			fieldsToPopulate = Field.values();
		}
		MDictEntry entry = new MDictEntry(word);

		if (lang.equals("iu")) {
			entry = entry4word_IU(word, fullRelatedWordEntries, fieldsToPopulate);
		} else if (lang.equals("en")) {
			entry = entry4word_EN(word);
		}

		computeGlossaryEntries(entry);

		return entry;
	}

	private void computeGlossaryEntries(MDictEntry entry) throws MachineGeneratedDictException {
		List<String> words = new ArrayList<String>();
		words.add(entry.word);
		Collections.addAll(words, entry.relatedWords);
		if (entry.lang.equals("iu")) {
			try {
				words = (List<String>)TransCoder.ensureRoman(words);
			} catch (TransCoderException e) {
				throw new MachineGeneratedDictException(e);
			}
		}
		for (String aWord: words) {
			List<GlossaryEntry> glossEntries = Glossary.get().entries4word(entry.lang, aWord);
			if (!glossEntries.isEmpty()) {
				entry.addGlossEntries4word(aWord, entry.otherLang(), glossEntries);
			}
		}
	}

	private MDictEntry entry4word_IU(
		String word, Boolean fullRelatedWordEntries, Field... fieldsToPopulate) throws MachineGeneratedDictException {
		Logger logger = LogManager.getLogger("org.iutools.worddict.MachineGeneratedDict.entry4word_IU");
		StopWatch watch = new StopWatch().start();
		MDictEntry entry = new MDictEntry(word);
		Script inputScript = TransCoder.textScript(word);
		try {
			WordInfo winfo = corpus.info4word(entry.wordRoman);
			traceRunningTime(logger, entry, "retrieving word info", watch);
			if (winfo != null) {
				entry.setDecomp(winfo.topDecomposition());
			} else {
				// Word could not be found in the corpus.
				// At least see if we can fill the entry's decomposition field
				String[] decomp = decomposeWord(word);
				entry.setDecomp(decomp);
			}
			if (ArrayUtils.contains(fieldsToPopulate, Field.BILINGUAL_EXAMPLES) ||
				ArrayUtils.contains(fieldsToPopulate, Field.TRANSLATIONS)) {
				computeOrigWordTranslationsAndExamples(entry);
			}
			traceRunningTime(logger, entry, "acomputing ORIGINAL word translations", watch);
			if (ArrayUtils.contains(fieldsToPopulate, Field.RELATED_WORDS)) {
				computeRelatedWords(entry, fullRelatedWordEntries);
			}
			if (ArrayUtils.contains(fieldsToPopulate, Field.TRANSLATIONS) &&
				entry.bestTranslations.isEmpty()) {
				// We haven't found a translation for the original word.
				// So, look for translations of related words
				computeRelatedWordsTranslationsAndExamples(entry);
			}
			traceRunningTime(logger, entry, "computing RELATED words", watch);
			entry.sortAndPruneTranslations(
				MAX_TRANSLATIONS, MIN_REQUIRED_PAIRS_FOR_TRANSLATION);
			traceRunningTime(logger, entry, "sorting and pruning translations", watch);
		} catch (CompiledCorpusException | StopWatchException e) {
			throw new MachineGeneratedDictException(e);
		}

		entry.ensureScript(inputScript);

		return entry;
	}

	private void computeRelatedWordsTranslationsAndExamples(MDictEntry entry) throws MachineGeneratedDictException {
		Logger logger = LogManager.getLogger("org.iutools.worddict.MachineGeneratedDict.computeRelatedWordsTranslationsAndExamples");
		logger.trace("invoked");
		if (entry.relatedWords.length > 0) {
			List<String> relatedWords = new ArrayList<String>();
			Collections.addAll(relatedWords, entry.relatedWords);
			retrieveTranslationsAndExamples(entry, relatedWords);
		}
		return;
	}

	private void traceRunningTime(Logger logger, MDictEntry entry, String phase,
											StopWatch watch) throws StopWatchException {
		if (logger.isTraceEnabled()) {
			double totalSecs = 1.0 * watch.totalTime() / 1000;
			double lapSecs = 1.0 * watch.lapTime() / 1000;
			logger.trace("["+entry.wordRoman+"] After "+phase+": total secs="+totalSecs+" (lap secs: "+lapSecs+")");
		}
		return;
	}

	private MDictEntry entry4word_EN(String word)
		throws MachineGeneratedDictException {
		MDictEntry entry = new MDictEntry(word, "en");
		computeOrigWordTranslationsAndExamples(entry);
		entry.sortAndPruneTranslations(MAX_TRANSLATIONS, MIN_REQUIRED_PAIRS_FOR_TRANSLATION);
		return entry;
	}

	private void computeRelatedWords(MDictEntry entry,
												Boolean fullRelatedWordEntries) throws MachineGeneratedDictException {
		Logger logger = LogManager.getLogger("org.iutools.worddict.MachineGeneratedDict.computeRelatedWords");
		StopWatch sw = new StopWatch().start();
		if (fullRelatedWordEntries == null) {
			fullRelatedWordEntries = true;
		}
		try {
			MorphologicalRelative[] rels =
				new MorphRelativesFinder(corpus).findRelatives(entry.wordRoman);
			traceRunningTime(logger, entry, "finding relatives", sw);
			List<String> relatedWords = new ArrayList<String>();
			for (MorphologicalRelative aRel: rels) {
				relatedWords.add(aRel.getWord());
			}

			if (fullRelatedWordEntries) {
				List<MDictEntry> relWordEntries =
					retrieveRelatedWordEntries(relatedWords);
				traceRunningTime(logger, entry, "retrieving related word entries", sw);
				traceRunningTime(logger, entry, "retrieving related word entries", sw);
				relatedWords = sortWordsByEntryComprehensiveness(relWordEntries);
				traceRunningTime(logger, entry, "After sorting related word entries", sw);
			}
			traceRunningTime(logger, entry, "After possibly collecting related words translations", sw);

			entry.relatedWords = relatedWords.toArray(new String[0]);

		} catch (MorphRelativesFinderException | StopWatchException e) {
			throw new MachineGeneratedDictException(e);
		}

	}

	private List<MDictEntry> retrieveRelatedWordEntries(
		List<String> relatedWords)
		throws MachineGeneratedDictException {

		List<MDictEntry> wordEntries = new ArrayList<MDictEntry>();

		// By default, we only populate the DEFINITION field of related words.
		// However, if populateTranslationsField=true, then we also populate the
		// TRANSLATIONS field
		//
		Field[] fieldsToPopulate = new Field[] {Field.DEFINITION};
		for (String aWord: relatedWords) {
			wordEntries.add(
				this.entry4word(aWord, false, fieldsToPopulate));
		}

		return wordEntries;
	}

	/**
	 * Sort the list of words so that those that have a more detailed dictionary
	 * entry come first. For example, words that have non-empty list of bilingual
	 * examples of use will come first.
	 */
	private List<String> sortWordsByEntryComprehensiveness(
		List<MDictEntry> wordEntries) throws MachineGeneratedDictException {
		Collections.sort(wordEntries,
			(w1, w2) -> {
				int answer = 0;

				if (answer == 0) {
					boolean w1HasTranslations = w1.hasTranslationsForOriginalWord();
					boolean w2HasTranslations = w2.hasTranslationsForOriginalWord();
					if (w1HasTranslations && ! w2HasTranslations) {
						answer = -1;
					} else if (!w1HasTranslations && w2HasTranslations) {
						answer = 1;
					}
				}

				if (answer == 0) {
					int w1examples = 0;
					int w2examples = 0;
					try {
						w1examples = w1.bilingualExamplesOfUse().size();
						w2examples = w2.bilingualExamplesOfUse().size();
					} catch (MachineGeneratedDictException e) {
						throw new RuntimeException(e);
					}
					answer = -Integer.compare(w1examples, w2examples);
				}

				if (answer == 0) {
					boolean w1HasDecomp = (w1.morphDecomp.size() > 0);
					boolean w2HasDecomp = (w2.morphDecomp.size() > 0);
					if (w1HasDecomp && !w1HasDecomp) {
						answer = -1;
					} else if (!w1HasDecomp && w1HasDecomp) {
						answer = 1;
					}
				}

				return answer;
			});

		List<String> sortedWords = new ArrayList<String>();
		for (MDictEntry anEntry: wordEntries) {
			sortedWords.add(anEntry.word);
		}

		return sortedWords;
	}

	private void computeOrigWordTranslationsAndExamples(
		MDictEntry entry) throws MachineGeneratedDictException {
		Logger tLogger = LogManager.getLogger("org.iutools.worddict.MachineGeneratedDict.computeTranslationsAndExamples");
		List<String> justOneWord = new ArrayList<String>();
		justOneWord.add(entry.word);
 		retrieveTranslationsAndExamples(entry, justOneWord);
		return;
	}

	private void retrieveTranslationsAndExamples(
		MDictEntry entry, List<String> l1WordsCluster) throws MachineGeneratedDictException {
		Logger tLogger = LogManager.getLogger("org.iutools.worddict.MachineGeneratedDict.retrieveTranslationsAndExamples");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("word="+entry.word+"/"+entry.wordInOtherScript+", iuWordGroup.size()="+ l1WordsCluster.size()+", iuWordGroup="+ StringUtils.join(l1WordsCluster.iterator(), ", "));
		}
		Boolean isForRelatedWords = null;
		if (l1WordsCluster.size() > 1) {
			// We are looking for translations for a list of words that are related
			// to the original word
			isForRelatedWords = true;
		} else {
			// We are searching for translation of just one word. Is that the
			// original word (if not, it means we are searching for translations of
			// a single related word).
			String singleWord = l1WordsCluster.get(0);
			if (singleWord.equals(entry.word) ||
				singleWord.equals(entry.wordInOtherScript)) {
				isForRelatedWords = false;
			} else {
				isForRelatedWords = true;
			}
		}
		tLogger.trace("isForRelatedWords="+isForRelatedWords);

		Map<String, CloseableIterator<Alignment>> iterators = null;
		try {
			String l1 = entry.lang;
			String l2 = otherLang(l1);

			if (l1.equals("iu")) {
				// We can only search for translations of words in syllabics
				l1WordsCluster = (List<String>) TransCoder.ensureSyllabic(l1WordsCluster);
			}

			// For each word in the words cluster, create an iterator for iterating
			// through that word's translations.
			iterators =
				translationsIteratorsForWords(l1WordsCluster, l1, l2);
			Map<String,Boolean> wordHasRemainingAlignments = new HashMap<String,Boolean>();
			for (String aWord: iterators.keySet()) {
				wordHasRemainingAlignments.put(aWord, true);
			}
			Set<String> alreadySeenPair = new HashSet<String>();
			int totalPairs = 1;
			boolean keepGoing = true;

			// Pull translations for each word in the cluster in a round-robbin fashion.
			// This is to avoid a situation where the first few translations all are for
			// the same word in the cluster.
			while (keepGoing) {
				int totalWordWithRemainingAligments = 0;
				for (String l1Word: iterators.keySet()) {
					if (!wordHasRemainingAlignments.get(l1Word)) {
						continue;
					}

					// Pull one alignment from each word in turn, until we have enough
					// translations, or we run out of alignments
					CloseableIterator<Alignment> alignmentIter = iterators.get(l1Word);
					if (alignmentIter == null || !alignmentIter.hasNext()) {
						// For some reason, we may get a null iterator for certain words
						wordHasRemainingAlignments.put(l1Word, false);
						continue;
					}
					totalWordWithRemainingAligments++;
					Alignment alignment = alignmentIter.next();
					SentencePair bilingualAlignment = null;
					bilingualAlignment = alignment.sentencePair(l1, l2);
					if (bilingualAlignment.hasWordLevel()) {
						new WordSpotter(bilingualAlignment)
							.highlight(l1, l1Word, TAG, true);
					}
					tLogger.trace(
						"Processing l1Word=" + l1Word + ", pair #" + totalPairs +
						"=\n" + l1 + ": " + bilingualAlignment.getText(l1) +
						"\n" + l2 + ": " + bilingualAlignment.getText(l2));
					totalPairs =
						onNewSentencePair(entry, bilingualAlignment, alreadySeenPair,
							totalPairs, l1Word);
					if (enoughBilingualExamples(entry, totalPairs, l1WordsCluster.size())) {
						keepGoing = false;
						break;
					}
				}
				if (totalWordWithRemainingAligments == 0) {
					keepGoing = false;
				}
			}
		} catch (WordSpotterException | MachineGeneratedDictException |
			TransCoderException e) {
			throw new MachineGeneratedDictException(e);
		} finally {
			if (iterators != null) {
				for (CloseableIterator<Alignment> iter: iterators.values()) {
					try {
						iter.close();
					} catch (Exception e) {
					}
				}
			}
		}

		return;
	}

	private Map<String, CloseableIterator<Alignment>> translationsIteratorsForWords(
		List<String> l1Words, String l1, String l2) throws MachineGeneratedDictException {

		Map<String, CloseableIterator<Alignment>> iterators =
			new HashMap<String, CloseableIterator<Alignment>>();
		try {
			for (String l1Word : l1Words) {
				CloseableIterator<Alignment> iter =
					new TMFactory().makeTM().search(l1, l1Word, l2);
				iterators.put(l1Word, iter);
			}
		} catch (TranslationMemoryException e) {
			throw new MachineGeneratedDictException(e);
		}

		return iterators;
	}

	private String otherLang(String lang) throws MachineGeneratedDictException {
		return MDictEntry.otherLang(lang);
	}

	private void assertIsSupportedLanguage(String lang) throws MachineGeneratedDictException {
		MDictEntry.assertIsSupportedLanguage(lang);
	}

	private boolean enoughBilingualExamples(
		MDictEntry entry, int totalPairs, int numInputWords) throws MachineGeneratedDictException {
		Logger logger = LogManager.getLogger("org.iutools.worddict.MultilingualDidct.enoughBilingualExamples");
		logger.trace("totalPairs="+totalPairs+", numInputWords="+numInputWords);
		int minPairs = 0;
		if (MIN_SENT_PAIRS != null) {
			minPairs = (MIN_SENT_PAIRS / numInputWords) + 1;
		}
		int maxPairs = 0;
		if (MAX_SENT_PAIRS != null) {
			maxPairs = (MAX_SENT_PAIRS / numInputWords) + 1;
		}
		int pairsSoFar = entry.totalBilingualExamples();
		int translationsSoFar =
			entry.bestTranslations.size();
		logger.trace("MAX_PROVISIONAL_TRANSLATIONS="+MAX_PROVISIONAL_TRANSLATIONS+",minPairs="+minPairs+", maxPairs="+maxPairs);
		logger.trace("translationsSoFar="+translationsSoFar+", pairsSoFar="+pairsSoFar);
		boolean enough =
			(translationsSoFar >= MAX_PROVISIONAL_TRANSLATIONS || pairsSoFar >= maxPairs) &&
			(totalPairs >= minPairs);

		logger.trace("Returning enough="+enough);
		return enough;
	}

	private int onNewSentencePair(MDictEntry entry,
		SentencePair bilingualAlignment, Set<String> alreadySeenPair,
		int totalPairs, String isForRelatedWord) throws MachineGeneratedDictException {

		Logger tLogger = LogManager.getLogger("org.iutools.worddict.MachineGeneratedDict.onNewSentencePair");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("isForRelatedWord="+isForRelatedWord+", bilingualAlignment="+ PrettyPrinter.print(bilingualAlignment));
		}

		String l1 = entry.lang;
		String l2 = otherLang(l1);
		String[] highlightedPair = new String[] {
			bilingualAlignment.getText(l1),
			bilingualAlignment.getText(l2),
		};
		String bothText = String.join(" <--> ", highlightedPair);
		if (!alreadySeenPair.contains(bothText)) {
			alreadySeenPair.add(bothText);
			String l2Translation = WordSpotter.spotHighlight(
				TAG, bilingualAlignment.langText.get(l2));
			l2Translation = canonizeTranslation(l2, l2Translation);
			tLogger.trace("l2Translation="+l2Translation);
			if (l2Translation != null && !l2Translation.isEmpty()) {
				if (l2Translation == null) {
					entry.addBilingualExample("MISC", highlightedPair, isForRelatedWord);
				} else {
					tLogger.trace("Adding example for translation of word='" + entry.wordRoman + "''" +
						", l2Translation='" + l2Translation + "'");
						entry.addBilingualExample(l2Translation, highlightedPair, isForRelatedWord);
				}
				entry.addBilingualExample("ALL", highlightedPair, isForRelatedWord);
				totalPairs++;
			}
		}
 		return totalPairs;
	}

	public static String canonizeTranslation(String l2, String l2Translation) throws MachineGeneratedDictException {
		try {
			if (l2Translation != null) {
				l2Translation = StopWords.remove(l2, l2Translation);
				l2Translation = l2Translation.replaceAll("\\s+", " ");
				l2Translation = l2Translation.replaceAll("(^\\.+|\\.+$)", "");
				l2Translation = l2Translation.replaceAll("\\*\\s*\\*", "*");
				l2Translation = l2Translation.replaceAll("\\*+", "...");
				while (true) {
					String previous = l2Translation;
					l2Translation = l2Translation.replaceAll("\\.\\.\\. \\.\\.\\.", "...");
					if (l2Translation.equals(previous)) {
						break;
					}
				}
				l2Translation = l2Translation.replaceAll("^\\s*\\.\\.\\.\\s*|\\s*\\.\\.\\.\\s*$", "");
				l2Translation = l2Translation.replaceAll("(^ | $)", "");
			}
		} catch (StopWordsException e) {
			throw new MachineGeneratedDictException(e);
		}
		return l2Translation;
	}

	public Pair<CloseableIterator<String>,Long> searchIter(String partialWord) throws MachineGeneratedDictException {
		return searchIter(partialWord, (String)null);
	}

	public Pair<List<String>,Long> search(String partialWord, String lang, Integer maxHits)
		throws MachineGeneratedDictException, TranslationMemoryException {
		List<String> hits = new ArrayList<String>();

		Pair<CloseableIterator<String>,Long> results = searchIter(partialWord, lang);
		Long totalHits = results.getRight();
		try (CloseableIterator<String> hitsIter = results.getLeft()) {
			int count = 0;
			while (hitsIter.hasNext()) {
				count++;
				String hit = hitsIter.next();
				try {
					hit = TransCoder.ensureSameScriptAsSecond(hit, partialWord);
				} catch (TransCoderException e) {
					throw new MachineGeneratedDictException(e);
				}
				hits.add(hit);
				if (maxHits != null && hits.size() == maxHits) {
					break;
				}
			}
		} catch (Exception e) {
			throw new MachineGeneratedDictException(e);
		}

		hits = sortHits(hits);
		adjustPartialWordInHits(hits, partialWord, lang);

		totalHits = Math.max(totalHits, hits.size());

		return Pair.of(hits, totalHits);
	}

	/** Ensure that:
	 * - partialWord is in the list of hits, IF it is a valid IU word
	 * - comes first IF it is in the list*/
	private void adjustPartialWordInHits(List<String> hits, String partialWord, String lang) throws MachineGeneratedDictException, TranslationMemoryException {
		String partialWordID = new WordInfo(partialWord).getIdWithoutType();
		if (!hits.contains(partialWordID)) {
			// The top list of hits did not contain an exact match.
			// Check to see if the exact match COULD have been found if
			// we had gone further, OR if the word analyzes as an IU word
			try {
				if (wordExists(partialWord, lang) ||
					(lang.equals("iu") &&  new MorphologicalAnalyzer_R2L().isDecomposable(partialWord))) {
					hits.add(0, partialWord);
				}
			} catch (MorphologicalAnalyzerException e) {
				throw new MachineGeneratedDictException(e);
			}
		}

		if (hits.contains(partialWordID)) {
			// If we found an exact match, make sure it comes first.
			hits.remove(partialWordID);
			hits.add(0, partialWordID);
		}
	}

	private List<String> sortHits(List<String> hits) {
		Collections.sort(hits, (h1, h2) -> {
			int comp = Integer.compare(h1.length(), h2.length());
			if (comp == 0) {
				comp = h1.compareTo(h2);
			}
			return comp;
		});
		return hits;
	}

	public Pair<CloseableIterator<String>,Long> searchIter(
		String partialWord, String lang) throws MachineGeneratedDictException {
		if (lang == null) {
			lang = "iu";
		}
		if (partialWord != null) {
			partialWord = partialWord.toLowerCase();
		}
		assertIsSupportedLanguage(lang);
		Pair<CloseableIterator<String>,Long> results = null;
		if (lang.equals("iu")) {
			results = search_IU(partialWord);
		} else if (lang.equals("en")) {
			results = search_EN(partialWord);
		}
		return results;
	}

	private Pair<CloseableIterator<String>, Long> search_IU(String partialWord) throws MachineGeneratedDictException {
		if (!partialWord.startsWith("^")) {
			partialWord = "^"+partialWord;
		}
		CloseableIterator<String> wordsIter = null;
		Long totalWords = null;
		try {
			partialWord = TransCoder.ensureScript(TransCoder.Script.ROMAN, partialWord);
			CompiledCorpus_ES.SearchOption[] options =
				new CompiledCorpus_ES.SearchOption[] {
					CompiledCorpus_ES.SearchOption.EXCL_MISSPELLED,
					CompiledCorpus_ES.SearchOption.WORD_ONLY
				};
			totalWords = corpus.totalWordsWithCharNgram(partialWord, options);
			wordsIter = corpus.wordsContainingNgram(partialWord, options);
		} catch (CompiledCorpusException | TransCoderException e) {
			throw new MachineGeneratedDictException(e);
		}
		return Pair.of(wordsIter, totalWords);
	}

	private boolean wordExists(String partialWord, String lang) throws MachineGeneratedDictException, TranslationMemoryException {
		assertIsSupportedLanguage(lang);
		Boolean exists = null;
		if (lang.equals("iu")) {
			exists = wordExists_IU(partialWord);
		} else {
			exists = wordExists_EN(partialWord);
		}
		return exists;
	}

	private Boolean wordExists_EN(String word) throws TranslationMemoryException {
		Iterator<Alignment> tmIter =
			new TMFactory().makeTM().search("en", word, "iu");
		return tmIter.hasNext();
	}

	private Boolean wordExists_IU(String word) throws MachineGeneratedDictException {
		word = TransCoder.ensureRoman(word);
		WordInfo winfo = null;
		try {
			winfo = corpus.info4word(word);
		} catch (CompiledCorpusException e) {
			throw new MachineGeneratedDictException(e);
		}
		return winfo != null;
	}

	private Pair<CloseableIterator<String>, Long> search_EN(String partialWord) throws MachineGeneratedDictException {
		// TODO-Alain: For now, we returns results that iterate through either:
		//
		//   - NO words at all, if the input word CANNOT be found in the TM
		//   - JUST the input word, if the input word CAN be found in the TM
		//
		// Note that this will not return any of the words that CONTAIN the input
		// word. So, eventually, we should compile an English CompiledCorpus_ES from
		// the TM content, and search for words in that corpus
		//
		CloseableIterator<String> wordsIter = null;
		Long totalWords = null;
		List<String> words = new ArrayList<String>();
		try (CloseableIterator<Alignment> tmIter =
				new TMFactory().makeTM().search("en", partialWord, "iu")) {
			if (tmIter.hasNext()) {
				words.add(partialWord);
			}
			wordsIter = new CloseableIteratorWrapper<String>(words.iterator());
			totalWords = new Long(words.size());
		} catch (Exception e) {
			throw new MachineGeneratedDictException(e);
		}
		return Pair.of(wordsIter, totalWords);
	}

	public String[] decomposeWord(String word) throws MachineGeneratedDictException {
		String decomp[] = new String[0];
		try {
			Decomposition[] decomps = new MorphologicalAnalyzer_R2L().decomposeWord(word);
			if (decomps != null && decomps.length > 0) {
				decomp = decomps[0].getMorphemes();
			}
		} catch (TimeoutException e) {
			// Leave the decomp to empty
		} catch (MorphologicalAnalyzerException | DecompositionException e) {
			throw new MachineGeneratedDictException(e);
		}
		return decomp;
	}
}