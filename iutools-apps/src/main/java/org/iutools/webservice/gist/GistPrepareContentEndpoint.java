package org.iutools.webservice.gist;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.iutools.concordancer.*;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.iutools.script.TransCoder;
import ca.nrc.datastructure.Pair;
import ca.nrc.json.PrettyPrinter;
import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.text.segmentation.Segmenter;
import org.iutools.webservice.EndPointHelper;
import org.iutools.webservice.ServiceException;
import org.iutools.webservice.ServiceResponse;
import org.iutools.webservice.tokenize.GistPrepareContentInputs;


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
		EndPointHelper.log4jReload();
		Logger tLogger = Logger.getLogger("org.iutools.webservice.GistPrepareContentEndpoint.doPost");
		
		tLogger.trace("invoked with request=\n"+request);
				
		String jsonResponse = null;

		EndPointHelper.setContenTypeAndEncoding(response);

		org.iutools.webservice.tokenize.GistPrepareContentInputs inputs = null;
		try {
			EndPointHelper.setContenTypeAndEncoding(response);
			inputs = EndPointHelper.jsonInputs(request, org.iutools.webservice.tokenize.GistPrepareContentInputs.class);
			tLogger.trace("inputs="+PrettyPrinter.print(inputs));
			ServiceResponse results = executeEndPoint(inputs);
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("endpoint execution yielded results=\n"+
						PrettyPrinter.print(results));
			}
			
			jsonResponse = new ObjectMapper().writeValueAsString(results);
		} catch (Exception exc) {
			jsonResponse = EndPointHelper.emitServiceExceptionResponse("General exception was raised\n", exc);
		}
		
		writeJsonResponse(response, jsonResponse);		
	}

	private ServiceResponse executeEndPoint(org.iutools.webservice.tokenize.GistPrepareContentInputs inputs)
			throws ServiceException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.gist.GistPrepareContentEndpoint.executeEndPoint");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("invoked with inputs="+PrettyPrinter.print(inputs));
		}
		GistPrepareContentResponse response = new GistPrepareContentResponse();
		
		if (inputs.isURL()) {
			doPrepareURL(inputs, response);
		} else {
			doPrepareActualText(inputs, response);
		}

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("returning response="+PrettyPrinter.print(response));
		}

		return response;
	}
	
	private void doPrepareActualText(org.iutools.webservice.tokenize.GistPrepareContentInputs inputs,
												GistPrepareContentResponse response) {
		String text = inputs.textOrUrl;
		text = TransCoder.ensureRoman(text);
		Segmenter segmenter = Segmenter.makeSegmenter("iu");
		List<String[]> sentences = segmenter.segmentTokenized(text);
		response.iuSentences = sentences;
	}

	private void doPrepareURL(GistPrepareContentInputs inputs,
									  GistPrepareContentResponse response) throws ServiceException {
		

		response.wasActualText = false;
		WebConcordancer concordancer =
			new WebConcordancer_HtmlCleaner(
				WebConcordancer.AlignOptions.ALL_TEXT,
				WebConcordancer.AlignOptions.ALIGNED_SENTENCES);
		URL url;
		try {
			url = new URL(inputs.textOrUrl);
			DocAlignment alignments =
				concordancer.alignPage(url, new String[] {"en", "iu"});
			response.fillFromDocAlignment(alignments);
		} catch (MalformedURLException | WebConcordancerException e) {
			throw new ServiceException(e);
		}
	}

	private void addAlignment(Alignment anAlignment, GistPrepareContentResponse response) {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.GistPrepareContentEndpoint.addAlignment");

		IUTokenizer tokenizer = new IUTokenizer();
		{
			String iuText = anAlignment.getText("iu");
			tokenizer.tokenize(iuText);
			List<Pair<String,Boolean>> iuTokensLst = tokenizer.getAllTokens();
			String[] iuTokens = new String[iuTokensLst.size()];
			for (int ii=0; ii < iuTokens.length; ii++) {
				String origToken = iuTokensLst.get(ii).getFirst();
				iuTokens[ii] = TransCoder.ensureRoman(origToken);
			}
			response.iuSentences.add(iuTokens);
		}

		{		
			String enText = anAlignment.getText("en");
			tokenizer.tokenize(enText);
			List<Pair<String,Boolean>> enTokensLst = tokenizer.getAllTokens();
			String[] enTokens = new String[enTokensLst.size()];
			for (int ii=0; ii < enTokens.length; ii++) {
				enTokens[ii] = enTokensLst.get(ii).getFirst();
			}
			response.enSentences.add(enTokens);
		}
	}

	private void writeJsonResponse(HttpServletResponse response, String json) throws IOException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.writeJsonResponse");
		
		tLogger.debug("json="+json);
		PrintWriter writer = response.getWriter();
		
		writer.write(json);
		writer.close();
	}

}
