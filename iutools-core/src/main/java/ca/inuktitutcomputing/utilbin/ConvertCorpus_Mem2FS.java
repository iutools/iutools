package ca.inuktitutcomputing.utilbin;

import java.io.File;
import java.util.Iterator;

import ca.pirurvik.iutools.corpus.CompiledCorpus_InFileSystem;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;
import ca.pirurvik.iutools.corpus.RW_CompiledCorpus;
import ca.pirurvik.iutools.corpus.WordInfo;

/******************************************************************************
 * 
 * @author desilets
 * 
 * 2020-06-29
 * Temporary utility. Delete it once all Mem corpora have been converted.
 *
 ******************************************************************************/
public class ConvertCorpus_Mem2FS {
	
	File memCorpusFile = null;
	File fsCorpusDir = null;
	CompiledCorpus_InMemory corpInMem = null;
	CompiledCorpus_InFileSystem corpInFS = null;
	boolean verbose = true;
	
	public ConvertCorpus_Mem2FS(File _memCorpusFile, File _fsCorpusDir) {
		this.memCorpusFile = _memCorpusFile;
		this.fsCorpusDir = _fsCorpusDir;
	}

	public static void main(String[] args) throws Exception {
		File memCorpusFile = new File(args[0]);
		File fsCorpusDir = new File(args[1]);
		ConvertCorpus_Mem2FS converter = 
			new ConvertCorpus_Mem2FS(memCorpusFile, fsCorpusDir);
		converter.run();
	}

	private void run() throws Exception {
		corpInMem = (CompiledCorpus_InMemory) RW_CompiledCorpus.read(memCorpusFile, corpInMem.getClass());
		corpInFS = (CompiledCorpus_InFileSystem) RW_CompiledCorpus.read(fsCorpusDir, corpInFS.getClass());
		
		Iterator<String> iter = corpInMem.allWords();
		while (iter.hasNext()) {
			String word = iter.next();
			WordInfo winfo = corpInMem.info4word(word);
			echo("  word="+word+"; freq="+winfo.frequency);
		}
	}

	private void echo(String mess) {
		if (verbose) {
			System.out.println(mess);
		}
	}
}
