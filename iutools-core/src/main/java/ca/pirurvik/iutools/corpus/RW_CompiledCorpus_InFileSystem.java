package ca.pirurvik.iutools.corpus;

import java.io.File;

import ca.nrc.file.FileCopy;

public class RW_CompiledCorpus_InFileSystem extends RW_CompiledCorpus {

	@Override
	protected CompiledCorpus_Base newCorpus(File savePath) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected CompiledCorpus_Base readCorpus(File savePath) throws CompiledCorpusException {
		CompiledCorpus_InFileSystem corpus = new CompiledCorpus_InFileSystem(savePath);
		
		return corpus;
	}

	@Override
	protected void writeCorpus(CompiledCorpus_Base corpus, File savePath) 
		throws CompiledCorpusException {
		
		File corpusDir = ((CompiledCorpus_InFileSystem)corpus).corpusDir;
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
