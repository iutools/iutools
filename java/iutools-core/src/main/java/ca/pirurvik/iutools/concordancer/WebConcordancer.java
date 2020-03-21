package ca.pirurvik.iutools.concordancer;

import java.io.IOException;
import java.net.URL;

import org.apache.poi.xwpf.usermodel.TextAlignment;

import ca.nrc.data.harvesting.LanguageGuesser;
import ca.nrc.data.harvesting.LanguageGuesserException;
import ca.nrc.data.harvesting.PageHarvester;
import ca.nrc.data.harvesting.PageHarvesterException;

/**
 * Use this class to fetch aligned sentences from a multilingual web page. 
 * 
 * @author desilets
 *
 */
public class WebConcordancer {
	
	PageHarvester harvester = new PageHarvester();	

	public AlignmentResult alignPage(URL url, String[] languages) 
			throws WebConcordancerException {
		AlignmentResult alignment = new AlignmentResult();
		for (String lang: languages) {
			alignment.setPageContent(lang, null);
		}
		
		harvestInputPage(url, alignment);
		harvestOtherLangPage(alignment);
		
		return alignment;
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
		lang = LanguageGuesser.detect(text);
		
		return lang;
	}

}
