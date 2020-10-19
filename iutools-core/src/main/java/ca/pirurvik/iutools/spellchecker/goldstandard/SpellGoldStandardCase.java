package ca.pirurvik.iutools.spellchecker.goldstandard;

import ca.pirurvik.iutools.spellchecker.SpellCheckerException;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

public class SpellGoldStandardCase {
    public String orig = null;
    public Set<String> correctSpellings = new HashSet<String>();
    protected Map<String,String> spelling4evaluator =
        new HashMap<String,String>();
    public String inDoc = null;
    private String _id = null;

    private static final Pattern pattDoc = Pattern.compile(".*/([^/]+)");

    public SpellGoldStandardCase(String _orig, String _inDoc) {
        init_SpellGoldStandardCase(_orig, _inDoc);
    }

    private void init_SpellGoldStandardCase(String _orig, String _inDoc) {
        this.orig = _orig.toLowerCase();
        this.inDoc = _inDoc;
    }

    public void addCorrectSpelling(String evaluator, String correct) throws SpellCheckerException {
        spelling4evaluator.put(evaluator, correct);
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
        if (_id == null) {
            _id = orig+":in "+DocHumanRevision.docID(inDoc);
        }
        return _id;
    }
}
