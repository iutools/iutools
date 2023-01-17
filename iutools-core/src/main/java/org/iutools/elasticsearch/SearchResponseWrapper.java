package org.iutools.elasticsearch;

import ca.nrc.dtrc.elasticsearch.Document;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;

import java.util.ArrayList;
import java.util.List;

/** Wrapper for an ElasticSearch SearchResponse */
public class SearchResponseWrapper<T extends Document> {

	SearchResponse<T> response = null;
	public SearchResponseWrapper(SearchResponse<T> _response) {
		this.response = _response;
		return;
	}

	public List<T> documents() {
		List<T> docs = new ArrayList<T>();
		for (Hit<T> aHit: response.hits().hits()) {
			docs.add((T) aHit.source());
		}
		return docs;
	}
}
