package ca.pirurvik.iutools.spellchecker.goldstandard;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

public class SpellGoldStandardCase {
    public String orig = null;
    public Set<String> correctSpellings = new HashSet<String>();
    public String inDoc = null;
    private String _id = null;

    private final Pattern pattDoc = Pattern.compile(".*/([^/]+)");

    public SpellGoldStandardCase(String _orig) {
        init_SpellGoldStandardCase(_orig);
    }

    private void init_SpellGoldStandardCase(String _orig) {
        this.orig = _orig.toLowerCase();
    }

    public void addCorrectSpelling(String correct) {
        correctSpellings.add(correct);
    }

    public boolean isCorrectlySpelled() {
        boolean isCorrect = true;
        for (String aSpelling: correctSpellings) {
            if (aSpelling != null && !aSpelling.toLowerCase().equals(orig)) {
                isCorrect = false;
                break;
            }
        }
        return isCorrect;
    }

    public String id() {
        System.out.println("--** SpellGoldStandardCase.id: this="+ PrettyPrinter.print(this));
        if (_id == null) {
            _id = DocHumanRevision.docID(inDoc);
        }
        return _id;
    }
}
