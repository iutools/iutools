package ca.pirurvik.iutools.edit_distance;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.inuktitutcomputing.script.Roman;
import ca.nrc.datastructure.Pair;
import ca.nrc.string.diff.DiffCosting;
import ca.nrc.string.diff.DiffResult;
import ca.nrc.string.diff.StringTransformation;


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
		boolean trace = (diff.origStr().equals("sunainna") && 
							diff.revStr().equals("tanna"));
		if (trace) {
			System.out.println("** cost: orig="+diff.origStr()+", rev="+diff.revStr());			
		}
		
		Double _cost = costAsLeadingCharChanges(diff);
		if (trace) {
			System.out.println("** cost: AFTER costAsLeadingCharChanges, _cost="+_cost);
		}
		
		for (int nn=0; nn < diff.transformations.size(); nn++) {
			_cost += costNthTransformation(nn, diff);
		}
		
		return _cost.doubleValue();
	}

	private Double costNthTransformation(int nn, DiffResult diff) {
		StringTransformation transf = diff.transformations.get(nn);
		
		Double _cost = costAsCharacterDoubling(nn, diff);
		
		if (_cost == null) {
			_cost = 1.0 * transf.numAffectedTokens();
		}
		
		return _cost;
	}
	
	private Double costAsCharacterDoubling(int transfNum, DiffResult diff) {
	
	Double cost = null;
	
	StringTransformation transf = diff.transformations.get(transfNum);
	if (transf.origTokens.length == 1 && 
			transf.revisedTokens.length == 1) {
		// A character was changed to a different want.
		// Check if one is the doubled version of the other, 
		// for example: 'ii' -> 'i' or 'i' -> 'ii'
		// 
		String origStr = String.join("", transf.origTokens);
		String revStr = String.join("", transf.revisedTokens);
		
		Matcher matcher = Pattern.compile("(.)\\1?").matcher(origStr);
		matcher.matches();
		String origChar = matcher.group(1);
		
		matcher = Pattern.compile(origChar+"{2}").matcher(revStr);
		if (matcher.matches()) {
			cost = SMALL_COST;
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
	private Double costAsLeadingCharChanges(DiffResult diff) {
		Double _cost = 0.0;
		List<StringTransformation> transf = diff.transformations;
		while (!transf.isEmpty()) {
			 
			StringTransformation headTransf = transf.get(0);
			if (headTransf.origTokenPos <= 2 ||
					headTransf.revisedTokenPos <= 2) {
				
				// In general, it is not allowed to change the first 
				// three chars of, but there can be exceptions...
				_cost = costAllowableLeadingCharChanges(diff);
				
				if (_cost == null) {
					// This is not one of those exceptions, so return
					// an infinite cost.
					_cost = INFINITE;
				}
				
				// Remove the first transformation because we have
				// costed it.
				//
				transf.remove(0);
			} else {
				// The rest of the transformations do not affect 
				// beginning of the word
				break;
			}
		}
		
		return _cost;
	}

	private Double costAllowableLeadingCharChanges(DiffResult diffRes) {
		Double cost = null;
		
		String origStr = diffRes.origStr();
		String revStr = diffRes.revStr();
		
		if (wordsAreSingleMorphemes(diffRes)) {
			// If the word is correcgtion is made up of a 
			// single morpheme, then it is OK to modify 
			// the first few characters.
			//
			cost = costNthTransformation(0, diffRes);
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
