package ca.pirurvik.iutools.QueryExpanderEvaluatorComparator;

import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertFalse;

import ca.inuktitutcomputing.config.IUConfig;
import ca.pirurvik.iutools.CompiledCorpus;
import ca.pirurvik.iutools.CompiledCorpusRegistry;
import ca.pirurvik.iutools.QueryExpanderEvaluator;

public class QueryExpanderEvaluatorCompTest {

	@Test @Ignore
	public void test__QueryExpanderEvaluatorComp() throws Exception {
		
		System.out.println("\n\n*** Running test__QueryExpanderEvaluatorComp. This test can take a few minutes to complete\n\n");;

		
		boolean computeStatsOverSurfaceForms = true;
		
		double targetPrecision = 0.7253;
		double targetRecall = 0.5494;
		
		boolean precisionFine = false;
		boolean recallFine = false;
		
		String diagnostic = null;
		
		double gotPrecision;
		double gotRecall;
		
		String goldStandardCSVFilePath = IUConfig.getIUDataPath("/src/test/resources/ca/pirurvik/iutools/IU100Words-expansions-added-to-alternatives.csv");
		
		QueryExpanderEvaluator evaluator = new QueryExpanderEvaluator();
		// Set this to true if you want to see print statements.
		evaluator.verbose = false;

		CompiledCorpus compiledCorpus = CompiledCorpusRegistry.getCorpus();
		compiledCorpus.setVerbose(false);
		evaluator.setCompiledCorpus(compiledCorpus);
		evaluator.setGoldStandard(new File(goldStandardCSVFilePath));
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
				if (gotRecall < targetRecall-0.005) {
					diagnostic += "\nRECALL: "+"<<< The recall has gone ***DOWN***. Was "+targetRecall+"; now "+gotRecall;
				} else {
					diagnostic += "\nRECALL: "+">>> The recall has gone ***UP***. Was "+targetRecall+"; now "+gotRecall;
				}
			}
			assertFalse(diagnostic,true);
		}
	}
	
	public String getLargeCompilationTrieFilePath() throws Exception {
		String compiledCorpusFilePath = IUConfig.getIUDataPath("/data/tries/trie_compilation-HANSARD-1999-2002---single-form-in-terminals.json");
		File compiledCorpusFile = new File(compiledCorpusFilePath);
		if ( !compiledCorpusFile.exists()) {
			throw new Exception("Did not find the large corpus compilation file. Please download it and place it in "+
					compiledCorpusFilePath+". You can download the file from "+
					"https://www.dropbox.com/s/ka3cn778wgs1mk4/trie_compilation-HANSARD-1999-2002---single-form-in-terminals.json?dl=0");
		}
		return compiledCorpusFilePath;
	}
}
