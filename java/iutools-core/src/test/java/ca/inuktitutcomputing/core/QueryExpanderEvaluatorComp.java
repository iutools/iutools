package ca.inuktitutcomputing.core;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.jupiter.api.Tag;

import static org.junit.Assert.assertFalse;

import ca.inuktitutcomputing.config.IUConfig;

@Tag("comparator")
public class QueryExpanderEvaluatorComp {

	@Test
	public void test__QueryExpanderEvaluatorComp() throws Exception {
		
		boolean computeStatsOverSurfaceForms = true;
		
		double targetPrecision = 0.7272;
		double targetRecall = 0.5656;
		
		String compiledCorpusTrieFilePath = IUConfig.getIUDataPath()+"src/main/resources/trie_compilation-HANSARD-1999-2002.json";
		String goldStandardCSVFilePath = IUConfig.getIUDataPath()+"src/main/resources/IU100Words-expansions-added-to-alternatives.csv";
		QueryExpanderEvaluator evaluator = 
			new QueryExpanderEvaluator(compiledCorpusTrieFilePath,goldStandardCSVFilePath);
		// whether statistics are to be computed over words (default [true]) or morphemes [false]:
		evaluator.setOptionComputeStatsOverSurfaceForms(computeStatsOverSurfaceForms);
		evaluator.run();
		
		if ((double)evaluator.precision > targetPrecision-0.001 && (double)evaluator.precision < targetPrecision+0.001) {
			assertTrue("",true);
		} else if ((double)evaluator.precision < targetPrecision-0.001) {
			assertFalse("<<< The precision has gone down. Was "+targetPrecision+"; now "+evaluator.precision,true);
		} else {
			assertFalse(">>> THE PRECISION HAS GONE ***UP***. Was "+targetPrecision+"; now "+evaluator.precision,true);
		}
	}
	

}
