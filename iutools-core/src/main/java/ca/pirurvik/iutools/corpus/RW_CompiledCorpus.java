package ca.pirurvik.iutools.corpus;

import java.io.File;

/*************************************
 * Class reading/writing CompiledCorpus objects from/to file.
 * 
 * @author desilets
 *
 */

public abstract class RW_CompiledCorpus {
	
	protected abstract void writeCorpus(CompiledCorpus_Base corpus, 
		File savePath) throws CompiledCorpusException;

	protected abstract CompiledCorpus_Base readCorpus(File savePath) 
		throws CompiledCorpusException;

	protected abstract CompiledCorpus_Base newCorpus(File savePath);

	public static void write(CompiledCorpus_Base corpus, File _savePath) 
		throws CompiledCorpusException {
		RW_CompiledCorpus rw = makeRW(corpus.getClass());
		rw.writeCorpus(corpus, _savePath);
	}

	public static CompiledCorpus_Base read(File savePath, 
			Class<? extends CompiledCorpus_Base> corpusClass) 
			throws CompiledCorpusException {
		RW_CompiledCorpus rw = makeRW(corpusClass);
		CompiledCorpus_Base corpus = rw.readCorpus(savePath);
		
		return corpus;
	}

	private static RW_CompiledCorpus makeRW(
			Class<? extends CompiledCorpus_Base> corpusClass) {
		RW_CompiledCorpus rw = null;
		if (corpusClass == CompiledCorpus_InFileSystem.class) {
			rw = new RW_CompiledCorpus_InFileSystem();
		} else if (corpusClass == CompiledCorpus_InMemory.class) {
			rw = new RW_CompiledCorpus_InMemory();
		}
		return rw;
	}	
}
