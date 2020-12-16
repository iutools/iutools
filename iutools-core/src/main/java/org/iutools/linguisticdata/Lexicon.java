// @@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@
//
// -----------------------------------------------------------------------
//           (c) Conseil national de recherches Canada, 2002
//           (c) National Research Council of Canada, 2002
// -----------------------------------------------------------------------

// -----------------------------------------------------------------------
// Document/File: Lexique.java
//
// Type/File type: code Java / Java code
// 
// Auteur/Author: Benoit Farley
//
// Organisation/Organization: Conseil national de recherches du Canada/
//				National Research Council Canada
//
// Date de cr�ation/Date of creation:
//
// Description: Classe et m�thodes reli�es au lexique et � la recherche
//              lexicale. Racines, affixesExp.
//
// -----------------------------------------------------------------------

package org.iutools.linguisticdata;

import java.util.*;

import org.iutools.script.Syllabics;

public class Lexicon {

    public static String consonants[] = {
    //		"g","j","k","l","m","n","p","q","r","s","t","v","&"};
            "k", "p", "q", "t" };

    public static String consonantsSyl[] = { "\u14a1", "\u153e", "\u1483", "\u14ea",
            "\u14bb", "\u14d0", "\u1449", "\u1585", "\u1550", "\u1505",
            "\u1466", "\u155d", "\u15a6", "\u1596" };

    // Recherche d'un terme dans le lexique.
    static public Vector<SurfaceFormOfAffix> lookForForms(String term, boolean syllabic) throws LinguisticDataException {
        Vector<SurfaceFormOfAffix> formsFound = LinguisticData.getInstance().getSurfaceForms(term);
        return formsFound;
    }

    /**
     * Returns a Vector of Morpheme (Base and Demonstrative) objects, or null.
     * @param term String string in the ICI (Inuit Cultural Institute) standard
     * @param syllabic boolean indicates whether the term is in syllabic (true) or not (false)
     * @return Vector<Morpheme> a vector of Morpheme objects or null
     * @throws LinguisticDataException 
     */
    static public Vector<Morpheme> lookForBase(String term, boolean syllabic) throws LinguisticDataException {
    	if (syllabic)
    		term = Syllabics.transcodeToRoman(term);
        Vector<Morpheme> basesFound = LinguisticData.getInstance().getBasesForCanonicalForm(term);
        return basesFound;
    }

}