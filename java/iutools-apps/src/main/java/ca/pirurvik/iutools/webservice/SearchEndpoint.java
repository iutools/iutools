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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import ca.pirurvik.iutools.search.BingSearchMultithrd;
import ca.pirurvik.iutools.search.SearchHit;


public class SearchEndpoint extends HttpServlet {
	private String endPointName = null;
	private String esDefaultIndex = "dedupster";
	EndPointHelper helper = null;
	
    QueryExpander expander = null;    
    
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
		writer.close();
		}

	public SearchResponse executeEndPoint(SearchInputs inputs) throws SearchEndpointException  {
		Logger logger = Logger.getLogger("SearchEndpoint.executeEndPoint");
		SearchResponse results = new SearchResponse();
		
		if (inputs.query == null || inputs.query.isEmpty()) {
			throw new SearchEndpointException("Query was empty or null");
		}
		
		List<String> queryWords = null;
		try {
			expandQuery(inputs.getQuerySyllabic(), results);
			queryWords = results.expandedQueryWords;
		} catch (CompiledCorpusRegistryException | QueryExpanderException e) {
			throw new SearchEndpointException("Unable to expand the query", e);
		}
		
		Pair<Long,List<SearchHit>> hitsInfo = search(queryWords, inputs);;
		results.totalHits = hitsInfo.getFirst();
		results.hits = hitsInfo.getSecond();
		return results;
	}

	private Pair<Long, List<SearchHit>> search(List<String> queryWords, SearchInputs inputs) throws SearchEndpointException {
		
		Long totalHits = new Long(0);
		BingSearchMultithrd engine;
		engine = new BingSearchMultithrd();
				
		Pair<Long, List<SearchHit>> results;
		String[] queryWordsArr = queryWords.toArray(new String[queryWords.size()]);
		results = engine.search(queryWordsArr, inputs.hitsPageNum, inputs.hitsPerPage, inputs.excludedHits);
		
		Long estTotalHits = results.getFirst();
		List<SearchHit> hits = results.getSecond();
	
		return Pair.of(estTotalHits, hits);
	}

	protected void expandQuery(String query, SearchResponse results) throws SearchEndpointException, CompiledCorpusRegistryException, QueryExpanderException {
		
		List<String> expansionWords = this.isExpandedQuery(query);
		String expandedQuery = query;
		if (expansionWords == null) {
		
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
			
			
			expandedQuery = "(";
			boolean isFirst = true;
			for (QueryExpansion exp: expansions) {
				if (!isFirst) {
					expandedQuery += " OR ";
				}
				expandedQuery += exp.word;
				isFirst = false;
			}
			expandedQuery += ")";	
			
			expansionWords = new ArrayList<String>();
			for (QueryExpansion anExpansion: expansions) {
				expansionWords.add(anExpansion.word);
			}
		}
	
		results.expandedQuery = expandedQuery;
		results.expandedQueryWords = expansionWords;
	}

	private List<String> isExpandedQuery(String query) {
		List<String> expansionWords = null;
		
		Matcher matcher = Pattern.compile("^\\s*\\(\\s*(\\s*[\\s\\S]*?)\\s*\\)\\s*$").matcher(query);
		if (matcher.matches()) {
			String termsORed = matcher.group(1);
			String[] terms = termsORed.split("\\s+OR\\s+");
			expansionWords = Arrays.asList(terms);
		}
				
		
		return expansionWords;
	}
}
