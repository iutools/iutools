package ca.pirurvik.iutools.webservice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.spellchecker.SpellChecker;
import ca.pirurvik.iutools.spellchecker.SpellCheckerException;
import ca.pirurvik.iutools.spellchecker.SpellingCorrection;


public class SpellEndpoint extends HttpServlet {
	
	EndPointHelper helper = null;
	
    static SpellChecker checker = null;    
    

	public SpellEndpoint() throws SpellCheckerException, FileNotFoundException, ConfigException {
		initialize();
	};
	
	
	protected void initialize() throws SpellCheckerException, FileNotFoundException, ConfigException {
		ensureCheckerIsInstantiated();
	}
	
	private synchronized void ensureCheckerIsInstantiated() throws SpellCheckerException, FileNotFoundException, ConfigException {
		if (checker == null) {
			try {
				checker = new SpellChecker().enablePartialCorrections();
			} catch (StringSegmenterException e) {
				throw new SpellCheckerException(e);
			}
			// Spell Checker service uses Hansard corpus
			try {
				checker.setDictionaryFromCorpus();
			} catch (FileNotFoundException | ConfigException e) {
				throw new SpellCheckerException("Could not load corpus dictionary", e);
			} 
		}
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
		EndPointHelper.log4jReload();
		
		String jsonResponse = null;

		SpellInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, SpellInputs.class);
			SpellResponse results = executeEndPoint(inputs);
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
		}
		
		writeJsonResponse(response, jsonResponse);
	}
	
	private void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.SpellEndpoint.writeJsonResponse");
		tLogger.debug("json="+json);
		PrintWriter writer = response.getWriter();		
		writer.write(json);
		writer.close();
	}

	public SpellResponse executeEndPoint(SpellInputs inputs) throws ServiceException, SpellCheckerException  {
		Logger tLogger = Logger.getLogger("SpellEndpoint.executeEndPoint");
		tLogger.trace("ngramStats['lauqs']= "+checker.ngramStats.get("lauqs"));
		SpellResponse response = new SpellResponse();
		
		if (inputs.text == null || inputs.text.isEmpty()) {
			throw new ServiceException("Query was empty or null");
		}
		
		checker.setPartialCorrectionEnabled(inputs.includePartiallyCorrect);
		List<SpellingCorrection> corrections = checker.correctText(inputs.text);
		
		tLogger.trace("inputs.text= "+inputs.text);
		tLogger.trace("corrections= "+PrettyPrinter.print(corrections));
		
		response.correction = corrections;

		return response;
	}

}
