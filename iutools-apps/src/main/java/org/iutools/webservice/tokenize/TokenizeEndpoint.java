package org.iutools.webservice.tokenize;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.text.segmentation.Token;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;

import java.util.List;

public class TokenizeEndpoint extends Endpoint<TokenizeInputs, TokenizeResult> {
	@Override
	protected TokenizeInputs requestInputs(String jsonRequestBody) throws ServiceException {
		return jsonInputs(jsonRequestBody, TokenizeInputs.class);
	}

	@Override
	public EndpointResult execute(TokenizeInputs inputs) throws ServiceException {
		Logger tLogger = LogManager.getLogger("org.iutools.webservice.tokenize.TokenizeEndpoint.execute");
		IUTokenizer tokenizer = new IUTokenizer();

		tokenizer.tokenize(inputs.text);
		List<Token> tokens = tokenizer.getAllTokens();
		int totaWords = totalWords(tokens);
		Integer maxWords = inputs.maxWords;
		if (maxWords == null || maxWords > 500) {
			// Note: We don't allow a transaction to provide a maxWords that is
			//   higher than 500. This is to prevent "attacks" where
			//   a client would send a very large text, AND provide a very large
			//   maxWords value.
			//
			maxWords = 500;
		}
		tLogger.trace("totalWords="+totaWords+", maxWords="+maxWords);
		TokenizeResult result = null;
		if (totaWords > maxWords) {
			result =
				(TokenizeResult)new TokenizeResult()
					.setError(
						"Text is too long ("+totaWords+" words).\n"+
						"Split it into chunks of at most "+maxWords+" words.");
		} else {
			result = new TokenizeResult(tokens);
		}

		return result;

	}

	private int totalWords(List<Token> tokens) {
		int total = 0;
		for (Token aToken: tokens) {
			if (aToken.isWord) {
				total++;
			}
		}
		return total;
	}
}