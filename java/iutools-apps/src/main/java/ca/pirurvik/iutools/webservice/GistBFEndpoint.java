package ca.pirurvik.iutools.webservice;


import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.inuktitutcomputing.data.Morpheme;
import ca.inuktitutcomputing.morph.Gist;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.Decomposition.DecompositionExpression;
import ca.inuktitutcomputing.nunhansearch.ProcessQuery;
import ca.nrc.config.ConfigException;
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.CompiledCorpus;
import ca.pirurvik.iutools.CompiledCorpusRegistry;
import ca.pirurvik.iutools.CompiledCorpusRegistryException;
import ca.pirurvik.iutools.morphemesearcher.MorphemeSearcher;
import ca.pirurvik.iutools.morphemesearcher.ScoredExample;

public class GistBFEndpoint extends HttpServlet {
			
	public GistBFEndpoint() {
		this.initialize();
	};

	protected void initialize() {
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.GistBFEndpoint.doPost");
		tLogger.trace("invoked");
		tLogger.trace("request URI= "+request.getRequestURI());
		
		String jsonResponse = null;
		
		GistBFInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, GistBFInputs.class);
			tLogger.trace("inputs= "+PrettyPrinter.print(inputs));
			ServiceResponse results = executeEndPoint(inputs);
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
		}
		writeJsonResponse(response, jsonResponse);
	}
	
	private void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.GistBFEndpoint.writeJsonResponse");
		PrintWriter writer = response.getWriter();
		writer.write(json);
		writer.close();
		tLogger.trace("Returning json="+json);
	}

	public GistBFResponse executeEndPoint(GistBFInputs inputs) 
			throws Exception  {
		Logger logger = Logger.getLogger("ca.pirurvik.iutools.webservice.GistBFEndpoint.executeEndPoint");
		logger.trace("inputs= "+PrettyPrinter.print(inputs));
		GistBFResponse results = new GistBFResponse();
		
		if (inputs.word == null || inputs.word.isEmpty()) {
			throw new SearchEndpointException("Word was empty or null");
		}
		
		MorphologicalAnalyzer analyzer = new MorphologicalAnalyzer();
		Decomposition[] decompositions = analyzer.decomposeWord(inputs.word,false);
		DecompositionExpression[] decompositionExpressions = new DecompositionExpression[decompositions.length];
		for (int idec=0; idec<decompositions.length; idec++) {
			DecompositionExpression decExp = new DecompositionExpression(decompositions[idec].toStr2());
			decExp.getMeanings("en");
			decompositionExpressions[idec] = decExp;
		}
		
		results.decompositions = decompositionExpressions;
		
		ProcessQuery processQuery = new ProcessQuery();
		String query = inputs.word;
		logger.trace("query= "+query);
		logger.trace("calling run() on processQuery="+processQuery);
		String[] alignments = processQuery.run(query);
		logger.trace("alignments= "+PrettyPrinter.print(alignments));
		results.alignments = alignments;
		
		

		return results;
	}


}
