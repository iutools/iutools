package org.iutools.worddict;

import org.apache.commons.lang3.ArrayUtils;
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
import org.iutools.worddict.IUWordDictEntry.*;

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
		return entry4word(word, (Boolean)null, (Field[])null);
	}

	public IUWordDictEntry entry4word(String word, Boolean sortRelatedWords)
		throws IUWordDictException {
		return entry4word(word, sortRelatedWords, (Field[])null);
	}

	public IUWordDictEntry entry4word(
		String word, Field... fieldsToPpulate)
		throws IUWordDictException {
		return entry4word(word, (Boolean)null, fieldsToPpulate);
	}

	public IUWordDictEntry entry4word(String word, Boolean sortRelatedWords,
		Field... fieldsToPopulate)
		throws IUWordDictException {

		if (fieldsToPopulate == null) {
			fieldsToPopulate = Field.values();
		}

		IUWordDictEntry entry = new IUWordDictEntry(word);
		TransCoder.Script script = TransCoder.textScript(word);
		try {
			WordInfo winfo = corpus.info4word(entry.wordRoman);
			if (winfo != null) {
				entry.setDecomp(winfo.topDecomposition());
			}
			if (ArrayUtils.contains(fieldsToPopulate, Field.BILINGUAL_EXAMPLES) ||
				ArrayUtils.contains(fieldsToPopulate, Field.TRANSLATIONS)) {
				computeTranslationsAndExamples(entry, script);
			}
			if (ArrayUtils.contains(fieldsToPopulate, Field.RELATED_WORDS)) {
				computeRelatedWords(entry, sortRelatedWords);
			}
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
			wordEntries.add(
				this.entry4word(aWord, false,
					// For the purpose of sorting, we don't need the
				   // RELATED_WORDS (which take time because they
				   // require that we decompose a bunch of words
					Field.DEFINITION, Field.TRANSLATIONS));
		}
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
			Iterator<Alignment_ES> alignmentIter =
				new TranslationMemory().searchIter("iu", entry.wordSyllabic, "en");
			int totalPairs = 1;
			while (alignmentIter.hasNext()) {
				Alignment_ES alignment = alignmentIter.next();
				SentencePair bilingualAlignment = null;
				bilingualAlignment = alignment.sentencePair("iu", "en");
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
			String enTranslation = WordSpotter.spotHighlight(
				TAG, bilingualAlignment.langText.get("en"));
			if (enTranslation == null) {
				entry.addBilingualExample("MISC", highlightedPair);
			} else {
				tLogger.trace("Adding example for translation of word='"+entry.wordRoman+"''" +
				", translation='"+enTranslation+"'");
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