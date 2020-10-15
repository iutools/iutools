package ca.pirurvik.iutools.spellchecker.goldstandard;

import ca.inuktitutcomputing.phonology.Dialect;
import ca.inuktitutcomputing.phonology.DialectException;
import ca.pirurvik.iutools.spellchecker.SpellCheckerException;
import org.apache.commons.lang3.tuple.Triple;

import java.util.*;

public class SpellGoldStandard {
    private Map<String, SpellGoldStandardCase> _cases = null;

    private Map<String,Set<Dialect.Name>> possibleDialectsForDoc =
        new HashMap<String,Set<Dialect.Name>>();

    /**
     * Key: String docName
     * Value: Map<String revisorName, DocHumanRevison revisionByThatRevisor></String>
     */
    private Map<String, Map<String,DocHumanRevision>> docRevisions =
        new HashMap<String, Map<String,DocHumanRevision>>();

    public void addCase(String origWord, String correctWord, String docName,
        String evaluator) throws SpellCheckerException {
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

        if (!possibleDialectsForDoc.containsKey(docName)) {
            possibleDialectsForDoc.put(docName, new HashSet<Dialect.Name>());
        }
        Set<Dialect.Name> dialecstThisDoc = possibleDialectsForDoc.get(docName);
        try {
            dialecstThisDoc.addAll(Dialect.possibleDialects(origWord));
        } catch (DialectException e) {
            throw new SpellCheckerException(e);
        }
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
        for (String doc: allDocs()) {
            Map<String, DocHumanRevision> oneDocRevisions = docRevisions.get(doc);
            Set<Triple<String, String, String>> missedOneDoc =
                missedRevisionInDoc(oneDocRevisions);
            missed.addAll(missedOneDoc);
        }

        return missed;
    }

    /**
     *
     * @param allRevsOneDoc:
     *    Map<String revisorName, DocHumanRevision revisionByThatRevisor>
     *    All revisions made for one document.
     *
     * @return
     */
    private Set<Triple<String, String, String>> missedRevisionInDoc(
        Map<String, DocHumanRevision> allRevsOneDoc) {

        Set<Triple<String, String, String>> missed =
                new HashSet<Triple<String, String, String>>();

        if (!allRevsOneDoc.isEmpty()) {
            String doc =
                allRevsOneDoc.entrySet().iterator().next()
                .getValue().docID;

            // Build a map of what evaluators looked at what words
            //
            Map<String, Set<String>> evaluators4word = new HashMap<String, Set<String>>();
            Set<String> allEvaluators = new HashSet<String>();
            for (String evaluator : allRevsOneDoc.keySet()) {
                allEvaluators.add(evaluator);
                DocHumanRevision aRev = allRevsOneDoc.get(evaluator);
                for (String word : aRev.allWords()) {
                    if (!evaluators4word.containsKey(word)) {
                        evaluators4word.put(word, new HashSet<String>());
                    }
                    Set<String> evaluatorsThisWord = evaluators4word.get(word);
                    evaluatorsThisWord.add(evaluator);
                }
            }

            // Report any word that has not been seen by all evaluators.
            for (String word : evaluators4word.keySet()) {
                Set<String> evaluatorsThisWord = evaluators4word.get(word);
                for (String evaluator : allEvaluators) {
                    if (!evaluatorsThisWord.contains(evaluator)) {
                        missed.add(Triple.of(doc, word, evaluator));
                    }
                }
            }
        }

        return missed;
    }

    private Set<String> allDocs() {
        return docRevisions.keySet();
    }

    public int totalDocs() {
        return allDocs().size();
    }

    public int totalMisspelledWords() {
        return misspelledWords().size();
    }

    public int totalCorrectlySpelledWords() {
        return correctlySpelledWords().size();
    }

    public int totalDocsInDialect(Dialect.Name dialect) {
        Set<String> docsInDialect = new HashSet<String>();
        for (String doc: allDocs()) {
            if (possibleDialectsForDoc.get(doc).contains(dialect)) {
                docsInDialect.add(doc);
            }
        }

        return docsInDialect.size();
    }

    public int totalErrorsMissedByAtLeastOneRevisor() {
        return missedRevisions().size();
    }
}

