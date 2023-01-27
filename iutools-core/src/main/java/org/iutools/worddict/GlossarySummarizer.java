package org.iutools.worddict;

import ca.nrc.dtrc.stats.FrequencyHistogram;
import org.apache.commons.lang3.tuple.Pair;

import java.util.*;

/**
 * Produce summary statistics for a glossary
 */
public class GlossarySummarizer {

    Glossary gloss = null;
    public Summary summarize(Glossary _gloss) throws GlossaryException {
        Summary summary = new Summary();
        gloss = _gloss;
        Set<String> terms = gloss.allTermDescriptions();
        for (String termDescr: terms) {
            onNewTermDescription(termDescr, summary);
        }
        return summary;
    }

    private void onNewTermDescription(String termDescr, Summary summary) throws GlossaryException {
        Pair<String,String> parsed = Glossary.parseTermDescription(termDescr);
        String lang = parsed.getLeft();
        String term = parsed.getRight();
        if (lang != null) {
            summary.incrementLanguageFrequency(lang);
            for (GlossaryEntry entry: gloss.entries4word(lang, term)) {
                String[] dialects = entry.dialects;
                if (dialects != null) {
                    for (String dialect : dialects) {
//                        System.out.println("--** onNewTermDescription: dialect="+dialect);
                        summary.incrementDialectFrequency(dialect);
                    }
                }
            }
        }
    }

    public static class Summary {

        FrequencyHistogram<String> _langTermsHistogram = new FrequencyHistogram<String>();
        FrequencyHistogram<String> _dialectTermsHistogram = new FrequencyHistogram<String>();

        public Long totalTerms4lang(String lang) {
            Long total = _langTermsHistogram.frequency(lang);
            return total;
        }

        public void incrementLanguageFrequency(String lang) {
            if (lang != null) {
                _langTermsHistogram.updateFreq(lang);
            }
        }

        public Set<String> allLanguages() {
            return _langTermsHistogram.allValues();
        }

        public Long totalTerms() {
            return _langTermsHistogram.totalOccurences();
        }

        public Set<String> iuDialects() {
            return _dialectTermsHistogram.allValues();
        }

        public void incrementDialectFrequency(String dialect) {
            if (dialect != null) {
                _dialectTermsHistogram.updateFreq(dialect);
            }
        }
    }
}
