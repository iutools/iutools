package ca.pirurvik.iutools.corpus;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;

public class WordInfo_ES extends WordInfo {

    @JsonIgnore // We don't store the key in ElasticSearch
    Long key = null;

    // We don't store the array form of the top decomp in ES.
    // Instead, we store its concatenated form which is easier to
    // process with ES.
    @JsonIgnore
    public String[] topDecompositions = null;

    /**
     Concatenation of the morphemes in the topd decomposition.
     This is easier to process with ES than the raw morpheme array form
     of the top decomposition.
     */
    public String topDecompositionStr = null;

    /**
     * Characters of the word, separated by space.
     * This allows us to compose an ES query that finds all the
     * words that contain a particular ngram of characters.
     */
    public String wordCharsSpaceConcatenated = null;

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
        init_WordInfo_ES(_word);
    }

    public static String insertSpaces(String ngram) {
        String[] ngramArr = ngram.split("");
        return insertSpaces(ngramArr);
    }

    public static String insertSpaces(String[] ngramArr) {
        String ngramWithSpaces = StringUtils.join(ngramArr, " ");
        return ngramWithSpaces;
    }


    private void init_WordInfo_ES(String _word) {
        this.id = _word;
    }

    @Override
    public WordInfo_ES setId(String _id) {
        super.setId(_id);
        word = _id;
        return this;
    }

    @Override
    public String getId() {
        return this.word;
    }

    @Override
    public void setDecompositions(String[][] sampleDecomps, Integer totalDecomps) {
        super.setDecompositions(sampleDecomps, totalDecomps);
        String[] topDecomp = topDecomposition();
        if (topDecomp != null) {
            topDecompositionStr = StringUtils.join(topDecomp, " ");
        }
    }

    public String getWordCharsSpaceConcatenated() {
        if (wordCharsSpaceConcatenated == null && word != null) {
            String[] wordChars = word.split("");
            this.wordCharsSpaceConcatenated =
                    "^ " + String.join(" ", wordChars) + " $";
        }
        return wordCharsSpaceConcatenated;
    }

    public String getMorphemesSpaceConcatenated() {
        if (morphemesSpaceConcatenated == null) {
            String[] topDecomp = topDecomposition();
            if (topDecomp != null) {
                morphemesSpaceConcatenated =
                    "^ " + String.join(" ", topDecomp) + " $";
            }
        }
        return morphemesSpaceConcatenated;
    }
}
