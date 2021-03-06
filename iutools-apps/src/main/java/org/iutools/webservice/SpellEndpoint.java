package org.iutools.webservice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.iutools.corpus.CompiledCorpusRegistry;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.nrc.config.ConfigException;
import ca.nrc.json.PrettyPrinter;
import org.iutools.spellchecker.SpellChecker;
import org.iutools.spellchecker.SpellCheckerException;
import org.iutools.spellchecker.SpellingCorrection;


public class SpellEndpoint extends HttpServlet {
	
	EndPointHelper helper = null;

	SpellChecker checker = null;
    

	public SpellEndpoint() throws SpellCheckerException, FileNotFoundException, ConfigException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.SpellEndpoint.new");
		tLogger.trace("invoked");
		initialize();
		tLogger.trace("exiting");
	};
	
	
	protected void initialize() throws SpellCheckerException, FileNotFoundException, ConfigException {
		ensureCheckerIsInstantiated();
	}
	
	private synchronized void ensureCheckerIsInstantiated() throws SpellCheckerException, FileNotFoundException, ConfigException {
		if (checker == null) {
			try {
			checker =
					new SpellChecker(CompiledCorpusRegistry.defaultCorpusName)
					.enablePartialCorrections();
			} catch (Exception e) {
				throw new SpellCheckerException(e);
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
			jsonResponse =
				EndPointHelper.emitServiceExceptionResponse(
					"General exception was raised\n",
					exc, inputs);
		}
		
		writeJsonResponse(response, jsonResponse);
	}
	
	private void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.SpellEndpoint.writeJsonResponse");
		tLogger.debug("json="+json);
		PrintWriter writer = response.getWriter();		
		writer.write(json);
		writer.close();
	}

	public SpellResponse executeEndPoint(SpellInputs inputs) throws ServiceException, SpellCheckerException  {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.SpellEndpoint.executeEndPoint");

		tLogger.trace("inputs.text= "+inputs.text);
		tLogger.trace("Spell checker has base ES index name = \n"+checker.corpusIndexName());

		SpellResponse response = new SpellResponse();
		
		if (inputs.text == null || inputs.text.isEmpty()) {
			throw new ServiceException("Query was empty or null");
		}
		
		checker.setPartialCorrectionEnabled(inputs.includePartiallyCorrect);
		List<SpellingCorrection> corrections = checker.correctText(inputs.text);
		
		tLogger.trace("corrections= "+PrettyPrinter.print(corrections));
		
		response.correction = corrections;

		tLogger.trace("Returning");

		return response;
	}

}
