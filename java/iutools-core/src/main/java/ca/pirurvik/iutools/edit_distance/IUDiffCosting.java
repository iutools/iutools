package ca.pirurvik.iutools.edit_distance;

import java.util.List;
import java.util.regex.Pattern;

import ca.nrc.string.diff.DiffCosting_Default;
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
public class IUDiffCosting extends DiffCosting_Default {

	public double cost(DiffResult diff) {
		String[] chars1 = diff.origTokens;
		String[] chars2 = diff.revTokens;
		List<StringTransformation> transf = diff.transformations;
		
		Double _cost = null;
		
		_cost = costLeadingCharChanges(diff);
			
		if (_cost == null) {
			_cost = super.cost(diff);		
		}
		
		return _cost.doubleValue();
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
	private Double costLeadingCharChanges(DiffResult diffRes) {
		Double _cost = null;
		List<StringTransformation> diff = diffRes.transformations;
		if (!diff.isEmpty()) {
			StringTransformation firstTransf = diff.get(0);
			if (firstTransf.origTokenPos <= 2 ||
					firstTransf.revisedTokenPos <= 2) {
				
				// In general, it is not allowed to change the first 
				// three chars of, but there can be exceptions...
				_cost = costAllowableLeadingCharChanges(diffRes);
				
				if (_cost == null) {
					// This is not one of those exceptions, so return
					// an infinite cost.
					_cost = INFINITE;
				}
				diff.remove(0);
			}
		}
		
		return _cost;
	}

	private Double costAllowableLeadingCharChanges(DiffResult diffRes) {
		Double cost = null;
		
		String origStr = diffRes.origStr();
		String revStr = diffRes.revStr();
		
		Pattern anniaq = Pattern.compile("^an+iaq[\\s\\S]*$");
		Pattern nniaq = Pattern.compile("^n+iaq[\\s\\S]*$");
		if (anniaq.matcher(origStr).matches() && 
				nniaq.matcher(revStr).matches()) {
			cost = 0.1;
		} else if (anniaq.matcher(revStr).matches() && 
				nniaq.matcher(origStr).matches()) {
			cost = 0.1;
		}
		
		return cost;
	}
}
