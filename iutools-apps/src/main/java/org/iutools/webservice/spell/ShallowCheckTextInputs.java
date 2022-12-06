package org.iutools.webservice.spell;

import org.iutools.webservice.ServiceInputs;

public class ShallowCheckTextInputs extends ServiceInputs {

	public String origText = null;

	public ShallowCheckTextInputs() {

	}

	public ShallowCheckTextInputs(String _origText) {
		super();
		origText = _origText;
	}
}
