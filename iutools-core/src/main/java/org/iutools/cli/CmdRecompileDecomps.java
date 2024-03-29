package org.iutools.cli;

import ca.nrc.debug.Debug;
import ca.nrc.ui.commandline.CommandLineException;
import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.morph.*;
import org.iutools.morph.r2l.MorphologicalAnalyzer_R2L;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.concurrent.TimeoutException;

public class CmdRecompileDecomps extends ConsoleCommand {

	MorphologicalAnalyzer_R2L analyzer = null;

	public CmdRecompileDecomps(String name) throws CommandLineException {
		super(name);
		analyzer = new MorphologicalAnalyzer_R2L();
	}

	@Override
	public String getUsageOverview() {
		return "Recompute the morphological decompositions of certain words in a corpus.";
	}

	@Override
	public void execute() throws Exception {
		Path inputFile = getInputFile();
		String comment = getComment();

		CompiledCorpus corpus = getCorpus();

		try (BufferedReader br =
			new BufferedReader(new FileReader(inputFile.toFile()))) {
			String word;
			int wordNum = 0;
			while ((word = br.readLine()) != null) {
				wordNum++;
				echo("redecomposing word #"+wordNum+": "+word);
				redecomposeWord(word, corpus, comment);
			}
		}
	}

	private void redecomposeWord(String word, CompiledCorpus corpus,
		String comment)  {
		try {
			Decomposition[] decompObjs = analyzer.decomposeWord(word);
			String[][] decomps = Decomposition.decomps2morphemes(decompObjs);
			corpus.addWordOccurence(word, decomps, new Integer(decomps.length), 0);
		} catch (TimeoutException e) {
			echo("   Analyzer timed out (or was interrupted)!");
		} catch (MorphologicalAnalyzerException | CompiledCorpusException | DecompositionException e) {
			echo(1);
			{
				echo("   Analyzer raised exception: " + Debug.printCallStack(e));
			}
			echo(-1);
		}
	}
}
