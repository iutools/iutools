package ca.inuktitutcomputing.core.console;

import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.corpus.*;

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
            corpusName = CompiledCorpusRegistry.defaultESCorpusName;
        }

        word = getWord();

//        CompiledCorpus corpus = new CompiledCorpus_ES(corpusName);

        CompiledCorpus corpus = null;
        try {
            corpus = CompiledCorpusRegistry.getCorpusWithName_ES(corpusName);
        } catch (CompiledCorpusRegistryException e) {
            corpus = CompiledCorpusRegistry.getCorpusWithName(corpusName);
        }
        WordInfo winfo = corpus.info4word(word);
        if (winfo == null) {
            echo("No occurence of word '"+word+"' found in corpus "+corpusName);
        } else {
            echo("Information about word '"+word+"' from corpus "+corpusName);
            echo(PrettyPrinter.print(winfo));
        }
    }

}
