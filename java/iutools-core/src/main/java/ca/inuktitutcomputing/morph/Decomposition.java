//@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//
// -----------------------------------------------------------------------
//           (c) Conseil national de recherches Canada, 2002
//           (c) National Research Council of Canada, 2002
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
// Document/File:		Decomposition.java
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
// Description: Classe d�crivant un terme d�compos� en ses diverses
//              parties: base de mot et suffixes.
//
// -----------------------------------------------------------------------

package ca.inuktitutcomputing.morph;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.data.Base;
import ca.inuktitutcomputing.data.Morpheme;
import ca.inuktitutcomputing.script.Orthography;

// Decomposition:
//    String word
//    MorceauRacine stem:
//        Base racine:
//           ...
//        String terme
//        int position
//        String niveau
//    Object [] morphParts:
//        MorceauAffixe morceau:
//            SurfaceFormOfAffix form
//            

public class Decomposition extends Object implements Comparable<Decomposition> {

	String word;
	RootPartOfComposition stem;
	public AffixPartOfComposition[] morphParts;

	// Au moment de la cr�ation d'un objet Decomposition, on d�termine
	// la cha�ne de caract�res � l'int�rieur du mot pour chaque morceau,
	// � partir de la position sauvegard�e dans l'objet MorceauAffixe
	// lors de la d�composition.

	public Decomposition(String word, RootPartOfComposition r, AffixPartOfComposition[] parts) {
		this.word = word;
		stem = r;
		String origState = stem.arc.startState.id;
		int nextPos = word.length();
		for (int i = parts.length - 1; i >= 0; i--) {
			AffixPartOfComposition m = parts[i];
			int pos = m.getPosition();
			m.setTerme(
				Orthography.orthographyICI(word.substring(pos, nextPos), false) );
			nextPos = pos;
		}
		for (int i=0; i<parts.length; i++) {
			// 'arc' de chaque morceau
			AffixPartOfComposition m = parts[i];
			for (int j=0; j<m.arcs.length; j++) {
			    if (m.arcs[j].destState.id.equals(origState)) {
			        m.arc = m.arcs[j];
			        origState = m.arc.startState.id;
			        break;
			    }
			}
		}
		morphParts = parts;
		//stem.terme = word.substring(0,nextPos);
	}

	public RootPartOfComposition getRootMorphpart() {
		return stem;
	}

	public Object[] getMorphParts() {
		return morphParts;
	}
    
    public void setMorphParts(AffixPartOfComposition [] parts) {
        morphParts = parts;
    }
    
    public AffixPartOfComposition getLastMorphpart() {
    	if (morphParts.length==0)
    		return null;
    	else
    		return morphParts[morphParts.length-1];
    }

	public int getNbMorphparts() {
		return morphParts.length;
	}

	//	// - Les racines les plus longues en premier
	//	// - Le nombre mininum de morphParts
	//	// - Les racines connues en premier
	//	public int compareTo(Object a) {
	//		int valeurRetour = 0;
	//		Decomposition otherDec = (Decomposition) a;
	//		boolean known = ((Base) stem.getRoot()).known;
	//		boolean otherDecConnue = ((Base) otherDec.stem.getRoot()).known;
	//		if ((known && otherDecConnue) || (!known && !otherDecConnue))
	//			valeurRetour = 0;
	//		else if (known && !otherDecConnue)
	//			valeurRetour = -1;
	//		else if (!known && otherDecConnue)
	//			valeurRetour = 1;
	//		if (valeurRetour == 0) {
	//			Integer lengthOfRoot =
	//				new Integer(((Base) stem.getRoot()).morpheme.length());
	//			Integer lengthOfRootOfOtherDec =
	//				new Integer(
	//					((Base) otherDec.stem.getRoot()).morpheme.length());
	//			valeurRetour = lengthOfRoot.compareTo(lengthOfRootOfOtherDec);
	//			if (valeurRetour == 0) {
	//				Integer nbOfMorphparts = new Integer(morphParts.length);
	//				Integer nbOfMorphpartsOfOtherDec =
	//					new Integer(otherDec.morphParts.length);
	//				valeurRetour = nbOfMorphparts.compareTo(nbOfMorphpartsOfOtherDec);
	//			}
	//		}
	//		return valeurRetour;
	//	} 

//	 - Les racines connues en premier
	// - Les racines les plus longues
	// - Le nombre mininum de morphParts en premier
	public int compareTo(Decomposition obj) {
		int returnValue = 0;
		Decomposition otherDec = (Decomposition) obj;
//		boolean known = ((Base) stem.getRoot()).known;
//		boolean otherDecConnue = ((Base) otherDec.stem.getRoot()).known;
//		if ((known && otherDecConnue) || (!known && !otherDecConnue))
//			returnValue = 0;
//		else if (known && !otherDecConnue)
//			returnValue = -1;
//		else if (!known && otherDecConnue)
//			returnValue = 1;
		if (returnValue == 0) {
			Integer lengthOfRoot =
				new Integer(((Base) stem.getRoot()).morpheme.length());
			Integer lengthOfRootOfOtherDec =
				new Integer(
					((Base) otherDec.stem.getRoot()).morpheme.length());
			returnValue = lengthOfRootOfOtherDec.compareTo(lengthOfRoot);
			if (returnValue == 0) {
				Integer nbOfMorphparts = new Integer(morphParts.length);
				Integer nbOfMorphpartsOfOtherDec = new Integer(otherDec.morphParts.length);
				returnValue = nbOfMorphparts.compareTo(nbOfMorphpartsOfOtherDec);
			}
		}
		return returnValue;
	}

	public boolean isEqualDecomposition(Decomposition dec) {
		if (this.toStr2().equals(dec.toStr2()))
			return true;
		else
			return false;
	}

	// Note: � faire avec des HashSet: plus rapide probablement.
	static public Decomposition[] removeMultiples(Decomposition[] decs) {
		if (decs == null || decs.length == 0)
			return decs;
		Vector<Decomposition> v = new Vector<Decomposition>();
		Vector<String> vc = new Vector<String>();
		v.add(decs[0]);
		vc.add(decs[0].toStr2());
		for (int i = 1; i < decs.length; i++) {
			String c = decs[i].toStr2();
			if (!vc.contains(c)) {
				v.add(decs[i]);
				vc.add(c);
			}
		}
		return (Decomposition[]) v.toArray(new Decomposition[] {
		});
	}
	
	
    // Éliminer les décompositions qui contiennent une suite de suffixes
    // pour laquelle il existe un suffixe composé, pour ne garder que
    // la décompositions dans laquelle se trouve le suffixe composé.
	// For example, apiqsuqtaujuksaq : there is a suffix -juksaq/vn which
	// is the combination of -juq/vn and -ksaq/nn. The analyzer will find
	// a decomposition with -juksaq but will also find a decomposition with
	// juq+ksaq ; this is to remove the latter.
	static public Decomposition[] removeCombinedSuffixes(Decomposition decs[]) {
		Logger logger = Logger.getLogger("Decomposition.removeCombinedSuffixes");
        Object[][] decsAndKeepstatus = new Object[decs.length][2];
        for (int i = 0; i < decs.length; i++) {
        	logger.debug("decs["+i+"] = "+decs[i].toStr2());
            decsAndKeepstatus[i][0] = decs[i];
            decsAndKeepstatus[i][1] = new Boolean(true); // initialiser à true (keep)
        }

        for (int i = 0; i < decsAndKeepstatus.length; i++) {
        	// Pendant l'exécution de cette boucle, certaines décompositions 
        	// plus loin dans la liste et pas encore traitées peuvent avoir été rejetées ;
            // on ne considère que les décompositions qui n'ont pas encore été rejetées.
            if (((Boolean) decsAndKeepstatus[i][1]).booleanValue()) { 
                Decomposition dec = (Decomposition) decsAndKeepstatus[i][0];
                Vector<AffixPartOfComposition> affixesOfDecompisition = new Vector<AffixPartOfComposition>(Arrays.asList(dec.morphParts));
//                vParts.add(0,dec.stem); // je ne comprends pas pouquoi j'ai ajouté la racine aux parties
                
                // Pour chaque affixe combiné, trouver celui qui le précède et
                // celui qui le suit, et vérifier dans les autres
                // décompositions retenues si ces deux affixes limites
                // contiennent les éléments de l'affixe combiné.  Si c'est
                // le cas, on rejète ces décompositions.
                for (int indexOfProcessedVPart = 0; indexOfProcessedVPart < affixesOfDecompisition.size(); indexOfProcessedVPart++) {
                    Morpheme morph = affixesOfDecompisition.elementAt(indexOfProcessedVPart).getMorpheme();
                    String[] cs = morph.getCombiningParts();
                    // Seulement pour les affixes combinés.
                    if (cs != null) {
                        // Trouver les décompositions qui ont les éléments du
                        // suffixe combiné flanqués de part et d'autre par les
                        // mêmes morphèmes, et les enlever de la liste.
                    	removeDecsWithCombinationAsSeparateElements(dec,affixesOfDecompisition,indexOfProcessedVPart,cs,decsAndKeepstatus);
                    }
                }
            }
        }
        Vector<Object> v = new Vector<Object>();
        for (int i = 0; i < decsAndKeepstatus.length; i++)
            if (((Boolean) decsAndKeepstatus[i][1]).booleanValue())
                v.add(decsAndKeepstatus[i][0]);
        Decomposition ndecs[] = (Decomposition[]) v
                .toArray(new Decomposition[] {});
        return ndecs;
    }
	
	
	protected static void removeDecsWithCombinationAsSeparateElements(
			Decomposition dec, Vector<AffixPartOfComposition> vParts, int indexOfProcessedVPart,
			String[] cs,
			Object[][] decsAndKeepstatus) {
        // Trouver les décompositions qui ont les éléments du
        // suffixe combiné flanqués de part et d'autre par les
        // mêmes morphèmes, et les enlever de la liste.
        String prec, follow;
        // Morphème précédant le morphème combiné.
        if (indexOfProcessedVPart == 0)
            prec = null;
        else if (indexOfProcessedVPart == 1)
            prec = dec.stem.root.id;
        else
            prec = ((PartOfComposition) vParts.elementAt(indexOfProcessedVPart - 1)).getMorpheme().id;
        // Morphème suivant le morphème combiné.
        if (indexOfProcessedVPart == vParts.size() - 1)
            follow = null;
        else
            follow = ((PartOfComposition) vParts.elementAt(indexOfProcessedVPart + 1)).getMorpheme().id;
        // Vérifier dans les décompositions retenues.
        int k = 0;
        while (k < decsAndKeepstatus.length) {
            // Décompositions retenues seulement.
            if (((Boolean) decsAndKeepstatus[k][1]).booleanValue()) {
                Decomposition deck = (Decomposition) decsAndKeepstatus[k][0];
                Vector<Object> vPartsk = new Vector<Object>(Arrays.asList(deck.morphParts));
                vPartsk.add(0,deck.stem);
                int l = 0;
                boolean cont = true;
                boolean inCombined = false;
                int iCombined = 0;
                // Analyser chaque morphème de cette
                // décomposition pour vérifier s'il
                // correspond à un élément du morphème
                // combiné.
                while (l < vPartsk.size() && cont) {
//                    MorceauAffixe mck = (MorceauAffixe) morphPartsk[l];
//                    Affix affk = mck.getAffix();
                    Morpheme morphk = ((PartOfComposition)vPartsk.elementAt(l)).getMorpheme();
                    if (inCombined) {
                        // On a d�j� d�termin� qu'un ou
                        // plusieurs morph�mes corres-
                        // pondent aux �l�ments du morph�me
                        // combin�.  V�rifier celui-ci.
//                        if (affk.id.equals(cs[iCombined])) {
                        if (morphk.id.equals(cs[iCombined])) {
                            // C'est aussi un �l�ment du
                            // morph�me combin�.
                            iCombined++;
                            if (iCombined == cs.length) {
                                // C'est le dernier �l�ment du
                                // morph�me combin�.  V�rifer
                                // si le morph�me qui le suit
                                // est le m�me que le morph�me
                                // suivant le morph�me combin�.
                                // Si c'est le cas, on rej�te
                                // cette d�composition.  De
                                // toute fa�on, on arr�te cette
                                // v�rification.
                                String followk;
                                if (l == vPartsk.size() - 1)
                                    followk = null;
                                else
                                    followk = ((PartOfComposition) vPartsk.elementAt(l + 1))
                                            .getMorpheme().id;
                                if ( (follow==null && followk==null) ||
                                        (follow!=null && followk!=null && followk.equals(follow)) )
                                	
                                    // *** REJETER CETTE DÉCOMPOSITION ***
                                    decsAndKeepstatus[k][1] = new Boolean(false);
                                
                                cont = false;
                            }
                        } else {
                            // Ce n'est pas un �l�ment du
                            // morph�me combin�.  On remet � 0.
                            inCombined = false;
                            iCombined = 0;
                        }
                    } else {
                        // On n'a pas encore reconnu un
                        // morph�me comme premier �l�ment du
                        // morph�me combin�.  Est-ce que celui-
                        // ci l'est?
                        if (morphk.id.equals(cs[iCombined])) {
                            // Premier �l�ment du morph�me
                            // combin�.
                            inCombined = true;
                            iCombined++;
                            String preck;
                            // V�rifier si le morph�me qui le
                            // pr�c�de est le m�me que le
                            // morph�me qui pr�c�de le morph�me
                            // combin�.  Si c'est le cas, on
                            // continue la v�rification.  Sinon
                            // on arr�te la v�rification de
                            // cette d�composition.
                            if (l == 0)
                                preck = null;
                            else if (l == 1)
                                preck = deck.stem.root.id;
                            else
                                preck = ((PartOfComposition) vPartsk.elementAt(l - 1))
                                        .getMorpheme().id;
                            if ( (preck == null && prec != null) || 
                                    (preck != null && prec == null) ||
                                    (preck != null && prec != null && !preck.equals(prec)))
                                cont = false;
                        }
                    }
                    l++;
                }
            }
            k++;
        }
	}
	
	
	/*
	 * --------------------------------------------------------------------
	 * �criture d'une d�composition
	 * --------------------------------------------------------------------
	 */
	
	static String startDelimitor = "{";
	static String endDelimitor = "}";
	static String interDelimitor = ":";
	
	/*
     * {<forme de surface>:<signature du morph�me>}...
	 */
	public String toStr2() {
		StringBuffer sb = new StringBuffer();
		Object[] morphParts = getMorphParts();
		sb.append(stem.toStr());
		for (int j = 0; j < morphParts.length; j++) {
			AffixPartOfComposition ma = (AffixPartOfComposition) morphParts[j];
//			sb.append("|");
			sb.append(ma.toStr());
		}
		return sb.toString();
	}
	
	public String toString() {
		return this.toStr2();
	}
	
	static public String[] getMeaningsInArrayOfStrings (String decstr, String lang, 
			boolean includeSurface, boolean includeId) {
		DecompositionExpression de = new DecompositionExpression(decstr);
		String mngs[] = de.getMeanings(lang);
		for (int i=0; i<mngs.length; i++) {
			if (includeSurface && includeId)
				mngs[i] = de.parts[i].str+ "---" + mngs[i];
			else if (includeSurface)
				mngs[i] = de.parts[i].surface+ "---" + mngs[i];
			else if (includeId)
				mngs[i] = de.parts[i].morphid+ "---" + mngs[i];
		}
		return mngs;
	}
	
	static public String getMeaningsInString (String decstr, String lang, 
			boolean includeSurface, boolean includeId) {
		DecompositionExpression de = new DecompositionExpression(decstr);
		StringBuffer sb = new StringBuffer();
		String mngs[] = de.getMeanings(lang);
		for (int i=0; i<mngs.length; i++)
			if (includeSurface && includeId)
				sb.append("{").append(de.parts[i].str).append("---").append(mngs[i]).append("}");
			else if (includeSurface)
				sb.append("{").append(de.parts[i].surface).append("---").append(mngs[i]).append("}");
			else if (includeId)
				sb.append("{").append(de.parts[i].morphid).append("---").append(mngs[i]).append("}");
			else
				sb.append("{").append(mngs[i]).append("}");
		return sb.toString();
	}
	
	//----------------------------------------------------------------------------------------------
	/*
     * {<forme de surface>:<signature du morphème>}{...}...
	 */

	static public class DecompositionExpression implements Serializable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		//
		public String decstr;
		protected String partsStr[];
		public DecPart parts[];
		public String meanings[] = null;
		//
		
		public DecompositionExpression() {
		}
		
		public DecompositionExpression (String decstr) {
			this.decstr = decstr;
			partsStr = expr2parts();
			parts = new DecPart[partsStr.length];
			for (int i=0; i<parts.length; i++)
				parts[i] = new DecPart(partsStr[i]);
		}
		
		public DecompositionExpression (String _decstr, DecPart[] _parts) {
			this.decstr = _decstr;
			this.parts = _parts;
		}
		
		public String[] getMeanings(String lang) {
			if (meanings == null) {
				meanings = new String[parts.length];
				for (int i = 0; i < parts.length; i++) {
					meanings[i] = lang.equals("en") ? Morpheme.getMorpheme(parts[i].morphid).englishMeaning
							: Morpheme.getMorpheme(parts[i].morphid).frenchMeaning;
					meanings[i] = meanings[i].replaceAll(" /", " ");
					meanings[i] = meanings[i].replace("^/", "");
				}
			}
			return meanings;
		}
		
		protected String[] expr2parts() {
			Pattern p = Pattern.compile("\\{[^}]+?\\}");
			Matcher mp = p.matcher(decstr);
			ArrayList<String> v = new ArrayList<String>();
			int pos=0;
			while (mp.find(pos)) {
				v.add(mp.group());
				pos = mp.end();
			}
			return (String[])v.toArray(new String[]{});
		}
		
		public String toStringWithoutSurfaceForms() {
			String expr = "";
			for (int ipart=0; ipart<parts.length; ipart++) {
				String str = parts[ipart].toStringWithoutSurfaceForm();
				expr += " " + str;
			}
			return expr.substring(1);
		}
		
		static public class DecPart implements Serializable {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;
			//
			public String str;
			public String surface;
			public String morphid;
			//
			
			public DecPart() {
			}
			
			public DecPart (String str) {
				this.str = str;
				Pattern p = Pattern.compile("\\"+startDelimitor+"(.+?)"+"\\"+endDelimitor);
				Matcher m = p.matcher(str);
				m.matches();
				String[] partParts = Pattern.compile(":").split(m.group(1));
				// We assume that if there is only 1 part, it is because the
				// parts contain only the morpheme's id, with no surface form
				if (partParts.length==1) {
					surface = "";
					morphid = partParts[0];
				} else {
					surface = partParts[0];
					morphid = partParts[1];
				}
			}
			
			public DecPart (String _surface, String id) {
				surface = _surface;
				morphid = id;
				str = startDelimitor + surface + interDelimitor + id + endDelimitor;
			}
			
			public DecPart (String _str, String _surface, String _id) {
				surface = _surface;
				morphid = _id;
				str = _str;
			}
			
			
			public String toStringWithoutSurfaceForm() {
				return startDelimitor + morphid + endDelimitor;
			}
		}
		
	}

	public Boolean containsMorpheme(String morpheme) {
		Boolean result = null;
		if (this.stem.root.id.equals(morpheme))
			result = true;
		if (result==null) {
			for (int imorph=0; imorph<this.morphParts.length; imorph++) {
				if (morphParts[imorph].getAffix().id.equals(morpheme)) {
					result = true;
					break;
				}
			}
		}
		if (result==null)
			result = false;
		
		return result;
	}

	public List<String> morphemeSurfaceForms() {
        List<String> surfaceForms = new ArrayList<String>();
        surfaceForms.add(stem.term);
        for (int ip=0; ip<morphParts.length; ip++) {
            surfaceForms.add(morphParts[ip].term);
        }
       
        return surfaceForms;
    }	

}
