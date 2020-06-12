package ca.pirurvik.iutools.corpus;

import java.io.File;

/*************************************
 * Class reading/writing CompiledCorpus objects from/to file.
 * 
 * @author desilets
 *
 */

public abstract class RW_CompiledCorpus {
	
	protected abstract void writeCorpus(CompiledCorpus corpus, 
		File savePath) throws CompiledCorpusException;

	protected abstract CompiledCorpus readCorpus(File savePath) 
		throws CompiledCorpusException;

	protected abstract CompiledCorpus newCorpus(File savePath);

	public static void write(CompiledCorpus corpus, File _savePath) 
		throws CompiledCorpusException {
		RW_CompiledCorpus rw = makeRW(corpus.getClass());
		rw.writeCorpus(corpus, _savePath);
	}

	public static CompiledCorpus read(File savePath, 
			Class<? extends CompiledCorpus> corpusClass) 
			throws CompiledCorpusException {
		RW_CompiledCorpus rw = makeRW(corpusClass);
		CompiledCorpus corpus = rw.readCorpus(savePath);
		
		return corpus;
	}

	private static RW_CompiledCorpus makeRW(
			Class<? extends CompiledCorpus> corpusClass) {
		RW_CompiledCorpus rw = null;
		if (corpusClass == CompiledCorpus_InFileSystem.class) {
			rw = new RW_CompiledCorpus_InFileSystem();
		} else if (corpusClass == CompiledCorpus_InMemory.class) {
			rw = new RW_CompiledCorpus_InMemory();
		}
		return rw;
	}	
}
