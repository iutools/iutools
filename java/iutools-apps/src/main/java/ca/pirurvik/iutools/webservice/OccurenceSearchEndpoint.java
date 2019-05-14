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
import ca.nrc.config.ConfigException;
import ca.nrc.data.harvesting.BingSearchEngine;
import ca.nrc.data.harvesting.SearchEngine;
import ca.nrc.data.harvesting.SearchEngine.Hit;
import ca.nrc.data.harvesting.SearchEngine.SearchEngineException;
import ca.nrc.data.harvesting.SearchEngine.Type;
import ca.nrc.datastructure.Pair;
import ca.pirurvik.iutools.CompiledCorpus;
import ca.pirurvik.iutools.CompiledCorpusRegistry;
import ca.pirurvik.iutools.CompiledCorpusRegistryException;
import ca.pirurvik.iutools.QueryExpanderException;
import ca.pirurvik.iutools.QueryExpander;
import ca.pirurvik.iutools.QueryExpansion;


public class OccurenceSearchEndpoint extends HttpServlet {
			
	public OccurenceSearchEndpoint() {
		this.initialize();
	};

	protected void initialize() {
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Logger tLogger = LogManager.getLogger("ca.pirurvik.iutools.webservice.CorpusMineEndpoint.doPost");
		tLogger.trace("invoked");
		
		
		PrintWriter out = response.getWriter();
		String jsonResponse = null;
		
		OccurenceSearchInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, OccurenceSearchInputs.class);
			ServiceResponse results = executeEndPoint(inputs);
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
		}
		writeJsonResponse(response, jsonResponse);
	}
	
	private void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
		response.setContentType("text/html");
		response.setCharacterEncoding("utf-8");
		PrintWriter writer = response.getWriter();
		writer.write(json);
		writer.close();
	}

	public OccurenceSearchResponse executeEndPoint(OccurenceSearchInputs inputs) throws SearchEndpointException, ConfigException, CompiledCorpusRegistryException  {
		OccurenceSearchResponse results = new OccurenceSearchResponse();
		
		if (inputs.wordPattern == null || inputs.wordPattern.isEmpty()) {
			throw new SearchEndpointException("Word pattern was empty or null");
		}
		
		CompiledCorpus corpus = getCorpus(inputs.corpusName);
		
		if (inputs.exampleWord == null) {
			// Retrieve all words that match the wordPattern
			// and put the results in results.matchingWords
		} else {
			// Retrieve some occurences of the provided exampleWord
			// and put the results in results.occurences
		}
		

		return results;
	}

	private CompiledCorpus getCorpus(String corpusName) throws ConfigException, CompiledCorpusRegistryException {
		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus(corpusName);
		return corpus;
	}


}
