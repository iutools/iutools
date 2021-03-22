package org.iutools.webservice.tokenize;

import org.apache.commons.lang3.tuple.Pair;
import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class TokenizeEndpoint extends Endpoint<TokenizeInputs, TokenizeResult> {
	@Override
	protected TokenizeInputs requestInputs(HttpServletRequest request) throws ServiceException {
		return jsonInputs(request, TokenizeInputs.class);
	}

	@Override
	public EndpointResult execute(TokenizeInputs inputs) throws ServiceException {
		IUTokenizer tokenizer = new IUTokenizer();

		tokenizer.tokenize(inputs.text);
		List<Pair<String, Boolean>> tokens = tokenizer.getAllTokens();

		TokenizeResult result = new TokenizeResult(tokens);

		return result;

	}
}