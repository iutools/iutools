package ca.pirurvik.iutools.corpus;

//import com.fasterxml.jackson.annotation.JsonIgnore;
//import org.apache.commons.lang3.StringUtils;

public class WordInfo_ES extends WordInfo {


    /**
     * Morphemes of the word, separated by space.
     * This allows us to compose an ES query that finds all the
     * words that contain a particular ngram of morphemes.
     */
    public String morphemesSpaceConcatenated = null;

    public WordInfo_ES() {
        super();
    }

    public WordInfo_ES(String _word) {
        super(_word);
    }

    public static String insertSpaces(String ngram) {
        return WordInfo.insertSpaces(ngram);
    }

    public static String insertSpaces(String[] ngramArr) {
        return WordInfo.insertSpaces(ngramArr);
    }
}
