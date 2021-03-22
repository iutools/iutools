package org.iutools.webservice.tokenize;

import org.iutools.webservice.ServiceInputs;

public class Tokenize2Inputs extends ServiceInputs {

	public String text = null;

	public Tokenize2Inputs() {}

	public Tokenize2Inputs(String _text) {
		this.text = _text;
	}
}