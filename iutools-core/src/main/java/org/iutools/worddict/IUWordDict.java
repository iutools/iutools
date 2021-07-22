package org.iutools.worddict;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.SentencePair;
import org.iutools.concordancer.WordAlignmentException;
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.corpus.*;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Dictionary of Inuktitut words.
 *
 * For more info about how to use this class see the DOCUMENTATION TESTS section
 * of IUWordDictTest.
 *
 */
public class IUWordDict {

	private static IUWordDict _singleton = null;

	public static int MAX_SENT_PAIRS = 10;

	private CompiledCorpus corpus = null;

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
		IUWordDictEntry entry = new IUWordDictEntry(word);
		TransCoder.Script script = TransCoder.textScript(word);
		try {
			WordInfo winfo = corpus.info4word(entry.wordRoman);
			entry.setDecomp(winfo.topDecomposition());
			computeTranslationsAndExamples(entry, script);
		} catch (CompiledCorpusException e) {
			throw new IUWordDictException(e);
		}
		return entry;
	}

	private void computeTranslationsAndExamples(IUWordDictEntry entry, TransCoder.Script script) throws IUWordDictException {
		try {
			Set<String> alreadySeenPair = new HashSet<String>();
			List<Alignment_ES> tmResults =
				new TranslationMemory().search("iu", entry.wordSyllabic);
			int totalPairs = 1;
			for (Alignment_ES hit: tmResults) {
				SentencePair bilingualAlignment = hit.sentencePair("iu", "en");
				totalPairs =
					onNewSentencePair(entry, bilingualAlignment, alreadySeenPair,
						totalPairs, script);
				if (totalPairs > MAX_SENT_PAIRS) {
					break;
				}
			}
		} catch (TranslationMemoryException | WordAlignmentException e) {
			throw new IUWordDictException(e);
		}

	}

	private int onNewSentencePair(IUWordDictEntry entry, SentencePair bilingualAlignment, Set<String> alreadySeenPair, int totalPairs, TransCoder.Script script) throws IUWordDictException {
		String iuText = null;
		try {
			iuText = TransCoder.ensureScript(
				script, bilingualAlignment.getText("iu"));
		} catch (TransCoderException e) {
			throw new IUWordDictException(e);
		}
		String enText = bilingualAlignment.getText("en");
		String bothText = iuText+" ||\n"+enText;
		if (!alreadySeenPair.contains(bothText)) {
			alreadySeenPair.add(bothText);
			Pair<String,String> asPair =
			Pair.of(iuText, bilingualAlignment.getText("en"));

			entry.addBilingualExample("MISC", asPair);
			entry.addBilingualExample("ALL", asPair);
			totalPairs++;
		}
		return totalPairs;
	}
}