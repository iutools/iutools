package org.iutools.webservice.gist;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;

import java.net.URL;
import java.util.List;
import java.util.Map;

public class GistPrepareContent2Inputs extends ServiceInputs {

	public String textOrUrl = null;

	private IUTokenizer tokenizer = new IUTokenizer();

	public GistPrepareContent2Inputs() {}

	public GistPrepareContent2Inputs(String _text) {
		this.textOrUrl = _text;
	}

	@JsonIgnore
	public boolean isURL() {
		boolean answer = false;
		try {
			URL url = new URL(textOrUrl);
			answer = true;
		} catch (Exception e) {
			answer = false;
		}

		return answer;
	}

	public Map<String, Object> summarizeForLogging() throws ServiceException {

		Map<String,Object> summary = super.summarizeForLogging();
		summary.remove("textOrUrl");
		String type = "text";
		if (isURL()) {
			type = "url";
			summary.put("address", textOrUrl);
			summary.put("host", hostOfUrl());
		} else {
			summary.put("totalWords", totalWordsInText());
		}
		summary.put("type", type);
		return summary;
	}

	private String hostOfUrl() {
		String host = null;
		try {
			URL url = new URL(textOrUrl);
			host = url.getHost();
		} catch (Exception e) {}
		return host;
	}

	private int totalWordsInText() {
		List<String> tokens = tokenizer.tokenize(textOrUrl);
		return tokens.size();
	}
}
