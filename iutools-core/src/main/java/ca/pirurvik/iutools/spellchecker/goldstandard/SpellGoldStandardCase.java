package ca.pirurvik.iutools.spellchecker.goldstandard;

import java.util.HashSet;
import java.util.Set;

public class SpellGoldStandardCase {
    public String orig = null;
    public Set<String> correctSpellings = new HashSet<String>();
    public String inDoc = null;

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
}
