package ca.pirurvik.iutools.concordancer;

import java.beans.Transient;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.nrc.data.harvesting.*;
import org.apache.log4j.Logger;

import ca.pirurvik.iutools.concordancer.DocAlignment.Problem;
import ca.nrc.datastructure.Pair;
import ca.nrc.string.StringUtils;
import ca.pirurvik.iutools.text.segmentation.Segmenter;

/**
 * Use this class to fetch aligned sentences from a multilingual web page. 
 * 
 * @author desilets
 *
 */
public abstract class WebConcordancer {

	protected static enum AlignOptions {ONLY_FETCH_CONTENT}
	protected static enum StepOutcome {SUCCESS, FAILURE, KEEP_TRYING};
	private static enum AlignmentPart {
		PAGES_CONTENT, PROBLEMS, SENTENCES, ALIGNMENTS};

	private static Map<String,String[]> langNames = new HashMap<String,String[]>();
	private static void setLangNames(String lang, String... names) {
		langNames.put(lang, names);
	}
	static {
		setLangNames("en", "english");
		setLangNames("fr", "français");
		setLangNames("iu", "ᐃᓄᒃᑎᑐᑦ", "ᐃᓄᒃᑐᑦ");
	}

	@Transient
	protected abstract PageHarvester getHarvester();


	PageHarvester harvester = null;
	LanguageGuesser langGuesser = new LanguageGuesser_IU();
	Aligner_Maligna aligner = new Aligner_Maligna();

	public DocAlignment alignPage(URL url, String[] languages)
		throws WebConcordancerException {
		return alignPage(url, languages, new AlignOptions[0]);
	}

	public DocAlignment alignPage(URL url, String[] languages,
		AlignOptions... options) throws WebConcordancerException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.concordancer.alignPage");

		Set<AlignOptions> optionsSet = new HashSet<AlignOptions>();
		Collections.addAll(optionsSet, options);

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
				AlignmentPart.PROBLEMS, AlignmentPart.SENTENCES);
		
		if (!alignment.encounteredSomeProblems()) {
			harvestOtherLangPage(alignment);
			trace(tLogger, "After fetching input URL", alignment, 
					AlignmentPart.PROBLEMS, AlignmentPart.SENTENCES);
		}

		if (!alignment.encounteredSomeProblems() &&
			!optionsSet.contains(AlignOptions.ONLY_FETCH_CONTENT)) {
			alignContents(alignment);
			trace(tLogger, "After fetching input URL", alignment, 
					AlignmentPart.PROBLEMS, AlignmentPart.SENTENCES,
					AlignmentPart.ALIGNMENTS);
			if (tLogger.isTraceEnabled()) {
				String mess = "After aligning content of the two pages, alignments are:";
				for (Alignment anAlignment: alignment.getAligments()) {
					mess += "   "+anAlignment+"\n";
				}
				tLogger.trace(mess);
			}
		}
		
		alignment.success = !alignment.encounteredSomeProblems();
		
		return alignment;
	}

	protected DocAlignment fetchParallelPages(DocAlignment alignment) throws WebConcordancerException {
		Logger tLogger = Logger.getLogger("ca.pirurvik.iutools.concordancer.fetchParallelPages");

		Pair<String,String> langs = langAndOtherLang(alignment);

		URL langURL = alignment.getPageURL(langs.getFirst());
		harvestInputPage(langURL, alignment, alignment.getLanguages());

		trace(tLogger, "After fetching input URL", alignment,
				AlignmentPart.PROBLEMS, AlignmentPart.SENTENCES);

		if (!alignment.encounteredSomeProblems()) {
			harvestOtherLangPage(alignment);
			trace(tLogger, "After fetching input URL", alignment,
				AlignmentPart.PROBLEMS, AlignmentPart.SENTENCES);
		}

		return alignment;
	}

	private void trace(Logger tLogger, String mess, 
		DocAlignment alignment, AlignmentPart... alignmentParts ) {
		if (tLogger.isTraceEnabled()) {
			for (AlignmentPart part: alignmentParts) {
				mess = traceAlignmentPart(tLogger, mess, alignment, part);
			}
			tLogger.trace(mess);
		}		
	}

	private String traceAlignmentPart(Logger tLogger, String mess, 
			DocAlignment alignment, AlignmentPart part) {
		if (tLogger.isTraceEnabled()) {
			if (part == AlignmentPart.PAGES_CONTENT) {
				mess = tracePagesContent(tLogger, mess, alignment);
			} else if (part == AlignmentPart.PROBLEMS) {
				mess = traceProblems(tLogger, mess, alignment);
			} else if (part == AlignmentPart.SENTENCES) {
				mess = traceSentences(tLogger, mess, alignment);
			} else if (part == AlignmentPart.ALIGNMENTS) {
				mess = traceAlignments(tLogger, mess, alignment);
			}
		}
		
		return mess;
	}


	private String traceAlignments(Logger tLogger, String mess, DocAlignment alignment) {
		if (tLogger.isTraceEnabled()) {
			mess += "Alignments are:\n";
			for (Alignment anAlignment: alignment.getAligments()) {
				mess += "   "+anAlignment+"\n";
			}
		}
		
		return mess;
	}

	private String traceProblems(Logger tLogger, 
		String mess, DocAlignment alignment) {
		if (tLogger.isTraceEnabled()) {
			mess += "\nEncoutered problems: ";
			if (!alignment.encounteredSomeProblems()) {
				mess += "None\n";
			} else {
				Iterator<DocAlignment.Problem> probIter = 
					alignment.problemsEncountered.keySet().iterator();
				mess += "\n   "+StringUtils.join(probIter, "\n   ")+"\n";
			}
		}		
		return mess;
	}

	private String tracePagesContent(Logger tLogger, 
		String mess, DocAlignment alignment) {
		if (tLogger.isTraceEnabled()) {
			mess += "\nContent of pages is:\n\n";
			for (String lang: alignment.getLanguages()) {
				mess += "   "+lang+": "+alignment.getPageContent(lang)+"\n\n";
			}
		}
		return mess;
	}
	
	private String traceSentences(Logger tLogger, 
			String mess, DocAlignment alignment) {
		if (tLogger.isTraceEnabled()) {
			mess += "\nSentences are:\n\n";
			for (String lang: alignment.getLanguages()) {
				mess += "   "+lang+": "+alignment.getPageSentences(lang)+"\n\n";
			}
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

	protected void harvestOtherLangPage(DocAlignment alignment)
		throws WebConcordancerException {
		if (alignment.encounteredSomeProblems()) {
			return;
		}
		
		StepOutcome status = StepOutcome.KEEP_TRYING;
		
		if (!alignment.bothLangsContentFetched()) {
			Pair<String,String> langPair = langAndOtherLang(alignment);
			String lang = langPair.getFirst();
			String otherLang = langPair.getSecond();
			
			status = harvestOtherLangPage_ByFollowingLanguageLink(alignment,
						lang, otherLang);

			if (status == null || status == StepOutcome.FAILURE || 
					status == StepOutcome.KEEP_TRYING) {
				raiseProblem(Problem.FETCHING_CONTENT_OF_OTHER_LANG_PAGE, 
					alignment, "Could not identify "+otherLang+
					" version of "+lang+" page "+alignment.getPageURL(lang));
			} else {
			
				List<String> sentences = null;
				if (status == StepOutcome.SUCCESS) {
					sentences = 
						segmentText(otherLang, alignment.getPageContent(otherLang));
				}
				alignment.setPageSentences(otherLang, sentences);
			}
		}
		
		return;
	}

	protected Pair<String, String> langAndOtherLang(
			DocAlignment alignment) {
		String filledLang = null;
		String unfilledLang = null;
		int filledCount = 0;
		
		for (String lang: alignment.getLanguages()) {
			if (alignment.getPageURL(lang) == null) {
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
	 * Harvest page in the other language, by following the language link
	 * on that page.
	 */
	protected StepOutcome harvestOtherLangPage_ByFollowingLanguageLink(
			DocAlignment alignment, String lang, String otherLang)
			throws WebConcordancerException {
		StepOutcome outcome = StepOutcome.FAILURE;
		try {
			String[] linkAnchors = names4lang(otherLang);
			// For now just follow the link for the first anchor
			if (linkAnchors.length > 0) {
				String anchor = linkAnchors[0];
				harvester.harvestSingleLink(anchor);
				URL otherLangURL = harvester.getCurrentURL();
				String otherLangContent = harvester.getText();
				alignment.setPageContent(otherLang, otherLangContent);
				alignment.setPageURL(otherLang, otherLangURL);

				outcome = StepOutcome.SUCCESS;
			}
		} catch (PageHarvesterException e) {
			// If we weren't able to fetch the other language page, just
			// leave the outcome at FAILURE and return that
		}

		return outcome;
	}

	private String[] names4lang(String lang) {
		String[] names = new String[0];
		if (langNames.containsKey(lang)) {
			names = langNames.get(lang);
		}
		return names;
	}

	/** 
	 * Harvest page in the other language, using rules that transform the
	 * original URL into the URL of the other page.
	 */
	private StepOutcome harvestOtherLangPage_UsingURLTransformation(
			DocAlignment alignment, String lang, String otherLang) throws WebConcordancerException {
		
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
					
					if (!alignment.encounteredSomeProblems()) {
						try {
							String otherLangContent = harvester.getText();

							URL otherLangURL = harvester.getCurrentURL();
							alignment.setPageContent(otherLang, otherLangContent);
							alignment.setPageURL(otherLang, otherLangURL);
							status = StepOutcome.SUCCESS;
						} catch (PageHarvesterException e) {
							throw new WebConcordancerException(e);
						}
					}
				}
			}
		}
		
		return status;
	}

	private void harvestInputPage(URL url, DocAlignment alignment,
		Set<String> languages) {
		harvestInputPage(url, alignment, languages.toArray(new String[0]));
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
		
		if (!alignment.encounteredSomeProblems()) {
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
