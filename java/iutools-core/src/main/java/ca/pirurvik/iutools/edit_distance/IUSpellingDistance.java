package ca.pirurvik.iutools.edit_distance;

import java.util.List;

import ca.inuktitutcomputing.script.Roman;
import ca.inuktitutcomputing.script.Syllabics;
import ca.inuktitutcomputing.script.TransCoder;
import ca.nrc.string.diff.DiffResult;
import ca.nrc.string.diff.StringDiffException;
import ca.nrc.string.diff.StringTransformation;
import ca.nrc.string.diff.TextualDiff;

/**
 * Computes edit distance between two Inuktut words, taking into 
 * account common typing or spelling mistakes.
 * 
 * @author desilets
 *
 */
public class IUSpellingDistance implements EditDistanceCalculator {
	
	IUDiffCosting diffCoster = new IUDiffCosting();
	TextualDiff diffFinder = new TextualDiff();

	@Override
	public double distance(String word1, String word2) throws EditDistanceCalculatorException {
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
