package org.iutools.webservice.tokenize;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.webservice.EndpointResult;

import java.util.List;

public class TokenizeResult extends EndpointResult {

	public List<Pair<String, Boolean>> tokens;

	public TokenizeResult() {}

	public TokenizeResult(List<Pair<String, Boolean>> _tokens) {
		this.tokens = _tokens;
	}
}