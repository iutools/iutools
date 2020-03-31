package ca.pirurvik.iutools.webservice.gist;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.inuktitutcomputing.script.TransCoder;
import ca.nrc.datastructure.Pair;
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.text.segmentation.IUTokenizer;
import ca.pirurvik.iutools.text.segmentation.Segmenter;
import ca.pirurvik.iutools.webservice.EndPointHelper;
import ca.pirurvik.iutools.webservice.ServiceResponse;
import ca.pirurvik.iutools.webservice.tokenize.GistPrepareContentInputs;
import ca.pirurvik.iutools.webservice.tokenize.TokenizeResponse;

/**
 * Endpoint used by the Gist application to prepare some text for 
 * gisting.
 *
 * If the text is a URL:
 * - fetch that page and its English equivalent and align the two
 * - tokenize the content
 * 
 * If the text is NOT a URL;
 * - just tokenize that text
 * 
 * @author desilets
 *
 */
public class GistPrepareContentEndpoint extends HttpServlet {
	public void doPost(HttpServletRequest request, 
			HttpServletResponse response) throws IOException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.GistPrepareContentEndpoint.doPost");
		
		tLogger.trace("invoked with request=\n"+request);
				
		String jsonResponse = null;

		EndPointHelper.setContenTypeAndEncoding(response);

		GistPrepareContentInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, GistPrepareContentInputs.class);
			tLogger.trace("inputs="+PrettyPrinter.print(inputs));
			ServiceResponse results = executeEndPoint(inputs);
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
		}
		
		writeJsonResponse(response, jsonResponse);
	}

	private ServiceResponse executeEndPoint(GistPrepareContentInputs inputs) {
		GistPrepareContentResponse response = new GistPrepareContentResponse();
		
		if (inputs.isURL()) {
			doPrepareURL(inputs, response);
		} else {
			doPrepareActualText(inputs, response);
		}
		
		return response;
	}
	
	private void doPrepareActualText(GistPrepareContentInputs inputs, 
			GistPrepareContentResponse response) {
		String text = inputs.textOrUrl;
		text = TransCoder.ensureRoman(text);
		Segmenter segmenter = Segmenter.makeSegmenter("iu");
		List<String[]> sentences = segmenter.segmentTokenized(text);
		response.iuSentences = sentences;
	}

	private void doPrepareURL(GistPrepareContentInputs inputs, GistPrepareContentResponse response) {
		// TODO Auto-generated method stub
		
	}

	private void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.webservice.writeJsonResponse");
		
		tLogger.debug("json="+json);
		PrintWriter writer = response.getWriter();
		
		writer.write(json);
		writer.close();
	}

}
