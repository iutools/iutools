package org.iutools.webservice.search;

import org.iutools.morphrelatives.MorphRelativesFinder;
import org.iutools.morphrelatives.MorphRelativesFinderException;
import org.iutools.morphrelatives.MorphologicalRelative;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;
import org.iutools.webservice.morphexamples.MorphemeExamplesInputs;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;

public class ExpandQuery2Endpoint
	extends Endpoint<ExpandQuery2Inputs, ExpandQuery2Result> {
	@Override
	protected ExpandQuery2Inputs requestInputs(HttpServletRequest request) throws ServiceException {
		return jsonInputs(request, ExpandQuery2Inputs.class);
	}

	@Override
	public EndpointResult execute(ExpandQuery2Inputs inputs) throws ServiceException {
		MorphRelativesFinder relsFinder = null;
		try {
			relsFinder = new MorphRelativesFinder();
		} catch (MorphRelativesFinderException e) {
			throw new ServiceException(
				"Could not instantiate the related words finder", e);
		}

		MorphologicalRelative[] relatedWords = null;
		if (!queryAlreadyExpanded(inputs.origQuery)) {
			try {
				relatedWords = relsFinder.findRelatives(inputs.origQuery);
			} catch (MorphRelativesFinderException e) {
				throw new ServiceException(
					"Exception raised while searching for related words", e);
			}
		}

		ExpandQuery2Result response =
			new ExpandQuery2Result(inputs.origQuery, relatedWords);

		return response;
	}

	private boolean queryAlreadyExpanded(String query) {
		boolean alreadyExpanded = (query.matches("^\\s*\\(.*\\)\\s*$"));
		return alreadyExpanded;
	}
}
