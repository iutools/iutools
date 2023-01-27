package org.iutools.worddict;

import ca.nrc.testing.AssertSet;
import org.apache.commons.lang3.tuple.Pair;
import org.iutools.config.IUConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Set;

public class GlossarySummarizerTest {

    ////////////////////////////////////////////////
    // DOCUMENTATION TESTS
    ////////////////////////////////////////////////

    @Test
    public void test__GlossarySummarizer__Synopsis() throws Exception {
        // Say you have a glossary
        Glossary gloss = Glossary.get();

        // You can get a summary of it as follows
        GlossarySummarizer.Summary summary = new GlossarySummarizer().summarize(gloss);

        // Total number of entries for all languages
        long totalEntries = summary.totalTerms();

        // List of languages for which there is at least one entry in the glossary
        Set<String> languages = summary.allLanguages();
        for (String lang: languages) {
            // Total number of terms for language lang.
            long totalEntries4lang = summary.totalTerms4lang(lang);
        }
    }

    ////////////////////////////////////////////////
    // DOCUMENTATION TESTS
    ////////////////////////////////////////////////

    @Test
    public void test__GlossarySummarizer__HappyPath() throws Exception {
        Glossary gloss = loadAllGlossFiles();
        GlossarySummarizer.Summary summary = new GlossarySummarizer().summarize(gloss);
        Assertions.assertEquals(52565, summary.totalTerms(),
                "Total number of entries not as expected");
        AssertSet.assertEquals("List of languages not as expected", new String[]{"en", "fr", "iu"}, summary.allLanguages());

        for (Pair<String, Long> expTotalTerms :
            new Pair[]{Pair.of("en", new Long(16616)), Pair.of("iu", new Long(35800)), Pair.of("fr", new Long(149))}) {
            String lang = expTotalTerms.getLeft();
            Long expTotal = expTotalTerms.getRight();
            Assertions.assertEquals(expTotal, summary.totalTerms4lang(lang),
                    "Wrong number of terms for language "+lang);
        }
        String[] expDialects = new String[] {
            "AIVILINGMIUTUT", "Back River (Central Arctic)(Calc) Summary", "Baffin", "Baffin Island", "East Greenland",
            "Labrador", "NORTH-QIKIQTAALUK", "North Greenland", "Ungava Northern Quebec", "West Greenland",
            "West Hudson's Bay"
        };
        AssertSet.assertEquals("List of dialects not as expected", expDialects, summary.iuDialects());
    }

    //////////////////////////////////////
    // TEST HELPERS
    //////////////////////////////////////
    private Glossary loadAllGlossFiles() throws Exception {
		String[] fileNames = new String[] {
			"Dorais 1978",
			"EDU 2000 (rev. 2019)",
			"NAC Kadlun-Jone & Angalik (1996)",
			"NAC Kublu (2005)",
			"SCHNEIDER",
			"tusaalanga",
			"iutools-loanWords",
			"iutools-locations",
			"wpGlossary",
		};
		File[] files = new File[fileNames.length];
		for (int ii=0; ii < files.length; ii++) {
            files[ii] = new IUConfig().glossaryFPath(fileNames[ii]+".gloss.json").toFile();
		}

        Glossary gloss = new Glossary();
        for (File aFile: files) {
            gloss.loadFile(aFile);
        }

        return gloss;
    }

}
