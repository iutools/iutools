/*
 * Conseil national de recherche Canada 2004/ National Research Council Canada
 * 2004
 * 
 * Cr�� le / Created on 27-Aug-2004 par / by Benoit Farley
 *  
 */
package org.iutools.linguisticdata;

import org.apache.log4j.Logger;

import org.iutools.linguisticdata.constraints.Conditions;

/*
 * This class represents a surface form of a morpheme, which will constiture
 * the "dictionary" of forms for the new morphological analyzer.
 * 
 * For roots, the forms will be the basic forms with the end consonant deleted
 * or replaced with all possible consonants that could happen due to the actions
 * of a following affix. For example, for the verb root malik, we will have the
 * forms malik, mali (deletion), malig (voicing), maling (nasalization). To those
 * will be added forms where the end consonant sees itself assimilated by the
 * consonant of the next affix. So for example, malik will produce malit; malig
 * will produce maliv; maling will produce malin, malim...
 *
 * For roots, the attributes endOfStem and context have null values since a root
 * cannot be preceded by a stem, obviously.
 * 
 * For affixes, we have the same as for roots, but additionally, for each of those
 * forms with a different ending, we will also have variations due to some actions
 * of the affix. For example, the NN suffix arjuk will have the variants raarjuk and
 * gaarjuk.
 *
 * 
 *  surfaceForm – string that represents the surface form of the morpheme as a result
 *                of what is described above
 *  endOfStem – string that represents the end of the stems to which this form may attach:
 *                          - null: no condition on the end of the stem
 *                          - a: the stem must end with a single 'a'
 *                          - i: the stem must end with a single 'i'
 *                          - u: the stem must end with a single 'u'
 *                          - V: the stem must end with a vowel
 *                          - 1V: the stem must end with any single vowel
 *                          - 2V: the stem must end with 2 vowels
 *                          - C: the stem must end with a consonant
 *                          - lower-case consonant: the stem must end with a specific consonant
 *
 *  context – string that represents the final of the stem to which this form may attach
 *            (last character of the canonical form of the morphemes):
 *                          - V, t, k, q
 *  morphemeId – id of the morpheme in the data base for this form
 *  
 * The context hence
 * becomes a constraint (a condition) to be met by the stem.
 * 
 * Example: affix 'arjuk/1nn'
 * after vowel : no action
 * after 2 vowels : insert 'ra'
 * after t : delete 't'
 * after t deleted : if 2 vowels, insert 'ra'
 * after k : delete 'k'
 * after k deleted : if 2 vowels, insert 'ra' or 'ga'
 * after q : delete 'q'
 * after q deleted : if 2 vowels, insert 'ra'
 * 
 * One should have those SurfaceFormInContext objects:
 * arjuk   / 1V  / V / arjuk/1nn : the form 'arjuk' may appear after a stem ending naturally with a single vowel 
 * raarjuk / 2V / V / arjuk/1nn : the form 'raarjuk' may appear after a stem ending naturally with 2 vowels
 * arjuk   / V  / t / arjuk/1nn : the form 'arjuk' may appear after a stem ending in a single vowel with a deleted 't'
 * raarjuk / VV / t / arjuk/1nn : the form 'raarjuk' may appear after a stem ending with 2 vowels with a deleted 't'
 * etc.
 */

public class SurfaceFormInContext extends Object {

    public String surfaceForm;
    public String endOfStem;
    public Character context;
    public String morphemeId;

    private String canonicalForm = null;
    private Boolean surfaceFinalIsDifferentThanCanonical = null;

    public SurfaceFormInContext(String _surfaceForm, String _endOfStem, Character _context, String _morphemeId) {
    	Logger logger = Logger.getLogger("SurfaceFormInContext.constructor");
//    	if (_morphemeId.equals("tikiq/1n") || _morphemeId.equals("patiq/1v") || _morphemeId.equals("jarniq/1vv"))
//    		logger.debug(_morphemeId+"; "+_surfaceForm+"; "+_endOfStem+"; "+String.valueOf(_context));
        this.surfaceForm = _surfaceForm;
        this.endOfStem = _endOfStem;
        this.context = _context;
        this.morphemeId = _morphemeId;
    }

    public String canonicalForm() {
    	if (this.canonicalForm != null)
    		return this.canonicalForm;
    	if (morphemeId != "") {
			String[] partsOfMorphemeId = this.morphemeId.split("/");
			this.canonicalForm = partsOfMorphemeId[0];
			return this.canonicalForm;
		} else {
    		return "";
		}
	}

	/*
	 	For both roots and affixes, for forms with a final different than the final of
	 	the canonical form, one needs a special attribute to indicate that there has to be
	 	something following, because any form with a different final means it is a final
	 	due to the action of a following affix.
	 */
	public boolean finalIsDifferentThanCanonical() {
		Logger logger = Logger.getLogger("SurfaceFormInContext.finalIsDifferentThanCanonical");
		logger.debug("this.surfaceFinalIsDifferentThanCanonical = "+this.surfaceFinalIsDifferentThanCanonical);
    	if (this.surfaceFinalIsDifferentThanCanonical == null) {
			char finalOfSurfaceForm = surfaceForm.charAt(surfaceForm.length() - 1);
			logger.debug("finalOfSurfaceForm of "+surfaceForm+"= "+finalOfSurfaceForm);
			String canonicalForm = canonicalForm();
			char finalOfCanonicalForm = canonicalForm.charAt(canonicalForm.length() - 1);
			logger.debug("finalOfCanonicalForm of "+canonicalForm+"= "+finalOfCanonicalForm);
			this.surfaceFinalIsDifferentThanCanonical = finalOfSurfaceForm != finalOfCanonicalForm;
		}
		logger.debug("this.surfaceFinalIsDifferentThanCanonical = "+this.surfaceFinalIsDifferentThanCanonical);

		return this.surfaceFinalIsDifferentThanCanonical;
	}
    
    public boolean isValidForStem(String stem) {
		String lastChar = stem.substring(stem.length()-1);
		if (endOfStem ==null) {
			return true;
		} else if (endOfStem.equals("V")) {
    		if (lastChar.equals("i") || lastChar.equals("u") || lastChar.equals("a"))
    			return true;
    		else
    			return false;
    	} else if (endOfStem.equals("VV")) {
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
    		logger.debug(oo.endOfStem +" vs "+this.endOfStem);
    		if ( !oo.endOfStem.equals(this.endOfStem) )
    			return false;
    		logger.debug(oo.context +" vs "+this.context);
//    		if ( !oo.context.equals(this.context) )
//    			return false;
			if ( oo.context==null && this.context==null )
				return true;
			if ( (oo.context==null && this.context!=null)
					|| (oo.context!=null && this.context==null)
					|| !oo.context.equals(this.context) )
				return false;
    		return true;
    	}
    	return false;
    }
    
    @Override
    public int hashCode() {
        return 1;
    }

	/**
	 * Check whether this type of morpheme may follow the preceding morpheme.
	 *
	 * @param precedingMorpheme The SurfaceFormInContext representation of the morpheme that precedes this one.
	 * @return a boolean
	 */
	public boolean validateAssociativityWithPrecedingMorpheme(SurfaceFormInContext precedingMorpheme) {
		boolean res = false;
		Logger logger = Logger.getLogger("SurfaceFormInContext.validateAssociativityWithPrecedingMorpheme");
		return res;
	}

//	public boolean validateAssociativityWithPrecedingMorpheme(SurfaceFormInContext precedingMorpheme) {
//		boolean res;
//		Logger logger = Logger.getLogger("SurfaceFormInContext.validateAssociativityWithPrecedingMorpheme");
//		String morphemeIdPrecedingMorpheme = precedingMorpheme.morphemeId;
//		String[] morphemeIdPrecedingMorphemeParts = morphemeIdPrecedingMorpheme.split("/");
//		String idPrecedingMorpheme = morphemeIdPrecedingMorphemeParts[1];
//		String morphemeIdThis = morphemeId;
//		String[] morphemeIdThisParts = morphemeIdThis.split("/");
//		String idThis = morphemeIdThisParts[1];
//		logger.debug("prec: "+idPrecedingMorpheme+"; this: "+idThis);
//		if (idThis.matches("^\\d+q$")) {
//			// queue
//			res = true;
//			logger.debug("queue");
//		} else if (idThis.matches("^\\d+n.+") ||               // NN or NV suffix
//				idThis.matches("^tn.+")) {                     // noun ending
//			// noun suffix or noun ending
//			logger.debug("this: nX suffix or noun ending");
//			if (idPrecedingMorpheme.matches("^\\d+n$") ||       // noun root
//					idPrecedingMorpheme.matches("^\\d+.n$") ||  // NN or VN suffix
//					idPrecedingMorpheme.matches("^\\d+pr?$")    // pronoun
//			) {
//				res = true;
//				logger.debug("prec: Xn suffix or noun root");
//			} else {
//				res = false;
//			}
//		} else if (idThis.matches("^\\d+v.+") ||
//				idThis.matches("^tv.+")) {
//			// verb suffix or verb ending
//			logger.debug("this: vX suffix or verb ending");
//			if (idPrecedingMorpheme.matches("^\\d+v$") ||
//					idPrecedingMorpheme.matches("^\\d+.v$")) {
//				res = true;
//				logger.debug("prec: Xv suffix or verb root");
//			} else {
//				res = false;
//			}
//		} else
//			res = false;
//
//		logger.debug("res= "+res);
//		return res;
//	}


	public boolean validateWithStem(SurfaceFormInContext precedingMorpheme) {
    	Logger logger = Logger.getLogger("SurfaceFormInContext.validateWithStem");
		char finalOfPrecedingMorpheme =
			precedingMorpheme.canonicalForm().substring(precedingMorpheme.canonicalForm().length()-1).charAt(0);
		if (this.context=='V') {
			if (finalOfPrecedingMorpheme!='i' && finalOfPrecedingMorpheme!='u' && finalOfPrecedingMorpheme!='a')
				return false;
		} else if (this.context=='C') {
			if (finalOfPrecedingMorpheme == 'i' || finalOfPrecedingMorpheme == 'u' || finalOfPrecedingMorpheme == 'a')
				return false;
		} else if (finalOfPrecedingMorpheme!=this.context)
			return false;
		String stem = precedingMorpheme.surfaceForm;
		String lastCharOfStem = stem.substring(stem.length()-1);
		if (endOfStem.equals("V"))
			if (lastCharOfStem.equals("i") || lastCharOfStem.equals("u") || lastCharOfStem.equals("a"))
				return true;
			else
				return false;
		else if (endOfStem.equals("C"))
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


	/**
	 * Check whether the constraining conditions imposed by this morpheme and the preceding morpheme are met.
	 *
	 * @param precedingMorpheme - The SurfaceFormInContext representation of the morpheme that precedes this one.
	 * @return a boolean
	 * @throws LinguisticDataException
	 */
	public boolean validateConstraints(SurfaceFormInContext precedingMorpheme) throws LinguisticDataException {
		Logger logger = Logger.getLogger("SurfaceFormInContext.validateConstraints");
		logger.debug("precedingMorpheme.morphemeId: "+precedingMorpheme.morphemeId);
		Morpheme prec = LinguisticData.getInstance().getMorpheme(precedingMorpheme.morphemeId);
		Morpheme cur = LinguisticData.getInstance().getMorpheme(this.morphemeId);
		Conditions condsOnPrecedingMorpheme = null;
		Conditions condsOnCurrentMorpheme = null;
		try {
			condsOnPrecedingMorpheme = cur.getPrecCond();
			condsOnCurrentMorpheme = prec.getNextCond();
		} catch (NullPointerException e) {
			System.err.println("NullPointerException for "+this.morphemeId);
		}
		boolean res = prec.meetsConditions(condsOnPrecedingMorpheme);
		if (res)
			res = cur.meetsConditions(condsOnCurrentMorpheme);
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
	
	@Override
	public String toString() {
		return "SurfaceFormInContext["+
				surfaceForm+"; "+endOfStem+"; "+context+"; "+morphemeId+"]";
	}

	public boolean isZeroLength() {
		return false;
	}

}

