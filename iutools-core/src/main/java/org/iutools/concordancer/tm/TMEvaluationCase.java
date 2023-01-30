package org.iutools.concordancer.tm;

import org.iutools.text.IUWord;

import java.util.*;

/**
 * A case for evaluating the performance of the TM.
 * The case is generated based on glossary entries for an IU word.
 */
public class TMEvaluationCase {
    /**
     * Word in IU roman
     */
    public String iuTerm_roman = null;
    /**
     * Word in IU syllabic
     */
    public String iuTerm_syll = null;

    public String[] relatedTerms = new String[0];

    /**
     * Known EN equivalents for the IU word.
     */
    public List<String> enEquivalents = new ArrayList<>();

    /**
     * Set of glossaries in which we found an entry for the IU word.
     */
    public Set<String> glossarySources = new HashSet<>();

    /**
     * Dialects for which the IU word is valid. If null, it means we don't know the dialect.
     */
    public Set<String> dialects = null;

    public TMEvaluationCase(IUWord word) {
        this.iuTerm_roman = word.inRoman();
        this.iuTerm_syll = word.inSyll();
    }

    public TMEvaluationCase addDialects(String[] newDialects) {
        if (newDialects != null){
            if (dialects == null) {
                dialects = new HashSet<>();
            }
            Collections.addAll(dialects, newDialects);
        }
        return this;
    }

    public TMEvaluationCase addSource(String newSource) {
        glossarySources.add(newSource);
        return this;
    }

    public TMEvaluationCase addEnEquilvalents(List<String> enTerms) {
        if (enTerms != null) {
            for (String anEnTerm: enTerms) {
                if (anEnTerm != null && !anEnTerm.isEmpty()) {
                    enEquivalents.add(anEnTerm);
                }
            }
        }
        return this;
    }
}
