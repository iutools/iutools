/*
 * Conseil national de recherche Canada 2004/ National Research Council Canada
 * 2004
 * 
 * Cr�� le / Created on 27-Aug-2004 par / by Benoit Farley
 *  
 */
package ca.inuktitutcomputing.data;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.data.constraints.Conditions;

/*
 * This class represents a surface form of an affix (suffix, ending) in a given
 * context (end of the stem to which it may be attached). The context hence
 * becomes a constraint (a condition) to be met by the stem.
 * 
 * The context can be:
 *   - null: no condition on the end of the stem
 *   - V: the stem must end with a single vowel
 *   - VV: the stem must end with 2 vowels
 *   
 */

public class SurfaceFormInContext extends Object {

    public String form;
    public String contextualConstraintOnStem;
    public String morphemeId;

    public SurfaceFormInContext(String form, String _constraintOnStem, String _morphemeId) {
        this.form = form;
        this.contextualConstraintOnStem = _constraintOnStem;
        this.morphemeId = _morphemeId;
    }
    
    public boolean isValidForStem(String stem) {
		String lastChar = stem.substring(stem.length()-1);
		if (contextualConstraintOnStem==null) {
			return true;
		} else if (contextualConstraintOnStem.equals("V")) {
    		if (lastChar.equals("i") || lastChar.equals("u") || lastChar.equals("a"))
    			return true;
    		else
    			return false;
    	} else if (contextualConstraintOnStem.equals("VV")) {
    		String penultChar = stem.substring(stem.length()-2,stem.length()-1);
    		if ( (lastChar.equals("i") || lastChar.equals("u") || lastChar.equals("a")) &&
    				(penultChar.equals("i") || penultChar.equals("u") || penultChar.equals("a")) )
    			return true;
    		else
    			return false;
    	} else if (lastChar.equals("i") || lastChar.equals("u") || lastChar.equals("a")) {
    		return false;
    	} else {
    		return true;
    	}
    }
    
    @Override
    public boolean equals(Object o) {
    	Logger logger = Logger.getLogger("SurfaceFormInContext.equals");
    	logger.debug("XXX");
    	if (o instanceof SurfaceFormInContext) {
    		SurfaceFormInContext oo = (SurfaceFormInContext)o;
    		logger.debug(oo.form+" vs "+this.form);
    		if ( !oo.form.equals(this.form) ) {
    			return false;
    		}
    		logger.debug(oo.morphemeId+" vs "+this.morphemeId);
    		if ( !oo.morphemeId.equals(this.morphemeId) ) {
    			return false;
    		}
    		logger.debug(oo.contextualConstraintOnStem+" vs "+this.contextualConstraintOnStem);
    		if ( !oo.contextualConstraintOnStem.equals(this.contextualConstraintOnStem) )
    			return false;
    		return true;
    	}
    	return false;
    }
    
    @Override
    public int hashCode() {
        return 1;
    }

	public boolean validateWithStem(String stem) {
		String lastCharOfStem = stem.substring(stem.length()-1);
		if (contextualConstraintOnStem.equals("V"))
			if (lastCharOfStem.equals("i") || lastCharOfStem.equals("u") || lastCharOfStem.equals("a"))
				return true;
			else
				return false;
		else if (contextualConstraintOnStem.equals("C"))
			if (lastCharOfStem.equals("i") || lastCharOfStem.equals("u") || lastCharOfStem.equals("a"))
				return false;
			else
				return true;
		else { // "VV"
			String penultCharOfStem = stem.substring(stem.length()-2,stem.length()-1);
			if ( (lastCharOfStem.equals("i") || lastCharOfStem.equals("u") || lastCharOfStem.equals("a")) &&
					(penultCharOfStem.equals("i") || penultCharOfStem.equals("u") || penultCharOfStem.equals("a")) )
				return true;
			else
				return false;
		}
	}

	public boolean validateWithPrecedingMorpheme(SurfaceFormInContext precedingMorpheme) {
		boolean res;
		Logger logger = Logger.getLogger("SurfaceFormInContext.validateWithPrecedingMorpheme");
		String morphemeIdPrecedingMorpheme = precedingMorpheme.morphemeId;
		String[] morphemeIdPrecedingMorphemeParts = morphemeIdPrecedingMorpheme.split("/");
		String idPrecedingMorpheme = morphemeIdPrecedingMorphemeParts[1];
		String morphemeIdThis = morphemeId;
		String[] morphemeIdThisParts = morphemeIdThis.split("/");
		String idThis = morphemeIdThisParts[1];
		logger.debug("prec: "+idPrecedingMorpheme+"; this: "+idThis);
		if (idThis.matches("^\\d+q$")) {
			res = true;
			logger.debug("queue");
		} else if (idThis.matches("^\\d+n.+") || 
				idThis.matches("^tn.+")) {
			logger.debug("this: nX suffix or noun ending");
			if (idPrecedingMorpheme.matches("^\\d+n$") ||
					idPrecedingMorpheme.matches("^\\d+.n$")) {
				res = true;
				logger.debug("prec: Xn suffix or noun root");
			} else {
				res = false;
			}
		} else if (idThis.matches("^\\d+v.+") || 
				idThis.matches("^tv.+")) {
			logger.debug("this: vX suffix or verb ending");
			if (idPrecedingMorpheme.matches("^\\d+v$") ||
					idPrecedingMorpheme.matches("^\\d+.v$")) {
				res = true;
				logger.debug("prec: Xv suffix or verb root");
			} else {
				res = false;
			}
		} else	
			res = false;
		
		logger.debug("res= "+res);
		return res;
	}

	public boolean valideConstraints(SurfaceFormInContext precedingMorpheme) {
		Morpheme prec = LinguisticDataAbstract.getMorpheme(precedingMorpheme.morphemeId);
		Morpheme cur = LinguisticDataAbstract.getMorpheme(this.morphemeId);
		Conditions conds = cur.getPrecCond();
		boolean res = prec.meetsConditions(conds);
		return true;
	}

    
}