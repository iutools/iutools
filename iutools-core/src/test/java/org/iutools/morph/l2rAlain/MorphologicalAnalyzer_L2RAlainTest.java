package org.iutools.morph.l2rAlain;


import org.iutools.morph.MorphologicalAnalyzer;
import org.iutools.morph.MorphologicalAnalyzerException;
import org.iutools.morph.MorphologicalAnalyzerTest;
import org.junit.Ignore;

@Ignore
public class MorphologicalAnalyzer_L2RAlainTest extends MorphologicalAnalyzerTest {

	@Override
	public MorphologicalAnalyzer makeAnalyzer() {
		try {
			return new MorphologicalAnalyzer_L2RAlain();
		} catch (MorphologicalAnalyzerException e) {
			throw new RuntimeException(e);
		}
	}

}
