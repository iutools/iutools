package org.iutools.linguisticdata;

import java.util.Hashtable;

public class TextualRendering {

    public static Hashtable<String,String[]> makeHashOfTextualRenderings() {
    	Hashtable<String,String[]> textualRenderings = new Hashtable<String,String[]>();
        textualRenderings.put("dec", new String[] { "declarative", "déclaratif" });
        textualRenderings.put("int", new String[] { "interrogative", "interrogatif" });
        textualRenderings.put("imp", new String[] { "imperative", "impératif" });
        textualRenderings.put("part", new String[] { "participle", "participe" });
        textualRenderings.put("prespas", new String[] { "present and past",
                "présent et passé" });
        textualRenderings.put("fut", new String[] { "future", "futur" });
        textualRenderings.put("pos", new String[] { "positive", "positif" });
        textualRenderings.put("neg", new String[] { "negative", "négatif" });
        textualRenderings.put("caus", new String[] { "becausative", "causatif" });
        textualRenderings.put("freq", new String[] { "frequentative", "fréquentatif" });
        textualRenderings.put("cond", new String[] { "conditional", "conditionnel" });
        textualRenderings.put("dub", new String[] { "dubitative", "dubitatif" });
        textualRenderings.put("tv",
                new String[] { "verbal ending", "terminaison verbale" });
        textualRenderings.put("q", new String[] { "tail suffix", "suffixe de queue" });
        textualRenderings.put("tn",
                new String[] { "noun ending", "terminaison nominale" });
        textualRenderings.put("sv", new String[] { "verbal suffix", "suffixe verbal" });
        textualRenderings.put("sn", new String[] { "noun suffix", "suffixe nominal" });
        textualRenderings
                .put("function",
                        new String[] { "producing a", "produisant un" });
        textualRenderings.put("vv", new String[] { "verb-to-verb", "verbe-à-verbe" });
        textualRenderings.put("nv", new String[] { "noun-to-verb", "nom-à-verbe" });
        textualRenderings.put("vn", new String[] { "verb-to-noun", "verbe-à-nom" });
        textualRenderings.put("nn", new String[] { "noun-to-noun", "nom-à-nom" });
        textualRenderings.put("nsp", new String[] { "non-specific", "non-spécifique" });
        textualRenderings.put("sp", new String[] { "specific", "spécifique" });
        textualRenderings.put("s", new String[] { "singular", "singulier" });
        textualRenderings.put("d", new String[] { "dual", "duel" });
        textualRenderings.put("p", new String[] { "plural", "plural" });
        textualRenderings.put("n", new String[] { "noun", "nom" });
        textualRenderings.put("v", new String[] { "verb", "verbe" });
        textualRenderings.put("a", new String[] { "adverb", "adverbe" });
        textualRenderings.put("e", new String[] { "expression or exclamation",
                "expression ou exclamation" });
        textualRenderings.put("c", new String[] { "conjunction", "conjonction" });
        textualRenderings.put("pr", new String[] { "pronoun", "pronom" });
        textualRenderings.put("m", new String[] { "medial", "médiane" });
        textualRenderings.put("t", new String[] { "terminal", "terminale" });
        textualRenderings.put("f", new String[] { "final", "finale" });
        textualRenderings.put("V", new String[] { "vowel", "voyelle" });
        textualRenderings.put("C", new String[] { "consonant", "consonne" });
        textualRenderings.put("VV", new String[] { "vowels", "voyelles" });
        textualRenderings.put("VC", new String[] { "vowel or consonant",
                "voyelle ou consonne" });
        textualRenderings.put("1ordinal", new String[] { "1st", "1ère" });
        textualRenderings.put("2ordinal", new String[] { "2nd", "2ème" });
        textualRenderings.put("3ordinal", new String[] { "3rd", "3ème" });
        textualRenderings.put("4ordinal", new String[] { "4th", "4ème" });
        textualRenderings.put("personne", new String[] { "person", "personne" });

        textualRenderings.put("nom", new String[] { "nominative", "nominatif" });
        textualRenderings.put("gen", new String[] { "genitive", "génitif" });
        textualRenderings.put("acc", new String[] { "accusative", "accusatif" });
        textualRenderings.put("abl", new String[] { "ablative", "ablatif" });
        textualRenderings.put("dat", new String[] { "dative", "datif" });
        textualRenderings.put("loc", new String[] { "locative", "locatif" });
        textualRenderings.put("sim", new String[] { "similaris", "similaris" });
        textualRenderings.put("via", new String[] { "vialis", "vialis" });
        textualRenderings.put("possessif", new String[] { "possessive", "possessif" });
        textualRenderings.put("possesseur", new String[] { "Possessor", "Possesseur" });
        textualRenderings.put("vt", new String[] { "transtitive", "transitif" });
        textualRenderings.put("vt1", new String[] { "transtitive", "transitif" });
        textualRenderings.put("vt2", new String[] { "transtitive", "transitif" });
        textualRenderings.put("vi", new String[] { "intransitive", "transitive" });
        textualRenderings.put("va", new String[] { "adjectival", "adjectif" });
        textualRenderings.put("ve", new String[] { "emotion, feeling",
                "d'émotion,de sentiment" });
        textualRenderings.put("vres", new String[] { "result", "de résultat" });
        textualRenderings
                .put(
                        "vres+",
                        new String[] {
                                "When this kind of verb is used transitively, the thing upon which the action "
                                        + "is done is the object of the verb.  When it is used intransitively, that thing "
                                        + "is the subject of the verb.  Similar verbs in English: to boil, to shatter.",
                                "Quand ce type de verbe est utilisé transitivement, la chose sur laquelle porte l'action "
                                        + "est l'objet du verbe.  Quand il est utilisé intransitivement, cette chose "
                                        + "est le sujet du verbe." });
        textualRenderings
                .put(
                        "m!",
                        new String[] {
                                "must be followed by another suffix, i.e. it cannot occur in word-final position",
                                "doit être suivi d'un autre suffixe, i.e. il ne peut pas se trouver à la fin d'un mot" });
        textualRenderings
                .put(
                        "f!",
                        new String[] {
                                "occurs only in word-final position, i.e. it cannot be followed by another suffix",
                                "ne peut se trouver qu'à la fin d'un mot, i.e. il ne peut pas être suivi d'un autre suffixe" });
        textualRenderings
                .put(
                        "t!",
                        new String[] {
                                "may occur in word-final position, but may also be followed by additional suffixes",
                                "peut se trouver à la fin d'un mot, mais peut cependant être suivi d'autres suffixes" });
        textualRenderings.put("neutre",
                new String[] { "does not affect", "n'affecte pas" });
        textualRenderings.put("suppr", new String[] { "deletes", "supprime" });
        textualRenderings.put("suppr1", new String[] { "is deleted", "est supprimé" });
        textualRenderings.put("nasal", new String[] { "nasalizes", "nasalise" });
        textualRenderings.put("nasal1", new String[] { "is nasalized to",
                "est nasalisé en" });
        textualRenderings.put("sonor", new String[] { "vocalizes", "sonorise" });
        textualRenderings.put("assim", new String[] { "assimilates", "assimile" });
        textualRenderings.put("assim2", new String[] { "to", "précédent à" });
        textualRenderings.put("allonge", new String[] { "lengthens", "allonge" });
        textualRenderings.put("fusion", new String[] { "fusions", "fusionne" });
        textualRenderings.put("sonor", new String[] { "vocalizes", "sonorise" });
        textualRenderings.put("sur", new String[] { "on", "sur" });
        textualRenderings.put("en", new String[] { "into", "en" });
        textualRenderings.put("au", new String[] { "to the", "au" });
        textualRenderings.put("du", new String[] { "of the", "du" });
        textualRenderings.put("à", new String[] { "to", "à" });
        textualRenderings.put("avec", new String[] { "with", "avec" });
        textualRenderings.put("voyellefinale", new String[] { "end vowel",
                "voyelle finale" });
        textualRenderings.put("finale", new String[] { "final", "finale" });
        textualRenderings.put("le", new String[] { "the", "le" });
        textualRenderings.put("la", new String[] { "the", "la" });
        textualRenderings.put("duradical", new String[] { "of the stem", "du radical" });
        textualRenderings
                .put("dusuffixe",
                        new String[] { "of the suffix", "du suffixe" });
        textualRenderings.put("ins1", new String[] { "inserts", "insére" });
        textualRenderings.put("devantsuffixe", new String[] { "in front of the suffix",
                "devant le suffixe" });
        textualRenderings.put("derniereVoyelle", new String[] { "last vowel",
                "dernière voyelle" });
        textualRenderings.put("casVV", new String[] { "the stem ends with 2 vowels",
                "le radical se termine par 2 voyelles" });
        textualRenderings.put("supprv2", new String[] { "the second vowel is deleted",
                "la seconde voyelle est supprimée" });
        textualRenderings.put("après", new String[] { "After", "Après" });
        textualRenderings.put("une", new String[] { "a", "une" });
        textualRenderings.put("deux", new String[] { "two", "deux" });
        textualRenderings.put("il", new String[] { "it", "il" });
        textualRenderings.put("et", new String[] { "and", "et" });
        textualRenderings.put("si", new String[] { "if", "si" });
        textualRenderings.put("estSing", new String[] { "is", "est" });
        textualRenderings.put("l'", new String[] { "the", "l'" });
        textualRenderings.put("inconnue", new String[] { "unknown", "inconnue" });

        // sources
        textualRenderings
        .put(
                "A1",
                new String[] {
                        "Alex Spalding, \"Inuktitut - A Grammar of North Baffin Dialects\", Wuerz Publishing Ltd., Winnipeg, 1992",
                        "Alex Spalding, \"Inuktitut - A Grammar of North Baffin Dialects\", Wuerz Publishing Ltd., Winnipeg, 1992" });
        textualRenderings
                .put(
                        "A2",
                        new String[] {
                                "A. Spalding, \"Inuktitut - A Multi-dialectal Outline Dictionary (with an Aivilingmiutaq base)\", Nunavut Arctic College, 1998",
                                "A. Spalding, \"Inuktitut - A Multi-dialectal Outline Dictionary (with an Aivilingmiutaq base)\", Nunavut Arctic College, 1998" });
        textualRenderings
                .put(
                        "H1",
                        new String[] {
                                "Kenn Harper, \"Suffixes of the Eskimo dialects of Cumberland Peninsula and North Baffin Island\", National Museum of Man, Mercury Series, Canadian Ethnology Service, Paper no. 54, Ottawa, 1979",
                                "Kenn Harper, \"Suffixes of the Eskimo dialects of Cumberland Peninsula and North Baffin Island\", Musée national de l'Homme, Collecion Mercure, Service canadien d'ethnologie, Dossier no. 54, Ottawa, 1979" });
        textualRenderings
                .put(
                        "H2",
                        new String[] {
                                "Kenn Harper, \"Some aspects of the grammar of the Eskimo dialects of Cumberland Peninsula and North Baffin Island\", National Museum of Man, Mercury Series, Ethnology Division, Paper no. 15, Ottawa, 1974",
                                "Kenn Harper, \"Some aspects of the grammar of the Eskimo dialects of Cumberland Peninsula and North Baffin Island\", Musée national de l'Homme, Collection Mercure, Division d'ethnologie, Dossier no. 15, Ottawa, 1974" });
        textualRenderings
                .put(
                        "M1",
                        new String[] {
                                "M. Mallon, \"Introductory Inuktitut Reference Grammar version 2.1\", Nunavut Arctic College, Ittukuluuk Language Programs, Iqaluit & Victoria, 1995",
                                "M. Mallon, \"Introductory Inuktitut Reference Grammar version 2.1\", Nunavut Arctic College, Ittukuluuk Language Programs, Iqaluit & Victoria, 1995" });
        textualRenderings.put("Hnsrd", new String[] { "Hansards of Nunavut",
                "Hansards du Nunavut" });
        textualRenderings
                .put(
                        "S1",
                        new String[] {
                                "L. Schneider, \"Dictionnaire des infixes de la langue eskimaude\", Minist�re des Affaires culturelles, Direction générale du Patrimoine, Dossier 43, 1979",
                                "L. Schneider, \"Dictionnaire des infixes de la langue eskimaude\", Minist�re des Affaires culturelles, Direction générale du Patrimoine, Dossier 43, 1979" });
        textualRenderings
                .put(
                        "S2",
                        new String[] {
                                "L. Schneider, \"Ulirnaisigutiit - An Inuktitut-English Dictionary of Northern Quebec, Labrador and Eastern Arctic Dialects\", Les Presses de l'Université Laval, Québec, 1985",
                                "L. Schneider, \"Ulirnaisigutiit - An Inuktitut-English Dictionary of Northern Quebec, Labrador and Eastern Arctic Dialects\", Les Presses de l'Université Laval, Québec, 1985" });
        // 	textualRenderings.put("",new String [] {"",""});
        // 	textualRenderings.put("",new String [] {"",""});
        // 	textualRenderings.put("",new String [] {"",""});
        return textualRenderings;
    }
}
