package org.iutools.webservice.tokenize;

import ca.nrc.datastructure.Pair;
import org.iutools.webservice.EndpointResult;

import java.util.List;

public class TokenizeResult extends EndpointResult {

	public List<Pair<String, Boolean>> tokens;

	public TokenizeResult() {}

	public TokenizeResult(List<Pair<String, Boolean>> _tokens) {
		this.tokens = _tokens;
	}
}