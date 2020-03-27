package ca.pirurvik.iutools.concordancer;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.nrc.data.harvesting.LanguageGuesser;
import ca.nrc.data.harvesting.LanguageGuesserException;
import ca.nrc.data.harvesting.PageHarvester_HtmlCleaner;
import ca.nrc.datastructure.Pair;
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
	
	PageHarvester_HtmlCleaner harvester = null;
	LanguageGuesser langGuesser = new LanguageGuesser_IU();
	Aligner_Maligna aligner = new Aligner_Maligna();
//	IUTokenizer tokenizer = new IUTokenizer();
	
	protected PageHarvester_HtmlCleaner getHarvester() {
		if (harvester == null) {
			harvester = new PageHarvester_HtmlCleaner();
			harvester.setHarvestFullText(true);
		}
		return harvester;
	}

	public DocAlignment alignPage(URL url, String[] languages) 
			throws WebConcordancerException {
		
		// For now, return 'failed' alignments for all URLs except for 
		// 'http://mockskite.nu.ca/'. Return hardcoded mock results for 
		// the later
		boolean failedAlignForAllButMock = false;
		if (url.toString().startsWith("http://mocksite.nu.ca/")) {
			return mockAlignmentResult();
		}
		
		DocAlignment alignment = new DocAlignment();
		for (String lang: languages) {
			alignment.setPageContent(lang, null);
		}
		
		harvestInputPage(url, alignment, languages);
		harvestOtherLangPage(alignment);
		alignContents(alignment);
		
		alignment.success = alignment.problemsEncountered.isEmpty();
		if (failedAlignForAllButMock) {
			alignment.success = false;
		}
		
		return alignment;
	}
	
	private void alignContents(DocAlignment docAlignment) throws WebConcordancerException {
		List<List<String>> langSents = new ArrayList<List<String>>();
		List<String> langs = new ArrayList<String>();
		for (String lang: docAlignment.getLanguages()) {
			langs.add(lang);
			Segmenter segmenter = Segmenter.makeSegmenter(lang);
			List<String> sents = 
				segmenter.segment(docAlignment.getPageContent(lang));
			langSents.add(sents);
		}
		
		List<Pair<String, String>> alignedPairs = null;
		try {
			alignedPairs = aligner.align(langSents.get(0), langSents.get(1));
		} catch (AlignerException e) {
			throw new WebConcordancerException(e);
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

	private void harvestOtherLangPage(DocAlignment alignment) throws WebConcordancerException {
		StepOutcome status = StepOutcome.KEEP_TRYING;
		
		Pair<String,String> langPair = langPairUnfilledSecond(alignment);
		String lang = langPair.getFirst();
		String otherLang = langPair.getSecond();
		
		status = harvestOtherLangPage_KnownSites(alignment, lang, otherLang);
		if (status == StepOutcome.KEEP_TRYING) {
			status = harvestOtherLangPage_UnknownSites(alignment, 
						lang, otherLang);
		}
		
		return;
	}

	protected Pair<String, String> langPairUnfilledSecond(
			DocAlignment alignment) throws WebConcordancerException {
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
		
		if (filledCount == 0) {
			throw new WebConcordancerException("None of the languages was filled.");
		}
		if (filledCount > 1) {
			throw new WebConcordancerException("Both of the languages were filled.");
		}
		
		Pair<String,String> langPair = Pair.of(filledLang, unfilledLang);
		
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
			DocAlignment alignment, String lang, String otherLang) throws WebConcordancerException 
					{
		
		StepOutcome status = StepOutcome.KEEP_TRYING;
		
		// For now, hardcode rules for dealing with just the one site:
		//    
		//    gov.nu.ca
		//
		// 
		URL langURL = alignment.getPageURL(lang);
		String site = langURL.getHost();
		if (site.endsWith("gov.nu.ca")) {
			if (otherLang.equals("iu")) {
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
					try {
						harvester.harvestSinglePage(new URL(otherLangURLstr));
						String otherLangContent = harvester.getText();
								
						URL otherLangURL = harvester.getCurrentURL();
						alignment.setPageContent(otherLang, otherLangContent);
						alignment.setPageURL(otherLang, otherLangURL);
						status = StepOutcome.FAILURE;
					} catch (MalformedURLException | PageHarvesterException e) {
						throw new WebConcordancerException(e);
					}					
				}
			}
		}
		
		return status;
	}

	private void harvestInputPage(URL url, DocAlignment alignment, 
			String[] languages) throws WebConcordancerException {
		String urlText = null;
		String urlLang = null;
		try {
			getHarvester().harvestSinglePage(url);
			urlText = getHarvester().getText();
			urlLang = guessLang(urlText);
			if (!urlLang.equals(languages[0]) && 
					!urlLang.equals(languages[1])) {
				throw new WebConcordancerException(
						"Langage of input page "+url+" was not as expected.\n"+
						"   Got : "+urlLang+"; expected: ["+
						String.join(",", languages)+"]");
			}
				
		} catch (PageHarvesterException e) {
			throw new WebConcordancerException("Could not harvest page to align", e);
		} catch (LanguageGuesserException e) {
			throw new WebConcordancerException(
					"Could not guess language of page to align", e);
		}
		
		alignment.setPageContent(urlLang, urlText);
		alignment.setPageURL(urlLang, url);

		return;
	}


	private String guessLang(String text) throws LanguageGuesserException {		
		String lang = langGuesser.detect(text);
		return lang;
	}
}
