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
import ca.nrc.json.PrettyPrinter;

/*
 * This class represents a surface form of a morpheme (root, suffix, ending) 
 * in a given context:
 * 
 *  form – string that represents the surface form of the morpheme resulting
 *         from an affix's actions
 *  constraintOnEndOfStem – string that represents the end of stems to which
 *                          this form may attach (see below)
 *                          - null: no condition on the end of the stem
 *                          - V: the stem must end with a single vowel
 *                          - VV: the stem must end with 2 vowels
 *                          - C: the stem must end with a consonant
 *
 *  endOfCanonicalFormOfReceivingMorpheme – string that represents the final
 *                                          character of the basic form of the
 *                                          morphemes to which this form may attach
 *                                          - V, t, k, q
 *  morphemeId – id of the morpheme in the data base
 *  
 * The context hence
 * becomes a constraint (a condition) to be met by the stem.
 * 
 * Example: affix 'aluk/
 * 
 */

public class SurfaceFormInContext extends Object {

    public String surfaceForm;
    public String constraintOnEndOfStem;
    public String endOfCanonicalFormOfReceivingMorpheme;
    public String morphemeId;
    public String basicForm;
    
	@Override
	public String toString() {
		return "SurfaceFormInContext["+
				surfaceForm+"; "+morphemeId+"; "+basicForm+"; "+constraintOnEndOfStem+"; "+endOfCanonicalFormOfReceivingMorpheme+"]";
	}


    public SurfaceFormInContext(String form, String _constraintOnStem, String _contextualContraintOnReceivingMorpheme, String _morphemeId) {
    	Logger logger = Logger.getLogger("SurfaceFormInContext.constructor");
    	if (_morphemeId.equals("tikiq/1n") || _morphemeId.equals("patiq/1v") || _morphemeId.equals("jarniq/1vv"))
    		logger.debug(_morphemeId+"; "+form+"; "+_constraintOnStem+"; "+String.valueOf(_contextualContraintOnReceivingMorpheme)); 
        this.surfaceForm = form;
        this.constraintOnEndOfStem = _constraintOnStem;
        this.endOfCanonicalFormOfReceivingMorpheme = _contextualContraintOnReceivingMorpheme;
        this.morphemeId = _morphemeId;
        String[] partsOfMorphemeId = this.morphemeId.split("/");
        this.basicForm = partsOfMorphemeId[0];
    }
    
    public boolean isValidForStem(String stem) {
		String lastChar = stem.substring(stem.length()-1);
		if (constraintOnEndOfStem==null) {
			return true;
		} else if (constraintOnEndOfStem.equals("V")) {
    		if (lastChar.equals("i") || lastChar.equals("u") || lastChar.equals("a"))
    			return true;
    		else
    			return false;
    	} else if (constraintOnEndOfStem.equals("VV")) {
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
    		logger.debug(oo.surfaceForm+" vs "+this.surfaceForm);
    		if ( !oo.surfaceForm.equals(this.surfaceForm) ) {
    			return false;
    		}
    		logger.debug(oo.morphemeId+" vs "+this.morphemeId);
    		if ( !oo.morphemeId.equals(this.morphemeId) ) {
    			return false;
    		}
    		logger.debug(oo.constraintOnEndOfStem+" vs "+this.constraintOnEndOfStem);
    		if ( !oo.constraintOnEndOfStem.equals(this.constraintOnEndOfStem) )
    			return false;
    		logger.debug(oo.endOfCanonicalFormOfReceivingMorpheme+" vs "+this.endOfCanonicalFormOfReceivingMorpheme);
    		if ( !oo.endOfCanonicalFormOfReceivingMorpheme.equals(this.endOfCanonicalFormOfReceivingMorpheme) )
    			return false;
    		return true;
    	}
    	return false;
    }
    
    @Override
    public int hashCode() {
        return 1;
    }

	public boolean validateWithStem(SurfaceFormInContext precedingMorpheme) {
    	Logger logger = Logger.getLogger("SurfaceFormInContext.validateWithStem");
    	logger.debug("--------------------\nprecedingMorpheme: "+PrettyPrinter.print(precedingMorpheme));
    	logger.debug("this: "+PrettyPrinter.print(this));
		char finalOfPrecedingMorpheme =
			precedingMorpheme.basicForm.substring(precedingMorpheme.basicForm.length()-1).charAt(0);
		if (this.endOfCanonicalFormOfReceivingMorpheme.equals("V")) {
			if (finalOfPrecedingMorpheme!='i' && finalOfPrecedingMorpheme!='u' && finalOfPrecedingMorpheme!='a')
				return false;
		} else if (finalOfPrecedingMorpheme!=this.endOfCanonicalFormOfReceivingMorpheme.charAt(0))
			return false;
		String stem = precedingMorpheme.surfaceForm;
		String lastCharOfStem = stem.substring(stem.length()-1);
		if (constraintOnEndOfStem.equals("V"))
			if (lastCharOfStem.equals("i") || lastCharOfStem.equals("u") || lastCharOfStem.equals("a"))
				return true;
			else
				return false;
		else if (constraintOnEndOfStem.equals("C"))
			if (lastCharOfStem.equals("i") || lastCharOfStem.equals("u") || lastCharOfStem.equals("a"))
				return false;
			else
				return true;
		else { // "VV"
			if (stem.length()<2) 
				return true;
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
			// queue
			res = true;
			logger.debug("queue");
		} else if (idThis.matches("^\\d+n.+") || 
				idThis.matches("^tn.+")) {
			// noun suffix or noun ending
			logger.debug("this: nX suffix or noun ending");
			if (idPrecedingMorpheme.matches("^\\d+n$") ||
					idPrecedingMorpheme.matches("^\\d+.n$") ||
					idPrecedingMorpheme.matches("^\\d+pr?$")
					) {
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

	public boolean validateConstraints(SurfaceFormInContext precedingMorpheme) throws LinguisticDataException {
		Logger logger = Logger.getLogger("SurfaceFormInContext.validateConstraints");
		logger.debug("precedingMorpheme.morphemeId: "+precedingMorpheme.morphemeId);
		Morpheme prec = LinguisticData.getInstance().getMorpheme(precedingMorpheme.morphemeId);
		Morpheme cur = LinguisticData.getInstance().getMorpheme(this.morphemeId);
		Conditions conds = null;
		try {
			conds = cur.getPrecCond();
		} catch (NullPointerException e) {
			System.err.println("NullPointerException for "+this.morphemeId);
		}
		boolean res = prec.meetsConditions(conds);
		return res;
	}

	public boolean validateFinal() {
		boolean res;
		Logger logger = Logger.getLogger("SurfaceFormInContext.validateFinal");
		String morphemeIdThis = morphemeId;
		logger.debug("morphemeId: "+morphemeId);
		String[] morphemeIdThisParts = morphemeIdThis.split("/");
		String idThis = morphemeIdThisParts[1];
		if (idThis.matches("^\\d+q$")) {
			res = true;
			logger.debug("queue");
		} else if (idThis.matches("^\\d+n$") || 
				idThis.matches("^tn.+")) {
			logger.debug("this: nX suffix or noun ending");
			res = true;
		} else	
			res = false;
		
		logger.debug("res= "+res);
		return res;
	}
	

    
}