package ca.pirurvik.iutools.spellchecker.goldstandard;

import java.util.*;

public class DocHumanRevision {
    String docID = null;
    String revisor = null;
    Map<String, Set<String>> wordRevisions = new HashMap<String,Set<String>>();

    public DocHumanRevision(String _docName, String _revisor) {
        this.docID = _docName;
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
}
