package ca.pirurvik.iutools.spellchecker.goldstandard;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertSet;
import ca.nrc.testing.Asserter;
import org.apache.commons.lang3.tuple.Triple;
import org.junit.Assert;

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

        Map<String, Set<String>> gotAnomalies = goldStandard().wordsWithMultipleCorrections();
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

    public AssertSpellGoldStandard spellings4wordAre(String word, String... expSpellingsArr)
        throws Exception {
        SpellGoldStandardCase aCase = goldStandard().case4word(word);
        Set<String> expSpellings = new HashSet<String>();
        Collections.addAll(expSpellings, expSpellingsArr);
        Set<String> gotSpellings = aCase.correctSpellings;
        AssertSet.assertEquals(
            baseMessage+"\nCorrect spellings were not as expected for word "+word,
             expSpellings, gotSpellings);
        return this;
    }

    public AssertSpellGoldStandard wordsAre(String... expWordsArr) throws Exception {
        Iterator<SpellGoldStandardCase> iter = goldStandard().allWords();
        Set<String> gotWords = new HashSet<String>();
        while (iter.hasNext()) {
            gotWords.add(iter.next().orig);
        }
        Set<String> expWords = new HashSet<String>();
        Collections.addAll(expWords, expWordsArr);
        AssertSet.assertEquals(baseMessage +"\nWords in the gold standard were not as expected", expWords, gotWords);

        return this;
    }

    public AssertSpellGoldStandard misspelledWordsAre(String... expBadArr) {
        Set<String> expBad = new HashSet<String>();
        Collections.addAll(expBad, expBadArr);
        Set<String> gotBad = goldStandard().misspelledWords();
        return this;
    }

    public AssertSpellGoldStandard missedRevisionsAre(Triple<String, String, String>... expMissedArr) throws Exception {
        Set<Triple<String, String, String>> expMissed = new HashSet<Triple<String, String, String>>();
        Collections.addAll(expMissed, expMissedArr);
        Set<Triple<String, String, String>> gotMissed = goldStandard().missedRevisions();
        AssertSet.assertEquals(baseMessage+"\nThe missed revisions were not as expected", expMissed, gotMissed);
        return this;
    }

    public AssertSpellGoldStandard totalDocsEquals(int expTotalDocs) {
        int gotTotalDocs = goldStandard().totalDocs();
        Assert.assertEquals(
            "Number of documents in the Gold Standard was not as expected.",
            expTotalDocs, gotTotalDocs);
        return this;
    }

    public AssertSpellGoldStandard totalMisspelledWordsEquals(int expTotalMisspelled) {
        int gotTotalMisspelled = goldStandard().totalMisspelledWords();
        Assert.assertEquals(
    "Total number of misspelled words was not as expected.",
            expTotalMisspelled, gotTotalMisspelled);
        return this;
    }

    public AssertSpellGoldStandard totalCorrectlySpelledWordsEquals(int expTotal) {
        int gotTotal = goldStandard().totalCorrectlySpelledWords();
        Assert.assertEquals(
            "Total number of correctly spelled words was not as expected.",
            expTotal, gotTotal);
        return this;
    }
}
