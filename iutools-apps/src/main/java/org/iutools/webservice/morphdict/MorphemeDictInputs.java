package org.iutools.webservice.morphdict;

import com.fasterxml.jackson.annotation.JsonIgnore;
import static org.iutools.script.TransCoder.Script;

import org.iutools.text.IUWord;
import org.iutools.text.WordException;
import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;

import java.util.Map;

public class MorphemeDictInputs extends ServiceInputs {

	public String canonicalForm = null;
	public String canonicalFormRoman = null;

	public String grammar = null;

	public String meaning = null;
	public String corpusName = null;
	public String nbExamples = "20";

	public boolean canonicalIsIUText = true;

	public MorphemeDictInputs() throws ServiceException {
		init_MorphemeExamplesInputs((String)null, (String)null, (String)null, (Script)null, (String)null, (String)null);
	};

	public MorphemeDictInputs(String _canonicalForm) throws ServiceException {
		init_MorphemeExamplesInputs(_canonicalForm, (String)null, (String)null, (Script)null, (String)null, (String)null);
	}

	public MorphemeDictInputs(String _canonicalForm, String _corpusName,
		String _nbExamples) throws ServiceException {
		init_MorphemeExamplesInputs(_canonicalForm, (String)null, (String)null, (Script)null, _corpusName, _nbExamples);
	}

	public MorphemeDictInputs(String _canonicalForm, String _grammar, String _meaning,
		String _corpusName, String _nbExamples) throws ServiceException {
		init_MorphemeExamplesInputs(_canonicalForm, _grammar, _meaning, (Script)null, _corpusName, _nbExamples);
	}

	private void init_MorphemeExamplesInputs(String _canonicalForm, String _grammar, String _meaning,
		Script inScript, String _corpusName, String _nbExamples) throws ServiceException {
		setCanonicalForm(_canonicalForm);
		this.grammar = _grammar;
		this.meaning = _meaning;
		this.corpusName = _corpusName;
		if (_nbExamples != null) {
			this.nbExamples = _nbExamples;
		}
		if (inScript == null) {
			inScript = Script.ROMAN;
		}
		this.setIUAlphabet(inScript);
//		validate();
	}

	public void setCanonicalForm(String _canonicalForm) {
		this.canonicalForm = _canonicalForm;
		try {
			IUWord word = new IUWord(canonicalForm);
			canonicalFormRoman = word.inRoman();
		} catch (WordException e) {
			// canonical form was not IU text
			canonicalIsIUText = false;
		}
	}

	@Override
	public Map<String, Object> summarizeForLogging() throws ServiceException {

		Map<String,Object> summary = asMap();
		summary.remove("canonicalFormRoman");
		summary.remove("canonicalIsIUText");
		summary.remove("corpusName");
		return summary;
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