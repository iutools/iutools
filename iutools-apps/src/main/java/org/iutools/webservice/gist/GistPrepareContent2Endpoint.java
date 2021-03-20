package org.iutools.webservice.gist;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.datastructure.Pair;

import org.apache.log4j.Logger;

import org.iutools.concordancer.*;
import org.iutools.script.TransCoder;
import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.text.segmentation.Segmenter;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;
import org.json.JSONObject;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class GistPrepareContent2Endpoint
	extends Endpoint<GistPrepareContent2Inputs, GistPrepareContent2Result> {

	IUTokenizer tokenizer = new IUTokenizer();

	@Override
	protected GistPrepareContent2Inputs requestInputs(HttpServletRequest request)
		throws ServiceException {
		return jsonInputs(request, GistPrepareContent2Inputs.class);
	}

	@Override
	public EndpointResult execute(GistPrepareContent2Inputs inputs) throws ServiceException {

		Logger tLogger = Logger.getLogger("org.iutools.webservice.gist.GistPrepareContentEndpoint.execute");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("invoked with inputs="+ PrettyPrinter.print(inputs));
		}
		GistPrepareContent2Result result = new GistPrepareContent2Result();

		if (inputs.isURL()) {
			doPrepareURL(inputs, result);
		} else {
			doPrepareActualText(inputs, result);
		}

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("returning response="+PrettyPrinter.print(result));
		}

		return result;
	}

	private void doPrepareActualText(GistPrepareContent2Inputs inputs,
		GistPrepareContent2Result result) {

		String text = inputs.textOrUrl;
		text = TransCoder.ensureRoman(text);
		Segmenter segmenter = Segmenter.makeSegmenter("iu");
		List<String[]> sentences = segmenter.segmentTokenized(text);
		result.iuSentences = sentences;
	}

	private void doPrepareURL(GistPrepareContent2Inputs inputs,
		GistPrepareContent2Result response) throws ServiceException {

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

	private void addAlignment(Alignment anAlignment,
  		GistPrepareContent2Result result) {

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
			result.iuSentences.add(iuTokens);
		}

		{
			String enText = anAlignment.getText("en");
			tokenizer.tokenize(enText);
			List<Pair<String,Boolean>> enTokensLst = tokenizer.getAllTokens();
			String[] enTokens = new String[enTokensLst.size()];
			for (int ii=0; ii < enTokens.length; ii++) {
				enTokens[ii] = enTokensLst.get(ii).getFirst();
			}
			result.enSentences.add(enTokens);
		}
	}

	private void writeJsonResponse(HttpServletResponse response, String json)
		throws IOException {

		Logger tLogger = Logger.getLogger("org.iutools.webservice.writeJsonResponse");

		tLogger.debug("json="+json);
		PrintWriter writer = response.getWriter();

		writer.write(json);
		writer.close();
	}
}
