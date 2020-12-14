package org.iutools.concordancer;

import ca.inuktitutcomputing.script.Syllabics;
import ca.nrc.data.harvesting.LanguageGuesser;
import ca.nrc.data.harvesting.LanguageGuesserException;

public class LanguageGuesser_IU extends LanguageGuesser {
	
	@Override
	public String detect(String text) throws LanguageGuesserException {
		String lang = null;
		if (Syllabics.syllabicCharsRatio(text) > 0.5) {
			lang = "iu";
		}
		if (lang == null) {
			lang = super.detect(text);
		}
		
		return lang;
	}

}
