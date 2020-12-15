package org.iutools.spellchecker.goldstandard;

import org.iutools.phonology.Dialect;
import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertSet;
import ca.nrc.testing.Asserter;
import org.iutools.spellchecker.SpellCheckerException;
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

    public AssertSpellGoldStandard totalWordsWithMultipleCorrectionsIs(int expTotal)
            throws Exception {
        Map<String, Set<String>> anomalies = goldStandard().wordsWithMultipleCorrections();
        int gotTotal = goldStandard().wordsWithMultipleCorrections().size();
        AssertObject.assertDeepEquals(
                baseMessage+"\n# of Words with multiple corrections was not as expected",
                expTotal, gotTotal);

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

    public AssertSpellGoldStandard misspelledWordsAre(String... expBadArr) throws SpellCheckerException {
        Set<String> expBad = new HashSet<String>();
        Collections.addAll(expBad, expBadArr);
        Set<String> gotBad = goldStandard().misspelledWords();
        return this;
    }

    public AssertSpellGoldStandard missedRevisionsAre(Triple<String, String, String>... expMissedArr) throws Exception {
        Set<Triple<String, String, String>> expMissed = new HashSet<Triple<String, String, String>>();
        if (expMissedArr != null) {
            Collections.addAll(expMissed, expMissedArr);
        }
        Set<Triple<String, String, String>> gotMissed =
            goldStandard().missedRevisions();
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

    public AssertSpellGoldStandard totalMisspelledWordsEquals(int expTotalMisspelled) throws SpellCheckerException {
        int gotTotalMisspelled = goldStandard().totalMisspelledWords();
        Assert.assertEquals(
    "Total number of misspelled words was not as expected.",
            expTotalMisspelled, gotTotalMisspelled);
        return this;
    }

    public AssertSpellGoldStandard totalCorrectlySpelledWordsEquals(int expTotal) throws SpellCheckerException {
        int gotTotal = goldStandard().totalCorrectlySpelledWords();
        Assert.assertEquals(
            "Total number of correctly spelled words was not as expected.",
            expTotal, gotTotal);
        return this;
    }

    public AssertSpellGoldStandard totalDocsInDialectIs(
            int expTotal, Dialect.Name dialect) {
        int gotTotal = goldStandard().totalDocsInDialect(dialect);
        Assert.assertEquals(
    "Total number of documents in dialect "+dialect+" was not as expected.",
            expTotal, gotTotal);
        return this;
    }

    public AssertSpellGoldStandard totalWordsMissedByAtLeastOneRevisorIs(int expTotal) {
        int gotTotal = goldStandard().totalErrorsMissedByAtLeastOneRevisor();
        Assert.assertEquals(
                "Total number of errors missed by at least one revisor was not as expected.",
                expTotal, gotTotal);
        return this;
    }

    public AssertSpellGoldStandard percentWordsWithMultipleCorrectionsIs(
        double expPercent) throws SpellCheckerException {

        double gotPercent = goldStandard().percentWordsWithMultipleCorrections();
        Assert.assertEquals(
    baseMessage+
            "\nPercentage of misspelled words that have more than one correction was not as expected.",
            expPercent, gotPercent, 0.01);
        return this;
    }
}
