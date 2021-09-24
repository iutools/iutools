package org.iutools.webservice.tokenize;

import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;

import java.util.Map;

public class TokenizeInputs extends ServiceInputs {

	public String text = null;
	public Integer maxWords = 500;

	public TokenizeInputs() {}

	public TokenizeInputs(String _text) throws ServiceException {
		init__TokenizeInputs(_text, (Integer)null);
	}

	public TokenizeInputs(String _text, Integer _maxWords) throws ServiceException {
		init__TokenizeInputs(_text, _maxWords);
	}

	private void init__TokenizeInputs(String _text, Integer _maxWords) throws ServiceException {
		this.text = _text;
		this.maxWords = _maxWords;
		validate();
	}

	@Override
	public Map<String, Object> summarizeForLogging() throws ServiceException {
		Map<String,Object> summary = asMap();
		summary.remove("text");
		int numWords = 0;
		if (text != null) {
			numWords = new IUTokenizer().tokenize(text).size();
		}
		summary.put("totalWords", numWords);
		return summary;
	}
}