package org.iutools.webservice.morphexamples;

import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;

import java.util.Map;

public class MorphemeExamplesInputs extends ServiceInputs {

	public String wordPattern = null;
	public String corpusName = null;
	public String nbExamples = "20";

	public MorphemeExamplesInputs() throws ServiceException {
		init_MorphemeExamplesInputs((String)null, (String)null, (String)null);
	};

	public MorphemeExamplesInputs(String _wordPattern) throws ServiceException {
		init_MorphemeExamplesInputs(_wordPattern, (String)null, (String)null);
	}

	public MorphemeExamplesInputs(String _wordPattern, String _corpusName,
		String _nbExamples) throws ServiceException {
		init_MorphemeExamplesInputs(_wordPattern, _corpusName, _nbExamples);
	}

	private void init_MorphemeExamplesInputs(String _wordPattern, String _corpusName,
		String _nbExamples) throws ServiceException {
		this.wordPattern = _wordPattern;
		this.corpusName = _corpusName;
		if (_nbExamples != null) {
			this.nbExamples = _nbExamples;
		}
		validate();
	}

	@Override
	public Map<String, Object> summarizeForLogging() throws ServiceException {
		return asMap();
	}
}