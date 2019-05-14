package ca.pirurvik.iutools.webservice;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.inuktitutcomputing.config.IUConfig;
import ca.nrc.config.ConfigException;
import ca.nrc.data.harvesting.BingSearchEngine;
import ca.nrc.data.harvesting.SearchEngine;
import ca.nrc.data.harvesting.SearchEngine.Hit;
import ca.nrc.data.harvesting.SearchEngine.SearchEngineException;
import ca.nrc.data.harvesting.SearchEngine.Type;
import ca.nrc.datastructure.Pair;
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.CompiledCorpus;
import ca.pirurvik.iutools.CompiledCorpusRegistry;
import ca.pirurvik.iutools.CompiledCorpusRegistryException;
import ca.pirurvik.iutools.QueryExpanderException;
import ca.pirurvik.iutools.QueryExpander;
import ca.pirurvik.iutools.QueryExpansion;


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
	};
	
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {		
		String jsonResponse = null;

		EndPointHelper.setContenTypeAndEncoding(response);

		SearchInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, SearchInputs.class);
			ServiceResponse results = executeEndPoint(inputs);
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
		}
		
		writeJsonResponse(response, jsonResponse);
	}
	
	
	private void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.writeJsonResponse");
		tLogger.debug("json="+json);
		PrintWriter writer = response.getWriter();
		
		writer.write(json);
//		writer.println(json);
		writer.close();
		}

	private void traceToFile(String mess) throws IOException {
	    Writer writer = new BufferedWriter(new OutputStreamWriter(
	    	    new FileOutputStream("outfilename"), "UTF-8"));	    
	    writer.write(mess);
	     
	    writer.close();
		
	}

	public SearchResponse executeEndPoint(SearchInputs inputs) throws SearchEndpointException  {
		Logger logger = Logger.getLogger("SearchEndpoint.executeEndPoint");
		SearchResponse results = new SearchResponse();
		
		if (inputs.query == null || inputs.query.isEmpty()) {
			throw new SearchEndpointException("Query was empty or null");
		}
		
		try {
			results.expandedQuery = expandQuery(inputs.getQuerySyllabic());
		} catch (CompiledCorpusRegistryException | QueryExpanderException e) {
			throw new SearchEndpointException("Unable to expand the query", e);
		}
		
		Pair<Long,List<SearchHit>> hitsInfo = search(results.expandedQuery, inputs);;
		results.totalHits = hitsInfo.getFirst();
		results.hits = hitsInfo.getSecond();
		
		String fromExpandedQuery = results.expandedQuery;
		String hardCoded = "ᓄᓇᕗ";
		logger.debug("fromExpandedQuery= "+fromExpandedQuery);
		logger.debug("hardCoded= "+hardCoded);

		return results;
	}

	private Pair<Long, List<SearchHit>> search(String query, SearchInputs inputs) throws SearchEndpointException {
		
		List<SearchHit> hits = new ArrayList<SearchHit>();
		Long totalHits = new Long(0);
		BingSearchEngine engine;
		try {
			engine = new BingSearchEngine();
		} catch (IOException | SearchEngineException e) {
			throw new SearchEndpointException(e);
		}
		SearchEngine.Query webQuery = 
				new SearchEngine.Query(query).setType(Type.ANY)
						.setLang("iu").setMaxHits(inputs.hitsPerPage)
				;
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
			totalHits = bingHit.outOfTotal;
			SearchHit aHit = new SearchHit(bingHit.url.toString(), bingHit.title, bingHit.summary);
			hits.add(aHit);
			
		}
		
		return Pair.of(totalHits, hits);
	}

	protected String expandQuery(String query) throws SearchEndpointException, CompiledCorpusRegistryException, QueryExpanderException {
		String expandedQuery = query;
		
		// TODO: Re-activate this once Microsoft has figured out the bug in Bing that causes
		//   expanded queries to return pages in Asian languages

//		QueryExpansion[] expansions = null;
//		try {
//			if (expander == null) {
//				CompiledCorpus compiledCorpus = CompiledCorpusRegistry.getCorpus();
//				expander = new QueryExpander(compiledCorpus);
//			}
//			expansions = expander.getExpansions(query);			
//		} catch (ConfigException e) {
//			throw new SearchEndpointException(e);
//		}
//		
//		
//		String expandedQuery = "(";
//		boolean isFirst = true;
//		for (QueryExpansion exp: expansions) {
//			if (!isFirst) {
//				expandedQuery += " OR ";
//			}
//			expandedQuery += exp.word;
//			isFirst = false;
//		}
//		expandedQuery += ")";	
	
		return expandedQuery;		
	}
}
