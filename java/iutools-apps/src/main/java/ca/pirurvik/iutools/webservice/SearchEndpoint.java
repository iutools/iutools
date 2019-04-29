package ca.pirurvik.iutools.webservice;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.inuktitutcomputing.config.IUConfig;
import ca.inuktitutcomputing.core.CompiledCorpus;
import ca.inuktitutcomputing.core.CompiledCorpusRegistry;
import ca.inuktitutcomputing.core.CompiledCorpusRegistryException;
import ca.inuktitutcomputing.core.QueryExpander;
import ca.inuktitutcomputing.core.QueryExpanderException;
import ca.inuktitutcomputing.core.QueryExpansion;
import ca.nrc.config.ConfigException;
import ca.nrc.data.harvesting.BingSearchEngine;
import ca.nrc.data.harvesting.SearchEngine;
import ca.nrc.data.harvesting.SearchEngine.Hit;
import ca.nrc.data.harvesting.SearchEngine.SearchEngineException;
import ca.nrc.data.harvesting.SearchEngine.Type;


public class SearchEndpoint extends HttpServlet {
	private String endPointName = null;
	private String esDefaultIndex = "dedupster";
	EndPointHelper helper = null;
	
    QueryExpander expander = null;    
    
    static int MAX_HITS = 10;


	protected void initialize(String _esIndexName, String _endPointName) {
		if (_esIndexName != null) this.esDefaultIndex = _esIndexName;
		if (_endPointName != null) this.endPointName = _endPointName;
	}
	
	public SearchEndpoint() {
//		initialize(null, "put");
	};
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Logger tLogger = LogManager.getLogger("ca.nrc.dtrc.dedupster.webservice.PutEndPoint.doPost");
		tLogger.trace("invoked");
		
		
		PrintWriter out = response.getWriter();
		String jsonResponse = null;
		
		SearchInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, SearchInputs.class);
			ServiceResponse results = executeEndPoint(inputs);
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (MalformedURLException exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("The training URL was malformed", exc);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
		}
		
		
		out.println(jsonResponse);
	}
	
	public SearchResponse executeEndPoint(SearchInputs inputs) throws SearchEndpointException  {
		SearchResponse results = new SearchResponse();
		
		if (inputs.query == null || inputs.query.isEmpty()) {
			throw new SearchEndpointException("Query was empty or null");
		}
		
		try {
			results.expandedQuery = expandQuery(inputs.getQuerySyllabic());
		} catch (CompiledCorpusRegistryException | QueryExpanderException e) {
			throw new SearchEndpointException("Unable to expand the query", e);
		}
		
		results.hits = search(results.expandedQuery);
		

		return results;
	}

	private List<SearchHit> search(String query) throws SearchEndpointException {
		
		List<SearchHit> hits = new ArrayList<SearchHit>();
		BingSearchEngine engine;
		try {
			engine = new BingSearchEngine();
		} catch (IOException | SearchEngineException e) {
			throw new SearchEndpointException(e);
		}
		SearchEngine.Query webQuery = new SearchEngine.Query(query).setType(Type.ANY).setLang("iu");
		List<SearchEngine.Hit> results;
		try {
			results = engine.search(webQuery);
		} catch (SearchEngineException e) {
			throw new SearchEndpointException(e);
		}
		
		Iterator<Hit> iter = results.iterator();
		int hitsCount = 0;
		while (iter.hasNext()) {
			hitsCount++;
			if (hitsCount > MAX_HITS) {
				break;
			}
			Hit bingHit = iter.next();
			SearchHit aHit = new SearchHit(bingHit.url.toString(), bingHit.title, bingHit.summary);
			hits.add(aHit);
		}
		
		return hits;
	}

	protected String expandQuery(String query) throws SearchEndpointException, CompiledCorpusRegistryException, QueryExpanderException {
        QueryExpansion[] expansions = null;
		try {
			if (expander == null) {
				CompiledCorpus compiledCorpus = CompiledCorpusRegistry.getCorpus();
				expander = new QueryExpander(compiledCorpus);
			}
			expansions = expander.getExpansions(query);			
		} catch (ConfigException e) {
			throw new SearchEndpointException(e);
		}
		
		String expandedQuery = "(";
		boolean isFirst = true;
		for (QueryExpansion exp: expansions) {
			if (!isFirst) {
				expandedQuery += " OR ";
			}
			expandedQuery += exp.word;
			isFirst = false;
		}
		expandedQuery += ")";	
	
		return expandedQuery;		
	}
}
