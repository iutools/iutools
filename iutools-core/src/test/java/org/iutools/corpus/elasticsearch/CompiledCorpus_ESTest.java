package org.iutools.corpus.elasticsearch;

import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusRegistry;
import org.iutools.corpus.CompiledCorpusTest;

public class CompiledCorpus_ESTest extends CompiledCorpusTest {

	@Override
	protected CompiledCorpus makeCorpusWithDefaultSegmenter() throws Exception {
		CompiledCorpus corpus = new CompiledCorpusRegistry().makeCorpus(testIndex);
		return corpus;
	}

}
