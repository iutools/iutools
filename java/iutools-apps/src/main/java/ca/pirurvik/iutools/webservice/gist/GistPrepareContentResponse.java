package ca.pirurvik.iutools.webservice.gist;

import java.util.ArrayList;
import java.util.List;

import ca.nrc.datastructure.Pair;
import ca.nrc.string.SimpleTokenizer;
import ca.pirurvik.iutools.concordancer.Alignment;
import ca.pirurvik.iutools.concordancer.DocAlignment;
import ca.pirurvik.iutools.concordancer.DocAlignment.Problem;
import ca.pirurvik.iutools.text.segmentation.IUTokenizer;
import ca.pirurvik.iutools.webservice.ServiceResponse;

public class GistPrepareContentResponse extends ServiceResponse {

	public List<String[]> iuSentences = new ArrayList<String[]>();
	public List<String[]> enSentences = null;
	public boolean wasActualText = true;

	public GistPrepareContentResponse() {
		
	}

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

	public void fillFromDocAlignment(DocAlignment docAlignment) {
		if (docAlignment.encounteredProblem(Problem.ALIGNING_SENTENCES) ||
				docAlignment.encounteredProblem(Problem.FETCHING_CONTENT_OF_OTHER_LANG_PAGE)) {
			fillFromUnsuccessfulAlignment(docAlignment);
		} else {
			fillFromSuccessfulAlignment(docAlignment);
		}
		
		return;
	}

	/** 
	 * Use this to fill the response when the DocAlignment was not completely
	 * successful (ex: was not able to fetch the page in one of the two languages 
	 * to be aligned).
	 * 
	 * @param docAlignment
	 */
	private void fillFromUnsuccessfulAlignment(DocAlignment docAlignment) {
		if (docAlignment != null) {
			iuSentences = new ArrayList<String[]>();
			List<String> alignmentSents = docAlignment.getPageSentences("iu");
			if (alignmentSents != null) {
				IUTokenizer iuTokenizer = new IUTokenizer();
				for (String sent: alignmentSents) {
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
		for (Alignment anAlignment: docAlignment.getAligments()) {
			iuTokenizer.tokenize(anAlignment.getText("iu"));
			List<String> iuWords = iuTokenizer.wordsAndAll();
			iuSentences.add(iuWords.toArray(new String[iuWords.size()]));
			
			String[] enWords = enTokenizer.tokenize(anAlignment.getText("en"), true);
			enSentences.add(enWords);
		}
		
	}
	
	
}
