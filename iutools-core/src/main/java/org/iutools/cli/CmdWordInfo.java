package org.iutools.cli;

import ca.nrc.json.PrettyPrinter;
import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.iutools.corpus.CompiledCorpusRegistryException;
import org.iutools.corpus.WordInfo;

public class CmdWordInfo extends ConsoleCommand {

    String corpusName = null;
    String word = null;

    @Override
    public String getUsageOverview() {
        return "Print information about a word found in a given corpus";
    }

    public CmdWordInfo(String name) {
        super(name);
    }

    @Override
    public void execute() throws Exception {
        corpusName = getCorpusName(false);
        if (corpusName == null) {
            corpusName = CompiledCorpusRegistry.defaultCorpusName;
        }

        word = getWord();

        CompiledCorpus corpus = null;
        try {
            corpus = new CompiledCorpusRegistry().getCorpus(corpusName);
        } catch (CompiledCorpusRegistryException e) {
            corpus = new CompiledCorpusRegistry().getCorpus(corpusName);
        }
        echo("Corpus is of type: "+corpus.getClass().getSimpleName());
        WordInfo winfo = corpus.info4word(word);
        if (winfo == null) {
            echo("No occurence of word '"+word+"' found in corpus "+corpusName);
        } else {
            echo("Information about word '"+word+"' from corpus "+corpusName);
            echo(PrettyPrinter.print(winfo));
        }
    }

}
