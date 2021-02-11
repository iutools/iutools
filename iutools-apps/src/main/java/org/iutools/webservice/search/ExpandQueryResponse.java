package org.iutools.webservice.search;

import ca.nrc.string.StringUtils;
import org.iutools.morphrelatives.MorphologicalRelative;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.webservice.ServiceResponse;

import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.List;

public class ExpandQueryResponse extends ServiceResponse {

	public String origQuery = null;
	public String expandedQuery = null;
	public String expandedQuerySyll = null;

	public ExpandQueryResponse() {}

	public ExpandQueryResponse(String _origQuery) {
		init_ExpandQueryResponse(_origQuery, (MorphologicalRelative[])null);
	}

	public ExpandQueryResponse(
		String _origQuery, MorphologicalRelative[] _relatedWords) {
		init_ExpandQueryResponse(_origQuery, _relatedWords);
	}

	private void init_ExpandQueryResponse(
	String _origQuery, MorphologicalRelative[] _relatedWords) {
		origQuery = _origQuery;
		expandedQuery = origQuery;
		if (_relatedWords != null && _relatedWords.length > 0) {
			List<String> expandedTerms = new ArrayList<String>();
			for (MorphologicalRelative aRelatedWord : _relatedWords) {
				expandedTerms.add(aRelatedWord.getWord());
			}
			expandedQuery =
				"(" + StringUtils.join(expandedTerms.iterator(), " OR ") + ")";
		}

		try {
			expandedQuerySyll =
				TransCoder.ensureScript(TransCoder.Script.SYLLABIC, expandedQuery);
		} catch (TransCoderException e) {
			throw new WebServiceException(e);
		}
	}
}