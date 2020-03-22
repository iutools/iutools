package ca.pirurvik.iutools.concordancer;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.poi.xwpf.usermodel.TextAlignment;

import ca.nrc.data.harvesting.LanguageGuesser;
import ca.nrc.data.harvesting.LanguageGuesserException;
import ca.nrc.data.harvesting.PageHarvester_Barebones;
import ca.nrc.data.harvesting.PageHarvesterException;

/**
 * Use this class to fetch aligned sentences from a multilingual web page. 
 * 
 * @author desilets
 *
 */
public class WebConcordancer {
	
	PageHarvester_Barebones harvester = new PageHarvester_Barebones();	

	public AlignmentResult alignPage(URL url, String[] languages) 
			throws WebConcordancerException {
		
		// For now, return 'failed' alignments for all URLs except for 
		// 'http://mockskite.nu.ca/'. Return hardcoded mock results for 
		// the later
		boolean failedAlignForAllButMock = true;
		if (url.toString().startsWith("http://mocksite.nu.ca/")) {
			return mockAlignmentResult();
		}
		
		AlignmentResult alignment = new AlignmentResult();
		for (String lang: languages) {
			alignment.setPageContent(lang, null);
		}
		
		harvestInputPage(url, alignment);
		harvestOtherLangPage(alignment);
		
		if (failedAlignForAllButMock) {
			alignment.success = false;
		}
		
		return alignment;
	}
	
	private AlignmentResult mockAlignmentResult() {
		AlignmentResult result = new AlignmentResult();
		
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

	private void harvestOtherLangPage(AlignmentResult alignment) {
		
		
	}

	private void harvestInputPage(URL url, AlignmentResult alignment) throws WebConcordancerException {
		String urlText = null;
		String urlLang = null;
		try {
			urlText = harvester.harvestSinglePage(url);
			urlLang = guessLang(urlText);
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
		
		String lang;
		lang = new LanguageGuesser().detect(text);
		
		return lang;
	}

}
