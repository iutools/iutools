package org.iutools.webservice.search;

import ca.nrc.string.StringUtils;
import org.iutools.morphrelatives.MorphologicalRelative;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ExpandQueryResult extends EndpointResult {

	public String origQuery = null;
	public String expandedQuery = null;
	public String expandedQuerySyll = null;

	public ExpandQueryResult() {}

	public ExpandQueryResult(String _origQuery) throws ServiceException {
		init_ExpandQueryResponse(_origQuery, (MorphologicalRelative[])null);
	}

	public ExpandQueryResult(
		String _origQuery, MorphologicalRelative[] _relatedWords) throws ServiceException {
		init_ExpandQueryResponse(_origQuery, _relatedWords);
	}

	private void init_ExpandQueryResponse(
	String _origQuery, MorphologicalRelative[] _relatedWords) throws ServiceException {
		origQuery = _origQuery;
		expandedQuery = origQuery;
		if (_relatedWords != null && _relatedWords.length > 0) {
			List<String> expandedTerms = new ArrayList<String>();
			for (MorphologicalRelative aRelatedWord : _relatedWords) {
				expandedTerms.add(aRelatedWord.getWord());
			}
			if (!expandedTerms.contains(origQuery)) {
				expandedTerms.add(0, origQuery);
			}

			expandedQuery =
				"(" + StringUtils.join(expandedTerms.iterator(), " OR ") + ")";
		}

		try {
			expandedQuerySyll =
				TransCoder.ensureScript(TransCoder.Script.SYLLABIC, expandedQuery);
		} catch (TransCoderException e) {
			throw new ServiceException(e);
		}

		return;
	}
}
