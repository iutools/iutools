package ca.inuktitutcomputing.core.console;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import ca.inuktitutcomputing.morph.failureanalysis.MorphFailureAnalyzer;
import ca.inuktitutcomputing.morph.failureanalysis.ProblematicNGram;
import ca.inuktitutcomputing.morph.failureanalysis.ProblematicNGram.SortBy;
import ca.nrc.ui.commandline.UserIO.Verbosity;
import ca.pirurvik.iutools.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;
import ca.pirurvik.iutools.corpus.WordInfo;

public class CmdMorphFailureAnalysis extends ConsoleCommand {
	
	Verbosity verbosity = getVerbosity();
	
	public CmdMorphFailureAnalysis(String name) {
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

		CompiledCorpus corpus = CompiledCorpusRegistry.getCorpus();
		MorphFailureAnalyzer analyzer = makeAnalyzer();
				
		loadCorpusWords(corpus, analyzer);
		
		Long numProblems = getMaxNgrams();
		if (numProblems == null) {
			numProblems = new Long(200);
		}
		
		printMostProblematicNgrams(analyzer, SortBy.FS_RATIO, 
				numProblems);
		printMostProblematicNgrams(analyzer, SortBy.N_FAILURES, 
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

		return analyzer;
	}

	private void loadCorpusWords(CompiledCorpus corpus, 
			MorphFailureAnalyzer analyzer) {
		
		Long countdown = getMaxWords();
		Iterator<String> iter = corpus.allWords();
		while (iter.hasNext()) {
			String word = iter.next();
			WordInfo wInfo = corpus.info4word(word);
			Boolean decomposes = wInfo.decomposesSuccessfully();
			if (verbosityLevelIsMet(Verbosity.Level1)) {
				echo("loading word: "+word+" (decomposes: "+decomposes+")");
			}
			analyzer.addWord(word, decomposes, wInfo.frequency);
			countdown--;
			if (countdown == 0) {
				break;
			}
		}
		
		analyzer.analyseFailures();
	}
	
	private void printMostProblematicNgrams(MorphFailureAnalyzer analyzer, 
			SortBy sortBy, Long maxProblems) {
		
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
