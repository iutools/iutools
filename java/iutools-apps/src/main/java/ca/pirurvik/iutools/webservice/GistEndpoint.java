package ca.pirurvik.iutools.webservice;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.Decomposition.DecompositionExpression;
import ca.inuktitutcomputing.nunhansearch.ProcessQuery;
import ca.inuktitutcomputing.utilities.Alignment;
import ca.inuktitutcomputing.morph.MorphInukException;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.nrc.config.ConfigException;

public class GistEndpoint extends HttpServlet {

	EndPointHelper helper = null;
	
    static MorphologicalAnalyzer analyzer = null;  
	

	public GistEndpoint() throws GistEndpointException  {
		init_GistEndpoint();
	};
	
	protected void init_GistEndpoint() throws GistEndpointException  {
		if (analyzer == null) {
	    	try {
				analyzer = new MorphologicalAnalyzer();
			} catch (LinguisticDataException e) {
				throw new GistEndpointException(
						"Could not create the morphological analyzer", e);
			}
		}
	}
	
	protected void doPost(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		String jsonResponse = null;

		GistInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, GistInputs.class);
			GistResponse results = processInputs(inputs);			
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
		}
		
		writeJsonResponse(response, jsonResponse);
	}
	
	private GistResponse processInputs(GistInputs inputs) throws GistEndpointException {
		Logger logger = Logger.getLogger("ca.pirurvik.iutools.webservice.GistEndpoint.processInputs");
		GistResponse results = new GistResponse();
		
		try {
			Decomposition[] decompositions = analyzer.decomposeWord(inputs.word,false);
			logger.debug("Nb. decompositions for "+inputs.word+": "+decompositions.length);
			DecompositionExpression[] decompositionExpressions = new DecompositionExpression[decompositions.length];
			for (int idec=0; idec<decompositions.length; idec++) {
				DecompositionExpression decExp = new DecompositionExpression(decompositions[idec].toStr2());
				decExp.getMeanings("en");
				decompositionExpressions[idec] = decExp;
			}
			logger.debug("Nb. decompositionExpression: "+decompositionExpressions.length);
			results.decompositions = decompositionExpressions;
		} catch (TimeoutException | MorphInukException e) {
			throw new GistEndpointException(
					"Error trying to decompose word "+inputs.word, e);
		}
		
		Alignment[] sentencePairs = getSentencePairs(inputs.word);
		results.sentencePairs = sentencePairs;
		
		
		return results;
	}

	private Alignment[] getSentencePairs(String word) {
		Alignment[] sentencePairs = new Alignment[] {};
		ProcessQuery processQuery;
		try {
			processQuery = new ProcessQuery();
			String query = word;
			String[] alignments = processQuery.run(query);
			sentencePairs = new Alignment[alignments.length];
			for (int ial=0; ial<alignments.length; ial++) {
				String alignment = alignments[ial];
				String[] alignmentParts = alignment.split(":: ");
				String[] sentences = alignmentParts[1].split("@----@");
				String inuktitutSentence = 
						sentences[0].replace(word,"<span class='highlighted'>"+word+"</span>")
						.replace("/\\.{5,}/","...");
				String englishSentence = sentences[1].replace("/\\.{5,}/","...");
				Alignment sentencePair = new Alignment("in",inuktitutSentence,"en",englishSentence);
				sentencePairs[ial] = sentencePair;
			}
		} catch (ConfigException | IOException e) {
		}
		
		return sentencePairs;
	}

	private void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.GistEndpoint.writeJsonResponse");
		tLogger.debug("json="+json);
		PrintWriter writer = response.getWriter();		
		writer.write(json);
		writer.close();
	}
}
