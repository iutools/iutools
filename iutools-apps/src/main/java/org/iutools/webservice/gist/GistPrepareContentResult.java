package org.iutools.webservice.gist;

import ca.nrc.string.SimpleTokenizer;
import org.iutools.concordancer.DocAlignment;
import org.iutools.concordancer.SentencePair;
import org.iutools.script.TransCoder;
import org.iutools.script.TransCoderException;
import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.webservice.EndpointResult;
import org.iutools.webservice.ServiceException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GistPrepareContentResult extends EndpointResult {

	public List<String[]> iuSentences = new ArrayList<String[]>();
	public List<String[]> enSentences = null;
	public boolean wasActualText = true;

	public GistPrepareContentResult() {}

	public boolean getAlignmentsAvailable() {
		boolean available =
			(iuSentences != null && !iuSentences.isEmpty() &&
				enSentences != null && !enSentences.isEmpty());

		return available;
	}


	public void setAlignmentsAvailable(boolean available) {
		// Do nothing. This method is there just so the Jackson serializer
		// does not raise an UnrecognizedPropertyException
	}

	public void fillFromDocAlignment(DocAlignment docAlignment)
		throws ServiceException {

		if (docAlignment.encounteredProblem(DocAlignment.Problem.FETCHING_INPUT_URL)) {
			// We weren't even able to download the input page from its URL
			fillFromUnsuccessfulInputPage(docAlignment);
		} else {
			// We were able to download the input page. But were we able to fetch
			// and align its other language version?
			//
			if (docAlignment.encounteredProblem(DocAlignment.Problem.ALIGNING_SENTENCES) ||
			docAlignment.encounteredProblem(DocAlignment.Problem.FETCHING_CONTENT_OF_OTHER_LANG_PAGE)) {
				fillFromUnsuccessfulAlignment(docAlignment);
			} else {
				fillFromSuccessfulAlignment(docAlignment);
			}
		}
		return;
	}

	private void fillFromUnsuccessfulInputPage(DocAlignment docAlignment) throws ServiceException {
		fillFromUnsuccessfulAlignment(docAlignment);
		this.errorMessage = "Unable to download the input page";
	}

	/**
	 * Use this to fill the response when the DocAlignment was not completely
	 * successful (ex: was not able to fetch the page in one of the two languages
	 * to be aligned).
	 */
	private void fillFromUnsuccessfulAlignment(DocAlignment docAlignment) throws ServiceException {
		if (docAlignment != null) {
			iuSentences = new ArrayList<String[]>();
			List<String> alignmentSents = docAlignment.getPageSentences("iu");
			if (alignmentSents != null) {
				IUTokenizer iuTokenizer = new IUTokenizer();
				for (String sent: alignmentSents) {
					try {
						sent = TransCoder.ensureScript(TransCoder.Script.ROMAN, sent);
					} catch (TransCoderException e) {
						throw new ServiceException(e);
					}
					iuTokenizer.tokenize(sent);
					List<String> wordsLst = iuTokenizer.wordsAndAll();
					iuSentences
						.add(wordsLst.toArray(new String[wordsLst.size()]));
				}
			}

			enSentences = new ArrayList<String[]>();
			alignmentSents = docAlignment.getPageSentences("en");
			if (alignmentSents != null){
				SimpleTokenizer enTokenizer = new SimpleTokenizer();
				for (String sent: alignmentSents) {
					try {
						sent = TransCoder.ensureScript(TransCoder.Script.ROMAN, sent);
					} catch (TransCoderException e) {
						throw new ServiceException(e);
					}
					String[] words = enTokenizer.tokenize(sent, true);
					enSentences.add(words);
				}
			}
		}

		return;
	}

	/**
	 * Use this to fill the response when the DocAlignment was completely
	 * successful, ie., we were able to fetch the age in both languages and
	 * align them.
	 *
	 * @param docAlignment
	 */

	private void fillFromSuccessfulAlignment(DocAlignment docAlignment) {
		iuSentences = new ArrayList<String[]>();
		enSentences = new ArrayList<String[]>();
		IUTokenizer iuTokenizer = new IUTokenizer();
		SimpleTokenizer enTokenizer = new SimpleTokenizer();
		for (SentencePair anAlignment: docAlignment.getAligments()) {
			String iuSent = TransCoder.ensureRoman(anAlignment.getText("iu"));
			iuTokenizer.tokenize(iuSent);
			List<String> iuWords = iuTokenizer.wordsAndAll();
			iuSentences.add(iuWords.toArray(new String[iuWords.size()]));

			String enSent = TransCoder.ensureRoman(anAlignment.getText("en"));
			String[] enWords = enTokenizer.tokenize(enSent, true);
			enSentences.add(enWords);
		}

	}
}
