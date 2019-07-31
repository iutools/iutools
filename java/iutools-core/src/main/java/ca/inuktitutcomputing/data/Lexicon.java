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

package ca.inuktitutcomputing.data;

import java.util.*;

import ca.inuktitutcomputing.utilities.Debugging;

public class Lexicon {

    static String consonants[] = {
    //		"g","j","k","l","m","n","p","q","r","s","t","v","&"};
            "k", "p", "q", "t" };

    static String consonantsSyl[] = { "\u14a1", "\u153e", "\u1483", "\u14ea",
            "\u14bb", "\u14d0", "\u1449", "\u1585", "\u1550", "\u1505",
            "\u1466", "\u155d", "\u15a6", "\u1596" };

    static String vowels[] = { "a", "i", "u" };

    static String vowelsSyl[] = { "\u140A", "\u1403", "\u1405" };

    // Recherche d'un terme dans le lexique.
    // -------------------------------------
    // Le term est une cha�ne dont le dernier caract�re peut �tre un '*',
    // qui tient la place d'une consonne possiblement supprim�e par un
    // affixe, ou qui tient une place vide. Par exemple, on pourrait
    // avoir: "ANI*" apr�s d�termination de l'affixe "NNGIT" dans le term
    // "ANINNGITTUQ"; dans ce cas, la racine est "ANI". On pourrait aussi
    // avoir: "PISU*" apr�s d�termination du m�me affixe dans le term
    // "PISUNNGITTUQ"; dans ce cas, la racine est "PISUK".

    static public Vector<SurfaceFormOfAffix> lookForForms(String term, boolean syllabic) {
        String cons[], vows[];
        Vector<SurfaceFormOfAffix> termsFound = null;
        cons = syllabic ? consonantsSyl : consonants;
        vows = syllabic ? vowelsSyl : vowels;

        // V�rifier si le term se termine par '%*'
        if (term.endsWith("%*")) {
            Vector<SurfaceFormOfAffix> termFound;
            String termWithoutPniE, termWithVowel;
            Vector<SurfaceFormOfAffix> termWithoutStarFound;
            termsFound = new Vector<SurfaceFormOfAffix>();
            termWithoutPniE = term.substring(0, term.length() - 2);
            termWithoutStarFound = lookForForms(termWithoutPniE + "*",
                    syllabic);
            if (termWithoutStarFound != null)
                termsFound.addAll(termWithoutStarFound);
            for (int i = 0; i < vows.length; i++) {
                termWithVowel = termWithoutPniE + vows[i] + "*";
                termFound = lookForForms(termWithVowel, syllabic);
                if (termFound != null)
                    termsFound.addAll(termFound);
            }
            if (termsFound.size() == 0)
                termsFound = null;
        }
        // V�rifier si le term se termine par '*'
        else if (term.endsWith("*")) {
            Vector<SurfaceFormOfAffix> termFound;
            String termWithoutStar, termWithConsonant;
            termsFound = new Vector<SurfaceFormOfAffix>();
            termWithoutStar = term.substring(0, term.length() - 1);
            //	    termWithoutStarFound = chercherAffixe(termWithoutStar,syllabic);
            //	    if (termWithoutStarFound != null)
            //		termsFound.addAll(termWithoutStarFound);
            for (int i = 0; i < cons.length; i++) {
                termWithConsonant = termWithoutStar + cons[i];
                termFound = lookForForms(termWithConsonant, syllabic);
                if (termFound != null)
                    termsFound.addAll(termFound);
            }
            if (termsFound.size() == 0)
                termsFound = null;
        } else {
            // On cherche un affixe, de n'importe quel type:
            // terminaison verbale ou nominale, ou suffixe.
            termsFound = LinguisticDataAbstract.getSurfaceForms(term);
        }

        return termsFound;
    }

    static public Vector<Morpheme> lookForBase(String term, boolean syllabic) {
        Vector<Morpheme> termsFound;
        String cons[], vows[];
        Debugging.mess("lookForBase/2", 1, "> term= " + term + "  syllabic="
                + syllabic);
        cons = syllabic ? consonantsSyl : consonants;
        vows = syllabic ? vowelsSyl : vowels;
        //System.out.println("lookForBase: term= "+term);
        // Vérifier si le term se termine par '%*'
        if (term.endsWith("%*")) {
            termsFound = new Vector<Morpheme>();
            String termWithoutPniE = term.substring(0, term.length() - 2);
            Vector<Morpheme> termWithoutStarFound = lookForBase(termWithoutPniE + "*", syllabic);
            if (termWithoutStarFound != null)
                termsFound.addAll(termWithoutStarFound);
            for (int i = 0; i < vows.length; i++) {
                String termWithVowel = termWithoutPniE + vows[i] + "*";
                Vector<Morpheme> termsFoundForVowel = lookForBase(termWithVowel, syllabic);
                if (termsFoundForVowel != null)
                    termsFound.addAll(termsFoundForVowel);
            }
            if (termsFound.size() == 0)
                termsFound = null;
        }
        // Vérifier si le term se termine par '*'
        else if (term.endsWith("*")) {
            termsFound = new Vector<Morpheme>();
            String termWithoutStar = term.substring(0, term.length() - 1);
            Vector<Morpheme> termWithoutStarFound = lookForBase(termWithoutStar, syllabic);
            if (termWithoutStarFound != null)
                termsFound.addAll(termWithoutStarFound);
            for (int i = 0; i < cons.length; i++) {
                String termWithConsonant = termWithoutStar + cons[i];
                Vector<Morpheme> termFoundForConsonant = lookForBase(termWithConsonant, syllabic);
                if (termFoundForConsonant != null)
                    termsFound.addAll(termFoundForConsonant);
            }
            if (termsFound.size() == 0)
                termsFound = null;
        } else {
            // On cherche une racine
            termsFound = LinguisticDataAbstract.getBases(term);
        }
        Debugging.mess("lookForBase/2", 1, "termsFound= " + termsFound);
        return termsFound;
    }

}