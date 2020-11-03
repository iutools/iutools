package ca.pirurvik.iutools.corpus;

import ca.nrc.ui.commandline.UserIO;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RW_CompiledCorpus_ES extends RW_CompiledCorpus {

    static Pattern pattSavePath = Pattern.compile(".*?(^|[^/\\\\.]*)\\.ES\\.json$");

    private String _corpusName;

    public RW_CompiledCorpus_ES() {
        super((UserIO)null);
        init_RW_CompiledCorpus_ES(null);
    }

    public RW_CompiledCorpus_ES(String _intoCorpusNamed) {
        super((UserIO)null);
        init_RW_CompiledCorpus_ES(_intoCorpusNamed);
    }

    public RW_CompiledCorpus_ES(String _intoCorpusNamed, UserIO io) {
        super(io);
        init_RW_CompiledCorpus_ES(_intoCorpusNamed);
    }

    private void init_RW_CompiledCorpus_ES(String _corpusName) {
        this._corpusName = _corpusName;
    }

    @Override
    public void writeCorpus(CompiledCorpus corpus, File savePath)
        throws CompiledCorpusException {
    }

    @Override
    public CompiledCorpus readCorpus(File jsonFile) throws CompiledCorpusException {
        String corpusName = corpusName(jsonFile);
        CompiledCorpus_ES corpus =
                new CompiledCorpus_ES(corpusName);
        echo("Loading file "+jsonFile+
                " into ElasticSearch corpus "+corpusName);
        boolean verbose =
            (userIO != null &&
                userIO.verbosityLevelIsMet(UserIO.Verbosity.Level1));

        corpus.loadFromFile(jsonFile, verbose, (Boolean)null, corpusName);

        return corpus;
    }

    @Override
    protected CompiledCorpus newCorpus(File savePath) throws CompiledCorpusException {
        String corpusName = CompiledCorpus_ES.corpusName4File(savePath);
        return new CompiledCorpus_ES(corpusName);
    }

    protected String corpusName(File jsonFile) {
        if (_corpusName == null) {
            _corpusName = CompiledCorpus_ES.corpusName4File(jsonFile);
        }
        return _corpusName;
    }
}