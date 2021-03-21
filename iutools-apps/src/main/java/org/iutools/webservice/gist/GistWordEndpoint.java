package org.iutools.webservice.gist;

import ca.nrc.config.ConfigException;
import ca.nrc.json.PrettyPrinter;
import org.apache.log4j.Logger;
import org.iutools.concordancer.nunhansearch.ProcessQuery;
import org.iutools.utilities.Alignment;
import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.morph.Gist;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

public class GistWordEndpoint
	extends Endpoint<GistWordInputs, GistWordResult> {
	@Override
	protected GistWordInputs requestInputs(HttpServletRequest request) throws ServiceException {
		return jsonInputs(request, GistWordInputs.class);
	}

	@Override
	public EndpointResult execute(GistWordInputs inputs) throws ServiceException {
		Logger tLogger = Logger.getLogger("org.iutools.webservice.GistWordEndpoint.execute");

		GistWordResult response = new GistWordResult(inputs.word);

		// Get the decomposition of the word into morphemes with their
		// respective meanings
		//
		Gist gistOfWord = null;

		try {
			gistOfWord = new Gist(inputs.word);
			tLogger.trace("gist= " + PrettyPrinter.print(gistOfWord));

			// Retrieve aligned sentences that contain the word
			//
			ProcessQuery processQuery = new ProcessQuery();
			String query = inputs.getWordRomanized();
			tLogger.trace("query= " + query);
			tLogger.trace("calling run() on processQuery=" + processQuery);
			String[] alignments = processQuery.run(query);
			Alignment[] aligns = new Alignment[alignments.length];
			for (int ial=0; ial<alignments.length; ial++)
				aligns[ial] = computeSentencePair(alignments[ial]);
			tLogger.trace("alignments= " + PrettyPrinter.print(alignments));


			response.wordGist = gistOfWord;
			response.alignments = aligns;
		} catch (LinguisticDataException | ConfigException | IOException e) {
			throw new ServiceException(e);
		}

		return response;
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
}
