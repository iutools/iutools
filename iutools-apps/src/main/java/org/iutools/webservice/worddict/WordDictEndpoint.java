package org.iutools.webservice.worddict;

import ca.nrc.json.PrettyPrinter;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.morph.Decomposition;
import org.iutools.morph.MorphologicalAnalyzerException;
import org.iutools.morph.r2l.MorphologicalAnalyzer_R2L;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.ServiceException;
import org.iutools.worddict.MultilingualDict;
import org.iutools.worddict.MultilingualDictEntry;
import org.iutools.worddict.MultilingualDictException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class WordDictEndpoint extends Endpoint<WordDictInputs,WordDictResult> {
	@Override
	protected WordDictInputs requestInputs(HttpServletRequest request) throws ServiceException {
		return jsonInputs(request, WordDictInputs.class);
	}

	@Override
	public WordDictResult execute(WordDictInputs inputs) throws ServiceException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.worddict.WordDictEndpoint.execute");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("invoked with inputs="+ PrettyPrinter.print(inputs));
		}
		Long totalWords = null;
		List<String> topWords = null;
		MultilingualDictEntry firstWordEntry = null;
		try {
			MultilingualDict dict = MultilingualDict.getInstance();
			Pair<Iterator<String>, Long> searchResults =
				dict.searchIter(inputs.word, inputs.lang);
			totalWords = searchResults.getRight();
			Iterator<String> wordsIter = searchResults.getLeft();
			topWords = new ArrayList<String>();
			while (wordsIter.hasNext() && topWords.size() < 10) {
				topWords.add(wordsIter.next());
			}

			if (!topWords.contains(inputs.word)) {
				boolean addQueryWord = false;
				// The query word was not included in the top words.
				// See if the word is in the dictionary.
				String wordRoman = TransCoder.ensureRoman(inputs.word);
				if (null != dict.corpus.info4word(wordRoman)) {
					addQueryWord = true;
				} else {
					// Word is not in the corpus. See if it at least it
					// decomposes
					try {
						Decomposition[] decompositions = new MorphologicalAnalyzer_R2L().decomposeWord(wordRoman);
						if (decompositions != null && decompositions.length > 0) {
							addQueryWord = true;
						}
					} catch (TimeoutException e) {
						// Nothing to do if the word could not be decomposed in time
					}
				}
				if (addQueryWord) {
					topWords.add(0, inputs.word);
				}
			}

			topWords = fixScriptOfWordEntries(inputs, topWords);

			if (!topWords.isEmpty()) {
				firstWordEntry = dict.entry4word(topWords.get(0), inputs.lang);
			}
		} catch (MultilingualDictException | CompiledCorpusException
			| MorphologicalAnalyzerException  e) {
			throw new ServiceException(e);
		}

		WordDictResult result = null;
		try {
			result =
				new WordDictResult(firstWordEntry, topWords, totalWords);
			if (firstWordEntry != null) {
				result
				.setLang(firstWordEntry.lang)
				.setOtherLang(firstWordEntry.otherLang());
			}
		} catch (MultilingualDictException e) {
			throw new ServiceException(e);
		}

		tLogger.trace("Completed");
		return result;
	}

	/** Ensure that the matching words use the same script as the input query.
	 */
	private List<String> fixScriptOfWordEntries(
		WordDictInputs inputs, List<String> words) throws ServiceException {
		List<String> fixedWords = new ArrayList<String>();
		for (String aWord: words) {
			try {
				fixedWords.add(TransCoder.ensureSameScriptAsSecond(aWord, inputs.word));
			} catch (TransCoderException e) {
				throw new ServiceException(e);
			}
		}
		return fixedWords;
	}
}
