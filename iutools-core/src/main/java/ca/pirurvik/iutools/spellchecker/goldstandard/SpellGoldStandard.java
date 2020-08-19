package ca.pirurvik.iutools.spellchecker.goldstandard;

import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

public class SpellGoldStandard {
    private Map<String, SpellGoldStandardCase> _cases = null;

    private Map<String, Map<String,DocHumanRevision>> docRevisions =
        new HashMap<String, Map<String,DocHumanRevision>>();

    public void addCase(String origWord, String correctWord, String docName, String evaluator) {
        _cases = null; // Will force regeneration of the cases

        if (!docRevisions.containsKey(docName)) {
            docRevisions.put(docName, new HashMap<String,DocHumanRevision>());
        }
        Map<String,DocHumanRevision> revision4doc = docRevisions.get(docName);
        if (!revision4doc.containsKey(evaluator)) {
            revision4doc.put(evaluator, new DocHumanRevision(docName, evaluator));
        }
        DocHumanRevision revisionByEvaluator = revision4doc.get(evaluator);
        revisionByEvaluator.addWord(origWord, correctWord);
    }

    public Map<String, SpellGoldStandardCase> cases() {
        if (_cases == null) {
            _cases = new HashMap<String, SpellGoldStandardCase>();
            for (String doc: docRevisions.keySet()) {
                // Loop through all documents in the gold standard
                Map<String, DocHumanRevision> revisions =
                    docRevisions.get(doc);
                for (String evaluator: revisions.keySet()) {
                    // For a document, loop through all the human revisions
                    // of that document.
                    //
                    DocHumanRevision revOneEvaluator = revisions.get(evaluator);
                    for (String word: revOneEvaluator.allWords()) {
                        // For a human revision of a document, loop through all
                        // word corrections produced by that human evaluator
                        //
                        if (!_cases.containsKey(word)) {
                            _cases.put(word, new SpellGoldStandardCase(word));
                        }
                        SpellGoldStandardCase case4word = _cases.get(word);

                        Set<String> spellings = revOneEvaluator.spellings4word(word);
                        for (String aSpelling: spellings) {
                            case4word.addCorrectSpelling(aSpelling);
                        }
                    }

                }
            }
        }
        return _cases;
    }

    public Map<String,Set<String>> wordsWithMultipleCorrections() {
        Map<String,Set<String>> anomalies = new HashMap<String,Set<String>>();
        cases().forEach((word, wordCase) -> {
            if (wordCase.correctSpellings.size() > 1) {
                anomalies.put(word, wordCase.correctSpellings);
            }
        });
        return anomalies;
    }

    public Set<String> correctlySpelledWords() {
        Set<String> correctlySpelled = new HashSet<String>();
        for (SpellGoldStandardCase aCase: cases().values()) {
            if (aCase.isCorrectlySpelled()) {
                correctlySpelled.add(aCase.orig);
            }

        }

        return correctlySpelled;
    }


    public Iterator<SpellGoldStandardCase> allWords() {
        return cases().values().iterator();
    }

    public SpellGoldStandardCase case4word(String word) {
        return cases().get(word);
    }

    public Set<String> misspelledWords() {
        Set<String> badWords = new HashSet<String>();
        Iterator<SpellGoldStandardCase> iter = allWords();
        while (iter.hasNext()) {
            SpellGoldStandardCase aCase = iter.next();
            if (!aCase.isCorrectlySpelled()) {
                badWords.add(aCase.orig);
            }
        }
        return badWords;
    }

    public Set<Triple<String, String, String>> missedRevisions() {
        Set<Triple<String, String, String>> missed = new HashSet<Triple<String, String, String>>();

        Map<String,Map<String,Set<String>>> wordsByDoc =
            new HashMap<String,Map<String,Set<String>>>();
        Iterator<SpellGoldStandardCase> iter = allWords();
        while (iter.hasNext()) {
            SpellGoldStandardCase aCase = iter.next();
        }

        return missed;
    }
}
