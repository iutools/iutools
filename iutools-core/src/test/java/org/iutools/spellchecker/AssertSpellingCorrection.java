package org.iutools.spellchecker;

import java.util.*;

import org.junit.Assert;

import ca.nrc.testing.AssertObject;
import ca.nrc.testing.AssertString;

public class AssertSpellingCorrection {

    private String baseMessage;
    private SpellingCorrection gotCorrection;

    public static AssertSpellingCorrection assertThat(
            SpellingCorrection _gotCorrection,
            String _mess) {

        return new AssertSpellingCorrection(_gotCorrection, _mess);
    }

    public AssertSpellingCorrection(SpellingCorrection _gotCorrection,
                                    String _mess) {
        this.baseMessage = _mess;
        this.gotCorrection = _gotCorrection;
    }

    public AssertSpellingCorrection wasMisspelled() {
        Assert.assertTrue(baseMessage+"\nWord should have been mis-spelled",
                gotCorrection.wasMispelled);
        return this;
    }

    public AssertSpellingCorrection wasNotMisspelled() {
        Assert.assertFalse(baseMessage+"\nWord should NOT have been mis-spelled",
                gotCorrection.wasMispelled);
        return this;
    }

    public AssertSpellingCorrection suggestsSpellings(String... expSuggs)
            throws Exception {
        return suggestsSpellings((Boolean)null, expSuggs);
    }

    public AssertSpellingCorrection suggestsSpellings(
            Boolean onlyTopExpected, String... expSuggs)
            throws Exception {
        List<String> gotSuggs = gotCorrection.getPossibleSpellings();
        if (onlyTopExpected == null) {
            onlyTopExpected = false;
        }
        if (onlyTopExpected && gotSuggs.size() > expSuggs.length) {
            gotSuggs = gotSuggs.subList(0, expSuggs.length);
        }
        AssertObject.assertDeepEquals(
                baseMessage+"\nSuggested spellings were not as expected.",
                expSuggs, gotSuggs);
        return this;
    }

    public AssertSpellingCorrection highlightsIncorrectTail(String expLead) {
        String gotLead = gotCorrection.getCorrectLead();
        AssertString.assertStringEquals(
                baseMessage+"\nSuggested correct leading chars were not as expected.",
                expLead, gotLead);
        return this;
    }

    public AssertSpellingCorrection highlightsIncorrectLead(String expTail) {
        String gotTail = gotCorrection.getCorrectTail();
        AssertString.assertStringEquals(
                baseMessage+"\nSuggested correct tailing chars were not as expected.",
                expTail, gotTail);
        return this;
    }

    public AssertSpellingCorrection highlightsIncorrectMiddle(String expPartial) {
        String gotPartial = gotCorrection.highlightIncorrectMiddle();
        AssertString.assertStringEquals(
                baseMessage+"\nSuggested correct portions of the word were not as expected.",
                expPartial, gotPartial);
        return this;
    }

    public AssertSpellingCorrection providesSuggestions(String... expSugg)
            throws Exception {
        AssertObject.assertDeepEquals(
                baseMessage+"\nSuggestions were not as expected for word "+
                        gotCorrection.orig,
                expSugg, gotCorrection.getAllSuggestions());
        return this;
    }

    public static void candidatesEqual(
        String[] expWords, Collection<ScoredSpelling> candidates)
        throws Exception {
        Set<String> gotWords = new HashSet<String>();
        for (ScoredSpelling aCandidate: candidates) {
            gotWords.add(aCandidate.spelling);
        }
        AssertObject.assertDeepEquals(
            "List of candidate spellings was not as expected",
            expWords, gotWords);
    }

}
