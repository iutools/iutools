//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//
// -----------------------------------------------------------------------
//           (c) Conseil national de recherches Canada, 2003
//           (c) National Research Council of Canada, 2003
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
// Document/File:		Suffixes.java
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
// Description: La méthode statique 'getDef' retourne dans un format HTML
//              l'information sur un suffixe.
//
// -----------------------------------------------------------------------

package ca.inuktitutcomputing.data;

import java.io.PrintStream;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.log4j.Logger;

import org.iutools.utilities1.Util;
import ca.inuktitutcomputing.data.constraints.Conditions;
import org.iutools.script.Roman;
import org.iutools.script.TransCoder;

public class Suffixes {

    private static Logger LOG = Logger.getLogger(Suffixes.class);

	//static OutputStreamWriter out;
	static boolean syllabic = true;
	static String lang = "en";
	static String lang_default = "en";
    static String inuktitutDisplayFont = null;
    static boolean plain = false;
    static String nl = System.getProperty("line.separator");

	//-------------------------------------------------------------------
	// DÉFINITION DE SUFFIXE ET DE TERMINAISON
	//-------------------------------------------------------------------

	// Les arguments 'args' sont:
	//   - 0: l'identificateur unique du suffixe

    public static void getDef(String[] args, PrintStream out) {
        try {
            String suffixId = null;
            lang = Util.getArgument(args,"l");
            suffixId = Util.getArgument(args,"m");
            String font = Util.getArgument(args, "p");
            String plainFlag = Util.getArgument(args, "plain");
            if (plainFlag != null && plainFlag.toLowerCase().equals("1"))
            	plain = true;
            if (font != null)
                inuktitutDisplayFont = font;
            if (lang == null)
            	lang = lang_default;

            StringBuffer output = new StringBuffer();

            Suffix suf = (Suffix)  LinguisticData.getInstance().getSuffixWithId(suffixId);

            output.append(startParagraph());
//            output.append(html.scriptDescSufJSP(lang,font));
            // Pour chaque avatar du suffixe demandé:
            output.append(composeSuffixeDisplay(suf));
            output.append(blankLine());
            output.append(endParagraph());
            out.print(output.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static String startParagraph() {
    	return plain ? nl+nl : nl+"<p>"+nl;
    }
    private static String endParagraph() {
    	return plain ? "" : "</p>"+nl;
    }
    private static String blankLine() {
    	return plain ? nl+nl : "<br><br>"+nl;
    }
    private static String twoSpaces() {
    	return plain ? "  " : "&nbsp;&nbsp;";
    }
    private static String threeSpaces() {
    	return plain ? "   " : "&nbsp;&nbsp;&nbsp;";
    }
    private static String horizontalLine() {
    	return plain ? nl+"----------------------------------------------" : "<hr>"+nl;
    }
    private static String newLine() {
    	return plain ? nl : "<br>";
    }

	private static String composeSuffixeDisplay(Suffix aff) throws LinguisticDataException {
		StringBuffer output = new StringBuffer();
		if (!plain) {
			output.append("<span style=\"font-size:10pt;color:green\">");
			output.append("<span style=\"font-weight:bold;\">");
		}
        if (inuktitutDisplayFont==null) {
        	LOG.debug(aff.morpheme);
        	String unicode = TransCoder.romanToUnicode(aff.morpheme);
        	LOG.debug(unicode);
            output.append(unicode);
        }
        else {
        	if (!plain)
        		output.append("<span style=\"font-family:"+inuktitutDisplayFont+"\">");
            output.append(TransCoder.romanToLegacy(aff.morpheme, inuktitutDisplayFont));
            if (!plain)
            	output.append("</span>"); // font-family
        }
        output.append(twoSpaces());
        output.append(aff.morpheme+" ");
        if (!plain) {
        	output.append("</span>"+nl); // font-weight:bold
        	output.append("<span style=\"font-style:italic;font-size:8pt\">");
        }
		output.append("("+aff.getSignature()+")");
		if (!plain)
			output.append("</span>"); // font-style:italic;font-size:8pt
		if (!plain)
			output.append("</span>"); // font-size:10pt; color:green
		output.append(horizontalLine());

        // Signification
        output.append(startParagraph());
        if (!plain) output.append("<span style=\"font-weight:bold\">");
        output.append((lang.equals("en")) ? "Meaning" : "Signification");
        if (!plain) output.append("</span>"); else output.append(":");
        output.append(newLine());
        String meaning = lang.equals("en") ? aff.englishMeaning : aff.frenchMeaning;
        output.append(Item.process_marked_up(meaning, plain));
        if (aff.nature!=null && aff.nature.equals("freq")) {
        	output.append(blankLine());
            if (lang.equals("en")) {
                output.append("This infix is a frequentative.  Frequentatives are often used ");
                output.append("with perfect verb roots (action in the past with result in the present) to indicate that the action is ");
                output.append("actually taking place in the present time.  For example: ");
                output.append(newLine());
                output.append(threeSpaces());
                if (!plain)
                	output.append("<b>iqqanaiqpaa</b> - he has finished it");
                else
                	output.append("iqqanaiqpaa - he has finished it");
                output.append(newLine());
                output.append(threeSpaces());
                if (!plain)
                	output.append("<b>iqqanai<i>jaq</i>paa</b> - he is finishing it");
                else
                	output.append("iqqanaijaqpaa - he is finishing it");
            } else if (lang.equals("fr")) {
                output.append("Cet infixe est un fréquentatif.  Les fréquentatifs sont souvent utilisés ");
                output.append("avec les racines verbales à aspect résultatif (une action dans le passé avec un effet dans le présent), pour indiquer que l'action se déroule ");
                output.append("dans le présent.  Par exemple:");                
                output.append(newLine());
                output.append(threeSpaces());
                if (!plain)
                	output.append("<b>iqqanaiqpaa</b> - il l'a terminé");
                else
                	output.append("iqqanaiqpaa - il l'a terminé");
                output.append(newLine());
                output.append(threeSpaces());
                if (!plain)
                	output.append("<b>iqqanai<i>jaq</i>paa</b> - il le termine, est en train de le terminer");
                else
                	output.append("iqqanaijaqpaa - il le termine, est en train de le terminer");
            } else {
            }
        }
        output.append(endParagraph());

		// Type
        output.append(startParagraph());
        if (!plain) output.append("<span style=\"font-weight:bold\">");
        output.append("Type");
        if (!plain) output.append("</span>"); else output.append(":");
        output.append(newLine());
		output.append( LinguisticData.getInstance().getTextualRendering(aff.type, lang));
	
		// Suffix: fonction, mobilité, position
		if (aff.type.equals("sn") || aff.type.equals("sv")) {
			// Fonction
			output.append(", " +  LinguisticData.getInstance().getTextualRendering(aff.function, lang));
			output.append(
				", "
					+  LinguisticData.getInstance().getTextualRendering("function", lang)
					+ " "
					+  LinguisticData.getInstance().getTextualRendering(aff.function.substring(1, 2), lang));
            
			// Transitivité
	        if (aff.antipassive != null) {
	            output.append("<ul><li>");
	            if (lang.equals("en")) {
	                output.append("requires the suffix(es) ");
	            } else if (lang.equals("fr")) {
	                output.append("requiert le(s) suffixe(s) ");
	            } 
	            String[] antipassives = aff.antipassive.split(" ");
	            for (int i=0; i<antipassives.length; i++) {
	                String[] parts = antipassives[i].split("/");
	                output.append("<a href=");
	                output.append("\"javascript:appelerDescriptionSuffixe(");
	                if (inuktitutDisplayFont==null)
	                    output.append("'"+antipassives[i]+"',null");
	                else
	                    output.append("'"+antipassives[i]+"','"+inuktitutDisplayFont+"'");
	                output.append(")\">");
	                output.append(parts[0]);
	                output.append("</a>");
	                output.append("<sub style=\"font-size:75%\">"+parts[1]+"</sub>");
	                output.append("&nbsp;");
	            }
	            if (lang.equals("en")) {
	                output.append(" when used with intransitive verb endings or suffixes in order to keep the transitive sense of the verbal suffix and avoid a passive or reflexive interpretation:");
	            } else if (lang.equals("fr")) {
	                output.append(" lorsqu'utilisée avec des terminaisons verbales ou des suffixes intransitifs de façon à conserver le sens transitif du suffixe verbal et ne pas être interprété passivement ou réflexivement&nbsp;:");
	            } 
	            for (int i=0; i<antipassives.length; i++) {
	            	String combination = aff.id+"+"+antipassives[i];
	            	if (LOG.isDebugEnabled()) LOG.debug("combination= '"+combination+"'");
	            	String combined = Morpheme.combine(combination, true, null);
					output.append(Roots.composeCombinationDisplay(combined));
	            }
	            output.append("</li></ul>");
	        }

            // Mobilité
            if (aff.mobility != null) {
            	output.append(startParagraph());
                if (!plain) output.append("<span style=\"font-weight:bold\">");
                output.append(lang.equals("en")?"Mobility":"Mobilité");
                if (!plain) output.append("</span>"); else output.append(":");
                output.append(newLine());
                if (aff.mobility.equals("non"))
                    output.append(lang.equals("en")?"not ":"non ");
                output.append("mobile: ");
                if (lang.equals("en")) {
                    if (aff.mobility.equals("non")) {
                        output.append("this suffix cannot be used at will; it can be used only with certain well defined roots.");
                    } else {
                        output.append("this suffix can be used at will with all roots and stems of the proper type.");                    
                    }
                } else if (lang.equals("fr")) {
                    if (aff.mobility.equals("non")) {
                        output.append("ce suffixe ne peut pas être utilisé à volonté; il peut être utilisé uniquement avec certaines racines bien définies.");                    
                    } else {
                        output.append("ce suffixe peut être utilisé à volonté avec toutes les racines et tous les radicaux du type approprié.");                                        
                    }                
                }
                output.append(endParagraph());
            }
            output.append(endParagraph());

			// Position
        	output.append(startParagraph());
            if (!plain) output.append("<span style=\"font-weight:bold\">");
            output.append("Position");
            if (!plain) output.append("</span>"); else output.append(":");
            output.append(newLine());
			output.append(
				(lang.equals("en") ? "This suffix" : "Ce suffixe")
					+ " "
					+  LinguisticData.getInstance().getTextualRendering(aff.position + "!", lang)
					+ ".");
            output.append(endParagraph());
		}
		
		// Terminaison verbale: mode, spécificité, personne(s), number(s)
		else if (aff.type.equals("tv")) {
			// spécificité
			String specif = ((VerbEnding)(Affix) aff).spec;
			output.append(", " +  LinguisticData.getInstance().getTextualRendering(specif, lang) + ", ");
			
			// mode
			String mode = ((VerbEnding)(Affix) aff).mode;
        	output.append(startParagraph());
            if (!plain) output.append("<span style=\"font-weight:bold\">");
            output.append((lang.equals("en") ? "Mood" : "Mode"));
            if (!plain) output.append("</span>"); else output.append(":");
            output.append(newLine());
			output.append( LinguisticData.getInstance().getTextualRendering(mode, lang));
			if (mode.equals("part")) {
				String tense = ((VerbEnding)(Affix) aff).tense;
				String pn = ((VerbEnding)(Affix) aff).posneg;
				if (tense != null)
					output.append(", " +  LinguisticData.getInstance().getTextualRendering(tense, lang));
				output.append(", " +  LinguisticData.getInstance().getTextualRendering(pn, lang));
			}
			output.append(endParagraph());
			
			// personnes et nombres
        	output.append(startParagraph());
            if (!plain) output.append("<span style=\"font-weight:bold\">");
            output.append((lang.equals("en") ? "Subject" : "Sujet"));
            if (!plain) output.append("</span>"); else output.append(":");
            output.append(newLine());
			String persSubj = ((VerbEnding)(Affix) aff).subjPers;
			String numbSuj = ((VerbEnding)(Affix) aff).subjNumber;
			output.append( LinguisticData.getInstance().getTextualRendering(persSubj + "ordinal", lang));
			output.append(" " +  LinguisticData.getInstance().getTextualRendering("personne", lang));
			output.append(" " +  LinguisticData.getInstance().getTextualRendering("du", lang));
			output.append(" " +  LinguisticData.getInstance().getTextualRendering(numbSuj, lang));
			output.append(endParagraph());
			if (specif.equals("sp")) {
	        	output.append(startParagraph());
	            if (!plain) output.append("<span style=\"font-weight:bold\">");
	            output.append((lang.equals("en") ? "Object" : "Objet"));
	            if (!plain) output.append("</span>"); else output.append(":");
	            output.append(newLine());
				String persObj = ((VerbEnding)(Affix) aff).objPers;
				String numbObj = ((VerbEnding)(Affix) aff).objNumber;
				output.append( LinguisticData.getInstance().getTextualRendering(persObj + "ordinal", lang));
				output.append(" " +  LinguisticData.getInstance().getTextualRendering("personne", lang));
				output.append(" " +  LinguisticData.getInstance().getTextualRendering("du", lang));
				output.append(" " +  LinguisticData.getInstance().getTextualRendering(numbObj, lang));
				output.append(endParagraph());
			}
		}
		// Terminaison nominale: cas, number, possPers, possNumber
		else if (aff.type.equals("tn")) {
			String grammCase = ((NounEnding)(Affix) aff).grammCase;
			String nb = ((NounEnding)(Affix) aff).number;
        	output.append(startParagraph());
            if (!plain) output.append("<span style=\"font-weight:bold\">");
            output.append((lang.equals("en") ? "Case and number" : "Cas et number"));
            if (!plain) output.append("</span>"); else output.append(":");
            output.append(newLine());
			output.append( LinguisticData.getInstance().getTextualRendering(grammCase, lang));
			output.append(" " +  LinguisticData.getInstance().getTextualRendering(nb, lang));
			String persPos = ((NounEnding)(Affix) aff).possPers;
			String nbPos = ((NounEnding)(Affix) aff).possNumber;
			if (persPos != null) {
				output.append(" " +  LinguisticData.getInstance().getTextualRendering("possessif", lang));
				output.append(endParagraph());
	        	output.append(startParagraph());
	            if (!plain) output.append("<span style=\"font-weight:bold\">");
				output.append( LinguisticData.getInstance().getTextualRendering("possesseur", lang));
	            if (!plain) output.append("</span>"); else output.append(":");
	            output.append(newLine());
				output.append( LinguisticData.getInstance().getTextualRendering(persPos + "ordinal", lang));
				output.append(" " +  LinguisticData.getInstance().getTextualRendering("personne", lang));
				output.append(" " +  LinguisticData.getInstance().getTextualRendering("du", lang));
				output.append(" " +  LinguisticData.getInstance().getTextualRendering(nbPos, lang));
				output.append(endParagraph());
			}
		} else
			output.append(endParagraph());
			
		// Combinaison
		if (aff.combinedMorphemes != null) {
        	output.append(startParagraph());
            if (!plain) output.append("<span style=\"font-weight:bold\">");
            output.append((lang.equals("en")) ? "Combination" : "Combinaison");
            if (!plain) output.append("</span>"); else output.append(":");
            output.append(newLine());
            output
                    .append((lang.equals("en")) ? "This complex suffix is a combination of the suffixes: "
                            : "Ce suffixe complexe est la combinaison des suffixes: ");
            for (int i = 0; i < aff.combinedMorphemes.length; i++) {
                if (i != 0)
                    if (!plain)
                    	output.append("&nbsp;+&nbsp;");
                    else
                    	output.append(" + ");
                if (!plain) {
                	output.append("<a href=\"javascript:appelerDescriptionSuffixe(");
                	if (inuktitutDisplayFont==null)
                		output.append("'"+aff.combinedMorphemes[i]+"',null"); 
                	else
                		output.append("'"+aff.combinedMorphemes[i]+"','"+inuktitutDisplayFont+"'"); 
                	output.append(")\">");
                }
                String ps[] = aff.combinedMorphemes[i].split("/");
                output.append(ps[0]);
                if (!plain)
                	output.append("</a>");
                if (!plain)
                	output.append("<sub style=\"font-size:75%\">");
                else
                	output.append("/");
                output.append(ps[1]);
                if (!plain)
                	output.append("</sub>");
            }
            output.append(endParagraph());
        }
        
        // Variantes et actions
    	output.append(startParagraph());
        if (!plain) output.append("<span style=\"font-weight:bold\">");
        output.append((lang.equals("en")) ? "Variants and actions"
                : "Variantes et actions");
        if (!plain) output.append("</span>"); else output.append(":");
        output.append(newLine());
		Vector<Object[]> vars = new Vector<Object[]>();
		vars.add(new Object[] { "V", aff.vform, aff.vaction1, aff.vaction2 });
		vars.add(new Object[] { "t", aff.tform, aff.taction1, aff.taction2 });
		vars.add(new Object[] { "k", aff.kform, aff.kaction1, aff.kaction2 });
		vars.add(new Object[] { "q", aff.qform, aff.qaction1, aff.qaction2 });
		if (!plain)
			output.append("<table border=1 style=\"font-family:Pigiarniq;font-size:8pt\">");
		// Netscape ne semble pas supporter <span style="font-family...;font-size=..."> 
		// à l'intérieur des tables.  Mais il reconnaît <span style="font-weight..."> (???)
		// On remplace par <font>. (cf. http://www.webreference.com/dev/html4nsie/tables.html)
//		String styletd = "\n<td><span style=\"font-family:Pigiarniq;font-size=8pt\">";
//		String fintd = "</span></td>";
		String fintd = "</td>";
		for (int k = 0; k < vars.size(); k++) {
			Object[] var = (Object[]) vars.elementAt(k);
			String cntxt = (String) var[0];
			String[] forms = (String[]) var[1];
			Action[] act1 = (Action[]) var[2];
			Action[] act2 = (Action[]) var[3];

            // Après V,T,K,Q
			if (!plain) {
				output.append(nl+"<tr valign=top>");
				output.append(nl+"<td colspan=4 style=\"background-color:#ffffcc;\">");
//            	output.append("<span style=\"text-decoration:underline\">");
			} else
				output.append(nl);
			output.append( LinguisticData.getInstance().getTextualRendering("après", lang) + " ");
			if (cntxt.equals("V"))
				output.append( LinguisticData.getInstance().getTextualRendering(cntxt, lang));
			else
				output.append(cntxt);
			if (!plain) {
//            	output.append("</span>");
				output.append("</td>");
				output.append("</tr>");
			}
            
            if (forms.length==0) {
            	if (!plain) {
            		output.append(nl+"<tr valign=top>");
            		output.append(nl+"<td colspan=4>");
            	} else {
            		output.append(nl);
            	}
                if (lang.equals("en")) {
                    output.append("This morpheme is never encountered in this context.");
                } else if (lang.equals("fr")) {
                    output.append("Ce morphème n'est jamais rencontré dans ce contexte.");
                }
                if (!plain) {
                	output.append("</td>");
                	output.append("</tr>");
                }
            } else
                for (int z = 0; z < forms.length; z++) {
                    String outputAction = null;
                    if (!plain)
                    	output.append(nl+"<tr valign=top>");
                    else
                    	output.append(nl);
                    // La forme
                    if (forms[z].equals("?")) {
                    	if (!plain)
                    		output.append(nl+"<td colspan=4>");
                        if (lang.equals("en"))
                            output.append(
                            "The form in this context is not known to us at this moment.  ");
                        else
                            output.append(
                            "La forme dans ce contexte ne nous est pas connue actuellement.  ");
                        if (!plain)
                        	output.append("</td>");
                    } else {
                        int rowSpan = 1;
                        if (act2[z].getType() != Action.NULLACTION)
                            rowSpan = 2;
                        // Forme en syllabique
                        String form = forms[z].equals("*")?aff.morpheme
                                : forms[z];
                        if (!plain) {
                        	output.append(nl+"<td rowspan="+rowSpan+">");
                        	output.append( "<span style=\"font-weight:bold\">");
                        }
                        if (inuktitutDisplayFont==null)
                                output.append(TransCoder.romanToUnicode(form));
                        else {
                            if (!plain) output.append("<span style=\"font-family:"+inuktitutDisplayFont+"\">");
                            output.append(TransCoder.romanToLegacy(form, inuktitutDisplayFont));
                            if (!plain) output.append("</span>");
                        }
                        if (!plain) {
                        	output.append("</span>") ;
                        	output.append("</td>");
                        }
                        // Cellule d'espacement
//                        output.append("\n<td rowspan="+rowSpan+">&nbsp;&nbsp;&nbsp;</td>");
                        // Forme en alphabet latin
                        if (!plain) {
                        	output.append(nl+"<td rowspan="+rowSpan+">");
                        	output.append("<span style=\"font-weight:bold\">");
                        } else {
                        	output.append("\t");
                        }
                        output.append(form);
                        if (!plain) output.append("</span></td>");
                        // Cellule d'espacement
//                        output.append("\n<td rowSpan="+rowSpan+">&nbsp;&nbsp;&nbsp;</td>");
                        // L'action
                        if (!plain)
                        	output.append(nl+"<td style=\"width:50%;\">");
                        else
                        	output.append("\t");
                        outputAction =
                            composeAction1(
                                    act1[z],
                                    aff,
                                    forms[z],
                                    cntxt,
                                    lang);
                        output.append(outputAction);
                        if (!plain) output.append("</td>");
                        // Cellule d'espacement
//                        output.append("\n<td rowspan="+rowSpan+">&nbsp;&nbsp;&nbsp;</td>");
                        // Expression
                        if (!plain)
                        	output.append("<td><i>");
                        else
                        	output.append("\t");
                        String actRes = act1[z].expressionResult(cntxt,forms[z],null);
                        if (plain)
                        	actRes = actRes.replace("&rarr;", "-->");
                        output.append(actRes);
                        if (!plain) output.append("</i></td>");               
                    }
                    if (!plain)
                    	output.append("</tr>");
                    if (act2[z].getType()!=Action.NULLACTION) {
                    	if (!plain) {
                    		output.append("<tr>");
                    		output.append("<td>");
                    	} else {
                    		output.append(nl);
                    	}
                        outputAction =
                            composeAction2(
                                    act1[z],
                                    act2[z],
                                    aff,
                                    forms[z],
                                    cntxt,
                                    lang);
                        output.append(outputAction);
                        if (!plain)
                        	output.append("</td>");
                        if (!plain)
                        	output.append("<td><i>");
                        else
                        	output.append("\t");
                        String actRes = act1[z].expressionResult(cntxt,forms[z],act2[z]); 
                        if (plain)
                        	actRes = actRes.replace("&rarr;", "-->");
                        output.append(actRes);
                        if (!plain) {
                        	output.append("</i></td>");
                        	output.append("</tr>");
                        }
                    }
                }
            if (!plain)
            	output.append("</tr>");
		}
		if (!plain)
			output.append("</table>");
        
		output.append(startParagraph());
		if (!plain)
			output.append("<i>");
		output.append("Note");
		if (!plain)
			output.append("</i>");
		output.append(": ");
		if (lang.equals("en")) {
			output
					.append("When the final consonant of the stem is not affected by the "
							+ "suffix, or when it is voiced or nasalized, "
							+ "or when a consonant is inserted before the suffix, and the suffix starts with "
							+ "a consonant, that group of consonants may be subject to gemination, "
							+ "a phenomenon encountered in several dialects by which the first "
							+ "consonant is assimilated to the second one, for example, ");
		} else if (lang.equals("fr")) {
			output
					.append("Lorsque la consonne finale du radical n'est pas affectée "
							+ "par le suffixe, ou lorsqu'elle est sonorisée ou nasalisée, "
							+ "ou lorsqu'une consonne est insérée devant le suffixe, et que le suffixe "
							+ "commence par une consonne, ce groupe de consonnes peut être sujet "
							+ "à la gémination, un phénomène rencontré dans plusieurs dialectes selon "
							+ "lequel la première consonne est assimilée à la deuxième, par exemple, ");
		}
		String ex = "TP &rarr; PP, PV &rarr; VV";
		if (plain)
			ex = ex.replaceAll("&rarr;", "-->");
		output.append(ex);
		output.append(lang.equals("en")? ", that is, in syllabics: " : "soit, en syllabique&nbsp;: ");
		ex = "  \u1466\u1431 &rarr; \u1449\u1431, \u1449\u1555 &rarr; \u155d\u1555.";
		if (plain)
			ex = ex.replaceAll("&rarr;", "-->");
		output.append(ex);
        output.append(endParagraph());

		// Conditions spécifiques précédentes
// if (condsPrecs != null && condsPrecs.size() != 0) {
// output.append("\n<p><span style=\"font-weight:bold\">");
// output.append(
// (lang.equals("en"))
//					? "Special conditions"
//					: "Conditions sp�ciales");
//			output.append("</span><br>");
//			for (int icds = 0; icds < condsPrecs.size(); icds++) {
//			    MorphemeCondition cond = (MorphemeCondition)condsPrecs.elementAt(icds);
//			    output.append(cond.parameters+" ");
//			}
//		}
        if (aff.getPrecCond() != null) {
        	output.append(startParagraph());
            if (!plain) output.append("<span style=\"font-weight:bold\">");
            output.append(
                    (lang.equals("en"))
                        ? "Special conditions on the preceding morpheme"
                        : "Conditions spéciales sur le morphème précédent");
            if (!plain) output.append("</span>"); else output.append(":");
            output.append(newLine());
            output.append(aff.getPrecCond().toText(lang));
        }
        
        if (aff.getNextCond() != null) {
        	output.append(startParagraph());
            if (!plain) output.append("<span style=\"font-weight:bold\">");
            output.append(
                    lang.equals("en")
                        ? "Special conditions on the following morpheme"
                        : "Conditions spéciales sur le morphème suivant");
            if (!plain) output.append("</span>"); else output.append(":");
            output.append(newLine());
            output.append(aff.getNextCond().toText(lang));
        }

		// Examples
    	output.append(startParagraph());
        if (!plain) output.append("<span style=\"font-weight:bold\">");
		output.append((lang.equals("en") ? "Examples" : "Examples"));
        if (!plain) output.append("</span>"); else output.append(":");
        output.append(newLine());

        if (!plain)
        	output.append("<table style=\"font-family:Pigiarniq;font-size:8pt\">");
		String signatureKey = aff.morpheme + aff.getSignature();
		boolean keyFound = false;
		String endTDplusNBSP = "&nbsp;&nbsp;&nbsp;</td>";
		String []exKeys =  LinguisticData.getInstance().getAllExamplesIds();
		for (int ie=0; ie<exKeys.length; ie++) {
			String key = exKeys[ie];
			if (key.equals(signatureKey)) {
				Vector<Example> exs =  LinguisticData.getInstance().getExample(signatureKey);
				for (int i = 0; i < exs.size(); i++) {
					Example ex1 = (Example) exs.elementAt(i);
					if (!plain)
						output.append("<tr><td>" + nl);
					else 
						output.append(nl);
					output.append(ex1.termExLat);
					if (!plain)
						output.append(endTDplusNBSP);
					if (!plain)
						output.append("<td>" + nl);
					else
						output.append("\t");
					output.append(ex1.termExSyl);
					if (!plain)
						output.append(endTDplusNBSP);
					String exLat = ex1.exampleLat;
					StringBuffer exLat1 = new StringBuffer();
					exLat1.append(exLat.substring(0, exLat.indexOf('[')));
					if (!plain)
						exLat1.append("<span style=\"color:red\">");
					exLat1.append(
						exLat.substring(
							exLat.indexOf('[') + 1,
							exLat.indexOf(']')));
					if (!plain)
						exLat1.append("</span>");
					exLat1.append(exLat.substring(exLat.indexOf(']') + 1));
					if (!plain)
						output.append("<td>" + nl);
					else
						output.append("\t");
					output.append(exLat1.toString());
					if (!plain)
						output.append(endTDplusNBSP);
					String exSyl = ex1.exampleSyl;
					StringBuffer exSyl1 = new StringBuffer();
					exSyl1.append(exSyl.substring(0, exSyl.indexOf('[')));
					if (!plain)
						exSyl1.append("<span style=\"color:red\">");
					exSyl1.append(
						exSyl.substring(
							exSyl.indexOf('[') + 1,
							exSyl.indexOf(']')));
					if (!plain)
						exSyl1.append("</span>");
					exSyl1.append(exSyl.substring(exSyl.indexOf(']') + 1));
					if (!plain)
						output.append("<td>" + nl);
					else
						output.append("\t");
					output.append(exSyl1.toString());
					if (!plain)
					output.append(endTDplusNBSP);
					if (!plain)
						output.append("<td>" + nl);
					output.append((lang.equals("en")) ? ex1.eng : ex1.fre);
					if (!plain)
						output.append(fintd);
					if (!plain)
						output.append("</tr>");
				}
				keyFound = true;
				break;
			}
		}
		if (!plain)
			output.append("</table>");
		if (!keyFound)
			output.append(
				(lang.equals("en"))
					? "No example available at this moment."
					: "Aucun exemple disponible pour le moment.");
		output.append(endParagraph());
		
        // Sources
        String[] sources = aff.sources;
        if (sources != null) {
        	output.append(startParagraph());
            if (!plain) output.append("<span style=\"font-weight:bold\">");
    		output.append("Sources");
            if (!plain) output.append("</span>"); else output.append(":");
            output.append(newLine());
            for (int n = 0; n < sources.length; n++) {
                Source src =  LinguisticData.getInstance().getSource(sources[n]);
                output.append(src.authorSurName+", "+src.authorFirstName);
                if (src.authorMidName!=null)
                    output.append(" "+src.authorMidName);
                output.append(", \"");
                if (!plain)
                	output.append("<i>");
                output.append(src.title);
                if (src.subtitle!=null)
                    output.append(" "+src.subtitle);
                output.append("\"");
                if (!plain)
                	output.append("</i>");
                output.append(". ");
                output.append(src.publisher);
                if (src.publisherMisc!=null)
                    output.append(", "+src.publisherMisc);
                output.append(", "+src.location+", "+src.year+".");
                output.append(blankLine());
            }
            output.append(endParagraph());
        }

		return output.toString();

	}
	
	/* NOT USED --- private static String process_marked_up(String marked_up) {
		Pattern pat = Pattern.compile("\\[\\[(.+?)\\]\\]");
		Matcher mpat = pat.matcher(marked_up);
		String res = "";
		int pos = 0;
		while (mpat.find(pos)) {
			res += marked_up.substring(pos, mpat.start());
			res += "<a href=\"javascript:appelerDescriptionItem('";
			res += mpat.group(1);
			res += "','";
            if (inuktitutDisplayFont==null)
                 res += "null"; 
            else
                res += inuktitutDisplayFont; 
			res += "');\">";
			res += mpat.group(1);
			res += "</a>";
			pos = mpat.end();
		}
		res += marked_up.substring(pos);
		return res;
	}*/

    private static String composeAction1(
            Action action1,
            Suffix aff,
            String form,
            String cntxt,
            String lang) throws LinguisticDataException {
            
            String morpheme = aff.morpheme;
            StringBuffer output = new StringBuffer();
            int action1Type = action1.getType();
            if (action1Type == Action.UNKNOWN) {
                output.append(Util.premiereMaj( LinguisticData.getInstance().getTextualRendering("l'", lang)));
                if (lang.equals("en"))
                    output.append(" ");
                output.append("action ");
                output.append( LinguisticData.getInstance().getTextualRendering("estSing", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("inconnue", lang));
            } else if (action1Type == Action.NEUTRAL) {
                if (action1.getCondition() != null) {
                    if (lang.equals("en")) {
                        output.append("If the stem ");
                        if (action1.getCondition().startsWith("!"))
                            output.append("does not meet ");
                        else
                            output.append("meets ");
                        output.append("the condition ");
                    } else if (lang.equals("fr")) {
                        output.append("Si le radical ");
                        if (action1.getCondition().startsWith("!"))
                            output.append("ne rencontre pas ");
                        else
                            output.append("rencontre ");
                        output.append("la condition ");
                    }
                    if (action1.getCondition().startsWith("!("))
                        output.append(action1.getCondition().substring(2,action1.getCondition().length()-1));
                    else if (action1.getCondition().startsWith("!"))
                        output.append(action1.getCondition().substring(1));
                    else
                        output.append(action1.getCondition());
                output.append(", ");
                output.append( LinguisticData.getInstance().getTextualRendering("il", lang) + " ");
                } else
                    output.append(Util.premiereMaj( LinguisticData.getInstance().getTextualRendering("il", lang)) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("neutre", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("la", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("finale", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("duradical", lang));
            } else if (action1Type == Action.DELETION) {
                output.append(Util.premiereMaj( LinguisticData.getInstance().getTextualRendering("il", lang)) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("suppr", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("le", lang) + " ");
                output.append(cntxt + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("duradical", lang));
            } else if (action1Type == Action.CONDITIONALDELETION) {
                if (lang.equals("en")) {
                    output.append("If the stem ");
                    if (action1.getCondition().startsWith("!"))
                        output.append("does not meet ");
                    else
                        output.append("meets ");
                    output.append("the condition ");
                }
                else if (lang.equals("fr")) {
                    output.append("Si le radical ");
                    if (action1.getCondition().startsWith("!"))
                        output.append("ne rencontre pas ");
                    else
                        output.append("rencontre ");
                    output.append("la condition ");
                }
                if (action1.getCondition().startsWith("!"))
                    output.append(action1.getCondition().substring(1));
                else
                    output.append(action1.getCondition());
                output.append(", ");
                output.append( LinguisticData.getInstance().getTextualRendering("il", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("suppr", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("le", lang) + " ");
                output.append(cntxt + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("duradical", lang));
            } else if (action1Type == Action.VOICING) {
                output.append(Util.premiereMaj( LinguisticData.getInstance().getTextualRendering("il", lang)) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("sonor", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("le", lang) + " ");
                output.append(cntxt + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("duradical", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("en", lang) + " ");
//              char so = Roman.voicedOfOcclusiveUnvoicedLat(cntxt.charAt(0));
                String son = Action.Voicing.changementPhonologique(cntxt.charAt(0));
                output.append(son);
                String orth = Roman.orthographyOfVoiced(son);
                if (!orth.equals(son)) {
                    output.append(lang.equals("en")?", written ":", écrit ");
                    output.append(orth);
                }
            } else if (action1Type == Action.NASALIZATION) {
                output.append(Util.premiereMaj( LinguisticData.getInstance().getTextualRendering("il", lang)) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("nasal", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("le", lang) + " ");
                output.append(cntxt + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("duradical", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("en", lang) + " ");
//              char na = Roman.nasalOfOcclusiveUnvoicedLat(cntxt.charAt(0));
//              output.append(Orthography.orthographyICILat(na));
                String son = Action.Nasalization.changementPhonologique(cntxt.charAt(0));
                output.append(son);
                String orth = Roman.orthographyOfVoiced(son);
                if (!orth.equals(son)) {
                    output.append(lang.equals("en")?", written ":", écrit ");
                    output.append(orth);
                }
            } else if (action1Type == Action.FUSION) {
                output.append(Util.premiereMaj( LinguisticData.getInstance().getTextualRendering("il", lang)) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("fusion", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("le", lang) + " ");
                output.append(cntxt + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("duradical", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("avec", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("le", lang) + " ");
                output.append(morpheme.substring(0, 1) + " "); //******
                output.append( LinguisticData.getInstance().getTextualRendering("dusuffixe", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("en", lang) + " ");
                output.append(form.substring(0, 1));
            } else if (action1Type == Action.INSERTION) {
                output.append(Util.premiereMaj( LinguisticData.getInstance().getTextualRendering("il", lang)) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("ins1", lang) + " ");
                if (!plain)
                	output.append("<span style=\"font-weight:bold\">");
                output.append(action1.getInsert());
                if (!plain)
                	output.append("</span>");
                output.append(" " +  LinguisticData.getInstance().getTextualRendering("devantsuffixe", lang));
            } else if (action1Type == Action.ASSIMILATION) {
                output.append(Util.premiereMaj( LinguisticData.getInstance().getTextualRendering("il", lang)) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("assim", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("le", lang) + " ");
                output.append(cntxt + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("duradical", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("au", lang) + " ");
                output.append(form.substring(0, 1) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("dusuffixe", lang));
            } else if (action1Type == Action.SPECIFICASSIMILATION) {
                output.append(Util.premiereMaj( LinguisticData.getInstance().getTextualRendering("il", lang)) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("assim", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("le", lang) + " ");
                output.append(cntxt + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("duradical", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("à", lang) + " ");
                output.append(action1.getAssimA());
            } else if (action1Type == Action.VOWELLENGTHENING) {
                output.append(Util.premiereMaj( LinguisticData.getInstance().getTextualRendering("il", lang)) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("allonge", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("la", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("voyellefinale", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("duradical", lang) + " ");
            } else if (action1Type == Action.DELETIONVOWELLENGTHENING) {
                output.append(Util.premiereMaj( LinguisticData.getInstance().getTextualRendering("il", lang)) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("suppr", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("le", lang) + " ");
                output.append(cntxt + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("duradical", lang) + ".  ");
                output.append((lang.equals("en") ? "Then, it " : "Ensuite, il "));
                output.append( LinguisticData.getInstance().getTextualRendering("allonge", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("la", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("voyellefinale", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("duradical", lang) + " ");
            } else if (action1Type == Action.INSERTIONVOWELLENGTHENING) {
                output.append(Util.premiereMaj( LinguisticData.getInstance().getTextualRendering("il", lang)) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("ins1", lang) + " ");
                if (!plain)
                	output.append("<span style=\"font-weight:bold\">");
                output.append(action1.getInsert());
                if (!plain)
                	output.append("</span>");
                output.append(" " +  LinguisticData.getInstance().getTextualRendering("devantsuffixe", lang) + ".  ");
                output.append((lang.equals("en") ? "Then, it " : "Ensuite, il "));
                output.append( LinguisticData.getInstance().getTextualRendering("allonge", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("la", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("voyellefinale", lang) + " ");
                output.append( LinguisticData.getInstance().getTextualRendering("duradical", lang) + " ");
            }
            output.append(".");
            return output.toString();
        }

    private static String composeAction2(
            Action action1,
            Action action2,
            Suffix aff,
            String form,
            String cntxt,
            String lang) throws LinguisticDataException {
            
            StringBuffer output = new StringBuffer();
            int action1Type = action1.getType();
            int action2Type = action2.getType();
            if (action2Type != Action.NULLACTION) {
                output.append((lang.equals("en")?"If":"Si"));
                if (action1Type != Action.NEUTRAL && action1Type != Action.UNKNOWN) {
                    output.append(", ");
                    output.append(lang.equals("en")?"after ":"après ");
                    output.append(action1.toString(lang));
                    output.append(",");
                }
                output.append(" "+ LinguisticData.getInstance().getTextualRendering("casVV", lang) + ", ");
                if (action2Type == Action.UNKNOWN) {
                    output.append( LinguisticData.getInstance().getTextualRendering("l'", lang));
                    if (lang.equals("en"))
                        output.append(" ");
                    output.append("action ");
                    output.append( LinguisticData.getInstance().getTextualRendering("estSing", lang) + " ");
                    output.append( LinguisticData.getInstance().getTextualRendering("inconnue", lang));
                }
                if (action2Type == Action.INSERTION) {
                    output.append( LinguisticData.getInstance().getTextualRendering("il", lang) + " ");
                    output.append( LinguisticData.getInstance().getTextualRendering("ins1", lang) + " ");
                    if (!plain)
                    	output.append("<span style=\"font-weight:bold\">");
                    output.append(action2.getInsert());
                    if (!plain)
                    	output.append("</span>");
                    output.append(" " +  LinguisticData.getInstance().getTextualRendering("devantsuffixe", lang));
                } else if (action2Type == Action.DELETION) {
                    output.append( LinguisticData.getInstance().getTextualRendering("il", lang) + " ");
                    output.append( LinguisticData.getInstance().getTextualRendering("suppr", lang) + " ");
                    output.append( LinguisticData.getInstance().getTextualRendering("la", lang) + " ");
                    output.append( LinguisticData.getInstance().getTextualRendering("derniereVoyelle", lang) + " ");
                    output.append( LinguisticData.getInstance().getTextualRendering("duradical", lang));
                } else if (action2Type == Action.SPECIFICDELETION) {
                    output.append( LinguisticData.getInstance().getTextualRendering("et", lang) + " ");
                    output.append( LinguisticData.getInstance().getTextualRendering("si", lang) + " ");
                    output.append( LinguisticData.getInstance().getTextualRendering("la", lang) + " ");
                    output.append( LinguisticData.getInstance().getTextualRendering("derniereVoyelle", lang) + " ");
                    output.append( LinguisticData.getInstance().getTextualRendering("duradical", lang) + " ");
                    output.append( LinguisticData.getInstance().getTextualRendering("estSing", lang) + " ");
                    output.append(action2.getSuppr() + ", ");
                    output.append( LinguisticData.getInstance().getTextualRendering("il", lang) + " ");
                    output.append( LinguisticData.getInstance().getTextualRendering("suppr", lang) + " ");
                    output.append( LinguisticData.getInstance().getTextualRendering("le", lang) + " ");
                    output.append(action2.getSuppr());
                }
                output.append(".");
            }
            return output.toString();
        }

    public static void displayListOfSuffixes(String args[], PrintStream out) throws LinguisticDataException {
        lang = Util.getArgument(args,"l");
        Hashtable<String,Morpheme> infs =  LinguisticData.getInstance().getId2SuffixTable();
        Roots.displayListOfMorphemes("suf",out,infs,lang);
 }
 
    
    /*
     * Retourne un texte grammatical correspondant � une condition, comme
     * !type:tn,!(type:n,number:d),!(type:n,number:p)
     */
    static String makeCondStr(Conditions cond) {
        String text;
        text = cond.toText(lang);
        return text;
    }
    

}
