package org.iutools.webservice.spell;

import org.iutools.webservice.ServiceInputs;

public class Spell2Inputs extends ServiceInputs {
	public String text = null;
	public boolean includePartiallyCorrect = false;

	public Spell2Inputs() {}

	public Spell2Inputs(String _text) {
		this.text = _text;
	}
}