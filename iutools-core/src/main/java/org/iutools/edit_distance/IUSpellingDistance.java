package org.iutools.edit_distance;

import org.iutools.script.Roman;
import org.iutools.script.Syllabics;
import org.iutools.script.TransCoder;
import ca.nrc.string.diff.DiffResult;
import ca.nrc.string.diff.StringDiffException;
import ca.nrc.string.diff.TextualDiff;
import org.iutools.spellchecker.SpellDebug;

/**
 * Computes edit distance between two Inuktut words, taking into 
 * account common typing or spelling mistakes.
 * 
 * @author desilets
 *
 */
public class IUSpellingDistance implements EditDistanceCalculator {
	
	IUDiffCosting diffCoster = new IUDiffCosting();
	TextualDiff diffFinder = 
			new TextualDiff().setIgnoreSpaces(false);

	@Override
	public double distance(String word1, String word2) throws EditDistanceCalculatorException {
		SpellDebug.trace("IUSpellingDistance.distance", 
				"Invoked", word1, word2);
		
		String wordRoman1 = word1;
		if (Syllabics.allInuktitut(wordRoman1)) {
			wordRoman1 = TransCoder.unicodeToRoman(wordRoman1);
		}
		String[] wordChars1 = Roman.splitChars(wordRoman1);
				
		String wordRoman2 = word2;
		if (Syllabics.allInuktitut(wordRoman2)) {
			wordRoman2 = TransCoder.unicodeToRoman(wordRoman2);
		}
		String[] wordChars2 = Roman.splitChars(wordRoman2);
		
		DiffResult diff = null;
		try {
			diff = diffFinder.diffResult(wordChars1, wordChars2);
		} catch (StringDiffException e) {
			throw new EditDistanceCalculatorException(e);
		}
		
		double dist = diffCoster.cost(diff);
		
		return dist;
	}

}
