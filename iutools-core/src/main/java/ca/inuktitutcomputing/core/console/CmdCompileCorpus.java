package ca.inuktitutcomputing.core.console;

import java.io.File;
import java.io.IOException;

import ca.nrc.datastructure.trie.StringSegmenter_IUMorpheme;
import ca.pirurvik.iutools.corpus.CompiledCorpus;

public class CmdCompileCorpus extends ConsoleCommand {

	@Override
	public String getUsageOverview() {
		return "Compile a corpus from a series of corpus files.";
	}

	public CmdCompileCorpus(String name) {
		super(name);
	}

	@Override
	public void execute() throws Exception {
		
		String corpusDir = getCorpusDir();
		
		String compilationFile = getCompilationFile();
		
		boolean fromScratch = this.cmdLine.hasOption("from-scratch");
		boolean redoFailed = this.cmdLine.hasOption("redo-failed");

		echo("\nCompiling corpus:\n");
		echo(1);
		{
			echo("corpus directory: "+corpusDir+"\noutput json file: "+compilationFile+"\nfrom scratch: "+fromScratch+"\n");
		}
		echo(-1);
		
		CompiledCorpus compiledCorpus = new CompiledCorpus(StringSegmenter_IUMorpheme.class.getName());
		boolean ok = true;
		if ( compilationFile != null )
			ok = checkFilePath(compilationFile);
		if ( !ok ) {
			System.err.println("ERROR: The --corpus-dir argument points to a non-existent directory. Abort.");
			System.exit(1);
		}
		
		try {
			if (fromScratch)
				compiledCorpus.compileCorpusFromScratch(corpusDir);
			else if (!redoFailed)
				compiledCorpus.compileCorpus(corpusDir);
			else {
				File compilationBackupFile = new File(corpusDir + "/" + CompiledCorpus.JSON_COMPILATION_FILE_NAME);
				if (compilationBackupFile.exists()) {
					compiledCorpus = CompiledCorpus.createFromJson(compilationBackupFile.getAbsolutePath());
					compiledCorpus.recompileWordsThatFailedAnalysis(corpusDir);
				} else {
					System.err.println("ERROR: " + "No json compilation backup file in corpus directory. The compilation cannot resume. Abort.");
					System.exit(1);
				}
			}
			compiledCorpus.saveCompilerInJSONFile(compilationFile);
			echo("\nThe result of the compilation has been saved in the file " + compilationFile + ".\n");
		} catch (Exception e) {
			System.err.println("ERROR: " + e.getMessage());
		}
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
