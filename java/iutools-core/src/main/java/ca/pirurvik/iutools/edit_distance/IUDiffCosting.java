package ca.pirurvik.iutools.edit_distance;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.inuktitutcomputing.script.Roman;
import ca.nrc.datastructure.Pair;
import ca.nrc.string.diff.DiffCosting;
import ca.nrc.string.diff.DiffResult;
import ca.nrc.string.diff.StringTransformation;
import ca.pirurvik.iutools.spellchecker.SpellTracer;


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
		SpellTracer.trace("SpellChecker.computeCandidateSimilarity", 
				"Invoked", 
				diff.origStr(), diff.revStr());
		
		Double _cost = costLeadingCharChanges(diff);
		SpellTracer.trace("cost", 
				"AFTER costAsLeadingCharChanges, _cost="+_cost,
				diff.origStr(), diff.revStr());			
		
		for (int nn=0; nn < diff.transformations.size(); nn++) {
			_cost += costNthTransformation(nn, diff);
		}
		
		SpellTracer.trace("SpellChecker.computeCandidateSimilarity", 
				"returning _cost="+_cost, 
				diff.origStr(), diff.revStr());
		
		return _cost.doubleValue();
	}

	private Double costNthTransformation(int nn, DiffResult diff) {
		return costNthTransformation(nn, diff, false);
	}
		
	private Double costNthTransformation(int nn, DiffResult diff, 
						boolean asLeadingCharChanges) {
		
		SpellTracer.trace("costNthTransformation", 
				"Costing transf nn="+nn,
				diff.revStr(), diff.origStr());
		
		Double _cost = null;
		StringTransformation transf = diff.transformations.get(nn);
		
		double unitCost = SMALL_COST;
		if (asLeadingCharChanges && !wordsAreSingleMorphemes(diff)) {
			// Changing the first few characters of a word has an 
			// "infinite" cost, unless the word and its correction 
			// are single morphme words
			//
			unitCost = INFINITE;
		}
				
		// Note: If the transformation is doubling or 
		//   dedoubling a character, then it will always 
		//   use a small unit cost, even if it affects 
		//   chars of the first morpheme
		//
		_cost = costAsCharacterDoubling(nn, diff);			
		
		if (_cost == null) {
			// No "special" costing was applied. Use
			// a "generic" costing based on number of characters 
			// affected.
			//
			_cost = unitCost * transf.numAffectedTokens();
		}
		
		return _cost;
	}
	
	private Double costAsCharacterDoubling(int transfNum, DiffResult diff) {
	
		Double cost = null;
		
		// Note: Doubling or de-doubling a character ALWAYS has a small
		//  cost, even if the change affects leading characters of the 
		//  word.
		//
		Double unitCost = SMALL_COST;
		
		StringTransformation transf = diff.transformations.get(transfNum);
		if (transf.origTokens.length == 1 && 
				transf.revisedTokens.length == 1) {
			// A character was changed to a different want.
			// Check if one is the doubled version of the other, 
			// for example: 'ii' -> 'i' or 'i' -> 'ii'
			// 
			String origStr = String.join("", transf.origTokens);
			String revStr = String.join("", transf.revisedTokens);
			
			
			String regex = "(.)\\1?";			
			origStr = origStr.replaceAll(regex, "$1");
			revStr = revStr.replaceAll(regex, "$1");
			if (origStr.equals(revStr)) {
				cost = unitCost;
			}
		}
		
		return cost;
	}	


	/**
	 * Check if the first transformation in a diff affects the head of the word and 
	 * if so, see if it is an allowable transformation (if not, return an INFINITE 
	 * cost).
	 * 
	 * @param chars1
	 * @param chars2
	 * @param diff
	 * @return
	 */
	private Double costLeadingCharChanges(DiffResult diff) {
		Double _cost = 0.0;
		List<StringTransformation> transf = diff.transformations;
		
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
		
		if (lastLeadCharTransf >= 0) {
			_cost = costSpecialCase_anniaq(diff);
			if (_cost == null) {
			
				// Cost of any change in the leading chars of a word 
				// should be considered "INFINITE", unless the word 
				// and its correction are single morpheme words
				//
				boolean asLeadingCharChanges = true;
				if (wordsAreSingleMorphemes(diff)) {
					asLeadingCharChanges = false;
				}
				
				_cost = 0.0;
				for (int ii=0; ii <= lastLeadCharTransf; ii++) {
					_cost += costNthTransformation(ii, diff, asLeadingCharChanges);
				}
				
			
				// Remove transformations that affect leading chars
				//
				for (int ii=0; ii <= lastLeadCharTransf; ii++) {
					transf.remove(0);
				}
			}
		}
		
		return _cost;
	}

	private Double costSpecialCase_anniaq(DiffResult diff) {
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

	private Double costAllowableLeadingCharChanges(int lastLeadCharTransf, DiffResult diffRes) {
		Double cost = null;
		
		String origStr = diffRes.origStr();
		String revStr = diffRes.revStr();
		
		if (wordsAreSingleMorphemes(diffRes)) {
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

	private boolean wordsAreSingleMorphemes(DiffResult diffRes) {
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
}
