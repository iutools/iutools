//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//
// -----------------------------------------------------------------------
//           (c) Conseil national de recherches Canada, 2002
//           (c) National Research Council of Canada, 2002
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
// Document/File:		Morpheme.java
//
// Type/File type:		code Java / Java code
// 
// Auteur/Author:		Benoit Farley
//
// Organisation/Organization:	Conseil national de recherches du Canada/
//				National Research Council Canada
//
// Date de cr�ation/Date of creation:	
//
// Description: Classe Morpheme, englobant les sous-classes:
//                               Affix (Suffix et TerminaisonVerbale)
//                               Base
//
// -----------------------------------------------------------------------


package org.iutools.linguisticdata;

import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.iutools.morph.AffixPartOfComposition;
import org.iutools.linguisticdata.constraints.Conditions;

public abstract class Morpheme implements Cloneable {

	public static enum MorphFormat {WITH_BRACES, NO_BRACES};

	public String id = null;
	public String type = null;
	public String morpheme;
	public String englishMeaning = null;
	public String frenchMeaning = null;
	public String nb = null;
	String [] sources = null;
	Integer num = null;
	String dbName = null;
	String tableName = null;
	Morpheme.Id idObj = null;
	String dialect = null;
	String combinedMorphemes[] = null;

    String cf = null; // référence à d'autres morphèmes
    String[] cfs = null; // tableau de références à d'autres morphèmes

    Conditions preCondition = null;
    Conditions nextCondition = null;

    private HashMap<String,Object> attributes = null;

	//------------------------------------------------------------------------------------------------------------
	abstract boolean agreeWithTransitivity(String trans); //
    public abstract String showData() throws LinguisticDataException;
    abstract void setAttrs();

    //------------------------------------------------------------------------------------------------------------
	public abstract String getSignature(); //
	public abstract String getOriginalMorpheme(); //
	
    //------------------------------------------------------------------------------------------------------------
	public String getTableName() {
	    return tableName;
	}

	public String[] getCombiningParts() {
	    return combinedMorphemes;
	}

    public Morpheme copyOf() throws CloneNotSupportedException  {
        return (Morpheme)this.clone();
    }

    public Conditions getPrecCond() {
        return preCondition;
    }
    
    public Conditions getNextCond() {
        return nextCondition;
    }
    
    public boolean meetsTransitivityCondition(String transitivity) {
        /*
         * Vérifier la valeur de transitivité du candidat actuel. Les
         * sufffixes-noms et les racines autres que verbales ont une
         * valeur de transitivité nulle. La valeur de transitivité des
         * suffixes-verbes et des racines verbales est indiquée dans le
         * champ 'transitivite'.
         */
        boolean res;
        if (agreeWithTransitivity(transitivity))
            res = true;
        else
            res = false;
        return res;
    }
    
    
    public boolean meetsConditions (Conditions conds) throws LinguisticDataException {
        boolean res = true;
        /*
         * Il faut que les conditions spécifiques soient rencontrées. Par
         * exemple, si le morphème trouvé précédemment exige de suivre
         * immédiatement un nom au cas datif, le suffixe ou la terminaison
         * actuelle doit rencontrer cette contrainte.
         */
        if (conds != null) {
            res = conds.isMetBy(this);
        }
        
        return res;
    }
  
    public boolean meetsConditions (Conditions conds, Vector<AffixPartOfComposition> followingMorphemes) throws LinguisticDataException {
    	Logger logger = Logger.getLogger("Morpheme.meetsConditions");
//    	String[] idsOfFollowingMorphemes = new String[followingMorphemes.size()];
//    	for (int iv=0; iv<followingMorphemes.size(); iv++)
//    		idsOfFollowingMorphemes[iv] = followingMorphemes.get(iv).getAffix().id;
//    	logger.debug(String.join("; ", idsOfFollowingMorphemes));
        boolean res = true;
        /*
         * Il faut que les conditions spécifiques soient rencontrées. Par
         * exemple, si le morphème trouvé précédemment exige de suivre
         * immédiatement un nom au cas datif, le suffixe ou la terminaison
         * actuelle doit rencontrer cette contrainte.
         */
        if (conds != null) {
            res = conds.isMetBy(this);
        }
        /*
         * Le suffixe peut aussi avoir des contraintes sur ce qui doit le suivre
         * immédiatement. Vérifier ces contraintes.
         */
        if (res) {
            if (getNextCond() != null) {
                // Morphème suivant, i.e. le dernier morphème trouvé.
                if (followingMorphemes.size() != 0) {
                    Affix affPrec = followingMorphemes.elementAt(0).getAffix();
                    res = getNextCond().isMetBy(affPrec);
                }
            }
        }
    return res;
	}

    public Morpheme getLastCombiningMorpheme() throws LinguisticDataException {
        String [] parts = getCombiningParts();
        String lastPart = null;
        Morpheme lastMorpheme = null;
        if (parts!=null) {
            lastPart = parts[parts.length-1];
            if (!lastPart.equals("?")) {
                lastMorpheme = getMorpheme(lastPart);
            }
        }
        return lastMorpheme;
    }
    
    public static Morpheme getMorpheme(String morphemeId) throws LinguisticDataException {
    	    // Look for the morpheme in the affixes
    	    Morpheme morph = (Morpheme)LinguisticData.getInstance().getAffixWithId(morphemeId);
    	    // If not found, look for the morpheme in the roots
    	    if (morph == null)
				 morph = (Morpheme) LinguisticData.getInstance().getBaseWithId(morphemeId);
            return morph;
        }
    
    public boolean attrEqualsValue (String attr, String val, boolean eq) {
    	Logger logger = Logger.getLogger("Morpheme.attrEqualsValue");
    	logger.debug("morpheme's id: "+this.id);
    	logger.debug("attr= "+attr+"; val= "+val);
        boolean res = false;
        String valAttr = getAttr(attr);
        // It is possible that the value of the attribute for the morpheme
        // is not unique.  For example, the value of 'intransinfix' (field
        // 'suffixe-intans') of the verbal root 'quviak' is 
        // "gusuk/1vv suk/1vv", that is, a dual value.  When the values
        // are compared, one has to take this into account.
        String valAspect = val;
        logger.debug("valAspect= "+valAspect);
        // This is the value that the morpheme's attribute should have
        // to meet the condition.  If that value is the name of an attribute
        // of the morpheme (marked by an initial X), find the value of that attribute.
        if (valAspect.startsWith("X") && attributes.containsKey(valAspect.substring(1))) {
            logger.debug("valAspect "+valAspect+" contained in attributes");
            valAspect = getAttr(valAspect.substring(1));
        }
        logger.debug("valAspect= "+valAspect);
        // Check for the morpheme
        if (valAspect==null || valAspect.equals("null"))
            if (eq)
                res = (valAttr==null);
            else
                res = (valAttr!=null);
        else if (valAttr != null) {
            String[] valAttrs;
            valAttrs = valAttr.split(" ");
            if (eq) {
                for (int iValAttrs=0; iValAttrs < valAttrs.length; iValAttrs++)
                    if (res = valAttrs[iValAttrs].equals(valAspect))
                        break;
            }
            else
                for (int iValAttrs=0; iValAttrs < valAttrs.length; iValAttrs++)
                    res = !valAttr.equals(valAspect);
        }
        else
            res = false;
        return res;
    }
    
    //------------------------------------------------------------------------------------------------------------
    String getNb() {
    	return nb;
    }
    
    void setId() {
	    Logger logger = Logger.getLogger("Morpheme.setId");
	    String canonicalForm = getOriginalMorpheme();
	    String signature = getSignature();
        idObj = new Morpheme.Id(canonicalForm,signature);
        id = idObj.id;
        logger.debug(id+" -- "+canonicalForm+"; "+signature+" -> "+id);
		attributes.put("id",id);
    }
    
    String getAttr(String attr) {
        return (String)this.attributes.get(attr);
    }
    
	void setAttributes(HashMap<String,Object> attrs) {
		attributes = new HashMap<String,Object>();
		attributes.putAll(attrs);
		attributes.put("type",type);
		attributes.put("nb", nb);
		attributes.put("morpheme",morpheme);
		attributes.put("englishMeaning",englishMeaning);
		attributes.put("frenchMeaning",frenchMeaning);
		attributes.put("sources",sources);
		attributes.put("num",num);
		attributes.put("dbName",dbName);
		attributes.put("tableName",tableName);
		attributes.put("idObj",idObj);
		attributes.put("dialect",dialect);
		attributes.put("cf",cf);
		attributes.put("cfs",cfs);
		attributes.put("preCondition",preCondition);
		attributes.put("nextCondition",nextCondition);		
		attributes.put("combinedMorphemes",combinedMorphemes);
	}
	
    public static String combine(String combination, boolean withAction2, String highlightedMorpheme) throws LinguisticDataException {
        String combinedForm;
        String[]morphemes = combination.split("\\x2b"); // +
        Morpheme morpheme = LinguisticData.getInstance().getMorpheme(morphemes[0]);
        combinedForm = morpheme.morpheme;
        char context = combinedForm.charAt(combinedForm.length()-1);
        for (int i=1; i<morphemes.length; i++) {
            String precForm = combinedForm;
            Affix aff = (Affix)LinguisticData.getInstance().getMorpheme(morphemes[i]);
            Action[] action1 = aff.getAction1(context);
            Action[] action2 = aff.getAction2(context);
            String formAff = aff.getForm(context)[0];
            Action a2 = null;
            if (withAction2)
                a2 = action2[0];
            combinedForm = action1[0].combine(precForm,formAff,a2);
            context = combinedForm.charAt(combinedForm.length()-1);
            if (highlightedMorpheme!=null && morphemes[i].equals(highlightedMorpheme))
                combinedForm = combinedForm.replaceFirst(formAff,"<span style=\"color:red\">"+formAff+"</span>");
        }
        return combinedForm;
    }
    
    //------------------------------------------------------------------------------------------
    // STATIC CLASS "ID"
    static public class Id {
    	//
	    static public String delimiter = "/";
	    public String morphemeName;
	    public String signature;
	    public String id;
	    //
	    
	    //-------------------------------------------------------------------------
	    public Id(String morphId) {
	        StringTokenizer st = new StringTokenizer(morphId,delimiter);
	        if (st.countTokens()==2) {
	            morphemeName = st.nextToken();
	            signature = st.nextToken();
	            id = morphId;
	        }
	    }
	    
	    public Id(String morphName, String sign) {
	        morphemeName = morphName;
	        signature = sign;
	        id = morphemeName + delimiter + signature;
	    }
	    
	    //-------------------------------------------------------------------------	    
	    public String toHTML() {
	        return morphemeName+"<sup>"+signature+"</sup>";
	    }
        
        static public String toHTML(String morphemeId) {
            String morphName = morphemeId.substring(0,morphemeId.indexOf(delimiter));
            String sign = morphemeId.substring(morphemeId.indexOf(delimiter)+1);
            return morphName+"<sub>"+sign+"</sub>";
        }
	}

	public static boolean hasCanonicalForm(String morpheme, String canonicalForm) {
		boolean answer = 
			morpheme.matches("^\\{?"+canonicalForm+"/.*$");
		return answer;
	}
	
	public static String[] withBraces(String[] morphemes) {
		String[] morphsWithBraces = new String[morphemes.length];
		for (int ii=0; ii < morphemes.length; ii++) {
			morphsWithBraces[ii] = withBraces(morphemes[ii]);
		}
		
		return morphsWithBraces;
	}

	public static String withBraces(String morph) {
		if (!morph.startsWith("{")) {
			morph = "{" + morph;
		}
		if (!morph.endsWith("}")) {
			morph += "}";
		}
		
		return morph;
	}

	public static String[] format(String[] origMorphemes, MorphFormat format) {
	    if (format == null) {
	        format = MorphFormat.WITH_BRACES;
        }
	    String[] formatted = null;
	    if (origMorphemes != null) {
	        formatted = new String[origMorphemes.length];
	        for (int ii=0; ii < origMorphemes.length; ii++) {
	            String aFormatted = origMorphemes[ii];
	            if (format == MorphFormat.NO_BRACES) {
	                aFormatted = aFormatted.replaceAll("[\\{\\}]", "");
                } else {
	                if (!aFormatted.startsWith("{")) {
	                    aFormatted = "{"+aFormatted;
                    }
	                if (!aFormatted.endsWith("}")) {
	                    aFormatted = aFormatted+"}";
                    }
                }
	            formatted[ii] = aFormatted;
            }
        }
        return formatted;
    }

	public static String humanReadableDescription(String morphID) {
		return MorphemeHumanReadableDescr.humanReadableDescription(morphID);
	}
}


