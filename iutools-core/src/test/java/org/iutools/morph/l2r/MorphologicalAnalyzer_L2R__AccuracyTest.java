package org.iutools.morph.l2r;

import ca.nrc.config.ConfigException;
import org.iutools.datastructure.trie.TrieException;
import org.iutools.morph.MorphologicalAnalyzer;
import org.iutools.morph.MorphologicalAnalyzer__AccuracyTest;
import org.junit.Ignore;

import java.io.IOException;

@Ignore
public class MorphologicalAnalyzer_L2R__AccuracyTest
	extends MorphologicalAnalyzer__AccuracyTest {
	@Override
	protected MorphologicalAnalyzer makeAnalyzer() {
		try {
			return new MorphologicalAnalyzer_L2R();
		} catch (TrieException | IOException | ConfigException e) {
			throw new RuntimeException(e);
		}
	}
}
