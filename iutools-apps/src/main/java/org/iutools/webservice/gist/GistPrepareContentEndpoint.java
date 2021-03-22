package org.iutools.webservice.gist;

import ca.nrc.json.PrettyPrinter;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.log4j.Logger;

import org.iutools.concordancer.*;
import org.iutools.script.TransCoder;
import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.text.segmentation.Segmenter;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

public class GistPrepareContentEndpoint
	extends Endpoint<GistPrepareContentInputs, GistPrepareContentResult> {

	IUTokenizer tokenizer = new IUTokenizer();

	@Override
	protected GistPrepareContentInputs requestInputs(HttpServletRequest request)
		throws ServiceException {
		return jsonInputs(request, GistPrepareContentInputs.class);
	}

	@Override
	public EndpointResult execute(GistPrepareContentInputs inputs) throws ServiceException {

		Logger tLogger = Logger.getLogger("org.iutools.webservice.gist.GistPrepareContentEndpoint.execute");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("invoked with inputs="+ PrettyPrinter.print(inputs));
		}
		GistPrepareContentResult result = new GistPrepareContentResult();

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

	private void doPrepareActualText(GistPrepareContentInputs inputs,
												GistPrepareContentResult result) {

		String text = inputs.textOrUrl;
		text = TransCoder.ensureRoman(text);
		Segmenter segmenter = Segmenter.makeSegmenter("iu");
		List<String[]> sentences = segmenter.segmentTokenized(text);
		result.iuSentences = sentences;
	}

	private void doPrepareURL(GistPrepareContentInputs inputs,
									  GistPrepareContentResult response) throws ServiceException {

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
  		GistPrepareContentResult result) {

		Logger tLogger = Logger.getLogger("org.iutools.webservice.GistPrepareContentEndpoint.addAlignment");

		IUTokenizer tokenizer = new IUTokenizer();
		{
			String iuText = anAlignment.getText("iu");
			tokenizer.tokenize(iuText);
			List<Pair<String,Boolean>> iuTokensLst = tokenizer.getAllTokens();
			String[] iuTokens = new String[iuTokensLst.size()];
			for (int ii=0; ii < iuTokens.length; ii++) {
				String origToken = iuTokensLst.get(ii).getLeft();
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
				enTokens[ii] = enTokensLst.get(ii).getLeft();
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
