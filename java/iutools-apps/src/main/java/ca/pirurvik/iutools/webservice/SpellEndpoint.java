package ca.pirurvik.iutools.webservice;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.FileWriter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
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
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.CompiledCorpus;
import ca.pirurvik.iutools.CompiledCorpusRegistry;
import ca.pirurvik.iutools.CompiledCorpusRegistryException;
import ca.pirurvik.iutools.QueryExpanderException;
import ca.pirurvik.iutools.QueryExpander;
import ca.pirurvik.iutools.QueryExpansion;
import ca.pirurvik.iutools.SpellChecker;
import ca.pirurvik.iutools.SpellCheckerException;
import ca.pirurvik.iutools.SpellingCorrection;
import ca.pirurvik.iutools.search.BingSearchMultithrd;
import ca.pirurvik.iutools.search.SearchHit;


public class SpellEndpoint extends HttpServlet {
//	private String endPointName = null;
//	private String esDefaultIndex = "dedupster";
	EndPointHelper helper = null;
	
    static SpellChecker checker = null;    
    

	public SpellEndpoint() throws SpellCheckerException {
		initialize();
	};
	
	
	protected void initialize() throws SpellCheckerException {
		ensureCheckerIsInstantiated();
	}
	
	private synchronized void ensureCheckerIsInstantiated() throws SpellCheckerException {
		if (checker == null) {
			checker = new SpellChecker();
			checker.setDictionaryFromCorpus(); // Spell Checker service uses Hansard corpus
		}
		
	}
	
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {		
		String jsonResponse = null;

		EndPointHelper.setContenTypeAndEncoding(response);

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
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.writeJsonResponse");
		tLogger.debug("json="+json);
		PrintWriter writer;
		writer = response.getWriter();
		
		writer.write(json);
		writer.close();
	}

	public SpellResponse executeEndPoint(SpellInputs inputs) throws ServiceException, SpellCheckerException  {
		Logger tLogger = Logger.getLogger("SpellEndpoint.executeEndPoint");
		tLogger.trace("idfStats['lauqs']= "+checker.idfStats.get("lauqs"));
		SpellResponse response = new SpellResponse();
		
		if (inputs.text == null || inputs.text.isEmpty()) {
			throw new ServiceException("Query was empty or null");
		}
		
		
		List<SpellingCorrection> corrections = checker.correctText(inputs.text);
		
		tLogger.trace("inputs.text= "+inputs.text);
		tLogger.trace("corrections= "+PrettyPrinter.print(corrections));
		
		response.correction = corrections;

		return response;
	}

}
