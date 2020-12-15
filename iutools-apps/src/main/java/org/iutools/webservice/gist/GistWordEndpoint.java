package org.iutools.webservice.gist;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.iutools.morph.Gist;
import ca.inuktitutcomputing.nunhansearch.ProcessQuery;
import ca.inuktitutcomputing.utilities.Alignment;
import ca.nrc.json.PrettyPrinter;
import org.iutools.webservice.EndPointHelper;

public class GistWordEndpoint extends HttpServlet {
	
	public GistWordEndpoint() {
		this.init_GistWordEndpoint();
	};

	protected void init_GistWordEndpoint() {
	}
	
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		EndPointHelper.log4jReload();
		Logger tLogger = Logger.getLogger("org.iutools.webservice.OccurenceExampleEndpoint.doPost");
		tLogger.trace("invoked");
		tLogger.trace("request URI= "+request.getRequestURI());
		
		String jsonResponse = null;
		
		GistWordInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, GistWordInputs.class);
			tLogger.trace("inputs= "+PrettyPrinter.print(inputs));
			GistWordResponse results = executeEndPoint(inputs);
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
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
	
	public GistWordResponse executeEndPoint(GistWordInputs inputs) throws Exception {
		Logger logger = Logger.getLogger("org.iutools.webservice.GistWordEndpoint.executeEndPoint");
		logger.trace("inputs= " + PrettyPrinter.print(inputs));

		GistWordResponse response = new GistWordResponse(inputs.word);

		// Get the decomposition of the word into morphemes with their 
		// respective meanings
		//
		Gist gistOfWord = new Gist(inputs.word);
		logger.trace("gist= " + PrettyPrinter.print(gistOfWord));
		
		// Retrieve aligned sentences that contain the word
		//
		ProcessQuery processQuery = new ProcessQuery();
		String query = inputs.getWordRomanized();
		logger.trace("query= " + query);
		logger.trace("calling run() on processQuery=" + processQuery);
		String[] alignments = processQuery.run(query);
		Alignment[] aligns = new Alignment[alignments.length];
		for (int ial=0; ial<alignments.length; ial++)
			aligns[ial] = computeSentencePair(alignments[ial]);
		logger.trace("alignments= " + PrettyPrinter.print(alignments));
		
		
		response.wordGist = gistOfWord;
		response.alignments = aligns;

		return response;
	}
	
	protected Alignment computeSentencePair(String alignmentString) {
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
