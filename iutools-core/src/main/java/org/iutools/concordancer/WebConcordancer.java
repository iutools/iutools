package org.iutools.concordancer;

import java.beans.Transient;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import ca.nrc.data.harvesting.*;
import ca.nrc.json.PrettyPrinter;
import org.apache.commons.lang.ArrayUtils;
import org.apache.log4j.Logger;

import ca.nrc.datastructure.Pair;
import ca.nrc.string.StringUtils;
import org.iutools.text.segmentation.Segmenter;

/**
 * Use this class to fetch aligned sentences from a multilingual web page. 
 * 
 * @author desilets
 *
 */
public abstract class WebConcordancer {

	public static enum AlignOptions {
		HTML, ALL_TEXT, MAIN_TEXT, ALIGNED_SENTENCES}
	protected static enum StepOutcome {SUCCESS, FAILURE, KEEP_TRYING};
	private static enum AlignmentPart {
		PAGES_CONTENT, PROBLEMS, SENTENCES, ALIGNMENTS};

	boolean includeAllText = false;
	boolean includeMainText = false;
	boolean includeAlignedSentences = false;
	boolean includeHtml = false;

	private static Map<String,String[]> langNames = new HashMap<String,String[]>();
	private static void setLangNames(String lang, String... names) {
		langNames.put(lang, names);
	}
	static {
		setLangNames("en", "english");
		setLangNames("fr", "français");
		// Note: Some sites display the language link for Inuktitut using the english
		// name for the language, i.e. 'inuktitut'
		setLangNames("iu", "ᐃᓄᒃᑎᑐᑦ", "ᐃᓄᒃᑐᑦ", "inuktitut");
	}

	@Transient
	protected abstract PageHarvester getHarvester();

	PageHarvester harvester = null;
	LanguageGuesser langGuesser = new LanguageGuesser_IU();
	Aligner_Maligna aligner = new Aligner_Maligna();

	public WebConcordancer() {
		init_WebConcordancer(new AlignOptions[0]);
	}

	public WebConcordancer(AlignOptions... options) {
		init_WebConcordancer(options);
	}

	private void init_WebConcordancer(AlignOptions[] options) {
		if (ArrayUtils.contains(options, AlignOptions.HTML)) {
			includeHtml = true;
		}
		if (ArrayUtils.contains(options, AlignOptions.ALL_TEXT)) {
			includeAllText = true;
		}
		if (ArrayUtils.contains(options, AlignOptions.MAIN_TEXT)) {
			includeMainText = true;
		}
		if (ArrayUtils.contains(options, AlignOptions.ALIGNED_SENTENCES)) {
			includeAlignedSentences = true;
		}

		if (!includeMainText && !includeAllText) {
			includeAllText = true;
		}
	}

	public DocAlignment alignPage(URL url, String[] languages)
		throws WebConcordancerException {

		Logger tLogger = Logger.getLogger("org.iutools.concordancer.alignPage");

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("invoked with url="+url+
			", languages="+String.join(", ", languages));
		}

		DocAlignment alignment = new DocAlignment(languages);

		harvestInputPage(url, alignment, languages);
		
		trace(tLogger, "After fetching input URL", alignment, 
				AlignmentPart.PROBLEMS, AlignmentPart.SENTENCES);
		
		if (!alignment.encounteredSomeProblems()) {
			harvestOtherLangPage(alignment);
			trace(tLogger, "After fetching other language URL", alignment,
					AlignmentPart.PROBLEMS, AlignmentPart.SENTENCES);
		}

		if (!alignment.encounteredSomeProblems()
			&& this.includeAlignedSentences) {
			alignTexts(alignment);
			trace(tLogger, "After fetching input URL", alignment, 
					AlignmentPart.PROBLEMS, AlignmentPart.SENTENCES,
					AlignmentPart.ALIGNMENTS);
			if (tLogger.isTraceEnabled()) {
				String mess = "After aligning content of the two pages, alignments are:";
				for (SentencePair anAlignment: alignment.getAligments()) {
					mess += "   "+anAlignment+"\n";
				}
				tLogger.trace(mess);
			}
		}
		
		alignment.success = !alignment.encounteredSomeProblems();

		if (tLogger.isTraceEnabled()) {
			tLogger.trace("Upon exit, alignment=\n"+ PrettyPrinter.print(alignment));
		}

		return alignment;
	}

	protected DocAlignment fetchParallelPages(DocAlignment alignment)
		throws WebConcordancerException {
		Logger tLogger = Logger.getLogger("org.iutools.concordancer.fetchParallelPages");

		Pair<String,String> langs = langAndOtherLang(alignment);

		URL langURL = null;
		try {
			langURL = alignment.getPageURL(langs.getFirst());
		} catch (DocAlignmentException e) {
			throw new WebConcordancerException(
				"Could not get URL of page in lang: "+langs.getFirst(), e);
		}
		harvestInputPage(langURL, alignment, alignment.languages());

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
			for (SentencePair anAlignment: alignment.getAligments()) {
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
			for (String lang: alignment.languages()) {
				mess += "   "+lang+": "+alignment.getPageText(lang)+"\n\n";
			}
		}
		return mess;
	}
	
	private String traceSentences(Logger tLogger, 
			String mess, DocAlignment alignment) {
		if (tLogger.isTraceEnabled()) {
			mess += "\nSentences are:\n\n";
			for (String lang: alignment.languages()) {
				mess += "   "+lang+": "+alignment.getPageSentences(lang)+"\n\n";
			}
		}
		
		return mess;
	}
	

	private void alignTexts(DocAlignment docAlignment) throws WebConcordancerException {
		Logger tLogger = Logger.getLogger("org.iutools.concordancer.alignTexts");

		alignOneText(DocAlignment.PageSection.ALL, docAlignment);
		alignOneText(DocAlignment.PageSection.MAIN, docAlignment);
	}

	private void alignOneText(DocAlignment.PageSection pageSection, DocAlignment docAlignment) throws WebConcordancerException {
		Logger tLogger = Logger.getLogger("org.iutools.concordancer.alignOneText");

		if (docAlignment.hasTextForBothLanguages(pageSection)) {
			Map<String,String> text4lang = null;
			if (pageSection == DocAlignment.PageSection.ALL) {
				text4lang = docAlignment.pagesTextHash();
			} else {
				text4lang = docAlignment.pagesMainTextHash();
			}
			List<List<String>> langSents = new ArrayList<List<String>>();
			List<String> langs = new ArrayList<String>();
			for (String lang: text4lang.keySet()) {
				langs.add(lang);
				String text = text4lang.get(lang);

				Segmenter segmenter = Segmenter.makeSegmenter(lang);
				if (tLogger.isTraceEnabled()) {
					tLogger.trace("segmenting content for lang="+lang+": "+text);
				}

				List<String> sents =
					segmentText(lang, text4lang.get(lang));
				langSents.add(sents);
			}

			List<Pair<String, String>> alignedPairs = null;
			try {
				alignedPairs = aligner.align(langSents.get(0), langSents.get(1));
			} catch (AlignerException e) {
				raiseProblem(DocAlignment.Problem.ALIGNING_SENTENCES, docAlignment, e);
			}

			List<SentencePair> alignments = docAlignment.getAligments(pageSection);

			alignments = new ArrayList<SentencePair>();
			for (Pair<String,String> aPair: alignedPairs) {
				SentencePair anAlignment =
					new SentencePair(
						langs.get(0), aPair.getFirst(),
						langs.get(1), aPair.getSecond());
				docAlignment.addAlignment(pageSection, anAlignment);
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

		try {
			result.setPageText("en", enText);
			result.setPageText("iu", iuText);
		} catch (DocAlignmentException e) {
			// SHOULD NEVER HAPPEN
		}
		
		result.addAlignment(new SentencePair("en", enSentences[0], "iu", iuSentences[0]));
		result.addAlignment(new SentencePair("en", enSentences[1], "iu", iuSentences[1]));
		
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
			
			status =
				harvestOtherLangPage_ByFollowingLanguageLink(alignment,
						lang, otherLang);

			if (status == null || status == StepOutcome.FAILURE || 
					status == StepOutcome.KEEP_TRYING) {
				try {
					raiseProblem(DocAlignment.Problem.FETCHING_CONTENT_OF_OTHER_LANG_PAGE,
						alignment, "Could not identify "+otherLang+
						" version of "+lang+" page "+alignment.getPageURL(lang));
				} catch (DocAlignmentException e) {
					throw new WebConcordancerException(e);
				}
			} else {
				List<String> sentences = null;
				if (status == StepOutcome.SUCCESS) {
					sentences = 
						segmentText(otherLang, alignment.getPageText(otherLang));
				}
				alignment.setPageSentences(otherLang, sentences);
			}
		}
		
		return;
	}

	protected Pair<String, String> langAndOtherLang(
			DocAlignment alignment) throws WebConcordancerException {
		String filledLang = null;
		String unfilledLang = null;
		int filledCount = 0;
		
		for (String lang: alignment.languages()) {
			try {
				if (alignment.getPageURL(lang) == null) {
					unfilledLang = lang;
				} else {
					filledLang = lang;
					filledCount++;
				}
			} catch (DocAlignmentException e) {
				throw new WebConcordancerException(e);
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
		String[] linkAnchors = names4lang(otherLang);
		for (String anchor: linkAnchors) {
			try {
				harvester.harvestSingleLink(anchor);
				URL otherLangURL = harvester.getCurrentURL();
				if (harvester.getText() == null || harvester.getText().isEmpty()) {
					continue;
				}

				if  (includeAllText) {
					String otherLangText = harvester.getText();
					alignment.setPageText(otherLang, otherLangText);
					String actualLang = guessLang(otherLangText);
					if (!actualLang.equals(otherLang)) {
						// Although were able to get a page by following this version
						// of the other language link, the content of that page was
						// actually NOT in the other language.
						//
						// So try with a different version of the language link.
						continue;
					}
				}

				if (includeMainText) {
					String otherLangMainText = harvester.getMainText();
					alignment.setPageMainText(otherLang, otherLangMainText);
				}

				if (includeHtml && !alignment.encounteredSomeProblems()) {
					alignment.setPageRawContent(otherLang, getHarvester().getHtml());
				}


				alignment.setPageURL(otherLang, otherLangURL);

				outcome = StepOutcome.SUCCESS;

				// We successfully fetched the other language page through that
				// version of the other language link. Stop here.
				break;

			} catch (PageHarvesterException | DocAlignmentException | LanguageGuesserException e) {
				// We weren't able to fetch the other language page through
				// that  version of the language link. Try with with the next
				// version of the link.
			}
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

	private void harvestInputPage(URL url, DocAlignment alignment,
		Set<String> languages) throws WebConcordancerException {
		harvestInputPage(url, alignment, languages.toArray(new String[0]));
	}

	private void harvestInputPage(URL url, DocAlignment alignment, 
			String[] languages) throws WebConcordancerException {
		Logger tLogger = Logger.getLogger("org.iutools.concordancer.harvestInputPage");
		tLogger.trace("invoked with url="+url);
		String urlLang = null;
		try {
			try {
				getHarvester().harvestSinglePage(url);
			} catch (PageHarvesterException e) {
				raiseProblem(DocAlignment.Problem.FETCHING_INPUT_URL, alignment, e);
			}
			if (!alignment.encounteredSomeProblems()
					&& includeAllText) {
				String urlWholeText = getHarvester().getText();
				tLogger.trace("retrieved urlWholeText=\n"+urlWholeText);
				if (urlWholeText == null) {
					this.raiseProblem(DocAlignment.Problem.FETCHING_INPUT_URL, alignment,
						"Could not fetch COMPLETE plain text of url: "+url);
				}
				urlLang = guessLang(urlWholeText);
				try {
					alignment.setPageText(urlLang, urlWholeText);
					List<String> sentences = segmentText(urlLang, urlWholeText);
					alignment.setPageSentences(urlLang, sentences);

				} catch (DocAlignmentException e) {
					raiseProblem(DocAlignment.Problem.FETCHING_INPUT_URL, alignment,
					"Could not set COMPLETE text for url: "+url);
				}
			}

			if (includeMainText && !alignment.encounteredSomeProblems()) {
				String urlMainText = getHarvester().getMainText();
				tLogger.trace("retrieved urlMainText=\n"+urlMainText);
				if (urlMainText == null) {
					raiseProblem(DocAlignment.Problem.FETCHING_INPUT_URL, alignment,
					"Could not fetch MAIN plain text of url: "+url);
				}
				if (urlLang == null) {
					urlLang = guessLang(urlMainText);
				}
				try {
					alignment.setPageMainText(urlLang, urlMainText);
				} catch (DocAlignmentException e) {
					raiseProblem(DocAlignment.Problem.FETCHING_INPUT_URL, alignment,
					"Could not set MAIN text for url: "+url);
				}
			}

			if (includeHtml && !alignment.encounteredSomeProblems()) {
				alignment.setPageRawContent(urlLang, getHarvester().getHtml());
			}

			if (urlLang != null) {
//				alignment.pagesURL.put(urlLang, url);
				alignment.pagesID.put(urlLang, url.toString());
			}
		} catch (PageHarvesterException | LanguageGuesserException e) {
			throw new WebConcordancerException(e);
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
