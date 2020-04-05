package ca.pirurvik.iutools.concordancer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ca.pirurvik.iutools.concordancer.DocAlignment.Problem;
import ca.pirurvik.iutools.text.segmentation.IUTokenizer;
import ca.nrc.data.harvesting.LanguageGuesser;
import ca.nrc.data.harvesting.LanguageGuesserException;
import ca.nrc.data.harvesting.PageHarvester;
import ca.nrc.data.harvesting.PageHarvester_HtmlCleaner;
import ca.nrc.datastructure.Pair;
import ca.nrc.string.StringUtils;
import ca.pirurvik.iutools.text.segmentation.Segmenter;
import ca.nrc.data.harvesting.PageHarvesterException;

/**
 * Use this class to fetch aligned sentences from a multilingual web page. 
 * 
 * @author desilets
 *
 */
public class WebConcordancer {
	
	protected static enum StepOutcome {SUCCESS, FAILURE, KEEP_TRYING};
	
	private static enum AlignmentPart {
		PAGES_CONTENT, PROBLEMS};
	
	PageHarvester_HtmlCleaner harvester = null;
	LanguageGuesser langGuesser = new LanguageGuesser_IU();
	Aligner_Maligna aligner = new Aligner_Maligna();
	
	protected PageHarvester_HtmlCleaner getHarvester() {
		if (harvester == null) {
			harvester = new PageHarvester_HtmlCleaner();
			harvester.setHarvestFullText(true);
		}
		return harvester;
	}

	public DocAlignment alignPage(URL url, String[] languages) {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.concordancer.alignPage");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("invoked with url="+url+
			", languages="+String.join(", ", languages));
		}

		DocAlignment alignment = new DocAlignment();
		for (String lang: languages) {
			alignment.setPageContent(lang, null);
		}
		
		harvestInputPage(url, alignment, languages);
		
		trace(tLogger, "After fetching input URL", alignment, 
				AlignmentPart.PAGES_CONTENT, AlignmentPart.PROBLEMS);
		
		if (!alignment.encounteredProblems()) {
			harvestOtherLangPage(alignment);
			if (tLogger.isTraceEnabled()) {
				String mess = "After fetching other lang URL, content of pages is:\n\n";
				for (String lang: languages) {
					mess += "   "+lang+": "+alignment.getPageContent(lang)+"\n\n";
				}
				tLogger.trace(mess);
			}
		}

		if (!alignment.encounteredProblems()) {
			alignContents(alignment);
			if (tLogger.isTraceEnabled()) {
				String mess = "After aligning content of the two pages, alignments are:";
				for (Alignment anAlignment: alignment.getAligments()) {
					mess += "   "+anAlignment+"\n";
				}
				tLogger.trace(mess);
			}
		}
		
		alignment.success = !alignment.encounteredProblems();
		
		return alignment;
	}
	
	private void trace(Logger tLogger, String mess, 
		DocAlignment alignment, AlignmentPart... alignmentParts ) {
		if (tLogger.isTraceEnabled()) {
			for (AlignmentPart part: alignmentParts) {
				mess = traceAlignmentPart(mess, alignment, part);
			}
			tLogger.trace(mess);
		}		
	}

	private String traceAlignmentPart(String mess, 
			DocAlignment alignment, AlignmentPart part) {
		if (part == AlignmentPart.PAGES_CONTENT) {
			mess = tracePagesContent(mess, alignment);
		} else if (part == AlignmentPart.PROBLEMS) {
			mess = traceProblems(mess, alignment);
		}
		
		return mess;
	}

	private String traceProblems(String mess, DocAlignment alignment) {
		mess += "\nEncoutered problems: ";
		if (!alignment.encounteredProblems()) {
			mess += "None\n";
		} else {
			Iterator<DocAlignment.Problem> probIter = 
				alignment.problemsEncountered.keySet().iterator();
			mess += "\n   "+StringUtils.join(probIter, "\n   ")+"\n";
		}
		
		return mess;
	}

	private String tracePagesContent(String mess, DocAlignment alignment) {
		mess += "\nContent of pages is:\n\n";
		for (String lang: alignment.getLanguages()) {
			mess += "   "+lang+": "+alignment.getPageContent(lang)+"\n\n";
		}

		return mess;
	}

	private void alignContents(DocAlignment docAlignment)  {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.concordancer.alignContents");
		
		if (docAlignment.hasContentForBothLanguages()) {
			List<List<String>> langSents = new ArrayList<List<String>>();
			List<String> langs = new ArrayList<String>();
			for (String lang: docAlignment.getLanguages()) {
				langs.add(lang);
				
				Segmenter segmenter = Segmenter.makeSegmenter(lang);
				if (tLogger.isTraceEnabled()) {
					tLogger.trace("segmenting content for lang="+lang+": "+docAlignment.getPageContent(lang));
				}
				
				List<String >sents = 
						segmentText(lang, docAlignment.getPageContent(lang));
				docAlignment.setPageSentences(lang, sents);
				langSents.add(sents);
			}
			
			List<Pair<String, String>> alignedPairs = null;
			try {
				alignedPairs = aligner.align(langSents.get(0), langSents.get(1));
			} catch (AlignerException e) {
				raiseProblem(Problem.ALIGNING_SENTENCES, docAlignment, e);
			}
			
			docAlignment.alignments = new ArrayList<Alignment>();
			for (Pair<String,String> aPair: alignedPairs) {
				Alignment anAlignment = 
					new Alignment(
							langs.get(0), aPair.getFirst(),
							langs.get(1), aPair.getSecond());
				docAlignment.addAlignment(anAlignment);
			}
		}
	}

	private List<String> segmentText(String lang, String text) {
		List<String> sentences = new ArrayList<String>();
		if (text != null) {
			Segmenter segmenter = Segmenter.makeSegmenter(lang);
			sentences = segmenter.segment(text);			
		}
		return sentences;
	}

	private DocAlignment mockAlignmentResult() {
		DocAlignment result = new DocAlignment();
		
		String[] enSentences = new String[] {
			"As of today, there are no known cases of COVID-19 in the territory.", 
			"The public health communicable disease team is following approximately 70 persons under investigation."				
		};
		String enText = String.join(" ", enSentences);

		String[] iuSentences = new String[] {
			"ᐅᓪᓗᒥᒧᑦ ᑎᑭᖦᖢᒍ, ᖃᐅᔨᒪᔭᐅᔪᓂᒃ ᓄᕙᒡᔪᐊᕐᓇᖅ-19−ᒧᑦ ᐱᑕᖃᙱᓚᖅ ᑕᒫᓂ ᐅᑭᐅᖅᑕᖅᑐᒥ ᐊᕕᒃᓯᒪᓂᕆᔭᐅᔪᒥ.",
			"ᑭᒃᑯᑐᐃᓐᓇᓂᒃ ᐋᓐᓂᐊᖃᖅᑕᐃᓕᑎᑦᑎᓂᕐᒧᑦ ᐊᐃᑦᑐᕐᓘᑕᐅᔪᓐᓇᖅᑐᓄᑦ ᖃᓂᒪᑖᕆᔭᐅᔪᓐᓇᖅᑐᓄᑦ ᐱᓕᕆᖃᑎᒌᑦ ᒪᓕᒃᓯᕗᖅ 50-60−ᐸᓗᖕᓂᒃ ᐃᓄᖕᓂᒃ ᑖᒃᑯᐊ ᖃᐅᔨᓴᖅᑕᐅᕙᓪᓕᐊᓪᓗᑎᒃ."
		};
		String iuText = String.join(" ", iuSentences);
		
		try {
			result.setPageURL("en", new URL("http://mocksite.nu.ca/en"));
			result.setPageURL("iu", new URL("http://mocksite.nu.ca/iu"));
		} catch (MalformedURLException e) {
		}
		
		result.setPageContent("en", enText);
		result.setPageContent("iu", iuText);
		
		result.addAlignment(new Alignment("en", enSentences[0], "iu", iuSentences[0]));
		result.addAlignment(new Alignment("en", enSentences[1], "iu", iuSentences[1]));
		
		return result;
	}

	private void harvestOtherLangPage(DocAlignment alignment) {
		if (alignment.encounteredProblems()) {
			return;
		}
		
		StepOutcome status = StepOutcome.KEEP_TRYING;
		
		if (!alignment.bothLangsContentFetched()) {
			Pair<String,String> langPair = langAndOtherLang(alignment);
			String lang = langPair.getFirst();
			String otherLang = langPair.getSecond();
			
			status = harvestOtherLangPage_KnownSites(alignment, lang, otherLang);
			if (status == StepOutcome.KEEP_TRYING) {
				status = harvestOtherLangPage_UnknownSites(alignment, 
							lang, otherLang);
			}	
			
			List<String> sentences = null;
			if (status == StepOutcome.SUCCESS) {
				sentences = 
					segmentText(otherLang, alignment.getPageContent(otherLang));
			}
			alignment.setPageSentences(otherLang, sentences);
		}
		
		return;
	}

	protected Pair<String, String> langAndOtherLang(
			DocAlignment alignment) {
		String filledLang = null;
		String unfilledLang = null;
		int filledCount = 0;
		
		for (String lang: alignment.getLanguages()) {
			if (alignment.getPageContent(lang) == null) {
				unfilledLang = lang;
			} else {
				filledLang = lang;
				filledCount++;
			}
		}
		
		Pair<String,String> langPair = Pair.of(null, null);
		if (filledCount == 1) {
			langPair = Pair.of(filledLang, unfilledLang);
		}
		
		return langPair;
	}

	/** 
	 * Harvest page in the other language, using strategies which are 
	 * specific to that page's web site.
	 * 
	 * @param alignment
	 * @param otherLang 
	 * @param lang 
	 * @return
	 */
	private StepOutcome harvestOtherLangPage_UnknownSites(
			DocAlignment alignment, String lang, String otherLang) {
		// TODO Auto-generated method stub
		return null;
	}

	/** 
	 * Harvest page in the other language, using strategies which are 
	 * NOT specific to that page's web site.
	 * 
	 * @param alignment
	 * @param otherLang 
	 * @param lang 
	 * @return
	 * @throws WebConcordancerException 
	 * @throws Exception 
	 */
	private StepOutcome harvestOtherLangPage_KnownSites(
			DocAlignment alignment, String lang, String otherLang) { 
		
		StepOutcome status = StepOutcome.KEEP_TRYING;
		
		// For now, hardcode rules for dealing with just the one site:
		//    
		//    gov.nu.ca
		//
		// 
		URL langURL = alignment.getPageURL(lang);
		String site = langURL.getHost();
		if (site.endsWith("gov.nu.ca")) {
			if (otherLang.equals("iu") || otherLang.equals("en")) {
				String langURLstr = langURL.toString();
				String otherLangURLstr = null;
				Pattern patt = Pattern.compile("(.*?)/(iu|in|fr|en)?(/?)");
				Matcher matcher = patt.matcher(langURLstr);
				if (matcher.matches()) {
					otherLangURLstr = 
						matcher.group(1)+"/"+otherLang+
						matcher.group(3);
				}
				
				if (otherLangURLstr != null) {
					// The English version of a page does not have /en at the 
					// end
					otherLangURLstr = otherLangURLstr.replaceAll("/en/?$", "");
					
					try {
						harvester.harvestSinglePage(new URL(otherLangURLstr));
					} catch (MalformedURLException | PageHarvesterException e) {
						raiseProblem(
							Problem.FETCHING_CONTENT_OF_OTHER_LANG_PAGE, 
							alignment, 
							"Could not fetch content of other langauge page: "+
									otherLangURLstr);
					}
					
					if (!alignment.encounteredProblems()) {
						String otherLangContent = harvester.getText();
								
						URL otherLangURL = harvester.getCurrentURL();
						alignment.setPageContent(otherLang, otherLangContent);
						alignment.setPageURL(otherLang, otherLangURL);
						status = StepOutcome.FAILURE;
					}
				}
			}
		}
		
		return status;
	}

	private void harvestInputPage(URL url, DocAlignment alignment, 
			String[] languages)  {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.concordancer.harvestInputPage");
		tLogger.trace("invoked with url="+url);
		String urlText = null;
		String urlLang = null;
		try {
			getHarvester().harvestSinglePage(url);
			urlText = getHarvester().getText();
			if (urlText == null) {
 				raiseProblem(Problem.FETCHING_INPUT_URL, alignment, 
					"Could not fetch content of url: "+url);
			} else {
				tLogger.trace("retrieved urlText=\n"+urlText);
				urlLang = guessLang(urlText);
				if (urlLang != null && 
						!urlLang.equals(languages[0]) && 
						!urlLang.equals(languages[1])) {
					alignment.raiseProblem(
						Problem.GUESSING_LANGUAGE_OF_INPUT_URL,
						"Langage of input page "+url+" was not as expected.\n"+
						"   Got : "+urlLang+"; expected: ["+
						String.join(",", languages)+"]");
				}
			}
				
		} catch (PageHarvesterException e) {
			raiseProblem(DocAlignment.Problem.FETCHING_INPUT_URL, alignment, e);
		} catch (LanguageGuesserException e) {
			alignment.raiseProblem(
					Problem.GUESSING_LANGUAGE_OF_INPUT_URL,
					"Langage of input page "+url+" was not as expected.\n"+
					"   Got : "+urlLang+"; expected: ["+
					String.join(",", languages)+"]");
		}
		
		if (!alignment.encounteredProblems()) {
			alignment.setPageContent(urlLang, urlText);
			alignment.setPageURL(urlLang, url);
			List<String> sentences = segmentText(urlLang, urlText);
			alignment.setPageSentences(urlLang, sentences);
		}
		
		return;
	}

	private void raiseProblem(DocAlignment.Problem descr, 
			DocAlignment alignment, 
			String mess) {
		alignment.raiseProblem(descr, new Exception(mess));
	}

	private void raiseProblem(DocAlignment.Problem descr, DocAlignment alignment, 
		Exception e) {
		alignment.raiseProblem(descr, e);
	}

	private String guessLang(String text) throws LanguageGuesserException {
		String lang = null;
		if (text != null) {
			lang = langGuesser.detect(text);
		}
		return lang;
	}
}
