package org.iutools.cli;

import java.io.File;

import ca.nrc.ui.commandline.CommandLineException;
import org.iutools.corpus.elasticsearch.CompiledCorpus_ES;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.iutools.corpus.CompiledCorpusRegistryException;

public class CmdDescribeCorpus extends ConsoleCommand {

	@Override
	public String getUsageOverview() {
		return "Compile a corpus from a series of corpus files.";
	}

	public CmdDescribeCorpus(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public void execute() throws Exception {

		String corpusName = getCorpusName(true);


		echo("\nDescription of corpus: "+corpusName);

		CompiledCorpus_ES compiledCorpus = null;
		try {
			new CompiledCorpusRegistry().getCorpus(corpusName);
		} catch (CompiledCorpusRegistryException e) {
		}

		if (compiledCorpus == null) {
			echo("No corpus by the name of "+corpusName);
		} else {
			long totalOccurences = compiledCorpus.totalOccurences();
			long totalOccurencesNoDecomp =
					compiledCorpus.totalOccurencesWithNoDecomp();
			long totalOccurenceWithDecomp =
					totalOccurences - totalOccurencesNoDecomp;

			System.out.println(
					"Total number of analyzed words in trie (succeeded analysis): "+
							totalOccurenceWithDecomp);
			System.out.println("Total number of words that failed analysis: "+
					totalOccurencesNoDecomp);
			System.out.println("");
			System.out.println(
					"Number of distinct analyzed words in trie (succeeded analysis): "+
							compiledCorpus.totalWordsWithDecomps());
			System.out.println("Number of distinct words that failed analysis: "+
					compiledCorpus.totalWordsWithNoDecomp());
			System.out.println("");

			String action = "";
			while ( action!=null ) {
				action = prompt("q:quit    f:failed words");
				if (action==null)
					break;
				if (action.equals("f")) {
					String subaction = prompt("a:all words on screen\nf:all words in file\ns:random set of 100 words on screen\nq:quit");
					if (subaction==null)
						action = null;
					if (subaction!=null) {
// TODO-June2020: Reactivate this code
//					String allFailed[] = compiledCorpus.wor();
//					if (subaction.equals("a")) {
//						Arrays.sort(allFailed);
//						System.out.println(String.join(" ",allFailed)+"\n");
//					} else if (subaction.equals("s")) {
//						Random rand = new Random();
//						int randomIndices[] = rand.ints(0, allFailed.length).distinct().limit(50).toArray();
//						int maxWordsPerLine = 5;
//						for (int i=0; i<randomIndices.length; i++) {
//							System.out.print(allFailed[randomIndices[i]]+"   ");
//							if ((i+1) % maxWordsPerLine == 0)
//								System.out.println();
//						}
//						System.out.println("");
//					} else if (subaction.equals("f")) {
//						String fileName = prompt(">>> enter file name (or q to quit): ");
//						if (fileName==null)
//							break;
//						Arrays.sort(allFailed);
//						File outputFile = new File(fileName);
//						BufferedWriter bw = new BufferedWriter(new FileWriter(outputFile));
//						int maxWordsPerLine = 7;
//						for (int i=0; i<allFailed.length; i++) {
//							bw.write(allFailed[i]+"   ");
//							if ((i+1) % maxWordsPerLine == 0)
//								bw.newLine();
//						}
//						bw.flush();
//						bw.close();
//						System.out.println("Words saved in "+outputFile.getAbsolutePath()+"\n");
//					}
					}
				}
			}


		}
	}
	
	private void printRandom(String x) {
		System.out.println(x);
	}
	
	private boolean checkFilePath(String _trieFilePath) {
		File f = new File(_trieFilePath);
		File dirF = f.getParentFile();
		if ( dirF != null && !dirF.isDirectory() ) {
			return false;
		}
		return true;
	}
}
