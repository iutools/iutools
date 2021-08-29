package org.iutools.morph.r2l;

import org.iutools.morph.MorphologicalAnalyzerAbstract;
import org.iutools.morph.MorphologicalAnalyzerAbstractTest;

public class MorphologicalAnalyzer_L2RTest
	extends MorphologicalAnalyzerAbstractTest {

	@Override
	public MorphologicalAnalyzerAbstract makeAnalyzer() {
		return new MorphologicalAnalyzer__L2R();
	}
}