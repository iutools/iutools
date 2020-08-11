package ca.pirurvik.iutools.corpus;

import java.io.File;

import ca.nrc.file.FileCopy;

public class RW_CompiledCorpus_v2FS extends RW_CompiledCorpus {

	@Override
	protected CompiledCorpus newCorpus(File savePath) {
		return null;
	}

	@Override
	protected CompiledCorpus readCorpus(File savePath) throws CompiledCorpusException {
		CompiledCorpus_v2FS corpus = new CompiledCorpus_v2FS(savePath);
		
		return corpus;
	}

	@Override
	protected void writeCorpus(CompiledCorpus corpus, File savePath) 
		throws CompiledCorpusException {
		
		File corpusDir = ((CompiledCorpus_v2FS)corpus).corpusDir;
		// If the corpus is already using the savePath as its corpusDir, then
		// there is nothing to do.
		//
		// Otherwise, copy the content of the corpusDir to the new savePath
		//
		if (!corpusDir.equals(savePath)) {
			FileCopy.copyFolder(corpusDir, savePath);
		}
		
		return;
	}
}
