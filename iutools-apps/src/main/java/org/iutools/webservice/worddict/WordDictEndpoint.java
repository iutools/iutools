package org.iutools.webservice.worddict;

import ca.nrc.datastructure.CloseableIterator;
import ca.nrc.json.PrettyPrinter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.text.IUWord;
import org.iutools.text.Word;
import org.iutools.text.WordException;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.ServiceException;
import org.iutools.worddict.MDictEntry;
import org.iutools.worddict.MachineGeneratedDict;
import org.iutools.worddict.MachineGeneratedDictException;

import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.List;

public class WordDictEndpoint extends Endpoint<WordDictInputs,WordDictResult> {
	@Override
	protected WordDictInputs requestInputs(String jsonRequestBody) throws ServiceException {
		return jsonInputs(jsonRequestBody, WordDictInputs.class);
	}

	@Override
	public WordDictResult execute(WordDictInputs inputs) throws ServiceException, MachineGeneratedDictException {
		Logger tLogger = LogManager.getLogger("org.iutools.webservice.worddict.WordDictEndpoint.execute");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("invoked with inputs="+ PrettyPrinter.print(inputs));
		}

		WordDictResult result = ensureInputWordIsInInputLanguage(inputs);
		if (result == null) {
			if (inputs.exactWordLookup) {
				result = lookupExactWord(inputs);
			} else {
				result = searchWord(inputs);
			}

			setResultLangsAndScript(result, inputs);
		}

		tLogger.trace("Completed");
		return result;
	}

	private void setResultLangsAndScript(
		WordDictResult result, WordDictInputs inputs) throws ServiceException, MachineGeneratedDictException {
		fixScriptOfWordEntries(inputs, result);

		try {
			if (result.queryWordEntry != null) {
				result
				.setLang(result.queryWordEntry.getLang())
				.setOtherLang(result.queryWordEntry.otherLang());
			}
		} catch (MachineGeneratedDictException e) {
			throw new ServiceException(e);
		}
	}

	private WordDictResult ensureInputWordIsInInputLanguage(WordDictInputs inputs) throws MachineGeneratedDictException {
		WordDictResult errorResult = null;
		boolean ok = true;
		String lang = inputs.lang;
		try {
			Word word = Word.build(inputs.word, lang);
			if ((lang.equals("iu") && !(word instanceof IUWord)) ||
				(lang.equals("en") && (word instanceof IUWord))) {
				ok = false;
			}
		} catch (WordException e) {
			ok = false;
		}

		if (!ok) {
			errorResult = new WordDictResult(null, new ArrayList<String>());
			errorResult.errorMessage =
				"The word '"+inputs.word+"' is not in the language '"+lang+"'.\n" +
				"Change the input word or the input language.";
		}

		return errorResult;
	}

	private WordDictResult searchWord(WordDictInputs inputs) throws ServiceException {

		WordDictResult result = null;
		try {
			Long totalWords = null;
			List<String> topWords = null;
			MDictEntry firstWordEntry = null;
			MachineGeneratedDict dict = new MachineGeneratedDict();

			Pair<CloseableIterator<String>, Long> searchResults =
				dict.searchIter(inputs.word, inputs.lang);
			totalWords = searchResults.getRight();
			try (CloseableIterator<String> wordsIter = searchResults.getLeft()) {
				topWords = new ArrayList<String>();
				while (wordsIter.hasNext() && topWords.size() < 10) {
					topWords.add(wordsIter.next());
				}
			}

			if (!topWords.contains(inputs.word)) {
				// The query word was not one of the most frequents hits in the corpus.
				// Check if it's correctly spelled and if so, add it to the list
				// of matching words.
				MDictEntry exactWordEntry =
					dict.entry4word(inputs.word, inputs.lang);
				if (!exactWordEntry.isMisspelled()) {
					topWords.add(0, inputs.word);
					firstWordEntry = exactWordEntry;
				}
			}
			if (inputs.lang.equals("iu")) {
				topWords = (List<String>) TransCoder.ensureScript(inputs.iuAlphabet, topWords);
			}
			if (firstWordEntry == null && !topWords.isEmpty()) {
				firstWordEntry = dict.entry4word(topWords.get(0), inputs.lang);
			}
			result = new WordDictResult(firstWordEntry, topWords, totalWords);

		} catch (Throwable e) {
			throw new ServiceException(e);
		}

		return result;
	}

	private WordDictResult lookupExactWord(WordDictInputs inputs) {
		WordDictResult exactWordResult = null;
		try {
			MachineGeneratedDict dict = new MachineGeneratedDict();
			MDictEntry exactWordEntry = dict.entry4word(inputs.word, inputs.lang);
			List<String> foundWords = new ArrayList<String>();
			exactWordResult = new WordDictResult(exactWordEntry, foundWords, new Long(0));
		} catch (Exception e) {
			throw new WebServiceException(e);
		}

		return exactWordResult;
	}


	/** Ensure that the matching words use the same script as the input query.
	 */
	private void fixScriptOfWordEntries(
	WordDictInputs inputs, WordDictResult result) throws ServiceException {
		List<String> fixedWords = new ArrayList<String>();
		for (String aWord: result.matchingWords) {
			try {
				fixedWords.add(TransCoder.ensureSameScriptAsSecond(aWord, inputs.word));
			} catch (TransCoderException e) {
				throw new ServiceException(e);
			}
		}
		result.matchingWords = fixedWords;
	}
}
