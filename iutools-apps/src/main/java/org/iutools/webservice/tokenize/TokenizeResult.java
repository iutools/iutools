package org.iutools.webservice.tokenize;

import org.iutools.text.segmentation.Token;
import org.iutools.webservice.EndpointResult;

import java.util.List;

public class TokenizeResult extends EndpointResult {

	public List<Token> tokens;

	public TokenizeResult() {}

	public TokenizeResult(List<Token> _tokens) {
		this.tokens = _tokens;
	}
}