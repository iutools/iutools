package org.iutools.cli;

import ca.nrc.ui.commandline.CommandLineException;
import org.iutools.worddict.Glossary;
import org.iutools.worddict.GlossarySummarizer;

public class CmdGlossariesSummary extends ConsoleCommand {

    public CmdGlossariesSummary(String name) throws CommandLineException {
        super(name);
    }

    @Override
    public String getUsageOverview() {
        return "Summarize the content of various glossaries";
    }

    @Override
    public void execute() throws Exception {
        GlossarySummarizer summarizer = new GlossarySummarizer();
        GlossarySummarizer.Summary summary = summarizer.summarize(Glossary.get());
        echo("Glossary summary");
        echo(1);
        try {
            echo("Total terms : "+summary.totalTerms());
            echo("Total terms by language");
            echo(1);
            try {
                for (String lang: summary.allLanguages()) {
                    echo(lang+": "+summary.totalTerms4lang(lang));
                }
            } finally {
                echo(-1);
            }
            String dialects = "None";
            if (!summary.iuDialects().isEmpty()) {
                dialects = String.join(", ", summary.iuDialects());
            }
            echo("Dialects: "+dialects);
        } finally {
            echo(-1);
        }
    }
}
