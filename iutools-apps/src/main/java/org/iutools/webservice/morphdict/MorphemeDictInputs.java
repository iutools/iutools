package org.iutools.webservice.morphdict;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;

import java.util.Map;

public class MorphemeDictInputs extends ServiceInputs {

	public String canonicalForm = null;

	public String grammar = null;

	public String meaning = null;
	public String corpusName = null;
	public String nbExamples = "20";

	public MorphemeDictInputs() throws ServiceException {
		init_MorphemeExamplesInputs((String)null, (String)null, (String)null, (String)null, (String)null);
	};

	public MorphemeDictInputs(String _canonicalForm) throws ServiceException {
		init_MorphemeExamplesInputs(_canonicalForm, (String)null, (String)null, (String)null, (String)null);
	}

	public MorphemeDictInputs(String _canonicalForm, String _corpusName,
		String _nbExamples) throws ServiceException {
		init_MorphemeExamplesInputs(_canonicalForm, (String)null, (String)null, _corpusName, _nbExamples);
	}

	public MorphemeDictInputs(String _canonicalForm, String _grammar, String _meaning,
		String _corpusName, String _nbExamples) throws ServiceException {
		init_MorphemeExamplesInputs(_canonicalForm, _grammar, _meaning, _corpusName, _nbExamples);
	}

	private void init_MorphemeExamplesInputs(String _canonicalForm, String _grammar, String _meaning,
		String _corpusName, String _nbExamples) throws ServiceException {
		this.canonicalForm = _canonicalForm;
		this.grammar = _grammar;
		this.meaning = _meaning;
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

	@JsonIgnore
	public boolean isEmpty() {
		boolean empty = (
			(canonicalForm == null || canonicalForm.isEmpty()) &&
			(grammar == null || grammar.isEmpty()) &&
			(meaning == null || meaning.isEmpty()));

		return empty;
	}
}