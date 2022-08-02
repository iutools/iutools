package org.iutools.corpus.elasticsearch;

import org.iutools.corpus.CompiledCorpusTest;

public class CompiledCorpus_ESTest extends CompiledCorpusTest {

	@Override
	protected CompiledCorpus_ES makeCorpusWithDefaultSegmenter() throws Exception {
		CompiledCorpus_ES corpus = new CompiledCorpus_ES(testIndex);
		return corpus;
	}

}
