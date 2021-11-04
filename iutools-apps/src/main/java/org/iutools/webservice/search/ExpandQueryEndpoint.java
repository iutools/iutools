package org.iutools.webservice.search;

import org.iutools.morphrelatives.MorphRelativesFinder;
import org.iutools.morphrelatives.MorphRelativesFinderException;
import org.iutools.morphrelatives.MorphologicalRelative;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;

public class ExpandQueryEndpoint
	extends Endpoint<ExpandQueryInputs, ExpandQueryResult> {
	@Override
	protected ExpandQueryInputs requestInputs(String jsonRequestBody) throws ServiceException {
		return jsonInputs(jsonRequestBody, ExpandQueryInputs.class);
	}

	@Override
	public EndpointResult execute(ExpandQueryInputs inputs) throws ServiceException {
		MorphRelativesFinder relsFinder = null;
		try {
			relsFinder = new MorphRelativesFinder();
		} catch (MorphRelativesFinderException e) {
			throw new ServiceException(
				"Could not instantiate the related words finder", e);
		}

		MorphologicalRelative[] relatedWords = null;
		// Remove spaces at start and end of query
		inputs.origQuery = inputs.origQuery.replaceAll("(^\\s*|\\s*$)", "");
		if (!queryAlreadyExpanded(inputs.origQuery)) {
			try {
				relatedWords = relsFinder.findRelatives(inputs.origQuery);
			} catch (MorphRelativesFinderException e) {
				throw new ServiceException(
					"Exception raised while searching for related words", e);
			}
		}

		ExpandQueryResult response =
			new ExpandQueryResult(inputs.origQuery, relatedWords);

		return response;
	}

	private boolean queryAlreadyExpanded(String query) {
		boolean alreadyExpanded = (query.matches("^\\s*\\(.*\\)\\s*$"));
		return alreadyExpanded;
	}
}
