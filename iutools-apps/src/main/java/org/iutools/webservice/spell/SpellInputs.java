package org.iutools.webservice.spell;

import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceInputs;

import java.util.Map;

public class SpellInputs extends ServiceInputs {
	public String text = null;
	public boolean includePartiallyCorrect = false;

	public SpellInputs() {}

	public SpellInputs(String _text) {
		this.text = _text;
	}

	// We don't log individual word spell check
	public Map<String, Object> summarizeForLogging() throws ServiceException {
		return null;
	}
}