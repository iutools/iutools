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
		
		double targetPrecision = 0.7324;
		double targetRecall = 0.5474;
		
		boolean precisionFine = false;
		boolean recallFine = false;
		
		String diagnostic = null;
		
		double gotPrecision;
		double gotRecall;
		
		String compiledCorpusTrieFilePath = System.getenv("IUTOOLS_EXTERNAL_REPOSITORY")+"/trie_compilation-HANSARD-1999-2002---single-form-in-terminals.json";
		String goldStandardCSVFilePath = System.getenv("IUTOOLS_EXTERNAL_REPOSITORY")+"/IU100Words-expansions-added-to-alternatives.csv";
		QueryExpanderEvaluator evaluator = 
			new QueryExpanderEvaluator(compiledCorpusTrieFilePath,goldStandardCSVFilePath);
		// whether statistics are to be computed over words (default [true]) or morphemes [false]:
		evaluator.setOptionComputeStatsOverSurfaceForms(computeStatsOverSurfaceForms);

		evaluator.run();
		gotPrecision = (double)evaluator.precision;
		gotRecall = (double)evaluator.recall;
		
		if (gotPrecision > targetPrecision-0.001 && gotPrecision < targetPrecision+0.001) {
			precisionFine = true;
		}
		
		if (gotRecall > targetRecall-0.001 && gotRecall < targetRecall+0.001) {
			recallFine = true;
		}
		
		if (precisionFine && recallFine)
			assertTrue("",true);
		else {
			if ( !precisionFine ) {
				if (gotPrecision < targetPrecision-0.001) {
					diagnostic = "\nPRECISION: "+"<<< The precision has gone ***DOWN***. Was "+targetPrecision+"; now "+gotPrecision;
				} else {
					diagnostic = "\nPRECISION: "+">>> The precision has gone ***UP***. Was "+targetPrecision+"; now "+gotPrecision;
				}
			}
			if ( !recallFine ) {
				if (gotRecall < targetRecall-0.001) {
					diagnostic += "\nRECALL: "+"<<< The recall has gone ***DOWN***. Was "+targetRecall+"; now "+gotRecall;
				} else {
					diagnostic += "\nRECALL: "+">>> The recall has gone ***UP***. Was "+targetRecall+"; now "+gotRecall;
				}
			}
			assertFalse(diagnostic,true);
		}
	}
	

}
