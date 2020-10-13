package ca.pirurvik.iutools.spellchecker.goldstandard;

import ca.inuktitutcomputing.config.IUConfig;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This test reads the Spell Checker gold standard created from a small sample
 * inuktut web pages, and does a bunch of sanity checks on its content
 */
public class SpellGoldStandard_WebSampleSanityCheck {

    @Test
    public void test__sanityCheck() throws Exception {
        File gsDir = new File(IUConfig.getIUDataPath("data/InuktitutWebSample-2020-07"));

        SpellGoldStandard gs = SpellGoldStandardReader.read(gsDir);
        new AssertSpellGoldStandard(gs, "")
            .totalDocsEquals(1)
            .totalMisspelledWordsEquals(30)
            .totalCorrectlySpelledWordsEquals(-1)
            ;
    }
}
