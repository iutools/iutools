package org.iutools.webservice.tokenize;

import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;

import java.util.Map;

public class TokenizeInputs extends ServiceInputs {

	public String text = null;

	public TokenizeInputs() {}

	public TokenizeInputs(String _text) {
		this.text = _text;
	}

	@Override
	public Map<String, Object> summarizeForLogging() throws ServiceException {
		Map<String,Object> summary = super.summarizeForLogging();
		summary.remove("text");
		int numWords = new IUTokenizer().tokenize(text).size();
		summary.put("totalWords", numWords);
		return summary;
	}
}