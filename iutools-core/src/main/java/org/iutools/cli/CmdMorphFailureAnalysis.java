package org.iutools.cli;

import java.util.Iterator;
import java.util.List;

import ca.nrc.ui.commandline.CommandLineException;
import org.iutools.morph.failureanalysis.MorphFailureAnalyzer;
import org.iutools.morph.failureanalysis.MorphFailureAnalyzerException;
import org.iutools.morph.failureanalysis.ProblematicNGram;
import org.iutools.morph.failureanalysis.ProblematicNGram.SortBy;
import ca.nrc.ui.commandline.UserIO.Verbosity;
import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.iutools.corpus.WordInfo;

public class CmdMorphFailureAnalysis extends ConsoleCommand {
	
	Verbosity verbosity = getVerbosity();
	CompiledCorpus corpus = null;
	
	public CmdMorphFailureAnalysis(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return 
			"Analyse failures of the Morphological Analyzer for words in a corpus and \n"+
			"ngrams that seem to cause many failures."
			;
	}
	
	@Override
	public void execute() throws Exception {

		corpus = new CompiledCorpusRegistry().getCorpus();

		this.user_io.setVerbosity(getVerbosity());
		MorphFailureAnalyzer analyzer = makeAnalyzer();
				
		loadCorpusWords(corpus, analyzer);
		
		Long numProblems = getMaxNgrams();
		if (numProblems == null) {
			numProblems = new Long(200);
		}


		printMostProblematicNgrams(analyzer, SortBy.FS_RATIO_THEN_FAILURES,
				numProblems);
		printMostProblematicNgrams(analyzer, SortBy.N_FAILURES, 
				numProblems);
		printMostProblematicNgrams(analyzer, SortBy.FS_RATIO_THEN_LENGTH_THEN_N_FAILURES,
			numProblems);
	}

	private MorphFailureAnalyzer makeAnalyzer() {
		MorphFailureAnalyzer analyzer = new MorphFailureAnalyzer();
		Integer minNgramLen = getMinNgramLen();
		if (minNgramLen == null) {
			minNgramLen = 3;
		}
		analyzer.setMinNgramLen(minNgramLen);
		
		String excludePatt = getExcludePattern();
		analyzer.setExclude(excludePatt);
		analyzer.userIO = this.user_io;

		return analyzer;
	}

	private void loadCorpusWords(CompiledCorpus corpus, 
			MorphFailureAnalyzer analyzer) throws CompiledCorpusException {

		Long countdown = getMaxWords();
		Iterator<String> iter = corpus.allWords();
		while (iter.hasNext()) {
			String word = iter.next();
			WordInfo wInfo = corpus.info4word(word);
			Boolean decomposes = wInfo.decomposesSuccessfully();
			analyzer.addWord(word, decomposes, wInfo.frequency);
			countdown--;
			if (countdown == 0) {
				break;
			}
		}
		
		analyzer.analyseFailures();
	}
	
	private void printMostProblematicNgrams(MorphFailureAnalyzer analyzer, 
			SortBy sortBy, Long maxProblems) throws CommandLineException {
		
		echo("List of "+maxProblems+" most problematic ngrams");
		echo("  sorted by "+sortBy);
		echo("  in CSV format");
		echo(1);
		{
			int problemCount = 0;
			echo(ProblematicNGram.csvHeaders());
			List<ProblematicNGram> problems = analyzer.getProblems(sortBy);
			for (int ii=0; ii < problems.size(); ii++) {
				ProblematicNGram aProblem = problems.get(ii);
				try {
					aProblem.computeExamples(this.corpus);
				} catch (MorphFailureAnalyzerException e) {
					throw new CommandLineException(e);
				}
				echo(aProblem.toCSV());
				if (problemCount == maxProblems) {
					break;
				}
				problemCount++;
			}
		}
		echo(-1);
	}
}
