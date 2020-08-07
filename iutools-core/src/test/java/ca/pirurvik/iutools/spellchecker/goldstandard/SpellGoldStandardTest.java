package ca.pirurvik.iutools.spellchecker.goldstandard;

import ca.pirurvik.iutools.spellchecker.goldstandard.AssertSpellGoldStandard;
import ca.pirurvik.iutools.spellchecker.goldstandard.SpellGoldStandard;
import ca.pirurvik.iutools.spellchecker.goldstandard.SpellGoldStandardCase;
import org.junit.Test;

import java.util.Iterator;
import java.util.Map;

public class SpellGoldStandardTest {

    /////////////////////////////////////////
    // DOCUMENTATION TESTS
    /////////////////////////////////////////

    @Test
    public void test__SpellGoldStandard__Synopsis() {
        // Use this class to create a Gold Standard for evaluating the accuracy
        // of the spell checker
        //
        SpellGoldStandard gs = new SpellGoldStandard();

        // A gold standard is maded up of a bunch of text document, where each
        // document was proof-read by one or more human evaluators.
        //
        // Here is how you add a document to the GS
        //
        String docID = "doc1";
        String docText = "Hello world";
        gs.addDoc(docID, docText);

        // This is how you add proof reading

        // You build the Gold Standard by adding "cases". Each case consists
        // of a word from a given document, that was evaluated by a human
        // proof-reader.
        //
        // For example, say you have one human evaluator named "Joe" who says
        // that the word 'helll', which appeared in document 'HelloWord.docs'
        // should have been spelled 'hello' instead
        //
        String origWord = "helll";
        String correctWord = "hello";
        String docName = "HelloWorld.docx";
        String evaluator = "Joe";
        gs.addCase(origWord, correctWord, docName, evaluator);

        // Someone else, called "Jane" may have a different opinion about that
        // particular word occurence...
        //
        gs.addCase(origWord, "hell", docName, "Jane");

        // The Gold Standard will typically include all the words that are seen
        // in a collection of documents. This includes words that are correctly
        // spelled. For those, just pass null as the correction.
        //
        gs.addCase("hell", null, "TheDivineComedy.docx", "Jane");

        // Once all the cases have been entered, you can check for anomalies in
        // the data provided by the human evaluators. For example, you can get
        // a list of words for which more than one possible corrections have
        // been provided
        //
        Map<String, String[]> anomalies = gs.wordsWithMultipleCorrections();
        for (String word: anomalies.keySet()) {
            String[] conflictingCorrections = anomalies.get(word);
        }

        // You can also iterate through all the words in the gold standard
        //
        Iterator<SpellGoldStandardCase> iter = gs.allWords();
        while (iter.hasNext()) {
            SpellGoldStandardCase aCase = iter.next();
        }
    }

    /////////////////////////////////////////
    // VERIFICATION TESTS
    /////////////////////////////////////////


    @Test
    public void test__SpellGoldStandard__HappyPath() throws Exception {
        SpellGoldStandard gs = new SpellGoldStandard();
        String origWord = "helll";
        String docName = "SomeDoc.txt";
        gs.addCase(origWord, "hello", docName, "Joe");
        gs.addCase(origWord, "hell", docName, "Jane");
        gs.addCase("hell", null, "TheDivineComedy.docx", "Jane");

//        gs.wordsWithMultipleCorrections()
        new AssertSpellGoldStandard(gs, "")
            .wordsWithMultipleCorrectionsAre(
                new String[][] {
                    new String[] {"helll", "hello", "hell"}
                }
            )
            .correctlySpelledWordsAre("hell")
            .spellings4wordAre("helll", "hello", "hell");
            ;
    }
}
