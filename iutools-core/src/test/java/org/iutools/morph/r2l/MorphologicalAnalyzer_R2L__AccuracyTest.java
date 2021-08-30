package org.iutools.morph.r2l;

import org.iutools.morph.MorphologicalAnalyzer;
import org.iutools.morph.MorphologicalAnalyzer__AccuracyTest;

public class MorphologicalAnalyzer_R2L__AccuracyTest
	extends MorphologicalAnalyzer__AccuracyTest {
	@Override
	protected MorphologicalAnalyzer makeAnalyzer() {
		return new MorphologicalAnalyzer_R2L();
	}
}
