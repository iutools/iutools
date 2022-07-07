package org.iutools.webservice.worddict;


import ca.nrc.json.PrettyPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;
import org.iutools.worddict.MultilingualDictEntry;
import org.iutools.worddict.MultilingualDictException;

import java.util.ArrayList;
import java.util.List;

public class WordDictResult extends EndpointResult {

	public List<String> matchingWords = new ArrayList<String>();

	// If the word pattern matched one word exactly, then this contains the
	// dictionary entry for that single word
	public MultilingualDictEntry queryWordEntry;
	public Long totalWords = new Long(0);
	public String lang = "iu";
	public String otherLang = "en";
	public String convertedQuery = null;

	public WordDictResult() {
		init_WordDictResult(
			(MultilingualDictEntry)null, (List<String>)null, (Long)null);
	}

	public WordDictResult(MultilingualDictEntry _entry) {
		init_WordDictResult(_entry, (List<String>)null, (Long)null);
	}

	public WordDictResult(MultilingualDictEntry _entry, List<String> _foundWords) {
		init_WordDictResult(_entry, _foundWords, (Long)null);
	}

	public WordDictResult(
		MultilingualDictEntry _entry, List<String> _foundWords, Long _totalWords) {
		init_WordDictResult(_entry, _foundWords, _totalWords);
	}


	private void init_WordDictResult(
		MultilingualDictEntry _qWordEntry, List<String> _foundWords, Long _totalWords) {
		Logger tLogger = LogManager.getLogger("org.iutools.webservice.worddict.WordDictResult.init_WordDictResult");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("_qWordEntry="+ PrettyPrinter.print(_qWordEntry));
			tLogger.trace("_foundWords="+ PrettyPrinter.print(_foundWords));
			tLogger.trace("_totalWords="+ _totalWords);
		}
		this.matchingWords = _foundWords;
		this.queryWordEntry = _qWordEntry;
		if (_qWordEntry != null) {
			convertedQuery = queryWordEntry.word;
		}
		this.totalWords = _totalWords;
		tLogger.trace("exited");
	}

	public WordDictResult setLang(String _lang) {
		this.lang = _lang;
		return this;
	}

	public WordDictResult setOtherLang(String _lang) {
		this.otherLang = _lang;
		return this;
	}

	@Override
	public void convertIUToRequestedAlphabet(ServiceInputs uncastInputs) throws ServiceException {
		WordDictInputs inputs = (WordDictInputs)uncastInputs;
		TransCoder.Script requestedAlphabet = uncastInputs.iuAlphabet;
		convertQuery(uncastInputs);
		convertFoundWords(inputs);
		convertQueryWordEntry(uncastInputs.iuAlphabet);
	}

	private void convertQuery(ServiceInputs inputs) throws ServiceException {
		WordDictInputs dictInputs = (WordDictInputs)inputs;
		String queryLang = dictInputs.lang;
		convertedQuery = dictInputs.word;
		if (queryLang.equals("iu")) {
			try {
				convertedQuery = TransCoder.ensureScript(dictInputs.iuAlphabet, dictInputs.word);
			} catch (TransCoderException e) {
				throw new ServiceException(e);
			}
		}
	}

	private void convertQueryWordEntry(TransCoder.Script iuAlphabet) throws ServiceException {
		if (queryWordEntry != null) {
			try {
				queryWordEntry.ensureScript(iuAlphabet);
			} catch (MultilingualDictException e) {
				throw new ServiceException(e);
			}
		}
	}

	private void convertFoundWords(WordDictInputs inputs) throws ServiceException {
		if (lang.equals("iu")) {
			// Convert list of matching words
			for (int ii = 0; ii < matchingWords.size(); ii++) {
				String origWord = matchingWords.get(ii);
				String convertedWord = null;
				try {
					convertedWord = TransCoder.ensureScript(inputs.iuAlphabet, origWord);
				} catch (TransCoderException e) {
					throw new ServiceException(e);
				}
				matchingWords.set(ii, convertedWord);
			}
		}
	}

}
