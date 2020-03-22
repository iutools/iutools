package ca.pirurvik.iutools.webservice;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
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
import ca.pirurvik.iutools.concordancer.AlignmentResult;
import ca.pirurvik.iutools.concordancer.WebConcordancer;
import ca.pirurvik.iutools.concordancer.WebConcordancerException;

public class GistTextEndpoint extends HttpServlet {

	EndPointHelper helper = null;
	
    static MorphologicalAnalyzer analyzer = null;  
	WebConcordancer webConcordancer = null;

	public GistTextEndpoint() throws GistTextEndpointException  {
		init_GistTextEndpoint();
	};
	
	protected void init_GistTextEndpoint() throws GistTextEndpointException  {
		if (analyzer == null) {
	    	try {
				analyzer = new MorphologicalAnalyzer();
			} catch (LinguisticDataException e) {
				throw new GistTextEndpointException(
						"Could not create the morphological analyzer", e);
			}
		}
		
		webConcordancer = new WebConcordancer();
	}
	
	protected void doPost(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		String jsonResponse = null;

		GistTextInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, GistTextInputs.class);
			GistTextResponse results = processInputs(inputs);			
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
		}
		
		writeJsonResponse(response, jsonResponse);
	}
	
	private GistTextResponse processInputs(GistTextInputs inputs) throws GistTextEndpointException {
		Logger logger = Logger.getLogger("ca.pirurvik.iutools.webservice.GistEndpoint.processInputs");
		GistTextResponse results = new GistTextResponse();
		
		String textToGist = inputs.word;
		AlignmentResult alignResult = null;
		
		URL url = inputs.inputURL();
		if (url != null) {
			// The input "text" was in fact a URL.
			// Fetch the "main" textual content of that page, and see if you 
			// can find a corresponding page in 'en' (if so, produce alignments
			// for the two pages).
			//
			try {
				alignResult = webConcordancer.alignPage(url, new String[] {"iu", "en"});
				textToGist = alignResult.getPageContent("iu"); 
			} catch (WebConcordancerException e) {
				throw new GistTextEndpointException(
						"Could not fetch and align text for page "+url, e);
			}
		}
		
		try {
			Decomposition[] decompositions = analyzer.decomposeWord(textToGist,false);
			logger.debug("Nb. decompositions for "+textToGist+": "+decompositions.length);
			DecompositionExpression[] decompositionExpressions = new DecompositionExpression[decompositions.length];
			for (int idec=0; idec<decompositions.length; idec++) {
				DecompositionExpression decExp = new DecompositionExpression(decompositions[idec].toStr2());
				decExp.getMeanings("en");
				decompositionExpressions[idec] = decExp;
			}
			logger.debug("Nb. decompositionExpression: "+decompositionExpressions.length);
			results.decompositions = decompositionExpressions;
		} catch (TimeoutException | MorphInukException e) {
			throw new GistTextEndpointException(
					"Error trying to decompose word "+textToGist, e);
		}
		
		Alignment[] sentencePairs = getSentencePairs(textToGist);
		results.sentencePairs = sentencePairs;
		
		
		return results;
	}

	private Alignment[] getSentencePairs(String word) {
		Alignment[] sentencePairs = new Alignment[] {};
		ProcessQuery processQuery;
		try {
			processQuery = new ProcessQuery();
			String query = word;
			String[] linesOfAlignedSentencesWithWord = processQuery.run(query);
			sentencePairs = new Alignment[linesOfAlignedSentencesWithWord.length];
			for (int ial=0; ial<linesOfAlignedSentencesWithWord.length; ial++) {
				String alignmentString = linesOfAlignedSentencesWithWord[ial];
				Alignment sentencePair = computeSentencePair(alignmentString);
				sentencePair.set("iu", sentencePair.get("iu").replace(word,"<span class='highlighted'>"+word+"</span>"));
				sentencePairs[ial] = sentencePair;
			}
		} catch (ConfigException | IOException e) {
		}
		
		return sentencePairs;
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

	private void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.GistEndpoint.writeJsonResponse");
		tLogger.debug("json="+json);
		PrintWriter writer = response.getWriter();		
		writer.write(json);
		writer.close();
	}
}
