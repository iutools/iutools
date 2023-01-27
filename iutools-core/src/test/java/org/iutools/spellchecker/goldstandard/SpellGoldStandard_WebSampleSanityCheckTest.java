package org.iutools.spellchecker.goldstandard;

import org.iutools.config.IUConfig;
import org.iutools.phonology.Dialect;
import org.iutools.spellchecker.SpellCheckerException;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Test;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * This test reads the Spell Checker gold standard created from a small sample
 * inuktut web pages, and does a bunch of sanity checks on its content
 */
public class SpellGoldStandard_WebSampleSanityCheckTest {

    @Test
    public void test__sanityCheck() throws Exception {

        // Set this to true if you want to see more details about the
        // gold standard
        boolean showDetails = true;

        File gsDir = new File(IUConfig.dataFilePath("data/NunavutWebSample-2020-07"));

        SpellGoldStandard gs = SpellGoldStandardReader.read(gsDir);

        if (showDetails) {
            printGSDetails(gs);
        }

        new AssertSpellGoldStandard(gs, "")
            .totalDocsEquals(1)
            .totalDocsInDialectIs(0, Dialect.Name.NUNAVIK)
            .totalMisspelledWordsEquals(96)
            .totalCorrectlySpelledWordsEquals(146)
            .totalWordsWithMultipleCorrectionsIs(33)
            .percentWordsWithMultipleCorrectionsIs(0.34)
            .totalWordsMissedByAtLeastOneRevisorIs(6)
            ;

    }

    private void printGSDetails(SpellGoldStandard gs) throws SpellCheckerException {
        System.out.println("\n\nSUMMARY of SpellChecker Gold Standard");
        System.out.println("  #docs: "+gs.totalDocs());
        System.out.println("  #words: "+gs.totalWords());
        System.out.println("  #misspelled: "+gs.totalMisspelledWords());
        System.out.format("  %%misspelled: %.1f%% \n", gs.percentMisspelledWords());
        System.out.println("");
        Map<String, Set<String>> wordsWithMultipleCorr = gs.wordsWithMultipleCorrections();
        double percMultipleCorr = gs.percentWordsWithMultipleCorrections();
        if (percMultipleCorr == 0.0) {
            System.out.println(
                    "None of the mispelled words had multiple corrections.");
        } else {
            System.out.println(
                    "" + String.format("%.1f", percMultipleCorr * 100) +
                            "% of mispelled words had multiple spellings.\nSee liste below:\n");
            for (String word : wordsWithMultipleCorr.keySet()) {
                System.out.print("   (" + word + ") -->");
                Iterator<String> iter = wordsWithMultipleCorr.get(word).iterator();
                while (iter.hasNext()) {
                    String aSpelling = iter.next();
                    String uncorrected = "";
                    if (isSameSpelling(word, aSpelling)) {
                        uncorrected = " -- [DEEMED CORRECT]";
                    }
                    System.out.print("\n      " + aSpelling + uncorrected);
                }
                System.out.println("\n");
            }
        }

        Set<Triple<String, String, String>> missed = gs.missedRevisions();
        if (missed.size() == 0) {
            System.out.println(
                    "\n\nAll words were looked at by all revisors.\n");
        } else {
            System.out.println(
                    "\n\n"+missed.size()+" words seem like they were not looked at by at least one revisors.\n" +
                            "See list below:");
            for (Iterator<Triple<String, String, String>> it = missed.iterator(); it.hasNext(); ) {
                Triple<String, String, String> aMissed = it.next();
                System.out.println("  "+aMissed.getLeft()+
                        "; "+aMissed.getMiddle()+"; "+aMissed.getRight());
            }
        }
    }

    private boolean isSameSpelling(String orig, String corrected) {
        orig = orig.replaceAll("[a-zA-Z0-9\\s\\p{Punct}]*", "");
        corrected = corrected.replaceAll("[a-zA-Z0-9\\s\\p{Punct}]*", "");
        boolean answer = (orig.equals(corrected));
        return answer;
    }
}
