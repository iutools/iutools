package ca.pirurvik.iutools.spellchecker.goldstandard;

import ca.inuktitutcomputing.config.IUConfig;
import ca.inuktitutcomputing.phonology.Dialect;
import org.junit.Test;

import java.io.File;

/**
 * This test reads the Spell Checker gold standard created from a small sample
 * inuktut web pages, and does a bunch of sanity checks on its content
 */
public class SpellGoldStandard_WebSampleSanityCheck {

    @Test
    public void test__sanityCheck() throws Exception {
        File gsDir = new File(IUConfig.getIUDataPath("data/NunavutWebSample-2020-07"));

        SpellGoldStandard gs = SpellGoldStandardReader.read(gsDir);
        gs.wordsWithMultipleCorrections();
        new AssertSpellGoldStandard(gs, "")
            .totalDocsEquals(1)
            .totalDocsInDialectIs(0, Dialect.Name.NUNAVIK)
            .totalMisspelledWordsEquals(30)
            .totalCorrectlySpelledWordsEquals(172)
            .wordsWithMultipleCorrectionsAre(new String[0][])
            .totalErrorsMissedByAtLeastOneRevisorIs(-1)
            ;
    }
}
