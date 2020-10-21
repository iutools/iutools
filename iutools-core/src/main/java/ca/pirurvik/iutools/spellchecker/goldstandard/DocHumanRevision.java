package ca.pirurvik.iutools.spellchecker.goldstandard;

import ca.pirurvik.iutools.spellchecker.SpellCheckerException;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocHumanRevision {
    private static final Pattern pattDoc = Pattern.compile(".*/([^/]+)");

    String docPath = null;
    String revisor = null;
    Map<String, String> wordRevisions = new HashMap<String,String>();
    String _docID = null;

    public DocHumanRevision(String _docName, String _revisor) {
        this.docPath = _docName;
        this.revisor = _revisor;
    }

    public void addWord(String origWord, String correctWord) throws SpellCheckerException {
        if (!wordRevisions.containsKey(origWord)) {
            wordRevisions.put(origWord, correctWord);
        } else {
            // That revisor already provided the correct spelling for that word
            // in that document. Make sure the new correction is the same
            // as the first one.
            //
            String prevCorrectWord = wordRevisions.get(origWord);
            if (!correctWord.equals(prevCorrectWord)
                // Note: If tne new correct spelling is the same as the
                //   original spelling, it means that the correction field
                //   was left blank. This can mean one of two things:
                //   - There is no correction for that word
                //   - The correction is the same as the previous one provided
                //     in that document
                //
                // In this case, we assume the second point
                //
                && !correctWord.equals(origWord)) {
                throw new SpellCheckerException(
                    "\nEvaluator " + this.revisor +
                    " provided two different spellings for a word in the same document.\n" +
                    "   word: '" + origWord + "'\n" +
                    "   in document: " + this.docID() + "\n" +
                    "   spellings: '" +
                    prevCorrectWord + "', '" + correctWord);
            }
        }
    }

    public Set<String> allWords() {
        return wordRevisions.keySet();
    }

    public int totalWords() {
        return allWords().size();
    }

    public String spelling4word(String word) {
        return wordRevisions.get(word);
    }


    public String docID() {
        if (_docID == null) {
            _docID = docID(docPath);
        }
        return _docID;
    }

    public static String docID(String docPath) {
        String id = null;
        Matcher matcher = pattDoc.matcher(docPath);
        if (matcher.matches()) {
            id = matcher.group(1);
        }
        return id;
    }

}
