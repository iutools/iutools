package org.iutools.webservice.spell;

import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;

import java.util.Map;

public class CheckWordInputs extends ServiceInputs {
	public String text = null;
	public boolean suggestCorrections = true;
	public int checkLevel = 3;

	public CheckWordInputs() throws ServiceException {
		init__SpellInputs((String)null);
	}

	public CheckWordInputs(String _text) throws ServiceException {
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