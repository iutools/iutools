package ca.pirurvik.iutools.spellchecker;

import static org.junit.Assert.*;

import java.text.DecimalFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.poi.ss.formula.EvaluationConditionalFormatRule;
import org.junit.Assert;
import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.string.StringUtils;
import ca.nrc.testing.AssertNumber;

public class SpellCheckerAccuracyTest {
	
	private static DecimalFormat df2 = new DecimalFormat("#.##");
	
	private static final SpellCheckerExample[] examplesForSuggestions = new SpellCheckerExample[] { 
		new SpellCheckerExample("nunavumi", 5, "nunavummi"),
		new SpellCheckerExample("immaqa", 5, "immaqaa"),
		new SpellCheckerExample("kiinaujatigut", 5, "kiinaujaqtigut"),
		new SpellCheckerExample("kiinaujat", 5, "kiinaujait"),
		new SpellCheckerExample("maligaliqtit", 5, "maligaliqtiit"),
		new SpellCheckerExample("nunavungmi", 5, "nunavummi"),
		new SpellCheckerExample("tamatuminnga", 5, "tamatuminga"),
		new SpellCheckerExample("katimmajjutiksaq", 5, "katimajjutiksaq"),
		new SpellCheckerExample("tanna", 5, "taanna"),
		new SpellCheckerExample("nunavuumit", 5, "nunavummit"),
		new SpellCheckerExample("nniaqtulirinirmut", 5, "aanniaqtulirinirmut"),
		new SpellCheckerExample("qallunaatitut", 5, "qallunaaqtitut"),
		new SpellCheckerExample("nakuqmi", 5, "nakurmiik"),
		new SpellCheckerExample("takkua", 5, "taakkua"),
		new SpellCheckerExample("nunavumiut", 5, "nunavummiut"),
		new SpellCheckerExample("nunavuumik", 5, "nunavummik"),
		new SpellCheckerExample("nunavutmi", 5, "nunavummi"),
		new SpellCheckerExample("asuillaak", 5, "asuilaak"),
		new SpellCheckerExample("pigiaqtitat", 5, "pigiaqtitait"),
		new SpellCheckerExample("uvalu", 5, "uvvalu"),
		new SpellCheckerExample("maligatigut", 5, "maligaqtigut"),
		new SpellCheckerExample("akitujutinut", 5, "akitujuutinut"),
		new SpellCheckerExample("arragumi", 5, "arraagumi"),
		new SpellCheckerExample("nniaqamangittulirinirmut", 5, "aanniaqamangittulirinirmut"),
		new SpellCheckerExample("nigiani", 5, "niggiani"),
		new SpellCheckerExample("tamakkuninnga", 5, "tamakkuninga"),
		new SpellCheckerExample("iksivautap", 5, "iksivautaup"),
		new SpellCheckerExample("sulikkanniiq", 5, "sulikkanniq"),
		new SpellCheckerExample("nunavumut", 5, "nunavummut"),
		new SpellCheckerExample("katimajit", 5, "katimajiit"),
		new SpellCheckerExample("tamatumunnga", 5, "tamatumunga"),
		new SpellCheckerExample("nniaqamangittulirijiit", 5, "aanniaqamangittulirijiit"),
		new SpellCheckerExample("ugaalautaa", 5, "uqaalautaa"),
		new SpellCheckerExample("tavani", 5, "tavvani"),
		new SpellCheckerExample("iksivauitaaq", 5, "iksivautaaq", "iksivautaak", "issivautaaq", "issivautaak", "itsivautaaq", "itsivautaak"),
		new SpellCheckerExample("tamaini", 5, "tamainni"),
		new SpellCheckerExample("nniaqtulirijikkunnut", 5, "aanniaqtulirijikkunnut"),
		new SpellCheckerExample("immaqaqai", 5, "immaqaaqai"),
		new SpellCheckerExample("taimak", 5, "taimaak")
	};
		

	@Test
	public void test__EvaluateSugestions() throws Exception {
		
		SpellCheckerEvaluator evaluator = new SpellCheckerEvaluator();
		
		for (SpellCheckerExample exampleData: examplesForSuggestions) {
			evaluator.onNewExample(exampleData);
		}
		
		System.out.println("** test__EvaluateSugestions: Histogram of the rank of the first appropriate spelling");
		List<Pair<Integer,Double>> histogram = evaluator.correctSpellingRankHistogramRelative();
		for (Pair<Integer,Double> entry: histogram) {
			String roundedFreq =  df2.format(entry.getSecond());
			System.out.println("  "+entry.getFirst()+": "+roundedFreq);
		}
		
		Double expAverageRank = new Double(1.17);
		double avgRankTolerance = 0.05;
		AssertNumber.isLessOrEqualTo(
				"The average rank was higher than expected.",
				evaluator.averageRank(), expAverageRank + avgRankTolerance);
		AssertNumber.isGreaterOrEqualTo(
				"Significant improvement found in the average rank.\nYou might want to decrease the expectation so we don't loose that gain in the future.",
				evaluator.averageRank(), expAverageRank - avgRankTolerance);;
		
		
		int N = 5;
		double expPercentFoundInTopN = 0.87;
		double tolerance = 0.01;
		assertPercentFoundInTopN(expPercentFoundInTopN, N, 
				histogram, tolerance);
		
		assertNoExampleWithBadRank(evaluator);
	}


	private void assertNoExampleWithBadRank(SpellCheckerEvaluator evaluator) {
		String errMess = null;
		if (evaluator.examplesWithBadRank.size() > 0) {
			errMess = 
				"There were examples for which the rank of the first correct suggestion exceeded the expected maximum.\n"+
				"List of such examples below.\n\n";
			for (SpellCheckerExample example: evaluator.examplesWithBadRank.keySet()) {
				Pair<Integer,List<String>> problem = 
						evaluator.examplesWithBadRank.get(example);
				String word = example.wordToCheck;
				Integer rank = problem.getFirst();
				List<String> topCandidates = 
						problem.getSecond()
							.stream()
							.limit(20)
							.collect(Collectors.toList());
				errMess += "  "+word+": rank="+
					evaluator.correctSpellingRank.get(word)+
					" (exp <= "+example.expMaxRank+")\n"+
					"  Correctly spelled forms: "+
					StringUtils.join(example.acceptableCorrections.iterator(), ", ")+"\n"+
					"  Top candidates were: "+
					StringUtils.join(topCandidates.iterator(), ", ")+"\n\n"
					;
			}
			if (errMess != null) {
				Assert.fail(errMess);
			}
		}
		
	}


	private void assertPercentFoundInTopN(double expPercentFoundInTopN, int N, 
			List<Pair<Integer, Double>> histogram, double tolerance) {
		
		double gotPercentFoundInTopN = 0.0;
		for (int rankFound=0; rankFound < N; rankFound++) {
			Pair<Integer,Double> histEntry = histogram.get(rankFound);
			gotPercentFoundInTopN += histEntry.getSecond();
		}
		
		double delta = gotPercentFoundInTopN - expPercentFoundInTopN;
		if (Math.abs(delta) > tolerance) {
			if (delta < 0) {
				fail("Significant DECREASE found for the percentage of words with an acceptable correction in the top "+N+
						"\n  Got: "+gotPercentFoundInTopN+"\n  Exp: "+expPercentFoundInTopN+"\n  Delta: "+delta);
			} else {
				fail("Significant INCREASE found for the percentage of words with an acceptable correction in the top "+N+
						"\n  Got: "+gotPercentFoundInTopN+"\n  Exp: "+expPercentFoundInTopN+"\n  Delta: "+delta+
						"\n\nYou should probably change the expectations for that test so we don't loose that improvement in the future.");
				
			}
		}
	}

}
