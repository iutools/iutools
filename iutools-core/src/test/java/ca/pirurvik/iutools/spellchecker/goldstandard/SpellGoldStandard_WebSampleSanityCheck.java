package ca.pirurvik.iutools.spellchecker.goldstandard;

import ca.inuktitutcomputing.config.IUConfig;
import ca.inuktitutcomputing.phonology.Dialect;
import ca.nrc.string.StringUtils;
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
public class SpellGoldStandard_WebSampleSanityCheck {

    @Test
    public void test__sanityCheck() throws Exception {

        // Set this to true if you want to see more details about the
        // gold standard
        boolean showDetails = true;

        File gsDir = new File(IUConfig.getIUDataPath("data/NunavutWebSample-2020-07"));

        SpellGoldStandard gs = SpellGoldStandardReader.read(gsDir);

        if (showDetails) {
            Map<String, Set<String>> wordsWithMultipleCorr = gs.wordsWithMultipleCorrections();
            double percMultipleCorr = gs.percentWordsWithMultipleCorrections();
            if (percMultipleCorr == 0.0) {
                System.out.println(
                    "\n\nNone of the mispelled words had multiple corrections.");
            } else {
                System.out.println(
                    "\n\n" + String.format("%.1f", percMultipleCorr * 100) +
                    "% of mispelled words had multiple spellings.\nSee liste below:");
                for (String word : wordsWithMultipleCorr.keySet()) {
                    System.out.println("   (" + word + ") --> \n      " +
                            StringUtils.join(wordsWithMultipleCorr.get(word).iterator(), "\n      "));
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
}
