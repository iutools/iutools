package org.iutools.webservice.tokenize;

import ca.nrc.datastructure.Pair;
import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class Tokenize2Endpoint extends Endpoint<Tokenize2Inputs, Tokenize2Result> {
	@Override
	protected Tokenize2Inputs requestInputs(HttpServletRequest request) throws ServiceException {
		return jsonInputs(request, Tokenize2Inputs.class);
	}

	@Override
	public EndpointResult execute(Tokenize2Inputs inputs) throws ServiceException {
		IUTokenizer tokenizer = new IUTokenizer();

		tokenizer.tokenize(inputs.text);
		List<Pair<String, Boolean>> tokens = tokenizer.getAllTokens();

		Tokenize2Result result = new Tokenize2Result(tokens);

		return result;

	}
}