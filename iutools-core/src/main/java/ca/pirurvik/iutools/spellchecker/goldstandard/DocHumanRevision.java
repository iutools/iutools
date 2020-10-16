package ca.pirurvik.iutools.spellchecker.goldstandard;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DocHumanRevision {
    private static final Pattern pattDoc = Pattern.compile(".*/([^/]+)");

    String docPath = null;
    String revisor = null;
    Map<String, Set<String>> wordRevisions = new HashMap<String,Set<String>>();
    String _docID = null;

    public DocHumanRevision(String _docName, String _revisor) {
        this.docPath = _docName;
        this.revisor = _revisor;
    }

    public void addWord(String origWord, String correctWord) {
        if (!wordRevisions.containsKey(origWord)) {
            wordRevisions.put(origWord, new HashSet<String>());
        }
        wordRevisions.get(origWord).add(correctWord);
    }

    public Set<String> allWords() {
        return wordRevisions.keySet();
    }

    public Set<String> spellings4word(String word) {
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
