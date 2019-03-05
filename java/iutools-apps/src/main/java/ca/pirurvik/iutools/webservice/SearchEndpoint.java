package ca.pirurvik.iutools.webservice;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.Arrays;
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


public class SearchEndpoint extends HttpServlet {
	private String endPointName = null;
	private String esDefaultIndex = "dedupster";
	EndPointHelper helper = null;

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
			results.expandedQuery = expandQuery(inputs.query);
		} catch (CompiledCorpusRegistryException | QueryExpanderException e) {
			throw new SearchEndpointException("Unable to expand the query", e);
		}
		
		//		String collection = inputs.collection;
//		if (collection == null) {
//			throw new DedupsterServiceException("Missing 'collection' argument");
//		}
//		
//		List<DocWithDups> bugs = inputs.inputBugs();
//				
//		
//		try {
//			
//			if (bugs == null) {
//				throw new DedupsterServiceException("No bugs provided for 'put' endpoint");
//			} else {
//				StreamlinedClient esClient = EndPointHelper.getESClient(inputs);
//				for (DocWithDups aBug: bugs) {
//					String jsonResp = esClient.putDocument(collection, aBug);
//				}
//			}
//		} catch (Exception exc) {
//			results.setException(exc);
//		}
		
		return results;
	}

	protected String expandQuery(String query) throws SearchEndpointException, CompiledCorpusRegistryException, QueryExpanderException {
        CompiledCorpus compiledCorpus;
        QueryExpansion[] expansions = null;
		try {
			compiledCorpus = CompiledCorpusRegistry.getCorpus();
	        QueryExpander expander = new QueryExpander(compiledCorpus);
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
			expandedQuery += exp;
			isFirst = false;
		}
		expandedQuery += ")";	
	
		return expandedQuery;		
	}
}
