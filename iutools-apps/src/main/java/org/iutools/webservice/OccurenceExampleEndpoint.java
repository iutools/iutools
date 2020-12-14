package org.iutools.webservice;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.inuktitutcomputing.morph.Gist;
import ca.inuktitutcomputing.nunhansearch.ProcessQuery;
import ca.inuktitutcomputing.utilities.Alignment;
import ca.nrc.json.PrettyPrinter;

public class OccurenceExampleEndpoint extends HttpServlet {
			
	public OccurenceExampleEndpoint() {
		this.initialize();
	};

	protected void initialize() {
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		EndPointHelper.log4jReload();
		Logger tLogger = Logger.getLogger("org.iutools.webservice.OccurenceExampleEndpoint.doPost");
		tLogger.trace("invoked");
		tLogger.trace("request URI= "+request.getRequestURI());
		
		String jsonResponse = null;
		
		OccurenceExampleInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, OccurenceExampleInputs.class);
			tLogger.trace("inputs= "+PrettyPrinter.print(inputs));
			ServiceResponse results = executeEndPoint(inputs);
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse =
				EndPointHelper.emitServiceExceptionResponse(
					"General exception was raised\n", exc);
		}
		writeJsonResponse(response, jsonResponse);
	}
	
	private void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.OccurenceExampleEndpoint.writeJsonResponse");
		PrintWriter writer = response.getWriter();
		writer.write(json);
		writer.close();
		tLogger.trace("Returning json="+json);
	}

	public OccurenceExampleResponse executeEndPoint(OccurenceExampleInputs inputs) throws Exception {
		Logger logger = Logger.getLogger("org.iutools.webservice.OccurenceExampleEndpoint.executeEndPoint");
		if (logger.isTraceEnabled()) {
			logger.trace("inputs= " + PrettyPrinter.print(inputs));
		}
				
		OccurenceExampleResponse results = new OccurenceExampleResponse();

		// Retrieve some occurences of the provided exampleWord
		// and put the results in results.occurences
		Gist gistOfWord = new Gist(inputs.exampleWord);
		logger.trace("gist= " + PrettyPrinter.print(gistOfWord));
		ProcessQuery processQuery = new ProcessQuery();
		String query = inputs.exampleWord;
		logger.trace("query= " + query);
		logger.trace("calling run() on processQuery=" + processQuery);
		String[] alignments = processQuery.run(query);
		Alignment[] aligns = new Alignment[alignments.length];
		for (int ial=0; ial<alignments.length; ial++)
			aligns[ial] = computeSentencePair(alignments[ial]);
		logger.trace("alignments= " + PrettyPrinter.print(alignments));
		// Gson gson = new Gson();
		// results.exampleWord = gson.toJson(gistOfWord, Gist.class);
		HashMap<String, Object> res = new HashMap<String, Object>();
		ExampleWordWithMorpheme exampleWord = 
				new ExampleWordWithMorpheme(inputs.exampleWord,gistOfWord,aligns);
		res.put("gist", gistOfWord);
		res.put("alignments", alignments);
		results.exampleWord = exampleWord;

		return results;
	}

	public Alignment computeSentencePair(String alignmentString) {
		Logger logger = Logger.getLogger("GistEndpoint.computeSentencePair");
		String[] alignmentParts = alignmentString.split("::");
		if (alignmentParts.length != 2)
			logger.debug("alignment string without ':: ' --- "+alignmentString);
		String[] sentences = alignmentParts[1].split("@----@");
		String inuktitutSentence = 
				sentences[0].replace("/\\.{5,}/","...").trim();
		String englishSentence = "";
		if (sentences.length > 1 && sentences[1] != null)
			englishSentence = sentences[1].replace("/\\.{5,}/","...").trim();
		Alignment sentencePair = new Alignment("iu",inuktitutSentence,"en",englishSentence);
		
		return sentencePair;
	}


}
