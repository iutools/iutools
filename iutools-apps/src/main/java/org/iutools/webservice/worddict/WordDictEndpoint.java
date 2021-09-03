package org.iutools.webservice.worddict;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.ServiceException;
import org.iutools.worddict.MultilingualDict;
import org.iutools.worddict.MultilingualDictEntry;
import org.iutools.worddict.MultilingualDictException;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class WordDictEndpoint extends Endpoint<WordDictInputs,WordDictResult> {
	@Override
	protected WordDictInputs requestInputs(HttpServletRequest request) throws ServiceException {
		return jsonInputs(request, WordDictInputs.class);
	}

	@Override
	public WordDictResult execute(WordDictInputs inputs) throws ServiceException {
		Long totalWords = null;
		List<String> topWords = null;
		MultilingualDictEntry firstWordEntry = null;
		try {
			MultilingualDict dict = MultilingualDict.getInstance();
			Pair<Iterator<String>, Long> searchResults =
				dict.search(inputs.word, inputs.lang);
			totalWords = searchResults.getRight();
			Iterator<String> wordsIter = searchResults.getLeft();
			topWords = new ArrayList<String>();
			while (wordsIter.hasNext() && topWords.size() < 10) {
				topWords.add(wordsIter.next());
			}
			if (!topWords.isEmpty()) {
				firstWordEntry = dict.entry4word(topWords.get(0), inputs.lang);
			}
		} catch (MultilingualDictException e) {
			throw new ServiceException(e);
		}

		WordDictResult result = null;
		try {
			result =
				new WordDictResult(firstWordEntry, topWords, totalWords)
					.setLang(firstWordEntry.lang)
					.setOtherLang(firstWordEntry.otherLang());
		} catch (MultilingualDictException e) {
			throw new ServiceException(e);
		}

		return result;
	}
}
