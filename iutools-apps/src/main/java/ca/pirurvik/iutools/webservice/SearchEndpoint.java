package ca.pirurvik.iutools.webservice;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import ca.pirurvik.iutools.morphrelatives.MorphRelativesFinder_ES;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.nrc.data.harvesting.SearchEngine.Query;
import ca.nrc.data.harvesting.SearchEngine.Hit;
import ca.nrc.data.harvesting.SearchEngine.SearchEngineException;
import ca.nrc.data.harvesting.SearchResults;
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.morphrelatives.MorphRelativesFinderException;
import ca.pirurvik.iutools.morphrelatives.MorphRelativesFinder;
import ca.pirurvik.iutools.morphrelatives.MorphologicalRelative;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistryException;
import ca.pirurvik.iutools.search.IUSearchEngine;
import ca.pirurvik.iutools.search.SearchHit;


public class SearchEndpoint extends HttpServlet {
	/**
	 * 
	 */
	private static final long serialVersionUID = -4670287970764735344L;
	EndPointHelper helper = null;
	
    MorphRelativesFinder expander = null;    
    	
	public SearchEndpoint() {
	};
	
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		Logger logger = Logger.getLogger("SearchEndpoint.doGet");
		logger.debug("doGet()");
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		EndPointHelper.log4jReload();		
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.SearchEndpoint.doPost");
		
				
		String jsonResponse = null;

		EndPointHelper.setContenTypeAndEncoding(response);

		SearchInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, SearchInputs.class);
			tLogger.trace("inputs="+PrettyPrinter.print(inputs));
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
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.SearchEndpoint.executeEndPoint");
		
		tLogger.trace("invoked");
		SearchResponse results = new SearchResponse();

		String query = inputs.query;
		if (query == null || query.isEmpty()) {
			throw new SearchEndpointException("Query was empty or null");
		}
		
		List<String> queryWords = null;
		try {
			expandQuery(inputs.convertQueryToSyllabic(), results);
			tLogger.trace("Expanded query is "+PrettyPrinter.print(results.expandedQueryWords));
			queryWords = results.expandedQueryWords;
		} catch (MorphRelativesFinderException e) {
			throw new SearchEndpointException("Unable to expand the query", e);
		}
		
		SearchResults searchResults = search(queryWords, inputs);;
		results.totalHits = searchResults.estTotalHits;
		
		results.hits = new ArrayList<SearchHit>();
		for (Hit aHit: searchResults.retrievedHits) {
			results.hits.add(new SearchHit(aHit.url.toString(), aHit.title, aHit.summary));
		}
		
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("Number of hits found: "+results.hits.size());
		}
		
		return results;
	}

	private SearchResults search(List<String> queryWords, SearchInputs inputs) throws SearchEndpointException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.SearchEndpoint.search");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("Invoked with queryWords="+PrettyPrinter.print(queryWords));
		}
		
		IUSearchEngine engine;
		try {
			engine = new IUSearchEngine();
		} catch (IOException | SearchEngineException e) {
			throw new SearchEndpointException("Unable to create the search engine.", e);
		}
		
		
		Query query = new Query();
		{
			// We collect 10 pages worth of hits
			int hitsPerPage = inputs.hitsPerPage;
			query.setMaxHits(10*hitsPerPage);
			
			query.terms = queryWords;
			query.lang = "iu";
		}
		
		SearchResults results;
		try {
			results = engine.search(query);
		} catch (SearchEngineException | IOException e) {
			throw new SearchEndpointException("Error while searching for hits", e);
		}
		
		return results;
	}

	protected void expandQuery(String query, SearchResponse results)
		throws MorphRelativesFinderException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.SearchEndpoint.expandQuery");
		List<String> expansionWords = this.isExpandedQuery(query);
		String expandedQuery = query;
		if (expansionWords == null) {
			// The query was not already an expanded query
			MorphologicalRelative[] expansions = null;
			expansionWords = new ArrayList<String>();			
			if (expander == null) {
				expander = new MorphRelativesFinder_ES();
			}
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("expanded is of type: " + expander.getClass() + "; its corpus is of type: " + expander.compiledCorpus.getClass() + "; corpusDetails = " + PrettyPrinter.print(expander.compiledCorpus));
			}
			expansions = expander.findRelatives(query);
						
			expandedQuery = "(";
			boolean inputWordInExpansions = false;
			boolean isFirst = true;
			for (MorphologicalRelative exp: expansions) {
				if (!isFirst) {
					expandedQuery += " OR ";
				}
				expandedQuery += exp.getWord();
				if (exp.getWord().equals(query)) {
					inputWordInExpansions = true;
				}
				isFirst = false;
			}
			if (!inputWordInExpansions) {

				if (!isFirst) {
					expandedQuery += " OR ";
				}
				expandedQuery += query;
				expansionWords.add(query);				
			}
			expandedQuery += ")";	
			
			for (MorphologicalRelative anExpansion: expansions) {
				expansionWords.add(anExpansion.getWord());
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
