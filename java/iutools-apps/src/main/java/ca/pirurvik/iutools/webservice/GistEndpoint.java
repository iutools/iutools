package ca.pirurvik.iutools.webservice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.TimeoutException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphInukException;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.Pair;
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.spellchecker.SpellChecker;
import ca.pirurvik.iutools.spellchecker.SpellCheckerException;
import ca.pirurvik.iutools.spellchecker.SpellingCorrection;

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
			
			// Benoit, l'appel ci-dessous à writeValueAsString() semble causer
			// une récursion infinie. Alors pour le moment, je ré-initialise 
			// results.decomps à un array vide.
			//
			results.decomps = new Decomposition[0];
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
		}
		
		writeJsonResponse(response, jsonResponse);
	}
	
	private GistResponse processInputs(GistInputs inputs) throws GistEndpointException {
		GistResponse results = new GistResponse();
		
		try {
			results.decomps = analyzer.decomposeWord(inputs.word);
		} catch (TimeoutException | MorphInukException e) {
			throw new GistEndpointException(
					"Error trying to decompose word "+inputs.word, e);
		}
		
		// Benoit: c'est ici que tu devrais aller chercher les
		//  paires de phrases dans le hansard.
		//
		// Pour le moment, je retourne juste une array vide.
		results.sentencePairs = new Pair[0];
		
		
		return results;
	}

	private void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.GistEndpoint.writeJsonResponse");
		tLogger.debug("json="+json);
		PrintWriter writer = response.getWriter();		
		writer.write(json);
		writer.close();
	}
}
