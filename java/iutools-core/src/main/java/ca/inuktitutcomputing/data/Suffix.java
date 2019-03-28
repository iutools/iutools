//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//
// -----------------------------------------------------------------------
//           (c) Conseil national de recherches Canada, 2002
//           (c) National Research Council of Canada, 2002
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
// Document/File:		Suffix.java
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
// Description: Classe Suffix
//
// -----------------------------------------------------------------------

package ca.inuktitutcomputing.data;

import java.io.ByteArrayInputStream;
import java.util.*;

import ca.inuktitutcomputing.data.constraints.Conditions;
import ca.inuktitutcomputing.data.constraints.Imacond;
import ca.inuktitutcomputing.data.constraints.ParseException;

public class Suffix extends Affix {
	//
	String nb;
	String transitivity;
	String antipassive;
	String nature;
	String constraintOnTransitivity = null;
	String pl;
    String mobility;
    
	//static Class conditionClass = null;
	static public Hashtable<String,Morpheme> hash = new Hashtable<String,Morpheme>();
	
	static String[] functions = {"nn", "nv", "vn", "vv"};
	//
	
	//----------------------------------------------------------------------------------------------------------
	public Suffix() { 
	}
	
	public Suffix(HashMap<String,String> v) {
		morpheme = v.get("morpheme");
		nb = v.get("nb");
		num = new Integer(nb);
		transitivity = v.get("transitivity");
		nature = v.get("nature");
		antipassive = v.get("antipassive");
		type = v.get("type");
		function = v.get("function");
		position = v.get("position");
		constraintOnTransitivity = v.get("condPrecTrans");
		pl = v.get("plural");
        mobility = v.get("mobility");

		// D�veloppement des diverses surfaceFormsOfAffixes associ�es aux 4 contextes
		// voyelle, t, k et q et � leurs actions.

		// Apr�s Voyelle
		String form = v.get("V-form");
		String act1 = v.get("V-action1");
		String act2 = v.get("V-action2");
		// La forme utilis�e pour le morph�me est la forme apr�s voyelle
		// par d�faut.  Toute forme pr�sente dans le champ 'V-form' est
		// une forme optionnelle � ajouter � la forme par d�faut.
		makeFormsAndActions("V", morpheme, form, act1, act2);

		// Apr�s 't'
		form = v.get("t-form");
		act1 = v.get("t-action1");
		act2 = v.get("t-action2");
		makeFormsAndActions("t", morpheme, form, act1, act2);

		// Apr�s 'k'
		form = v.get("k-form");
		act1 = v.get("k-action1");
		act2 = v.get("k-action2");
		makeFormsAndActions("k", morpheme, form, act1, act2);

		// Apr�s 'q'
		form = v.get("q-form");
		act1 = v.get("q-action1");
		act2 = v.get("q-action2");
		makeFormsAndActions("q", morpheme, form, act1, act2);

		englishMeaning = v.get("engMean");
		frenchMeaning = v.get("freMean");
		dbName = v.get("dbName");
		tableName = v.get("tableName");
		String cs = v.get("condPrec");
		if (cs == null || cs.equals("")) {
            /*
             * Pour les NV, si on n'a pas sp�cifi� une condition pr�c�dente, on
             * ajoute une condition par d�faut correspondant � l'�nonc� suivant:
             * "Les NV doivent suivre des radicaux nominaux", i.e. qu'ils ne
             * peuvent suivre une terminaison nominale, sauf dans des cas
             * sp�ciaux o� ce sera indiqu� sp�cifiquement. On emp�chera donc par
             * d�faut les terminaisons nominales. Et puisqu'il y a des noms
             * duels et pluriels dans la base de donn�es, on les emp�chera
             * aussi.
             */
            if (type.equals("sn") && function.equals("nv")) {
                String condStr = "!type:tn,!(type:n,number:d),!(type:n,number:p)";
                try {
                    preCondition = (Conditions) new Imacond(
                            new ByteArrayInputStream(condStr.getBytes())).ParseCondition();
                } catch (ParseException e) {
                }
			}
		} else {
            try {
                preCondition = (Conditions) new Imacond(
                        new ByteArrayInputStream(cs.getBytes())).ParseCondition();
            } catch (ParseException e) {
            }
            
		}
		cs = v.get("condOnNext");
        if (cs != null && !cs.equals(""))
            try {
                nextCondition = (Conditions) new Imacond(
                        new ByteArrayInputStream(cs.getBytes())).ParseCondition();
            } catch (ParseException e) {
            }
            
		String srcs = v.get("sources");
		if (srcs != null) {
			StringTokenizer st2 = new StringTokenizer(srcs);
			sources = new String[st2.countTokens()];
			int n = 0;
			while (st2.hasMoreTokens()) {
				sources[n++] = st2.nextToken();
			}
		}
		String comb = (String)v.get("combination");
		if (comb != null && !comb.equals("")) {
		    combinedMorphemes = comb.split("[+]");
		    if (combinedMorphemes.length < 2) {
		        combinedMorphemes = null;
		    }
		}
		setAttrs();
	}

	//----------------------------------------------------------------------------------------------------------
	public void addToHash(String key, Object obj) {
	    hash.put(key,(Suffix)obj);
	}

	public String getSignature() {
		return nb + (type.equals("q")?type:function);
	}
	
	public String getTransitivityConstraint() {
	    return constraintOnTransitivity;
	}
	
	//----------------------------------------------------------------------------------------------------------
	// Suffixes have transitivity values:
	// t: transitive: the resulting stem is transitive
	//    if antipassive!=null, the resulting stem may be used intransitively; it is
    //    then passive or reflexive.
	// i: intransitive: the resulting stem is intransitive
	//    if nature=t, the resulting stem may also be transitive
	// n: neutral: the resulting stem is the same as the preceding stem
	boolean agreeWithTransitivity(String trans) {
	    if (trans==null)
	        return true;
	    else if (transitivity==null || transitivity.equals("n"))
	        return true;
	    else if (transitivity.equals("t") && trans.equals("t"))
	        return true;
	    else if (transitivity.equals("i") &&
	            ( trans.equals("i") || (trans.equals("t") && 
	                    nature!=null && nature.equals("t"))))
	        return true;
	    else if (transitivity.equals("t")
	            && trans.equals("i") 
	            && antipassive != null)
	        return true;
	    else
	        return false;
	}
	
	
	// Comparaison entre cet affixe et celui donn� en argument.
	boolean is_the_same_affix(Affix aff) {
		if (morpheme.equals(aff.morpheme) && type.equals(aff.type))
			if (type.equals("q"))
				return true;
			else if (function.equals(aff.function))
				return true;
			else
				return false;
		else
			return false;
	}
	
	boolean needsAntipassive(String apId) {
	    if (antipassive != null) {
	        StringTokenizer st = new StringTokenizer(antipassive);
	        while (st.hasMoreTokens())
	            if (st.nextToken().equals(apId))
	                return true;
	        return false;
	    }
	    else
	        return false;
	}
	
//    Vector getIdsOfCompositesWithThisRoot() {
//        return null;
//    }
    
	
	void setAttrs() {
		setAttributes();
		setId();
	}
	
	void setAttributes() {
		HashMap<String,Object> suffAttrs = new HashMap<String,Object>();
		suffAttrs.put("nb",nb);
		suffAttrs.put("transitivity",transitivity);
		suffAttrs.put("antipassive",antipassive);
		suffAttrs.put("nature",nature);
		suffAttrs.put("constraintOnTransitivity",constraintOnTransitivity);
		suffAttrs.put("pl",pl);
		super.setAttributes(suffAttrs);
	}
	
	//-------------------
	public String showData() {
        StringBuffer sb = new StringBuffer();
        sb.append("[Suffix:\n");
        sb.append("morpheme= " + morpheme + "\n");
        sb.append("nb: "+ nb + "\n");
        sb.append("type= " + type + "\n");
        sb.append("function= " + function + "\n");
        sb.append("position= " + position + "\n");
        sb.append("antipassive= " + antipassive + "\n");
        sb.append(super.showData());
        if (preCondition != null) {
            sb.append("precedingSpecificCondition= "+preCondition.toString()+"\n");
        }
        if (nextCondition != null) {
            sb.append("followingSpecificConditions= "+nextCondition.toString()+"\n");
        }
        sb.append("englishMeaning= " + englishMeaning + "\n");
        sb.append("tableName= " + tableName + "\n");
        sb.append("sources= ");
        if (sources == null)
            sb.append(sources);
        else
            for (int n = 0; n < sources.length; n++)
                sb.append(sources[n] + " ");
        sb.append("]\n");
        return sb.toString();
    }

}

