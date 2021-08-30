package org.iutools.morph.r2l;

import org.iutools.morph.MorphologicalAnalyzer;
import org.iutools.morph.MorphologicalAnalyzerTest;

public class MorphologicalAnalyzer_R2LTest
	extends MorphologicalAnalyzerTest {

	@Override
	public MorphologicalAnalyzer makeAnalyzer() {
		return new MorphologicalAnalyzer_R2L();
	}
}