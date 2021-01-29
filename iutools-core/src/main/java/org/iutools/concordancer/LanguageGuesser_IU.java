package org.iutools.concordancer;

import org.iutools.script.Syllabics;
import ca.nrc.data.harvesting.LanguageGuesser;
import ca.nrc.data.harvesting.LanguageGuesserException;

public class LanguageGuesser_IU extends LanguageGuesser {
	
	@Override
	public String detect(String text) throws LanguageGuesserException {
		String lang = null;
		if (isInuktitut(text)) {
			lang = "iu";
		}
		if (lang == null) {
			lang = super.detect(text);
		}
		
		return lang;
	}

	public boolean isInuktitut(String text) {
		boolean isIU = false;
		if (Syllabics.syllabicCharsRatio(text) > 0.5) {
			isIU = true;
		}
		return isIU;
	}
}
