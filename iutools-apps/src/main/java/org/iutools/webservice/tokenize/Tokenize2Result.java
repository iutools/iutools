package org.iutools.webservice.tokenize;

import ca.nrc.datastructure.Pair;
import org.iutools.webservice.EndpointResult;

import java.util.List;

public class Tokenize2Result extends EndpointResult {

	public List<Pair<String, Boolean>> tokens;

	public Tokenize2Result() {}

	public Tokenize2Result(List<Pair<String, Boolean>> _tokens) {
		this.tokens = _tokens;
	}
}