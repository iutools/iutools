// -----------------------------------------------------------------------
//           (c) Conseil national de recherches Canada, 2002
//           (c) National Research Council of Canada, 2002
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
// Document/File:		Base.java
//
// Type/File type:		code Java / Java code
// 
// Auteur/Author:		Benoit Farley
//
// Organisation/Organization:	Conseil national de recherches du Canada/
//				National Research Council Canada
//
// Date de création/Date of creation:	
//
// Description: Classe Base
//
// -----------------------------------------------------------------------

package org.iutools.linguisticdata;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.iutools.utilities1.Util;
import org.iutools.linguisticdata.constraints.Conditions;
import org.iutools.linguisticdata.constraints.Imacond;
import org.iutools.linguisticdata.constraints.ParseException;

public class Base extends Morpheme {
	//
    String variant = null;
    // originalMorpheme:
    //  If a morpheme has various spellings, these are contained in
    //  the field 'variante' of the database table. For reach variant,
	//  another object will be created with that variant for its 'morpheme',
	//  'originalMorpheme' will be set to the original (this) morpheme.
    String originalMorpheme = null;
    String nature = null;
    Boolean known = new Boolean(true);
    String transitivity = null; // for verbs only
    String transinfix = null; // for verbs only
    String intransinfix = null; // for verbs only
    String antipassive = null; // for verbs only
    String number = null;
    String subtype = null;
    String source = null;
    String compositionRoot = null;
    
	private String transitiveMeaning_e = null;
	private String passiveMeaning_e = null;
	private String reflexiveMeaning_e = null;
	private String resultMeaning_e = null;
	private String transitiveMeaning_f = null;
	private String passiveMeaning_f = null;
	private String reflexiveMeaning_f = null;
	private String resultMeaning_f = null;
    
    private Vector<String> idsOfCompositesWithThisRoot = null;
    
	static public Hashtable<String,Morpheme> hash = new Hashtable<String,Morpheme>();
	//

	public Base(HashMap<String,String> v) throws LinguisticDataException {
		makeRoot(v);
	}
		
	public Base() {
	}


	//-----------------------------------------------------------------------------------------------
	private void makeRoot(HashMap<String,String> v) throws LinguisticDataException {
		getAndSetBaseAttributes(v);
		variant = v.get("variant");
		originalMorpheme = v.get("originalMorpheme");
		nb = v.get("nb");
		if (nb==null || nb.equals(""))
			nb = "1";
		num = new Integer(nb);
		type = v.get("type");
		number = v.get("number");
		if (number==null || number.equals(""))
		    number = "s";
		antipassive = v.get("antipassive");
		transinfix = v.get("transSuffix");
		intransinfix = v.get("intransSuffix");
		transitivity = v.get("transitivity");
        cf = v.get("cf");
        if (cf != null && !cf.equals("")) cfs = cf.split(" ");
        dialect = v.get("dialect");
		nature = v.get("nature");
        source = v.get("source");
		if (source != null && !source.equals(""))
			sources = source.split(" ");
		String cs = v.get("condOnNext");
        if (cs != null && !cs.equals(""))
            try {
                nextCondition = (Conditions) new Imacond(
                        new ByteArrayInputStream(cs.getBytes())).ParseCondition();
            } catch (ParseException e) {
            }

		// Racine de composition pour les racines duelles et plurielles
		compositionRoot = v.get("compositionRoot");
		subtype = v.get("subtype");

        String comb = v.get("combination");
		if (comb != null && !comb.equals("")) {
			combinedMorphemes = comb.split("[+]");
			if (combinedMorphemes.length < 2) {
				combinedMorphemes = null;
			}
		}
		setAttrs();
	}

	//-------------------------------------------------------------------------------------------------------
	public void addToHash(String key, Object obj) {
	    hash.put(key,(Base)obj);
	}
    
	public String getSignature() {
	    if (originalMorpheme != null)
	        return new Morpheme.Id(originalMorpheme).signature;
	    else
	        return nb+type;
	}
	
	public String getOriginalMorpheme() {
	    if (originalMorpheme != null)
	        return new Morpheme.Id(originalMorpheme).morphemeName;
	    else
	        return morpheme;
	}
	
	//-------------------------------------------------------------------------------------------------------	
	@JsonIgnore
	boolean isGiVerb() {
		if ( type.equals("v") && 
    				transinfix != null && transinfix.startsWith("gi") )
			return true;
		else
			return false;
	}

	@JsonIgnore
	boolean isSingular() {
		if ( number != null && number.equals("s") )
			return true;
		else
			return false;
	}

	@JsonIgnore
	boolean isDual() {
		if ( number != null && number.equals("d") )
			return true;
		else
			return false;
	}

	@JsonIgnore
	boolean isPlural() {
		if ( number != null && number.equals("p") )
			return true;
		else
			return false;
	}

	@JsonIgnore
	boolean isTransitiveVerb() {
		if ( type.equals("v") && transitivity != null && transitivity.equals("t") )
			return true;
		else
			return false;
	}

	@JsonIgnore
	boolean isIntransitiveVerb() {
		if ( type.equals("v") && transitivity != null && transitivity.equals("i") )
			return true;
		else
			return false;
	}

	@JsonIgnore
	String getAntipassive() {
		return antipassive;
	}

	@JsonIgnore
	String getVariant() {
		return variant;
	}
	
	/*
     * Roots' transitivity is defined as follows: 
     * t: transitive 
     *   If the value of 'antipassive' is not null, the root may be used
     *   intransitively (reflexive or passive transitiveMeaning)
     * i: intransitive 
     *   If the value of 'transinfix' is "nil", the root may also be transitive
     */
	boolean agreeWithTransitivity(String currentStateOfTransitivityDuringAnalysis) {
	    if (currentStateOfTransitivityDuringAnalysis==null) {
			return true;
		} else if (transitivity==null) {
			return false;
		} else if (transitivity.equals("t") && currentStateOfTransitivityDuringAnalysis.equals("t")) {
			return true;
		}
        /*
         * Certains verbes intransitifs peuvent �tre utilis�s transitivement sans
         * infixe de transitivit�.  Ceux-ci ont suffixe-trans=nil.
         */
	    else if (transitivity.equals("i") &&
	            (currentStateOfTransitivityDuringAnalysis.equals("i") ||
	                    (currentStateOfTransitivityDuringAnalysis.equals("t") && transinfix!=null && transinfix.equals("nil")))) {
			return true;
		}
        /*
         * Les verbes transitifs avec un antipassif non-nul peuvent �tre utilis�s
         * intransitivement; ils sont alors interpr�t�s passivement ou r�flexivement.
         */
	    else if (transitivity.equals("t") && antipassive!=null &&
	            currentStateOfTransitivityDuringAnalysis.equals("i") ) {
			return true;
		} else {
			return false;
		}
	}
	
    String[] getVariants() {
        return variant.split(" ");
    }
    
	String getCompositionRoot() {
	    return compositionRoot;
	}
	
	void getAndSetBaseAttributes(HashMap<String,String> v) {
		morpheme = v.get("morpheme");
		englishMeaning = v.get("engMean");
		frenchMeaning = v.get("freMean");
		dbName = v.get("dbName");
		tableName = v.get("tableName");		
	}

	void setCombiningParts( String comb ) {
	    combinedMorphemes = comb.split("[+]");
	    if (combinedMorphemes.length < 2) {
	        combinedMorphemes = null;
	    }
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

//    String getMeaning(String lang) {
//        if (type.equals("v") && transitivity != null && transitivity.equals("t")) {
//            String transitiveMeaning = lang.equals("en")?
//                    transitiveMeaning_e:transitiveMeaning_f;
//            if (transitiveMeaning==null)
//                makeVerbMeanings();
//            return transitiveMeaning;
//        } else
//            return lang.equals("en")?englishMeaning:frenchMeaning;
//
//    }
    
    /*
     * The next 4 methods should be called for transitive verbs only.
     */
    String getTransitiveMeaning(String lang) throws LinguisticDataException {
        if (type.equals("v") && transitivity != null && transitivity.equals("t")) {
            String transitiveMeaning = lang.equals("en")?
                    transitiveMeaning_e:transitiveMeaning_f;
            if (transitiveMeaning==null)
                makeVerbMeanings();
            return lang.equals("en")?
                    transitiveMeaning_e:transitiveMeaning_f;
        } else
            return "";
    }
    
    String getPassiveMeaning(String lang) throws LinguisticDataException {
        if (type.equals("v") && transitivity != null && transitivity.equals("t")) {
            String passiveMeaning = lang.equals("en")?
                    passiveMeaning_e:passiveMeaning_f;
            if (passiveMeaning==null)
                makeVerbMeanings();
            return lang.equals("en")?
                    passiveMeaning_e:passiveMeaning_f;
        } else
            return null;
    }
    
    String getResultMeaning(String lang) throws LinguisticDataException {
        if (type.equals("v") && transitivity != null && transitivity.equals("t")) {
            String resultMeaning = lang.equals("en")?
                    resultMeaning_e:resultMeaning_f;
            if (resultMeaning==null)
                makeVerbMeanings();
            return lang.equals("en")?
                    resultMeaning_e:resultMeaning_f;
        } else 
            return "";
    }
    
    String getReflexiveMeaning(String lang) throws LinguisticDataException {
        if (type.equals("v") && transitivity != null && transitivity.equals("t")) {
            String reflexiveMeaning = lang.equals("en")?
                    reflexiveMeaning_e:reflexiveMeaning_f;
            if (reflexiveMeaning==null)
                makeVerbMeanings();
            return lang.equals("en")?
                    reflexiveMeaning_e:reflexiveMeaning_f;
        } else
            return null;
    }

//    Vector getIdsOfCompositesWithThisRoot() {
//        return idsOfCompositesWithThisRoot;
//    }

	@JsonIgnore
	boolean isKnown() {
    	return known.booleanValue();
    }
    
	void setAttrs() {
		setAttributes();
		setId();
	}
	
    void setAttributes() {
    	setAttributes(new HashMap<String,Object>());
    }
    
    void setAttributes(HashMap<String,Object> attrs) {
    	HashMap<String,Object> baseAttrs = new HashMap<String,Object>();
    	baseAttrs.put("variant", variant);
    	baseAttrs.put("originalMorpheme", originalMorpheme);
    	baseAttrs.put("nature", nature);
    	baseAttrs.put("nb", nb);
    	baseAttrs.put("known", known);
    	baseAttrs.put("transitivity", transitivity);
    	baseAttrs.put("transinfix", transinfix);
    	baseAttrs.put("intransinfix", intransinfix);
    	baseAttrs.put("antipassive", antipassive);
    	baseAttrs.put("number", number);
    	baseAttrs.put("subtype", subtype);
    	baseAttrs.put("source", source);
    	baseAttrs.putAll(attrs);
    	super.setAttributes(baseAttrs);
    }

	String getNature() {
		return nature;
	}
	
	//---------------------------------------------------------------------------------------------------------
    private void setIdsOfCompositesWithThisRoot(Vector<String> v) {
        idsOfCompositesWithThisRoot = v;
    }
    
    /*
     * Modèle général d'une chaîne de texte pour le sens d'un verbe transitif:
     * ('[' '-'? ('R' | 'T' | 'P' | 'A') ']' texte )+
     * texte
     * 
     * où R : indique l'usage réflexif
     *    T : indique l'usage transitif
     *    P : indique l'usage passif
     *    A : indique l'usage 'ALL' (RTP)
     * 
     * Le texte contient les mécanismes spéciaux suivants :
     * 
     *   - verbes acceptant les terminaisons transitives :
     *        - le mot verbal est précédé de '/'
     *        - l'objet est représenté par 's.t.' ou 's.o.' (something ; someone)
     *        - ex. : to /touch s.t.
     *        
     *   - verbes acceptant seulement les terminaisons intransitives :
     *        - pour les verbes pouvant avoir un complément d'objet, 
     *          celui-ci est représenté par '(trans.: s.t.)' ou '(trans.: s.o.)'
     */
    private void makeVerbMeanings() throws LinguisticDataException {
		String[] englishMeanings = makeVerbMeanings(englishMeaning, "en");
		if (englishMeanings != null) {
			transitiveMeaning_e = englishMeanings[0];
			passiveMeaning_e = englishMeanings[1];
			reflexiveMeaning_e = englishMeanings[2];
			resultMeaning_e = englishMeanings[3];
		}
		String[] frenchMeanings = makeVerbMeanings(frenchMeaning, "fr");
		if (frenchMeanings != null) {
			transitiveMeaning_f = frenchMeanings[0];
			passiveMeaning_f = frenchMeanings[1];
			reflexiveMeaning_f = frenchMeanings[2];
			resultMeaning_f = frenchMeanings[3];
		}
	}
    
    private static String[] makeVerbMeanings(String sense, String lang) throws LinguisticDataException {
		if (sense == null)
			return null;
		String transitiveMeaning = "";
		String passiveMeaning = "";
		String resultMeaning = "";
		String reflexiveMeaning = "";

		/*
		 * Un mot commençant par / est un verbe à transformer au passif; le
		 * s.t. ou s.o. (ou qqch. ou qqn en français) représente le
		 * complément d'objet direct du verbe qui, dans le sens au passif,
		 * de même que pour les verbes marqués 'res', est supprimé.
		 */
		Pattern pt = Pattern.compile("\\x5B(-)?([RTPAI])\\x5D([^\\x5B]+)");
		// [(-)R|T|P|A|I]texte
		// groupe 1 : le signe -
		// groupe 2 : R, T, P, A ou I
		// groupe 3 : le texte
		Matcher mpt = pt.matcher(sense);
		int pos = 0;
		// [A] par défaut
//		if (!mpt.find()) {
		if (!sense.startsWith("[")) {
			sense = "[A]" + sense;
			mpt = pt.matcher(sense);
		}

		// Pour chaque '[' '-'? ('R' | 'T' | 'P' | 'A' ) ']' texte
		// R : reflexive
		// T : transitive
		// P : passive
		while (mpt.find(pos)) {
			String texte = mpt.group(3);
			String mode = mpt.group(2);
			String signe = mpt.group(1);
			Map<Character,Boolean> tpr = setTPR(signe,mode);
			boolean trans = tpr.get('t');
			boolean pass = tpr.get('p');
			boolean refl = tpr.get('r');

			Pattern pm = Pattern.compile("(to )?/(([a-zA-Zàâéèêëîïôùûüç]|-)+)"); // texte = (to) /verbe
			Matcher mpm = pm.matcher(texte);
			int pos2 = 0;
			//boolean toBool = lang.equals("en") ? false : true;

			while (mpm.find(pos2)) {
				String key = mpm.group(2);
				String to = mpm.group(1) != null ? mpm.group(1) : "";
				boolean toBool = to.equals("") ? false : true;

				String verbWord = key.replaceFirst("-[^-]+$","");
				String verbPart = key.replace(verbWord, "");
				VerbWord verb = (VerbWord) LinguisticData.getInstance().getVerbWord(verbWord);
				String partPassive = texte.substring(pos2, mpm.start());
				String partReflexive = texte.substring(pos2, mpm.start());
				if (lang.equals("en")) {
					partPassive = partPassive.replaceAll("on top of s.t.",
							"atop");
					partPassive = partPassive.replaceAll("s.t.'s", "its");
					partPassive = partPassive
							.replaceAll("s.o.'s", "his or her");
					partPassive = partPassive.replaceAll(
							" s[.]o[.] or s[.]t[.]", "");
					partPassive = partPassive.replaceAll(
							" s[.]t[.] or s[.]o[.]", "");
					partPassive = partPassive.replaceAll(" s[.]o[.]", "");
					partPassive = partPassive.replaceAll(" s[.]t[.]", "");
					partReflexive = partReflexive.replaceAll("s.t.'s",
							"its own");
					partReflexive = partReflexive.replaceAll("s.o.'s",
							"his or her own");
					partReflexive = partReflexive.replaceAll(
							"s[.]o[.] or s[.]t[.]", "oneself or itself");
					partReflexive = partReflexive.replaceAll(
							"s[.]t[.] or s[.]o[.]", "itself or oneself");
					partReflexive = partReflexive.replaceAll("s[.]o[.]",
							"oneself");
					partReflexive = partReflexive.replaceAll("s[.]t[.]",
							"itself");
				} else if (lang.equals("fr")) {
					partPassive = partPassive.replaceAll(
							" qqn ou qqch[.]", "");
					partPassive = partPassive.replaceAll(
							" qqch[.] ou qqn", "");
					partPassive = partPassive.replaceAll(" qqn", "");
					partPassive = partPassive.replaceAll(" qqch[.]", "");
					if (pass)
						passiveMeaning += partPassive;
					partReflexive = partReflexive.replaceAll(
							" qqn ou qqch[.]", "");
					partReflexive = partReflexive.replaceAll(
							" qqch[.] ou qqn", "");
					partReflexive = partReflexive.replaceAll(" qqn", "");
					partReflexive = partReflexive.replaceAll(" qqch[.]",
							"");
				}
				if (refl) {
					reflexiveMeaning += partReflexive + to;
					resultMeaning += partPassive + to;
					if (lang.equals("fr"))
						reflexiveMeaning += Util.isVowel(key.charAt(0)) ? "s'" : "se ";
					reflexiveMeaning += key.replaceFirst("-[^-]+$","");
					resultMeaning += "/" + key;
				}
				if (pass) {
					String vpass = null;
					if (lang.equals("en")) {
						passiveMeaning += partPassive
								+ (toBool ? (to + "be ") : "");
						if (verb != null)
							vpass = verb.passive;
						else if (key.endsWith("e"))
							vpass = key + "d";
						else if (key.endsWith("y"))
							vpass = key.substring(0, key.length() - 1) + "ied";
						else
							vpass = key + "ed";
					}
					else if (lang.equals("fr")) {
						passiveMeaning += toBool ? "être " : "";
						if (verb != null)
							vpass = verb.passive+verbPart;
						else if (verbWord.endsWith("er"))
							vpass = verbWord.replaceAll("er$", "é")+verbPart;
						else if (verbWord.endsWith("ir"))
							vpass = verbWord.replaceAll("ir$", "i")+verbPart;
						{
							if (verbWord.startsWith("se-"))
								verbWord = verbWord.replace("se-", "");
							if (key.endsWith("-à") || key.endsWith("-contre")
									|| key.endsWith("-de") || key.endsWith("-dans")) {
								vpass = "se faire "+verbWord;
							}
							else if (key.endsWith("-sur")) {
								vpass = "se faire "+verbWord+" dessus";
							}
						}
					}
					else
						vpass = key;
					passiveMeaning += "/" + vpass;
					passiveMeaning = passiveMeaning.replace("être /se faire", "/se faire");
				}
				if (trans)
					transitiveMeaning += texte.substring(pos2, mpm.start())
							+ to + key;
				pos2 = mpm.end();
			}

			// Partie finale
			String partPassive = texte.substring(pos2);
			String partReflexive = texte.substring(pos2);
			if (trans) {
				transitiveMeaning += texte.substring(pos2);
				if (pos2 != 0) {
					if (lang.equals("en")) {
						partPassive = partPassive.replaceAll("on top of s.t.", "atop");
						partPassive = partPassive.replaceAll("s.t.'s", "its");
						partPassive = partPassive.replaceAll("s.o.'s", "his or her");
						partPassive = partPassive.replaceAll(" s[.]o[.] or s[.]t[.]", "");
						partPassive = partPassive.replaceAll(" s[.]t[.] or s[.]o[.]", "");
						partPassive = partPassive.replaceAll(" s[.]o[.]", "");
						partPassive = partPassive.replaceAll(" s[.]t[.]", "");
						partReflexive = partReflexive.replaceAll("s.t.'s", "its own");
						partReflexive = partReflexive.replaceAll("s.o.'s", "his or her own");
						partReflexive = partReflexive.replaceAll("s[.]o[.] or s[.]t[.]", "oneself or itself");
						partReflexive = partReflexive.replaceAll("s[.]t[.] or s[.]o[.]", "itself or oneself");
						partReflexive = partReflexive.replaceAll("s[.]o[.]", "oneself");
						partReflexive = partReflexive.replaceAll("s[.]t[.]", "itself");
					} else if (lang.equals("fr")) {
						partPassive = partPassive.replaceAll(" qqn ou qqch[.]", "");
						partPassive = partPassive.replaceAll(" qqch[.] ou qqn", "");
						partPassive = partPassive.replaceAll(" qqn", "");
						partPassive = partPassive.replaceAll(" qqch[.]", "");
						partReflexive = partReflexive.replaceAll(" qqn ou qqch[.]", "");
						partReflexive = partReflexive.replaceAll(" qqch[.] ou qqn", "");
						partReflexive = partReflexive.replaceAll(" qqn", "");
						partReflexive = partReflexive.replaceAll(" qqch[.]", "");
					}
				}
				transitiveMeaning = transitiveMeaning.replace("se-", "se ");
				transitiveMeaning = transitiveMeaning.replace("-à", " à");
				transitiveMeaning = transitiveMeaning.replace("-contre", " contre");
				transitiveMeaning = transitiveMeaning.replace("-de ", " de ");
				transitiveMeaning = transitiveMeaning.replace("-dans ", " dans ");
				transitiveMeaning = transitiveMeaning.replace("-sur ", " sur ");
			}
			if (pass) {
				passiveMeaning += partPassive;
				passiveMeaning = passiveMeaning.replaceAll("be /made ",
						"be made to ");
				passiveMeaning = passiveMeaning.replaceAll("/", "");
			}
			if (refl) {
				reflexiveMeaning = reflexiveMeaning.replace("-à", "");
				reflexiveMeaning += partReflexive;
				resultMeaning += partPassive;
				resultMeaning = resultMeaning.replaceAll("to /make", "to");
				resultMeaning = resultMeaning.replaceAll("/make", "to");
				resultMeaning = resultMeaning.replaceAll("/", "");
			}
			pos = mpt.end();
		}

		passiveMeaning = passiveMeaning.trim();
		if (reflexiveMeaning.equals("")) reflexiveMeaning = null;
		if (resultMeaning.equals("")) resultMeaning = null;
		return new String[] { transitiveMeaning, passiveMeaning,
				reflexiveMeaning, resultMeaning };
	}
    
	private static Map<Character, Boolean> setTPR(String signe, String mode) {
		boolean trans = true;
		boolean pass = true;
		boolean refl = true;
		boolean modeBool = true;
		if (signe != null)
			modeBool = false;
		if (mode.equals("R")) {
			if (modeBool) {
				trans = false;
				pass = false;
			} else {
				refl = false;
			}
		} else if (mode.equals("T")) {
			if (modeBool) {
				refl = false;
				pass = false;
			} else {
				trans = false;
			}
		} else if (mode.equals("P")) {
			if (modeBool) {
				trans = false;
				refl = false;
			} else {
				pass = false;
			}
		}
		Map<Character, Boolean> tpr = new HashMap<Character, Boolean>();
		tpr.put(new Character('t'), new Boolean(trans));
		tpr.put(new Character('p'), new Boolean(pass));
		tpr.put(new Character('r'), new Boolean(refl));
		return tpr;
	}

	//---------------------------------------------------------------------------------------------------------
	public String showData() throws LinguisticDataException {
		StringBuffer sb = new StringBuffer();
		sb.append("[Base: morpheme= " + morpheme + "\n");
		sb.append("id= "+id+"\n");
		sb.append("variant= " + variant + "\n");
		sb.append("nb= " + nb + "\n");
		sb.append("type= " + type + "\n");
		sb.append("nature= " + nature + "\n");
		sb.append("number= " + number + "\n");
		sb.append("compositionRoot= "+compositionRoot+"\n");
		if (type.equals("v")) {
		    sb.append("antipassive= " + antipassive + "\n");
		}
        if (nextCondition!=null)
            sb.append("followingSpecificConditions= "+nextCondition.toString()+"\n");
        sb.append("englishMeaning= " + englishMeaning + "\n");
        if (type.equals("v") && transitivity != null && transitivity.equals("t")) {
            String trans = this.getTransitiveMeaning("en");
            String refl = this.getReflexiveMeaning("en");
            String pass = this.getPassiveMeaning("en");
            String res = this.getResultMeaning("en");
            sb.append("(trans) "+ trans + "\n" +
                    ((pass!=null)?"(pass) "+pass:"") + "\n" +
                    ((refl!=null)?"(reflex) "+refl:"") + "\n" +
                    ((res!=null)?"(res) "+res:"") + "\n");
        } 
        sb.append("frenchMeaning= " + frenchMeaning + "\n");
        if (type.equals("v") && transitivity != null && transitivity.equals("t")) {
            String trans = this.getTransitiveMeaning("fr");
            String refl = this.getReflexiveMeaning("fr");
            String pass = this.getPassiveMeaning("fr");
            String res = this.getResultMeaning("fr");
            sb.append("(trans) "+ trans + "\n" +
                    ((pass!=null)?"(pass) "+pass:"") + "\n" +
                    ((refl!=null)?"(reflex) "+refl:"") + "\n" +
                    ((res!=null)?"(res) "+res:"") + "\n");
        } 
        sb.append("dbName= "+dbName+"\n");
    	sb.append("tableName= "+tableName+"\n");
        sb.append("dialect= "+dialect+"\n");
        sb.append("cf= "+cf+"\n");
    	sb.append("]");
 		return sb.toString();
	}

}

	


