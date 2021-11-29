package org.iutools.worddict;

import ca.nrc.string.StringUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.SentencePair;
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.concordancer.tm.WordSpotter;
import org.iutools.concordancer.tm.WordSpotterException;
import org.iutools.corpus.*;
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
import org.iutools.worddict.MultilingualDictEntry.*;

import java.util.*;
import java.util.concurrent.TimeoutException;

/**
 * Dictionary of Inuktitut words.
 *
 * For more info about how to use this class see the DOCUMENTATION TESTS section
 * of IUWordDictTest.
 *
 */
public class MultilingualDict {

	private static final long MAX_WORDS = 20;
	private static MultilingualDict _singleton = null;

	public static int MAX_TRANSLATIONS = 5;
	public static int MAX_SENT_PAIRS = 20;


	public CompiledCorpus corpus = null;

	private static final String TAG = "strong";

	// Private constructor.
	// There should only be one instance of MultilingualDict and you should get it
	// by invoking MultilingualDict.getInstance();
	private MultilingualDict() throws MultilingualDictException {
		try {
			corpus = new CompiledCorpusRegistry().getCorpus();
		} catch (Exception e) {
			throw new MultilingualDictException(e);
		}
	}

	public static MultilingualDict getInstance() throws MultilingualDictException {
		if (_singleton == null) {
			_singleton = new MultilingualDict();
		}
		return _singleton;
	}

	public MultilingualDictEntry entry4word(String word) throws MultilingualDictException {
		return entry4word(word, (String)null, (Boolean)null, (Field[])null);
	}

	public MultilingualDictEntry entry4word(
		String word, Boolean sortRelatedWords)
		throws MultilingualDictException {
		return entry4word(word, (String)null, sortRelatedWords, (Field[])null);
	}

	public MultilingualDictEntry entry4word(String word, String lang)
		throws MultilingualDictException {
		return entry4word(word, lang, (Boolean)null, (Field[])null);
	}


	public MultilingualDictEntry entry4word(
		String word, Field... fieldsToPpulate)
		throws MultilingualDictException {
		return entry4word(word, (String)null, (Boolean)null, fieldsToPpulate);
	}

	public MultilingualDictEntry entry4word(
		String word, Boolean fullRelatedWordEntries, Field... fieldsToPopulate) throws MultilingualDictException {
		return entry4word(word, (String)null, fullRelatedWordEntries, fieldsToPopulate);
	}


	public MultilingualDictEntry entry4word(
		String word, String lang, Boolean fullRelatedWordEntries,
		Field... fieldsToPopulate)
		throws MultilingualDictException {

		if (lang == null) {
			lang = "iu";
		}
		assertIsSupportedLanguage(lang);

		if (fieldsToPopulate == null) {
			fieldsToPopulate = Field.values();
		}
		MultilingualDictEntry entry = new MultilingualDictEntry(word);

		if (lang.equals("iu")) {
			entry = entry4word_IU(word, fullRelatedWordEntries, fieldsToPopulate);
		} else if (lang.equals("en")) {
			entry = entry4word_EN(word);
		}

		return entry;
	}

	private MultilingualDictEntry entry4word_IU(
		String word, Boolean fullRelatedWordEntries, Field... fieldsToPopulate) throws MultilingualDictException {
		MultilingualDictEntry entry = new MultilingualDictEntry(word);
		Script script = TransCoder.textScript(word);
		try {
			WordInfo winfo = corpus.info4word(entry.wordRoman);
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
				computeOrigWordTranslationsAndExamples(entry, script);
			}
			if (ArrayUtils.contains(fieldsToPopulate, Field.RELATED_WORDS)) {
				computeRelatedWords(entry, fullRelatedWordEntries);
			}
		} catch (CompiledCorpusException e) {
			throw new MultilingualDictException(e);
		}

		entry.sortTranslations();

		entry.ensureIUScript(TransCoder.textScript(word));

		return entry;
	}

	private MultilingualDictEntry entry4word_EN(String word)
		throws MultilingualDictException {
		MultilingualDictEntry entry = new MultilingualDictEntry(word, "en");
		computeOrigWordTranslationsAndExamples(entry);
		entry.sortTranslations();
		return entry;
	}

	private void computeRelatedWords(MultilingualDictEntry entry,
		Boolean fullRelatedWordEntries) throws MultilingualDictException {
		if (fullRelatedWordEntries == null) {
			fullRelatedWordEntries = true;
		}
		try {
			MorphologicalRelative[] rels =
				new MorphRelativesFinder(corpus).findRelatives(entry.wordRoman);
			List<String> relatedWords = new ArrayList<String>();
			for (MorphologicalRelative aRel: rels) {
				relatedWords.add(aRel.getWord());
			}

			if (fullRelatedWordEntries) {
				List<MultilingualDictEntry> relWordEntries = retrieveRelatedWordEntries(relatedWords);
				relatedWords = sortWordsByEntryComprehensiveness(relWordEntries);
				collectRelatedWordTranslations(entry, relWordEntries);
			}

			entry.relatedWords = relatedWords.toArray(new String[0]);

		} catch (MorphRelativesFinderException e) {
			throw new MultilingualDictException(e);
		}

	}

	private void collectRelatedWordTranslations(
		MultilingualDictEntry origWordEntry) throws MultilingualDictException {
		collectRelatedWordTranslations(origWordEntry, (List<MultilingualDictEntry>) null);;
	}

	private void collectRelatedWordTranslations(
		MultilingualDictEntry origWordEntry, List<MultilingualDictEntry> relWordEntries) throws MultilingualDictException {

		Logger tLogger = Logger.getLogger("org.iutools.worddict.MultilingualDict.collectRelatedWordTranslations");


		if (relWordEntries == null) {
			relWordEntries = new ArrayList<MultilingualDictEntry>();
		}
		tLogger.trace("\n\n\ninvoked");
		for (MultilingualDictEntry entry: relWordEntries) {
			origWordEntry.addRelatedWordTranslations(entry);
		}
		return;
	}

	private List<MultilingualDictEntry> retrieveRelatedWordEntries(List<String> relatedWords)
		throws MultilingualDictException {

		List<MultilingualDictEntry> wordEntries = new ArrayList<MultilingualDictEntry>();
		for (String aWord: relatedWords) {
			wordEntries.add(
				this.entry4word(aWord, false,
					// For the purpose of sorting, we don't need the
				   // RELATED_WORDS (which take time because they
				   // require that we decompose a bunch of words
					Field.DEFINITION, Field.TRANSLATIONS));
		}

		return wordEntries;
	}

	/**
	 * Sort the list of words so that those that have a more detailed dictionary
	 * entry come first. For example, words that have non-empty list of bilingual
	 * examples of use will come first.
	 */
	private List<String> sortWordsByEntryComprehensiveness(
		List<MultilingualDictEntry> wordEntries) throws MultilingualDictException {
		Collections.sort(wordEntries,
			(w1, w2) -> {
				int answer = 0;

				if (answer == 0) {
					boolean w1HasTranslations = w1.origWordTranslations.size() > 0;
					boolean w2HasTranslations = w2.origWordTranslations.size() > 0;
					if (w1HasTranslations && ! w2HasTranslations) {
						answer = -1;
					} else if (!w1HasTranslations && w2HasTranslations) {
						answer = 1;
					}
				}

				if (answer == 0) {
					int w1examples = w1.bilingualExamplesOfUse().size();
					int w2examples = w2.bilingualExamplesOfUse().size();
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
		for (MultilingualDictEntry anEntry: wordEntries) {
			sortedWords.add(anEntry.word);
		}

		return sortedWords;
	}

	private void computeOrigWordTranslationsAndExamples(
		MultilingualDictEntry entry) throws MultilingualDictException {
		computeOrigWordTranslationsAndExamples(entry, (Script)null);
	}

	private void computeOrigWordTranslationsAndExamples(
		MultilingualDictEntry entry, Script script) throws MultilingualDictException {
		Logger tLogger = Logger.getLogger("org.iutools.worddict.MultilingualDict.computeTranslationsAndExamples");
		List<String> justOneWord = new ArrayList<String>();
		justOneWord.add(entry.word);
		retrieveTranslationsAndExamples(entry, justOneWord, script);
	}

	private void retrieveTranslationsAndExamples(
		MultilingualDictEntry entry, List<String> iuWordGroup, Script script) throws MultilingualDictException {
		Logger tLogger = Logger.getLogger("org.iutools.worddict.MultilingualDict.retrieveTranslationsAndExamples");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("word="+entry.word+"/"+entry.wordInOtherScript+", iuWordGroup="+ StringUtils.join(iuWordGroup.iterator(), ", "));
		}
		boolean isForRelatedWords = true;
		if (iuWordGroup.size() == 1) {
			String singleWord = iuWordGroup.get(0);
			if (singleWord.equals(entry.word) ||
				singleWord.equals(entry.wordInOtherScript)) {
				isForRelatedWords = false;
			}
		}

		try {
			String l1 = entry.lang;
			String l2 = otherLang(l1);
			String l1Word = entry.word;
			if (l1.equals("iu")) {
				l1Word = entry.wordSyllabic;
			}
			Set<String> alreadySeenPair = new HashSet<String>();
			Iterator<Alignment_ES> alignmentIter =
				new TranslationMemory()
					.searchIter(l1, l1Word, l2);
			int totalPairs = 1;
			while (alignmentIter.hasNext()) {
				Alignment_ES alignment = alignmentIter.next();
				SentencePair bilingualAlignment = null;
				bilingualAlignment = alignment.sentencePair(l1, l2);
				if (bilingualAlignment.hasWordLevel()) {
					new WordSpotter(bilingualAlignment)
						.highlight(l1, l1Word, TAG, true);
				}
				tLogger.trace(
					"Processing l1Word="+l1Word+", pair #"+totalPairs+
					"=\n"+l1+": "+bilingualAlignment.getText(l1)+
					"\n"+l2+": "+bilingualAlignment.getText(l2));
				totalPairs =
					onNewSentencePair(entry, bilingualAlignment, alreadySeenPair,
						totalPairs, script, isForRelatedWords);
				if (enoughBilingualExamples(entry)) {
					break;
				}
			}
		} catch (TranslationMemoryException | WordSpotterException | MultilingualDictException e) {
			throw new MultilingualDictException(e);
		}

		return;
	}

	private String otherLang(String lang) throws MultilingualDictException {
		return MultilingualDictEntry.otherLang(lang);
	}

	private void assertIsSupportedLanguage(String lang) throws MultilingualDictException {
		MultilingualDictEntry.assertIsSupportedLanguage(lang);
	}

	private boolean enoughBilingualExamples(MultilingualDictEntry entry) throws MultilingualDictException {
		boolean enough =
			(
				entry.possibleTranslationsIn(otherLang(entry.lang)).size() >= MAX_TRANSLATIONS ||
				entry.totalBilingualExamples() >= MAX_SENT_PAIRS
			);
		return enough;
	}

	private int onNewSentencePair(MultilingualDictEntry entry,
		SentencePair bilingualAlignment, Set<String> alreadySeenPair,
		int totalPairs, Script script, boolean forRelatedWord) throws MultilingualDictException {

		Logger tLogger = Logger.getLogger("org.iutools.worddict.MultilingualDict.onNewSentencePair");
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
					entry.addBilingualExample("MISC", highlightedPair, forRelatedWord);
				} else {
					tLogger.trace("Adding example for translation of word='" + entry.wordRoman + "''" +
						", l2Translation='" + l2Translation + "'");
						entry.addBilingualExample(l2Translation, highlightedPair, forRelatedWord);
				}
				entry.addBilingualExample("ALL", highlightedPair, forRelatedWord);
				totalPairs++;
			}
		}
 		return totalPairs;
	}

	public static String canonizeTranslation(String l2, String l2Translation) throws MultilingualDictException {
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
			throw new MultilingualDictException(e);
		}
		return l2Translation;
	}

	public Pair<Iterator<String>,Long> searchIter(String partialWord) throws MultilingualDictException {
		return searchIter(partialWord, (String)null);
	}

	public Pair<List<String>,Long> search(String partialWord, String lang, Integer maxHits)
	throws MultilingualDictException, TranslationMemoryException {
		List<String> hits = new ArrayList<String>();

		Pair<Iterator<String>,Long> results = searchIter(partialWord, lang);
		Long totalHits = results.getRight();
		Iterator<String> hitsIter = results.getLeft();
		int count = 0;
		while (hitsIter.hasNext()) {
			count++;
			String hit = hitsIter.next();
			try {
				hit = TransCoder.ensureSameScriptAsSecond(hit, partialWord);
			} catch (TransCoderException e) {
				throw new MultilingualDictException(e);
			}
			hits.add(hit);
			if (maxHits != null && hits.size() == maxHits) {
				break;
			}
		}

		hits = sortHits(hits);
		adjustPartialWordInHits(hits, partialWord, lang);

		totalHits = Math.max(totalHits, hits.size());

		return Pair.of(hits, totalHits);
	}

	/** Ensure that:
	 * - partialWord is in the list of hits, IF it is a valid IU word
	 * - comes first IF it is in the list*/
	private void adjustPartialWordInHits(List<String> hits, String partialWord, String lang) throws MultilingualDictException, TranslationMemoryException {
		if (!hits.contains(partialWord)) {
			// The top list of hits did not contain an exact match.
			// Check to see if the exact match COULD have been found if
			// we had gone further, OR if the word analyzes as an IU word
			try {
				if (wordExists(partialWord, lang) ||
					(lang.equals("iu") &&  new MorphologicalAnalyzer_R2L().isDecomposable(partialWord))) {
					hits.add(0, partialWord);
				}
			} catch (MorphologicalAnalyzerException e) {
				throw new MultilingualDictException(e);
			}
		}

		if (hits.contains(partialWord)) {
			// If we found an exact match, make sure it comes first.
			hits.remove(partialWord);
			hits.add(0, partialWord);
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

	public Pair<Iterator<String>,Long> searchIter(
		String partialWord, String lang) throws MultilingualDictException {
		if (lang == null) {
			lang = "iu";
		}
		if (partialWord != null) {
			partialWord = partialWord.toLowerCase();
		}
		assertIsSupportedLanguage(lang);
		Pair<Iterator<String>,Long> results = null;
		if (lang.equals("iu")) {
			results = search_IU(partialWord);
		} else if (lang.equals("en")) {
			results = search_EN(partialWord);
		}
		return results;
	}

	private Pair<Iterator<String>, Long> search_IU(String partialWord) throws MultilingualDictException {
		Iterator<String> wordsIter = null;
		Long totalWords = null;
		try {
			partialWord = TransCoder.ensureScript(TransCoder.Script.ROMAN, partialWord);
			CompiledCorpus.SearchOption[] options =
				new CompiledCorpus.SearchOption[] {
					CompiledCorpus.SearchOption.EXCL_MISSPELLED,
					CompiledCorpus.SearchOption.WORD_ONLY
				};
			totalWords = corpus.totalWordsWithCharNgram(partialWord, options);
			wordsIter = corpus.wordsContainingNgram(partialWord, options);
		} catch (CompiledCorpusException | TransCoderException e) {
			throw new MultilingualDictException(e);
		}

		return Pair.of(wordsIter, totalWords);
	}

	private boolean wordExists(String partialWord, String lang) throws MultilingualDictException, TranslationMemoryException {
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
		Iterator<Alignment_ES> tmIter = new TranslationMemory().searchIter("en", word);
		return tmIter.hasNext();
	}

	private Boolean wordExists_IU(String word) throws MultilingualDictException {
		word = TransCoder.ensureRoman(word);
		WordInfo winfo = null;
		try {
			winfo = corpus.info4word(word);
		} catch (CompiledCorpusException e) {
			throw new MultilingualDictException(e);
		}
		return winfo != null;
	}

	private Pair<Iterator<String>, Long> search_EN(String partialWord) throws MultilingualDictException {
		// TODO-Alain: For now, we returns results that iterate through either:
		//
		//   - NO words at all, if the input word CANNOT be found in the TM
		//   - JUST the input word, if the input word CAN be found in the TM
		//
		// Note that this will not return any of the words that CONTAIN the input
		// word. So, eventually, we should compile an English CompiledCorpus from
		// the TM content, and search for words in that corpus
		//
		Iterator<String> wordsIter = null;
		Long totalWords = null;
		try {
			List<String> words = new ArrayList<String>();
			Iterator<Alignment_ES> tmIter = new TranslationMemory().searchIter("en", partialWord);
			if (tmIter.hasNext()) {
				words.add(partialWord);
			}
			wordsIter = words.iterator();
			totalWords = new Long(words.size());
		} catch (TranslationMemoryException e) {
			throw new MultilingualDictException(e);
		}
		return Pair.of(wordsIter, totalWords);
	}

	public String[] decomposeWord(String word) throws MultilingualDictException {
		String decomp[] = new String[0];
		try {
			Decomposition[] decomps = new MorphologicalAnalyzer_R2L().decomposeWord(word);
			if (decomps != null && decomps.length > 0) {
				decomp = decomps[0].getMorphemes();
			}
		} catch (TimeoutException e) {
			// Leave the decomp to empty
		} catch (MorphologicalAnalyzerException | DecompositionException e) {
			throw new MultilingualDictException(e);
		}
		return decomp;
	}
}