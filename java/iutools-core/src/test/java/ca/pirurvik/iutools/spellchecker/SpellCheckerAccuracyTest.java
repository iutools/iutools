package ca.pirurvik.iutools.spellchecker;

import static org.junit.Assert.*;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import ca.nrc.datastructure.Pair;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.pirurvik.iutools.SpellChecker;
import ca.pirurvik.iutools.SpellingCorrection;

public class SpellCheckerAccuracyTest {
	
	private static DecimalFormat df2 = new DecimalFormat("#.##");
	
	private static final SpellCheckerExample[] examplesForSuggestions = new SpellCheckerExample[] { 
		new SpellCheckerExample("nunavumi", "nunavummi"),
		new SpellCheckerExample("immaqa", "immaqaa"),
		new SpellCheckerExample("kiinaujatigut", "kiinaujaqtigut"),
		new SpellCheckerExample("kiinaujat", "kiinaujait"),
		new SpellCheckerExample("maligaliqtit", "maligaliqtiit"),
		new SpellCheckerExample("nunavungmi", "nunavummi"),
		new SpellCheckerExample("tamatuminnga", "tamatuminga"),
		new SpellCheckerExample("katimmajjutiksaq", "katimajjutiksaq"),
		new SpellCheckerExample("tanna", "taanna"),
		new SpellCheckerExample("nunavuumit", "nunavummit"),
		new SpellCheckerExample("nniaqtulirinirmut", "aanniaqtulirinirmut"),
		new SpellCheckerExample("qallunaatitut", "qallunaaqtitut"),
		new SpellCheckerExample("nakuqmi", "nakurmiik"),
		new SpellCheckerExample("takkua", "taakkua"),
		new SpellCheckerExample("nunavumiut", "nunavummiut"),
		new SpellCheckerExample("nunavuumik", "nunavummik"),
		new SpellCheckerExample("nunavutmi", "nunavummi"),
		new SpellCheckerExample("asuillaak", "asuilaak"),
		new SpellCheckerExample("pigiaqtitat", "pigiaqtitait"),
		new SpellCheckerExample("uvalu", "uvvalu"),
		new SpellCheckerExample("maligatigut", "maligaqtigut"),
		new SpellCheckerExample("akitujutinut", "akitujuutinut"),
		new SpellCheckerExample("arragumi", "arraagumi"),
		new SpellCheckerExample("nniaqamangittulirinirmut", "aanniaqamangittulirinirmut"),
		new SpellCheckerExample("nigiani", "niggiani"),
		new SpellCheckerExample("tamakkuninnga", "tamakkuninga"),
		new SpellCheckerExample("iksivautap", "iksivautaup"),
		new SpellCheckerExample("sulikkanniiq", "sulikkanniq"),
		new SpellCheckerExample("nunavumut", "nunavummut"),
		new SpellCheckerExample("katimajit", "katimajiit"),
		new SpellCheckerExample("tamatumunnga", "tamatumunga"),
		new SpellCheckerExample("nniaqamangittulirijiit", "aanniaqamangittulirijiit"),
		new SpellCheckerExample("ugaalautaa", "uqaalautaa"),
		new SpellCheckerExample("tavani", "tavvani"),
		new SpellCheckerExample("iksivauitaaq", "iksivautaaq", "iksivautaak", "issivautaaq", "issivautaak", "itsivautaaq", "itsivautaak"),
		new SpellCheckerExample("tamaini", "tamainni"),
		new SpellCheckerExample("nniaqtulirijikkunnut", "aanniaqtulirijikkunnut"),
		new SpellCheckerExample("immaqaqai", "immaqaaqai"),
		new SpellCheckerExample("taimak", "taimaak")
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
		
		
		int N = 5;
		double expPercentFoundInTopN = 0.87;
		double tolerance = 0.01;
		assertPercentFoundInTopN(expPercentFoundInTopN, N, 
				histogram, tolerance);
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
