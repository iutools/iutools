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

	public static int MAX_TRANSLATIONS = 5;
	public static int MAX_SENT_PAIRS = 20;


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

	public IUWordDictEntry entry4word(String word, Boolean fullRelatedWordEntries,
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
				computeOrigWordTranslationsAndExamples(entry, script);
			}
			if (ArrayUtils.contains(fieldsToPopulate, Field.RELATED_WORDS)) {
				computeRelatedWords(entry, fullRelatedWordEntries);
			}
		} catch (CompiledCorpusException e) {
			throw new IUWordDictException(e);
		}

		entry.sortTranslations();

		entry.ensureIUScript(TransCoder.textScript(word));

		return entry;
	}


	private void computeRelatedWords(IUWordDictEntry entry,
		Boolean fullRelatedWordEntries) throws IUWordDictException {
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
				List<IUWordDictEntry> relWordEntries = retrieveRelatedWordEntries(relatedWords);
				relatedWords = sortWordsByEntryComprehensiveness(relWordEntries);
				collectRelatedWordTranslations(entry, relWordEntries);
			}

			entry.relatedWords = relatedWords.toArray(new String[0]);

		} catch (MorphRelativesFinderException e) {
			throw new IUWordDictException(e);
		}

	}

	private void collectRelatedWordTranslations(
		IUWordDictEntry origWordEntry, List<IUWordDictEntry> relWordEntries) throws IUWordDictException {

		Logger tLogger = Logger.getLogger("org.iutools.worddict.IUWordDict.collectRelatedWordTranslations");
		tLogger.trace("\n\n\ninvoked");
		for (IUWordDictEntry entry: relWordEntries) {
			origWordEntry.addRelatedWordTranslations(entry);
		}
		return;
	}

	private List<IUWordDictEntry> retrieveRelatedWordEntries(List<String> relatedWords)
		throws IUWordDictException {

		List<IUWordDictEntry> wordEntries = new ArrayList<IUWordDictEntry>();
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
		List<IUWordDictEntry> wordEntries) throws IUWordDictException {
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
		for (IUWordDictEntry anEntry: wordEntries) {
			sortedWords.add(anEntry.word);
		}

		return sortedWords;
	}

	private void computeOrigWordTranslationsAndExamples(
		IUWordDictEntry entry, TransCoder.Script script) throws IUWordDictException {
		Logger tLogger = Logger.getLogger("org.iutools.worddict.IUWordDict.computeTranslationsAndExamples");
		List<String> justOneWord = new ArrayList<String>();
		justOneWord.add(entry.word);
		retrieveTranslationsAndExamples(entry, justOneWord, script);
	}

	private void retrieveTranslationsAndExamples(
		IUWordDictEntry entry, List<String> iuWordGroup, TransCoder.Script script) throws IUWordDictException {
		Logger tLogger = Logger.getLogger("org.iutools.worddict.IUWordDict.retrieveTranslationsAndExamples");
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
				tLogger.trace(
					"Processing word="+entry.wordRoman+", pair #"+totalPairs+
					"=\niu: "+bilingualAlignment.getText("iu")+
					"\nen: "+bilingualAlignment.getText("en"));
				totalPairs =
					onNewSentencePair(entry, bilingualAlignment, alreadySeenPair,
						totalPairs, script, isForRelatedWords);
				if (enoughBilingualExamples(entry)) {
					break;
				}
			}
		} catch (TranslationMemoryException | WordSpotterException | IUWordDictException e) {
			throw new IUWordDictException(e);
		}

		return;
	}

	private boolean enoughBilingualExamples(IUWordDictEntry entry) throws IUWordDictException {
		boolean enough =
			(
				entry.possibleTranslationsIn("en").size() >= MAX_TRANSLATIONS ||
				entry.totalBilingualExamples() >= MAX_SENT_PAIRS
			);
		return enough;
	}

	private int onNewSentencePair(IUWordDictEntry entry,
		SentencePair bilingualAlignment, Set<String> alreadySeenPair,
		int totalPairs, TransCoder.Script script, boolean forRelatedWord) throws IUWordDictException {

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
			tLogger.trace("enTranslation="+enTranslation);
			if (enTranslation == null) {
				entry.addBilingualExample("MISC", highlightedPair, forRelatedWord);
			} else {
				tLogger.trace("Adding example for translation of word='"+entry.wordRoman+"''" +
				", translation='"+enTranslation+"'");
				entry.addBilingualExample(enTranslation, highlightedPair, forRelatedWord);
			}
			entry.addBilingualExample("ALL", highlightedPair, forRelatedWord);
			totalPairs++;
		}
 		return totalPairs;
	}

	public Pair<Iterator<String>,Long> search(String partialWord) throws IUWordDictException {
		Iterator<String> wordsIter = null;
		Long totalWords = null;
		try {
			CompiledCorpus.SearchOption[] options =
				new CompiledCorpus.SearchOption[] {
					CompiledCorpus.SearchOption.EXCL_MISSPELLED,
					CompiledCorpus.SearchOption.WORD_ONLY
				};
			totalWords = corpus.totalWordsWithCharNgram(partialWord, options);
			wordsIter = corpus.wordsContainingNgram(partialWord, options);
		} catch (CompiledCorpusException e) {
			throw new IUWordDictException(e);
		}
		return Pair.of(wordsIter,totalWords);
	}
}