package ca.pirurvik.iutools.spellchecker.goldstandard;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertSet;
import ca.nrc.testing.Asserter;

import java.util.*;

public class AssertSpellGoldStandard extends Asserter<SpellGoldStandard> {
    public AssertSpellGoldStandard(SpellGoldStandard _gs, String _mess) {
        super(_gs, _mess);
    }

    public SpellGoldStandard goldStandard() {
        return this.gotObject;
    }

    public AssertSpellGoldStandard wordsWithMultipleCorrectionsAre(String[][] wordsWithCorrections)
        throws Exception {
        Map<String,String[]> expAnomalies = new HashMap<String,String[]>();
        for (String[] entry: wordsWithCorrections) {
            String word = entry[0];
            String[] corrections = Arrays.copyOfRange(entry, 1, entry.length);
            expAnomalies.put(word, corrections);
        }

        Map<String, String[]> gotAnomalies = goldStandard().wordsWithMultipleCorrections();
        AssertObject.assertDeepEquals(
            baseMessage+"\nWords with multiple corrections were not as expected",
             expAnomalies, gotAnomalies);

        return this;
    }

    public AssertSpellGoldStandard correctlySpelledWordsAre(String... expWords)
        throws Exception {
        Set<String> gotWords = goldStandard().correctlySpelledWords();
        AssertSet.assertEquals(
            baseMessage+"\nCorrectly spelled words were not as expected",
             expWords, gotWords);
        return this;
    }

    public AssertSpellGoldStandard spellings4wordAre(String word, String... expSpellings)
        throws Exception {
        SpellGoldStandardCase aCase = goldStandard().case4word(word);
        List<String> gotSpellings = aCase.correctSpellings;
        AssertObject.assertDeepEquals(
            baseMessage+"\nCorrect spellings were not as expected for word "+word,
             expSpellings, gotSpellings);
        return this;
    }
}
