package org.iutools.morph.r2l;

import org.iutools.morph.MorphologicalAnalyzerAbstract;
import org.iutools.morph.MorphologicalAnalyzerAbstract__AccuracyTest;

public class MorphologicalAnalyzer_R2L__AccuracyTest
	extends MorphologicalAnalyzerAbstract__AccuracyTest {
	@Override
	protected MorphologicalAnalyzerAbstract makeAnalyzer() {
		return new MorphologicalAnalyzer_R2L();
	}
}
