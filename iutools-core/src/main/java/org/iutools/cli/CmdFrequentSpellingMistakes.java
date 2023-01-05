package org.iutools.cli;

import ca.nrc.datastructure.CloseableIterator;
import ca.nrc.ui.commandline.CommandLineException;
import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.WordInfo;

/**
 * Print the most frequently mis-spelled words in the corpus
 */
public class CmdFrequentSpellingMistakes extends ConsoleCommand {

	private static final Integer MIN_FREQUENCY = 50;
	public CmdFrequentSpellingMistakes(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Print the most frequent mis-spelled words in the corpus";
	}

	@Override
	public void execute() throws Exception {
		CompiledCorpus corpus = getCorpus();
		echo("Mis-spelled words in the corpus with frequency > "+MIN_FREQUENCY);
		echo(1);
		try {
			try (CloseableIterator<String> iter = corpus.wordsWithNoDecomposition()) {
				while (iter.hasNext()) {
					String word = iter.next();
					WordInfo winfo = corpus.info4word(word);
					if (winfo.frequency < MIN_FREQUENCY) {
						break;
					} else
						echo(winfo.word + " (freq: " + winfo.frequency + ")");
				}
			}
		} finally {
			echo(-1);
		}
		return;
	}

}
