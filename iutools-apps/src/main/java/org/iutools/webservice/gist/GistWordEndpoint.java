package org.iutools.webservice.gist;

import ca.nrc.json.PrettyPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.concordancer.SentencePair;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.concordancer.tm.TranslationMemoryException;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.morph.Gist;
import org.iutools.webservice.Endpoint;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class GistWordEndpoint extends Endpoint<GistWordInputs, GistWordResult> {


	public int maxAlignments = 100;


	@Override
	protected GistWordInputs requestInputs(String jsonRequestBody) throws ServiceException {
		return jsonInputs(jsonRequestBody, GistWordInputs.class);
	}

	@Override
	public EndpointResult execute(GistWordInputs inputs) throws ServiceException {
		Logger tLogger = LogManager.getLogger("org.iutools.webservice.GistWordEndpoint.execute");

		GistWordResult response = new GistWordResult(inputs.word);

		// Get the decomposition of the word into morphemes with their
		// respective meanings
		//
		Gist gistOfWord = null;

		try {
			gistOfWord = new Gist(inputs.word);
			tLogger.trace("gist= " + PrettyPrinter.print(gistOfWord));


			response.wordGist = gistOfWord;
			response.alignments = alignments4Word(inputs.getWordRomanized());
		} catch (LinguisticDataException e) {
			throw new ServiceException(e);
		}

		return response;
	}

	/**
	 * Retrieve aligned sentences that contain an inuktitut word
	 *
	 * @param word
	 * @return
	 */
	private SentencePair[] alignments4Word(String word)
		throws ServiceException {
		Logger tLogger = LogManager.getLogger("org.iutools.webservice.gist.GistWordEndpoint.alignments4Word__TM");
		SentencePair[] aligns = new SentencePair[0];
		try {
			word = TransCoder.ensureScript(TransCoder.Script.SYLLABIC, word);

			TranslationMemory tm = new TranslationMemory();
			List<Alignment_ES> alignmentResults = tm.search("iu", word);

			int numToKeep = Math.min(maxAlignments, alignmentResults.size());
			aligns = new SentencePair[numToKeep];
			for (int ii=0; ii < numToKeep; ii++) {
				Alignment_ES algES = alignmentResults.get(ii);
				aligns[ii] = esResult2alignment(algES);
			}
		} catch (TranslationMemoryException | TransCoderException e) {
			throw new ServiceException(e);
		}
		tLogger.trace("aligns= " + PrettyPrinter.print(aligns));

		return aligns;
	}

	private SentencePair esResult2alignment(Alignment_ES esAlignment) {

		SentencePair alignment = new SentencePair(
			"en",
			esAlignment.sentence4lang("en"),
			"iu",
			TransCoder.ensureRoman(esAlignment.sentence4lang("iu")));

		return alignment;
	}
}
