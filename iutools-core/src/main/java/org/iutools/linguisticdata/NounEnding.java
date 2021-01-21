// -----------------------------------------------------------------------
//           (c) Conseil national de recherches Canada, 2003
//           (c) National Research Council of Canada, 2003
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
// Document/File:		TerminaisonNominale.java
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
// Description: Classe TerminaisonNominale
//
// -----------------------------------------------------------------------

package org.iutools.linguisticdata;

import java.io.ByteArrayInputStream;
import java.util.*;

import org.iutools.linguisticdata.constraints.Conditions;
import org.iutools.linguisticdata.constraints.Imacond;
import org.iutools.linguisticdata.constraints.ParseException;
import org.iutools.utilities.Debugging;

public class NounEnding extends Affix {
	//
	String grammCase;
	String cas;
	String number;
	String possPers = null;
	String possNumber = null;
	String poss = null;
	
	static public Hashtable<String,NounEnding> hash = new Hashtable<String,NounEnding>();

	static String[] cases = {"abl", "acc", "dat", "gen", "loc", "nom", "sim", "via"};
	static String[] numbers = {"d", "p", "s"};
	static String[] booleans = {"false", "true"};
	//
	
    //------------------------------------------------------------------------------------------------------------
	public NounEnding() {
	}
	
	public NounEnding(HashMap<String,String> v) {
		morpheme = v.get("morpheme");
		Debugging.mess("NounEnding/1", 1, "morpheme= " + morpheme);
		type = v.get("type");
		grammCase = v.get("case");
		cas = v.get("case");
		number = v.get("number");
		possPers = v.get("perPoss");
		possNumber = v.get("numbPoss");
		if (possPers != null)
		    poss = "true";
		else
		    poss = "false";
		dbName = v.get("dbName");
		tableName = v.get("tableName");

		makeMeanings();

		// Développement des diverses surfaceFormsOfAffixes associées aux 4 contextes
		// voyelle, t, k et q et à leurs actions.

		// Après Voyelle
		String vform = v.get("V-form");
		String vact1 = v.get("V-action1");
		String vact2 = v.get("V-action2");

		// Après 't'
		String tform = v.get("t-form");
		String tact1 = v.get("t-action1");
		String tact2 = v.get("t-action2");

		// Après 'k'
		String kform = v.get("k-form");
		String kact1 = v.get("k-action1");
		String kact2 = v.get("k-action2");

		// Après 'q'
		String qform = v.get("q-form");
		String qact1 = v.get("q-action1");
		String qact2 = v.get("q-action2");

		makeFormsAndActions("V", morpheme, vform, vact1, vact2);
		makeFormsAndActions("t", morpheme, tform, tact1, tact2);
		makeFormsAndActions("k", morpheme, kform, kact1, kact2);
		makeFormsAndActions("q", morpheme, qform, qact1, qact2);
		makeContextualBehaviours();

		String cs = v.get("condPrec");
		if (cs != null) {
            try {
                preCondition = (Conditions) new Imacond(
                        new ByteArrayInputStream(cs.getBytes())).ParseCondition();
            } catch (ParseException e) {
            }
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
		setAttrs();
	}

    //------------------------------------------------------------------------------------------------------------
	public void addToHash(String key, Object obj) {
	    hash.put(key,(NounEnding)obj);
	}

	public String getSignature() {
		StringBuffer sb = new StringBuffer();
		sb.append(type);
		sb.append("-");
		sb.append(grammCase);
		sb.append("-");
		sb.append(number);
		if (possPers != null) {
			sb.append("-");
			sb.append(possPers);
			sb.append(possNumber);
		}
		return sb.toString();
	}
	
	public String getTransitivityConstraint() {
	    return null;
	}
		
	public String[] getCombiningParts() {
	    return null;
	}

    //------------------------------------------------------------------------------------------------------------
	public boolean agreeWithTransitivity(String trans) {
	    return true;
	}
	
    //------------------------------------------------------------------------------------------------------------
	// D�velopper les sens en fran�ais et en anglais des terminaisons
	// verbales � partir des donn�es suivantes: mode, sp�cificit�,
	// personne et nombres du sujet et de l'objet.

	private void makeMeanings() {
		StringBuffer frenchMeaning1 = new StringBuffer();
		StringBuffer englishMeaning1 = new StringBuffer();

		if (grammCase.equals("nom")) {
			frenchMeaning1.append("nominatif: ");
			englishMeaning1.append("nominative: ");
		} else if (grammCase.equals("gen")) {
			frenchMeaning1.append("génitif: de ");
			englishMeaning1.append("genitive: of ");
		} else if (grammCase.equals("acc")) {
			frenchMeaning1.append("accusatif: ");
			englishMeaning1.append("accusative: ");
		} else if (grammCase.equals("dat")) {
			frenchMeaning1.append("datif: à; avec (instrument) ");
			englishMeaning1.append("dative: to; with (instrument) ");
		} else if (grammCase.equals("abl")) {
			frenchMeaning1.append("ablatif: de; par (agent); que (comparaison) ");
			englishMeaning1.append("ablative: from; by (agent); than (comparaison) ");
		} else if (grammCase.equals("loc")) {
			frenchMeaning1.append("locatif: dans; sur ");
			englishMeaning1.append("locative: in; on; upon ");
		} else if (grammCase.equals("sim")) {
			frenchMeaning1.append("similaris: comme ");
			englishMeaning1.append("similaris: like ");
		} else if (grammCase.equals("via")) {
			frenchMeaning1.append("vialis: au moyen de; à travers; par; pendant ");
			englishMeaning1.append(
				"vialis: through; by; by means of; across; over; for (period of time) ");
		}

		if (possPers == null) {
			if (number.equals("s")) {
				frenchMeaning1.append("un; une; le; la ");
				englishMeaning1.append("a; the (one)");
			} else if (number.equals("d")) {
				frenchMeaning1.append("des; les (deux) ");
				englishMeaning1.append("two; the (two)");
			} else if (number.equals("p")) {
				frenchMeaning1.append("des; les (plusieurs) ");
				englishMeaning1.append("many; the (many)");
			}
		} else if (possPers.equals("1")) {
			if (possNumber.equals("s")) {
				if (number.equals("s")) {
					frenchMeaning1.append("mon; ma ");
					englishMeaning1.append("my (one thing) ");
				} else if (number.equals("d")) {
					frenchMeaning1.append("mes (deux choses)");
					englishMeaning1.append("my (two things) ");
				} else if (number.equals("p")) {
					frenchMeaning1.append("mes (plusieurs choses) ");
					englishMeaning1.append("my (many things) ");
				}
			} else if (possNumber.equals("d")) {
				if (number.equals("s")) {
					frenchMeaning1.append("notre (à nous deux) ");
					englishMeaning1.append("our (one thing to us two) ");
				} else if (number.equals("d")) {
					frenchMeaning1.append("nos (deux choses à nous deux) ");
					englishMeaning1.append("our (two things to us two) ");
				} else if (number.equals("p")) {
					frenchMeaning1.append("nos (plusieurs choses à nous deux) ");
					englishMeaning1.append("our (many things to us two) ");
				}
			} else if (possNumber.equals("p")) {
				if (number.equals("s")) {
					frenchMeaning1.append("notre (à nous plusieurs) ");
					englishMeaning1.append("our (one thing to us many) ");
				} else if (number.equals("d")) {
					frenchMeaning1.append("nos (deux choses à nous plusieurs) ");
					englishMeaning1.append("our (two things to us many) ");
				} else if (number.equals("p")) {
					frenchMeaning1.append(
						"nos (plusieurs choses à nous plusieurs) ");
					englishMeaning1.append("our (many things to us many) ");
				}
			}
		} else if (possPers.equals("2")) {
			if (possNumber.equals("s")) {
				if (number.equals("s")) {
					frenchMeaning1.append("ton; ta ");
					englishMeaning1.append("your (one thing to one person) ");
				} else if (number.equals("d")) {
					frenchMeaning1.append("tes (deux choses)");
					englishMeaning1.append("your (two things to one person) ");
				} else if (number.equals("p")) {
					frenchMeaning1.append("tes (plusieurs choses) ");
					englishMeaning1.append("your (many things to one person) ");
				}
			} else if (possNumber.equals("d")) {
				if (number.equals("s")) {
					frenchMeaning1.append("votre (à vous deux) ");
					englishMeaning1.append("your (one thing to you two) ");
				} else if (number.equals("d")) {
					frenchMeaning1.append("vos (deux choses à vous deux) ");
					englishMeaning1.append("your (two things to you two) ");
				} else if (number.equals("p")) {
					frenchMeaning1.append("vos (plusieurs choses à vous deux) ");
					englishMeaning1.append("your (many things to you two) ");
				}
			} else if (possNumber.equals("p")) {
				if (number.equals("s")) {
					frenchMeaning1.append("votre (à vous plusieurs) ");
					englishMeaning1.append("your (one thing to you many) ");
				} else if (number.equals("d")) {
					frenchMeaning1.append("vos (deux choses à vous plusieurs) ");
					englishMeaning1.append("your (two things to you many) ");
				} else if (number.equals("p")) {
					frenchMeaning1.append(
						"vos (plusieurs choses à vous plusieurs) ");
					englishMeaning1.append("your (many things to you many) ");
				}
			}
		} else if (possPers.equals("3")) {
			if (possNumber.equals("s")) {
				if (number.equals("s")) {
					frenchMeaning1.append("son; sa (même personne) ");
					englishMeaning1.append("his;her;its (one thing, same person) ");
				} else if (number.equals("d")) {
					frenchMeaning1.append("ses (deux choses, même personne)");
					englishMeaning1.append("his;her;its (two things, same person) ");
				} else if (number.equals("p")) {
					frenchMeaning1.append("ses (plusieurs choses, même personne) ");
					englishMeaning1.append("his;her;its (many things, same person) ");
				}
			} else if (possNumber.equals("d")) {
				if (number.equals("s")) {
					frenchMeaning1.append("leur (à eux deux, mêmes personnes) ");
					englishMeaning1.append("their (one thing to them two, same persons) ");
				} else if (number.equals("d")) {
					frenchMeaning1.append("leurs (deux choses à eux deux, même personnes) ");
					englishMeaning1.append("their (two things to them two, same persons) ");
				} else if (number.equals("p")) {
					frenchMeaning1.append("leurs (plusieurs choses à eux deux, mêmes personnes) ");
					englishMeaning1.append("their (many things to them two, same persons) ");
				}
			} else if (possNumber.equals("p")) {
				if (number.equals("s")) {
					frenchMeaning1.append("leur (à eux plusieurs, mêmes personnes) ");
					englishMeaning1.append("their (one thing to them many, same persons) ");
				} else if (number.equals("d")) {
					frenchMeaning1.append("leurs (deux choses à eux plusieurs, mêmes personnes) ");
					englishMeaning1.append("their (two things to them many, same persons) ");
				} else if (number.equals("p")) {
					frenchMeaning1.append(
						"leurs (plusieurs choses à eux plusieurs, mêmes personnes) ");
					englishMeaning1.append("their (many things to them many, same persons) ");
				}
			}
		}
		else if (possPers.equals("4")) {
			if (possNumber.equals("s")) {
				if (number.equals("s")) {
					frenchMeaning1.append("son; sa (autre personne) ");
					englishMeaning1.append("his;her;its (one thing, different person) ");
				} else if (number.equals("d")) {
					frenchMeaning1.append("ses (deux choses, autre personne)");
					englishMeaning1.append("his;her;its (two things, different person) ");
				} else if (number.equals("p")) {
					frenchMeaning1.append("ses (plusieurs choses, autre personne) ");
					englishMeaning1.append("his;her;its (many things, different person) ");
				}
			} else if (possNumber.equals("d")) {
				if (number.equals("s")) {
					frenchMeaning1.append("leur (à eux deux, autres personnes) ");
					englishMeaning1.append("their (one thing to them two, different persons) ");
				} else if (number.equals("d")) {
					frenchMeaning1.append("leurs (deux choses à eux deux, autres personnes) ");
					englishMeaning1.append("their (two things to them two, different persons) ");
				} else if (number.equals("p")) {
					frenchMeaning1.append("leurs (plusieurs choses à eux deux, autres personnes) ");
					englishMeaning1.append("their (many things to them two, different persons) ");
				}
			} else if (possNumber.equals("p")) {
				if (number.equals("s")) {
					frenchMeaning1.append("leur (à eux plusieurs, autres personnes) ");
					englishMeaning1.append("their (one thing to them many, different persons) ");
				} else if (number.equals("d")) {
					frenchMeaning1.append("leurs (deux choses à eux plusieurs, autres personnes) ");
					englishMeaning1.append("their (two things to them many, different persons) ");
				} else if (number.equals("p")) {
					frenchMeaning1.append(
						"leurs (plusieurs choses à eux plusieurs, autres personnes) ");
					englishMeaning1.append("their (many things to them many, different persons) ");
				}
			}
		}
		frenchMeaning = frenchMeaning1.toString();
		englishMeaning = englishMeaning1.toString();
	}

//    Vector getIdsOfCompositesWithThisRoot() {
//        return null;
//    }

	void setAttrs() {
		setAttributes();
		setId();
	}

	void setAttributes() {
		HashMap<String,Object> tnAttrs = new HashMap<String,Object>();
		tnAttrs.put("grammCase", grammCase);
		tnAttrs.put("cas", grammCase);
		tnAttrs.put("number", number);
		tnAttrs.put("possPers", possPers);
		tnAttrs.put("possNumber", possNumber);
		tnAttrs.put("poss", poss);
		super.setAttributes(tnAttrs);
	}
	
    //------------------------------------------------------------------------------------------------------------
	public String showData() {
		StringBuffer sb = new StringBuffer();
		sb.append("\n[NounEnding: morpheme= " + morpheme + "\n");
		sb.append("type= " + type + "\n");
		sb.append("grammCase= " + grammCase + "\n");
		sb.append("number=" + number + "\n");
		sb.append("possPers= " + possPers + "\n");
		sb.append("possNumber= " + possNumber + "\n");
		sb.append(super.showData());
        if (preCondition != null) {
            sb.append("precedingSpecificCondition= "+preCondition.toString()+"\n");
        }
    	sb.append("dbName= "+dbName+"\n");
    	sb.append("tableName= "+tableName+"\n");
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
