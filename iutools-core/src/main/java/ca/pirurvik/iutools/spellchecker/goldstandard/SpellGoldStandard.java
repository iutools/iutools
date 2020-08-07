package ca.pirurvik.iutools.spellchecker.goldstandard;

import java.util.*;

public class SpellGoldStandard {
//    protected Map<String, List<String>> acceptableSpellings
//        = new HashMap<String,List<String>>();

    protected Map<String, SpellGoldStandardCase> cases
            = new HashMap<String,SpellGoldStandardCase>();

    public void addCase(String origWord, String correctWord, String docName, String evaluator) {
//        if (!acceptableSpellings.containsKey(origWord)) {
//            acceptableSpellings.put(origWord, new ArrayList<String>());
//        }
//        acceptableSpellings.get(origWord).add(correctWord);

        if (!cases.containsKey(origWord)) {
            cases.put(origWord, new SpellGoldStandardCase(origWord));
        }

        cases.get(origWord).addCorrectSpelling(correctWord);

    }

    public Map<String,String[]> wordsWithMultipleCorrections() {
        Map<String,String[]> anomalies = new HashMap<String,String[]>();
//        acceptableSpellings.forEach((word, spellings) -> {
//            if (spellings.size() > 1) {
//                anomalies.put(word, spellings.toArray(new String[0]));
//            }
//        });
        cases.forEach((word, wordCase) -> {
            if (wordCase.correctSpellings.size() > 1) {
                anomalies.put(word, wordCase.correctSpellings.toArray(new String[0]));
            }
        });
        return anomalies;
    }

    public Set<String> correctlySpelledWords() {
        Set<String> correctlySpelled = new HashSet<String>();
        for (SpellGoldStandardCase aCase: cases.values()) {
            if (aCase.isCorrectlySpelled()) {
                correctlySpelled.add(aCase.orig);
            }

        }

        return correctlySpelled;
    }


    public Iterator<SpellGoldStandardCase> allWords() {
        return cases.values().iterator();
    }

    public SpellGoldStandardCase case4word(String word) {
        return cases.get(word);
    }

    public void addDoc(String docID, String docText) {
    }


//    public SpellGoldStandardCase case4word(String aWord) {
//        SpellCheckerExample case = new SpellCheckerExample();
//        return case;
//    }
}
