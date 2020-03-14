package ca.pirurvik.iutools.edit_distance;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;

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
		
//		SpellTracer.trace("IUDiffCosting.costNthTransformation", 
//				"Costing transf nn="+nn,
//				diff.origStr(), diff.revStr());
		
		Double _cost = null;
		StringTransformation transf = diff.transformations.get(nn);
		
		double unitCost = SMALL_COST;
		if (asLeadingCharChanges && !wordsAreSingleMorpheme(diff)) {
			// Changing the first few characters of a word has an 
			// "infinite" cost, unless the word and its correction 
			// are single morphme words
			//
			unitCost = INFINITE;
		}
				
		_cost = costAsCharacterDoubling(nn, diff, unitCost);			
		
		if (_cost == null) {
			// No "special" costing was applied. Use
			// a "generic" costing based on number of characters 
			// affected.
			//
			_cost = unitCost * transf.numAffectedTokens();
		}
		
		return _cost;
	}
	
	private Double costAsCharacterDoubling(int transfNum, 
				DiffResult diff, Double unitCost) {
		
		Double cost = null;
		
		StringTransformation transf = diff.transformations.get(transfNum);
		
		SpellTracer.trace("IUDiffCosting.costNthTransformation", 
				"Costing transf #"+transfNum+": "+transf.origStr()+"-->"+transf.revStr(),
				diff.origStr(), diff.revStr());
		
		if (transf.origTokens.length <= 3 && 
				transf.revisedTokens.length <= 3) {
			SpellTracer.trace("IUDiffCosting.costAsCharacterDoubling", 
					"Transformation affects only one char at one end",
					diff.origStr(), diff.revStr());
			
			String[] charDoublingResult = isCharDoubling2ways(transf);
			if (charDoublingResult != null) {
				// Transformation included a character doubling on 
				// either sides.
				//
				// Cost this in a special way.
				//
				
				// Character doubling encurs a "tiny" cost
				cost = TINY_COST;
				
				// Character changes other than the doubling 
				// encur the received unitCost (this may be INFINITE if
				// the transformation affects 1st morpheme
				//
				int totalXtraChars = 
						charDoublingResult[0].length() +
						charDoublingResult[1].length();
				
				cost += unitCost * totalXtraChars;
			}
		}
		
		return cost;
	}	
	
	/**
	 * Check if a character from origStr has been doubled to 
	 * yield revStr (also with a possible single char added or deleted 
	 * at the start or end).
	 */
	protected String[] isCharDoubling(String origStr, String revStr) {
		String[] answer = null;
		
//		System.out.println("** IUDiffCosting.isCharDoubling: origStr="+origStr+", revStr="+revStr);		
		
		Pattern patt = Pattern.compile("(.)(\\1){0,1}");
		
		{
			// Check if a character from Orig has been doubled 
			// in Rev
			//
			Matcher matcher = patt.matcher(revStr);
//			System.out.println("   ** IUDiffCosting.isCharDoubling: matching revStr against patt='"+patt+"'");
			
			if (matcher.find() && matcher.group(2) != null) {
				//
				// revStr contains a double character.
				//
//				System.out.println("   ** IUDiffCosting.isCharDoubling: revStr contains a doubled character");				
//				System.out.println("   ** IUDiffCosting.isCharDoubling: matcher.groups(x): 0='"+matcher.group(0)+"', 1='"+matcher.group(1)+"'"+"', 2='"+matcher.group(2)+"'");
//				System.out.println("   ** IUDiffCosting.isCharDoubling: matcher.start/end(0): '"+matcher.start(0)+"', '"+matcher.end(0)+"'");
//				System.out.println("   ** IUDiffCosting.isCharDoubling: matcher.start/end(1): '"+matcher.start(1)+"', '"+matcher.end(1)+"'");
//				System.out.println("   ** IUDiffCosting.isCharDoubling: matcher.start/end(2): '"+matcher.start(2)+"', '"+matcher.end(2)+"'");				
				
				String doubledChar = matcher.group(1);
				String prefix = revStr.substring(0, matcher.start());
				String suffix = "";
				if (matcher.end(2) > 0) {
					suffix = revStr.substring(matcher.end(2));
				}
				
//				System.out.println("   ** IUDiffCosting.isCharDoubling: p/c/s="+prefix+"/"+doubledChar+"/"+suffix);
				
				String revXtraChar = prefix+suffix;
//				System.out.println("   ** IUDiffCosting.isCharDoubling: revXtraChar='"+revXtraChar+"'");				
				
				String notDoubledChar = "([^"+doubledChar+"]?)";
				String regexp = notDoubledChar+doubledChar+"{1,2}"+notDoubledChar;
//				System.out.println("   ** IUDiffCosting.isCharDoubling: matching origStr against regexp="+regexp);				
				matcher = Pattern.compile(regexp).matcher(origStr);
				
				if (matcher.matches()) {
//					System.out.println("   ** IUDiffCosting.isCharDoubling: origStr matched regexp");
//					System.out.println("   ** IUDiffCosting.isCharDoubling: matcher.groups(x): 0='"+matcher.group(0)+"', 1='"+matcher.group(1)+"'"+"', 2='"+matcher.group(2)+"'");
					String origXtraChar = matcher.group(1)+matcher.group(2);
//					System.out.println("   ** IUDiffCosting.isCharDoubling: origXtraChar='"+origXtraChar+"'");					
					answer = new String[] {origXtraChar, revXtraChar};
				} else {
//					System.out.println("   ** IUDiffCosting.isCharDoubling: origStr did NOT match regexp");					
				}
			} else {
//				System.out.println("   ** IUDiffCosting.isCharDoubling: revStr did NOT contain a doubled character");
			}
		}
		
//		System.out.print("   ** IUDiffCosting.isCharDoubling: returning answer=");
//		if (answer == null) {
//			System.out.println("null");
//		} else {
//			System.out.println("["+String.join(",", answer)+"]");
//		}
		
		return answer;
	}
	
	protected String[] isCharDoubling2ways(StringTransformation transf) {
		
		String[] answer = null;
		
		if (transf.origTokens.length <= 3 && 
				transf.revisedTokens.length <= 3) {
			SpellTracer.trace("IUDiffCosting.isCharDoubling", 
					"Transformation affects only one char at one end",
					transf.origStr(), transf.revStr());
			
			String origStr = String.join("", transf.origTokens);
			String revStr = String.join("", transf.revisedTokens);
			
			answer = isCharDoubling(origStr, revStr);
			if (answer == null) {
				String[] answerOtherDir = isCharDoubling(revStr, origStr);
				if (answerOtherDir != null) {
					answer = new String[] {answerOtherDir[1], answerOtherDir[0]};
				}
			}
		}
		
		return answer;
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
			_cost = costSpecialCase_aanniaq(diff);
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
			
		}
		
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
}
