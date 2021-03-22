package org.iutools.webservice.tokenize;

import org.iutools.webservice.ServiceInputs;

public class TokenizeInputs extends ServiceInputs {

	public String text = null;

	public TokenizeInputs() {}

	public TokenizeInputs(String _text) {
		this.text = _text;
	}
}