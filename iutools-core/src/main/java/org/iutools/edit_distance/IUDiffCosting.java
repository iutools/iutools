package org.iutools.edit_distance;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.iutools.script.Roman;
import ca.nrc.datastructure.Pair;
import ca.nrc.string.diff.DiffCosting;
import ca.nrc.string.diff.DiffResult;
import ca.nrc.string.diff.StringTransformation;
import org.iutools.spellchecker.SpellDebug;


/**
 * This costing model is meant for character-level diffs between 
 * two Inuktut words. It takes into account common types of Inuktut 
 * spelling mistakes.
 * 
 * 
 * @author desilets
 *
 */
public class IUDiffCosting extends DiffCosting {

	public double cost(DiffResult diff) {
		SpellDebug.trace("IUDiffCosting.cost",
			"Invoked",
			diff.origStr(), diff.revStr());
		
		Double _cost = costFirstMorphemeChange(diff);
		SpellDebug.trace("IUDiffCosting.cost",
				"AFTER costAsLeadingCharChanges, _cost="+_cost,
				diff.origStr(), diff.revStr());			
		
		for (int nn=0; nn < diff.transformations.size(); nn++) {
			_cost += costNthTransformation(nn, diff);
		}
		
		SpellDebug.trace("IUDiffCosting.cost",
			"returning _cost="+_cost,
			diff.origStr(), diff.revStr());
		
		return _cost.doubleValue();
	}

	private Double costNthTransformation(int nn, DiffResult diff) {
		return costNthTransformation(nn, diff, false);
	}
		
	private Double costNthTransformation(int nn, DiffResult diff, 
						boolean asLeadingCharChanges) {
		
		
		Double _cost = null;
		StringTransformation transf = diff.transformations.get(nn);
		
		SpellDebug.trace("IUDiffCosting.costNthTransformation", 
				"Costing transf nn="+nn+
				": transf.origStr="+transf.origStr()+
				", transf.revStr="+transf.revStr(),
				diff);

		double unitCost = SMALL_COST;
		if (asLeadingCharChanges && !wordsAreSingleMorpheme(diff)) {
			// Changing the first few characters of a word has an 
			// "infinite" cost, unless the word and its correction 
			// are single morphme words
			//
			unitCost = 2*SMALL_COST;
		}
				
		_cost = costAsCharacterDoubling(nn, diff, unitCost);			
		
		SpellDebug.trace("IUDiffCosting.costNthTransformation", 
				"After costing for doubled characters, _cost="+_cost,
				diff);

		if (_cost == null) {
			// No "special" costing was applied. Use
			// a "generic" costing based on number of characters 
			// affected.
			//
			_cost = unitCost * transf.numAffectedTokens();
		}
		
		
		SpellDebug.trace("IUDiffCosting.costNthTransformation", 
				"returning _cost="+_cost,
				diff);
		
		return _cost;
	}
	
	private Double costAsCharacterDoubling(int transfNum, 
			DiffResult diff, Double unitCost) {
	
		Double cost = null;
		
		StringTransformation transf = diff.transformations.get(transfNum);
		
		SpellDebug.trace("IUDiffCosting.costAsCharacterDoubling", 
				"Costing transf #"+transfNum+": "+transf.origStr()+"-->"+transf.revStr(),
				diff);
		
		Pair<Integer,Integer> charDoublingResult = 
				isCharDoubling(transf.origStr(), transf.revStr());
		
		SpellDebug.trace("IUDiffCosting.costAsCharacterDoubling", 
				"Char doubling info: "+charDoublingResult,
				diff);
		
		if (charDoublingResult != null) {
			// Transformation included character doublings or
			// de-doubling.
			//
			// Cost this in a special way.
			//
			
			// Character doubling/de-doubling encurs a "tiny" cost
			cost = TINY_COST * charDoublingResult.getFirst();
			
			// Other types of transformation encure the received 
			// unit cost. This may be INFINITE if the transformation
			// affects the first phoneme
			//
			cost += unitCost * charDoublingResult.getSecond();				
		}
		
		return cost;
	}	

	/**
	 * Cost of transformations for the first morpheme of words follow different 
	 * rules than transformation affecting rest of the morphemes. 
	 */
	private Double costFirstMorphemeChange(DiffResult diff) {
		Double _cost = 0.0;
		List<StringTransformation> transf = diff.transformations;
		
		SpellDebug.trace("IUDiffCosting.costFirstMorphemeChange", 
				"Invoked",diff);
		
		// Identify transformations that affect the first
		// few characters of word and its correction
		//
		int lastLeadCharTransf = -1;
		int transfNum = -1;
		while (true) {
			transfNum++;
			if (transfNum >= transf.size()) {
				break;
			}
			StringTransformation headTransf = transf.get(transfNum);
			if (headTransf.origTokenPos <= 2 ||
					headTransf.revisedTokenPos <= 2) {
				lastLeadCharTransf = transfNum;
			} else {
				break;
			}
		}
		
		SpellDebug.trace("IUDiffCosting.costFirstMorphemeChange", 
				"lastLeadCharTransf="+lastLeadCharTransf,diff);
		
		if (lastLeadCharTransf >= 0) {
			SpellDebug.trace("IUDiffCosting.costFirstMorphemeChange", 
					"There are SOME changes in the first morphme", diff);
			
			_cost = costSpecialCase_aanniaq(diff);
			
			SpellDebug.trace("IUDiffCosting.costFirstMorphemeChange", 
					"After special case 'aanniaq', _cost="+_cost, diff);

			if (_cost == null) {
			
				// Cost of any change in the leading chars of a word 
				// should be considered "INFINITE", unless the word 
				// and its correction are single morpheme words
				//
				boolean asLeadingCharChanges = true;
				if (wordsAreSingleMorpheme(diff)) {
					asLeadingCharChanges = false;
				}
				
				_cost = 0.0;
				for (int ii=0; ii <= lastLeadCharTransf; ii++) {
					_cost += costNthTransformation(ii, diff, asLeadingCharChanges);
				}
			}
			
			// Remove transformations that affect leading chars
			//
			for (int ii=0; ii <= lastLeadCharTransf; ii++) {
				transf.remove(0);
			}
		} else {
			SpellDebug.trace("IUDiffCosting.costFirstMorphemeChange", 
					"No changes in the first morphme", diff);
		}
		
		SpellDebug.trace("IUDiffCosting.costFirstMorphemeChange", 
				"returning _cost="+_cost, diff);
		return _cost;
	}

	private Double costSpecialCase_aanniaq(DiffResult diff) {
		// This is a particular case that is very common in the 
		// Hansard.
		//
		// Any word that starts with 'anniaq' or 'aniaq' can 
		// be spelled alternatively with 'niaq' or 'nniaq'.
		// This is technically a spelling mistake, but it is 
		// used so commonly in the Hansard that it can be 
		// deemed acceptable.
		// 
		//
		Double cost = null;
				
		String origStr = diff.origStr();
		String revStr = diff.revStr();
		Pattern aanniaq = Pattern.compile("^aanniaq[\\s\\S]*$");
		Pattern nniaq = Pattern.compile("^nniaq[\\s\\S]*$");
		if (aanniaq.matcher(origStr).matches() && 
				nniaq.matcher(revStr).matches()) {
			cost = TINY_COST;
		} else if (aanniaq.matcher(revStr).matches() && 
				nniaq.matcher(origStr).matches()) {
			cost = TINY_COST;
		}
		
		return cost;
	}

	private Double costAllowableLeadingCharChanges(int lastLeadCharTransf, DiffResult diffRes) {
		Double cost = null;
		
		String origStr = diffRes.origStr();
		String revStr = diffRes.revStr();
		
		if (wordsAreSingleMorpheme(diffRes)) {
			// If the correction is made up of a 
			// single morpheme, then it is OK to modify 
			// the first few characters.
			//
			cost = 0.0;
			for (int ii=0; ii <=lastLeadCharTransf; ii++) {
				cost += costNthTransformation(ii, diffRes);
			}
		}
				
		// This is a particular case that is very common in the 
		// Hansard.
		//
		Pattern anniaq = Pattern.compile("^an+iaq[\\s\\S]*$");
		Pattern nniaq = Pattern.compile("^n+iaq[\\s\\S]*$");
		if (anniaq.matcher(origStr).matches() && 
				nniaq.matcher(revStr).matches()) {
			cost = SMALL_COST;
		} else if (anniaq.matcher(revStr).matches() && 
				nniaq.matcher(origStr).matches()) {
			cost = SMALL_COST;
		}
		
		return cost;
	}

	private boolean wordsAreSingleMorpheme(DiffResult diffRes) {
		// For simplicity's sake, we assume that the revision 
		// is a single morpheme if its length is at most 5.
		//
		// Note that we compute the lenght in terms of 
		// syllabic (in other words, doubled chars like 
		// 'ii' and 'nn' count as a single character).
		// 
		String[] charsRev = Roman.splitChars(diffRes.revStr());
		String[] charsOrig = Roman.splitChars(diffRes.origStr());
		boolean isSingle = (charsRev.length <= 5 && charsOrig.length <= 5);
		
		return isSingle;
	}

	/**
	 * Check if a transformation involves doubling/de-doubling of characters.
	 * If so, return a pair providing:
	 * 
	 * - number of doublings/de-doublings operations
	 * - number of chars affected by other types of operations (substitution, 
	 *     addition, deletion)
	 * 
	 * @param origStr
	 * @param revStr
	 * @return
	 */
	public Pair<Integer, Integer> isCharDoubling(String origStr, String revStr) {
		Pair<Integer,Integer> result = null;
		
		Integer totalDblOps = 0;
		Integer totalCharsOtherOps = 0;
		if (origStr.length() <= 5 && origStr.length() <=5) {
			
			// Calculate char doubling at START of string
			Pattern patt = Pattern.compile("^((.)(\\2?))");
			Matcher matcherOrig = patt.matcher(origStr);
			Matcher matcherRev = patt.matcher(revStr);
			if (matcherOrig.find() && matcherRev.find()) {
				String origStart = matcherOrig.group(1);
				String revStart = matcherRev.group(1);
				if ((origStart.length() > 1 || revStart.length() > 1) &&
						matcherOrig.group(2).equals(matcherRev.group(2))) {
					// The two strings start with same char, and one of them
					// is doubled.
					totalDblOps += 1;
					origStr = origStr.substring(origStart.length());
					revStr = revStr.substring(revStart.length());
				}
			}

			// Calculate char doubling at END of string
			patt = Pattern.compile("((.)(\\2?))$");
			matcherOrig = patt.matcher(origStr);
			matcherRev = patt.matcher(revStr);
			if (matcherOrig.find() && matcherRev.find()) {
				String origEnd = matcherOrig.group(1);
				String revEnd = matcherRev.group(1);
				if ((origEnd.length() > 1 || revEnd.length() > 1) &&
						matcherOrig.group(2).equals(matcherRev.group(2))) {
					// The two strings end with same char, and one of them
					// is doubled.
					totalDblOps += 1;
					origStr = origStr.substring(0, origStr.length() - origEnd.length());
					revStr = revStr.substring(0, revStr.length() - revEnd.length());
				}
			}
		
			// Any chars that still remaing are involved in a type of operation 
			// other than doubling/de-doubling.
			//
			totalCharsOtherOps = origStr.length() + revStr.length();
		}
		
		Pair<Integer,Integer> dblInfo = null;
		if (totalDblOps + totalCharsOtherOps > 0) {
			dblInfo = Pair.of(totalDblOps, totalCharsOtherOps);
		}
		
		return dblInfo;
	}
}
