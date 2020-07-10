package ca.pirurvik.iutools;


import static org.junit.Assert.assertEquals;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import ca.pirurvik.iutools.MorphRelativesFinderEvaluator;

public class MorphRelativesFinderEvaluatorTest {

	@Test(expected=FileNotFoundException.class)
	public void test__QueryExpanderEvaluator__Synopsis() throws Exception {
		
		String compiledCorpusTrieFilePath = "/path/to/json/file/of/compiled/corpus";
		String goldStandardCSVFilePath = "/path/to/gold/standard/csv/file";
		MorphRelativesFinderEvaluator evaluator = 
			new MorphRelativesFinderEvaluator(compiledCorpusTrieFilePath,goldStandardCSVFilePath);
		// if statistics are to be computed over morphemes instead of words:
		evaluator.setOptionComputeStatsOverSurfaceForms(true);
		evaluator.run();
	}
}
