package org.iutools.webservice.spell;

import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;

import java.util.Map;

public class SpellInputs extends ServiceInputs {
	public String text = null;
	public boolean includePartiallyCorrect = false;
	public boolean suggestCorrections = true;

	public SpellInputs() throws ServiceException {
		init__SpellInputs((String)null);
	}

	public SpellInputs(String _text) throws ServiceException {
		init__SpellInputs(_text);
	}

	protected void init__SpellInputs(String _text) throws ServiceException {
		this.text = _text;
		validate();
	}

	// We don't log individual word spell check
	public Map<String, Object> summarizeForLogging() throws ServiceException {
		return null;
	}
}