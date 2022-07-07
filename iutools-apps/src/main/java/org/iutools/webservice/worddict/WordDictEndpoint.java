package org.iutools.webservice.worddict;

import ca.nrc.json.PrettyPrinter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.ServiceException;
import org.iutools.worddict.MultilingualDict;
import org.iutools.worddict.MultilingualDictEntry;
import org.iutools.worddict.MultilingualDictException;

import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class WordDictEndpoint extends Endpoint<WordDictInputs,WordDictResult> {
	@Override
	protected WordDictInputs requestInputs(String jsonRequestBody) throws ServiceException {
		return jsonInputs(jsonRequestBody, WordDictInputs.class);
	}

	@Override
	public WordDictResult execute(WordDictInputs inputs) throws ServiceException {
		Logger tLogger = LogManager.getLogger("org.iutools.webservice.worddict.WordDictEndpoint.execute");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("invoked with inputs="+ PrettyPrinter.print(inputs));
		}

		WordDictResult result = null;
		if (inputs.exactWordLookup) {
			result = lookupExactWord(inputs);
		} else {
			result = searchWord(inputs);
		}

		setResultLangsAndScript(result, inputs);

		tLogger.trace("Completed");
		return result;
	}

	private void setResultLangsAndScript(
		WordDictResult result, WordDictInputs inputs) throws ServiceException {

		fixScriptOfWordEntries(inputs, result);

		try {
			if (result.queryWordEntry != null) {
				result
					.setLang(result.queryWordEntry.lang)
					.setOtherLang(result.queryWordEntry.otherLang());
			}
		} catch (MultilingualDictException e) {
			throw new ServiceException(e);
		}
	}

	private WordDictResult searchWord(WordDictInputs inputs) throws ServiceException {

		WordDictResult result = null;
		try {
			Long totalWords = null;
			List<String> topWords = null;
			MultilingualDictEntry firstWordEntry = null;
			MultilingualDict dict = new MultilingualDict();

			Pair<Iterator<String>, Long> searchResults =
				dict.searchIter(inputs.word, inputs.lang);
			totalWords = searchResults.getRight();
			Iterator<String> wordsIter = searchResults.getLeft();
			topWords = new ArrayList<String>();
			while (wordsIter.hasNext() && topWords.size() < 10) {
				topWords.add(wordsIter.next());
			}

			if (!topWords.contains(inputs.word)) {
				// The query word was not one of the most frequents hits in the corpus.
				// Check if it's correctly spelled and if so, add it to the list
				// of matching words.
				MultilingualDictEntry exactWordEntry =
					dict.entry4word(inputs.word, inputs.lang);
				if (!exactWordEntry.isMisspelled()) {
					topWords.add(0, inputs.word);
					firstWordEntry = exactWordEntry;
				}
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
			MultilingualDict dict = new MultilingualDict();
			MultilingualDictEntry exactWordEntry = dict.entry4word(inputs.word, inputs.lang);
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
