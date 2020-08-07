package ca.pirurvik.iutools.spellchecker.goldstandard;

import java.util.ArrayList;
import java.util.List;

public class SpellGoldStandardCase {
    public String orig = null;
    public List<String> correctSpellings = new ArrayList<String>();

    public SpellGoldStandardCase(String _orig) {
        this.orig = _orig;
    }

    public void addCorrectSpelling(String correct) {
        correctSpellings.add(correct);
    }

    public boolean isCorrectlySpelled() {
        boolean isCorrect = true;
        for (String aSpelling: correctSpellings) {
            if (aSpelling != null) {
                isCorrect = false;
                break;
            }
        }
        return isCorrect;
    }
}
