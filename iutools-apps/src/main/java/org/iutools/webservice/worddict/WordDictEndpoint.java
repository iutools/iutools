package org.iutools.webservice.worddict;

import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;
import org.iutools.webservice.tokenize.TokenizeInputs;
import org.iutools.worddict.IUWordDict;
import org.iutools.worddict.IUWordDictEntry;
import org.iutools.worddict.IUWordDictException;

import javax.servlet.http.HttpServletRequest;

public class WordDictEndpoint extends Endpoint<WordDictInputs,WordDictResult> {
	@Override
	protected WordDictInputs requestInputs(HttpServletRequest request) throws ServiceException {
		return jsonInputs(request, WordDictInputs.class);
	}

	@Override
	public WordDictResult execute(WordDictInputs inputs) throws ServiceException {
		IUWordDictEntry entry = null;
		try {
			entry = IUWordDict.getInstance().entry4word(inputs.word);
		} catch (IUWordDictException e) {
			throw new ServiceException(e);
		}

		WordDictResult result = new WordDictResult(entry);

		return result;
	}
}
