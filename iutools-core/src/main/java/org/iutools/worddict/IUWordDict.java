package org.iutools.worddict;

import org.apache.log4j.Logger;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.SentencePair;
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.concordancer.tm.WordSpotter;
import org.iutools.concordancer.tm.WordSpotterException;
import org.iutools.corpus.*;
import org.iutools.morphrelatives.MorphRelativesFinder;
import org.iutools.morphrelatives.MorphRelativesFinderException;
import org.iutools.morphrelatives.MorphologicalRelative;
import org.iutools.script.TransCoder;

import java.util.*;

/**
 * Dictionary of Inuktitut words.
 *
 * For more info about how to use this class see the DOCUMENTATION TESTS section
 * of IUWordDictTest.
 *
 */
public class IUWordDict {

	private static final long MAX_WORDS = 20;
	private static IUWordDict _singleton = null;

	public static int MAX_SENT_PAIRS = 10;

	private CompiledCorpus corpus = null;

	private static final String TAG = "strong";

	// Private constructor.
	// There should only be one instance of IUWordDict and you should get it
	// by invoking IUWordDict.getInstance();
	private IUWordDict() throws IUWordDictException {
		try {
			corpus = new CompiledCorpusRegistry().getCorpus();
		} catch (Exception e) {
			throw new IUWordDictException(e);
		}
	}

	public static IUWordDict getInstance() throws IUWordDictException {
		if (_singleton == null) {
			_singleton = new IUWordDict();
		}
		return _singleton;
	}

	public IUWordDictEntry entry4word(String word) throws IUWordDictException {
		return entry4word(word, (Boolean)null);
	}

	public IUWordDictEntry entry4word(String word, Boolean sortRelatedWords)
		throws IUWordDictException {
		IUWordDictEntry entry = new IUWordDictEntry(word);
		TransCoder.Script script = TransCoder.textScript(word);
		try {
			WordInfo winfo = corpus.info4word(entry.wordRoman);
			if (winfo != null) {
				entry.setDecomp(winfo.topDecomposition());
			}
			computeTranslationsAndExamples(entry, script);
			computeRelatedWords(entry, sortRelatedWords);
		} catch (CompiledCorpusException e) {
			throw new IUWordDictException(e);
		}

		entry.ensureIUScript(TransCoder.textScript(word));
		return entry;
	}

	private void computeRelatedWords(IUWordDictEntry entry,
		Boolean sortRelatedWords) throws IUWordDictException {
		if (sortRelatedWords == null) {
			sortRelatedWords = true;
		}
		try {
			MorphologicalRelative[] rels =
				new MorphRelativesFinder(corpus).findRelatives(entry.wordRoman);
			List<String> relatedWords = new ArrayList<String>();
			for (MorphologicalRelative aRel: rels) {
				relatedWords.add(aRel.getWord());
			}

			if (sortRelatedWords) {
				sortWordsByEntryComprehensiveness(relatedWords);
			}

			entry.relatedWords = relatedWords.toArray(new String[0]);
		} catch (MorphRelativesFinderException e) {
			throw new IUWordDictException(e);
		}

	}

	/**
	 * Sort the list of words so that those that have a more detailed dictionary
	 * entry come first. For example, words that have non-empty list of bilingual
	 * examples of use will come first.
	 * @param words
	 * @return
	 */
	private void sortWordsByEntryComprehensiveness(List<String> words) throws IUWordDictException {
		List<IUWordDictEntry> wordEntries = new ArrayList<IUWordDictEntry>();
		for (String aWord: words) {
			wordEntries.add(this.entry4word(aWord, false));
		}
		int x = 0;
		Collections.sort(wordEntries,
			(w1, w2) -> {
				int answer = 0;

				int w1examples = w1.bilingualExamplesOfUse().size();
				int w2examples = w2.bilingualExamplesOfUse().size();
				answer = - Integer.compare(w1examples, w2examples);

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

		return;
	}

	private void computeTranslationsAndExamples(IUWordDictEntry entry, TransCoder.Script script) throws IUWordDictException {
		Logger tLogger = Logger.getLogger("org.iutools.worddict.IUWordDict.computeTranslationsAndExamples");
		try {
			Set<String> alreadySeenPair = new HashSet<String>();
			List<Alignment_ES> tmResults =
				new TranslationMemory().search("iu", entry.wordSyllabic);
			sortTMResults(tmResults);
			int totalPairs = 1;
			for (Alignment_ES hit: tmResults) {
				SentencePair bilingualAlignment = null;
				bilingualAlignment = hit.sentencePair("iu", "en");
				if (bilingualAlignment.hasWordLevel()) {
					new WordSpotter(bilingualAlignment)
						.highlight("iu", entry.wordSyllabic, TAG, true);
				}
				tLogger.trace("Processing word="+entry.wordRoman+", pair #"+totalPairs);
				totalPairs =
					onNewSentencePair(entry, bilingualAlignment, alreadySeenPair,
						totalPairs, script);
				if (totalPairs > MAX_SENT_PAIRS) {
					break;
				}
			}
		} catch (TranslationMemoryException | WordSpotterException e) {
			throw new IUWordDictException(e);
		}
	}

	private void sortTMResults(List<Alignment_ES> tmResults) {
		Collections.sort(tmResults, new Comparator<Alignment_ES>(){
			public int compare(Alignment_ES a1, Alignment_ES a2){
				int answer = 0;
				if (a1.hasWordAlignmentForLangPair("iu", "en") &&
					!a2.hasWordAlignmentForLangPair("iu", "en")) {
					answer = 11;
				} else if (a2.hasWordAlignmentForLangPair("iu", "en") &&
					!a1.hasWordAlignmentForLangPair("iu", "en")) {
					answer = 1;
				}
				return answer;
			}
		});

	}

	private int onNewSentencePair(IUWordDictEntry entry,
		SentencePair bilingualAlignment, Set<String> alreadySeenPair,
		int totalPairs, TransCoder.Script script) throws IUWordDictException {

		Logger tLogger = Logger.getLogger("org.iutools.worddict.IUWordDict.onNewSentencePair");
		String[] highlightedPair = new String[] {
			bilingualAlignment.getText("iu"),
			bilingualAlignment.getText("en"),
		};
		String bothText = String.join(" <--> ", highlightedPair);
		if (!alreadySeenPair.contains(bothText)) {
			alreadySeenPair.add(bothText);
			String enTranslation = bilingualAlignment.highlightedText("en", TAG);
			if (enTranslation == null) {
				entry.addBilingualExample("MISC", highlightedPair);
			} else {
				entry.addBilingualExample(enTranslation, highlightedPair);
			}
			entry.addBilingualExample("ALL", highlightedPair);
			totalPairs++;
		}
 		return totalPairs;
	}

	public List<String> search(String partialWord) throws IUWordDictException {
		List<String> words = new ArrayList<String>();
		try {
			CompiledCorpus.SearchOption[] options =
				new CompiledCorpus.SearchOption[] {
					CompiledCorpus.SearchOption.EXCL_MISSPELLED,
					CompiledCorpus.SearchOption.WORD_ONLY
				};
			long totalWords = corpus.totalWordsWithCharNgram(partialWord, options);
			if (totalWords > MAX_WORDS) {
				throw new TooManyWordsException(totalWords);
			}
			Iterator<String> wordsIter = corpus.wordsContainingNgram(
				partialWord, options);
			while (wordsIter.hasNext()) {
				words.add(wordsIter.next());
			}
		} catch (CompiledCorpusException e) {
			throw new IUWordDictException(e);
		}
		return words;
	}
}