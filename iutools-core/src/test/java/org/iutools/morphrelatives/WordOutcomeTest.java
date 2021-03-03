package org.iutools.morphrelatives;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class WordOutcomeTest {

	WordOutcome outcome = null;
	String[] goldStandardRelatives = new String[] {
		"takujumaguvit", "takujumajuq", "takujumajut", "takugumajunga",
		"takugumavugut", "takujumagatta", "takujumavunga"
	};

	@BeforeEach
	public void setUp() {
		String word = "takujumaguvit";
		String[] relativesProduced = new String[] {
			"takugumajunga",  "takugumavugut", "takujumagatta", "takujumavunga",
			// This last one should be considered incorrect
			"takujumajuit"
		};
		outcome = new WordOutcome(word, relativesProduced);
	}

	////////////////////////////////
	// DOCUMENTATION TESTS
	////////////////////////////////

	@Test
	public void test__WordOutcome__Synopsis() {
		// This class represented the outcome (expected or actual) of running the
		// MorphRelativesFinder on a word
		//
		String word = "takujumaguvit";
		String[] relativesProduced = new String[] {
			"takugumajunga",  "takugumavugut", "takujumagatta", "takujumavunga",
			"takujumajut"
		};
		WordOutcome outcome = new WordOutcome(word, relativesProduced);

		// You can compare a word outcome to what should have been produced
		// by a "perfect" algorigthm.
		//
		String[] goldStandardRelatives = new String[] {
			"takujumaguvit", "takujumajuq", "takujumajut", "takugumajunga",
			"takugumavugut", "takujumagatta", "takujumavunga"
		};
		List<String> correctRels = outcome.correctRelatives(goldStandardRelatives);
		List<String> incorrectRels = outcome.incorrectRelatives(goldStandardRelatives);

		// You can also get the precision and recall for that word
		double prec = outcome.precision(goldStandardRelatives);
		double recall = outcome.recall(goldStandardRelatives);

		// You can also get a text summary of the outcome
		String outcomeSummary = outcome.fitnessToGoldStandard(goldStandardRelatives);
	}

	////////////////////////////////
	// VERIFICATION TESTS
	////////////////////////////////

	@Test
	public void test__WordOutcome__HappyPath() throws Exception {
		new AssertWordOutcome(outcome)
			.precisionIs(goldStandardRelatives, 0.8)
			.recallIs(goldStandardRelatives, 0.57)
			.relativesProducedAre(
				"takugumajunga",
				"takugumavugut",
				"takujumagatta",
				"takujumavunga",
				"takujumajuit"
			)
			.correctRelativesAre(
				goldStandardRelatives,
				"takugumajunga",
				"takugumavugut",
				"takujumagatta",
				"takujumavunga"
			)
			.incorrectRelativesAre(goldStandardRelatives,"takujumajuit")
			.prettyPrintIs(goldStandardRelatives,
				"  Word: takujumaguvit\n" +
				"  Precision: 0.8\n" +
				"  Recall: 0.5714285714285714\n" +
				"  Relatives produced (** = correct): \n" +
				"    takugumajunga**\n" +
				"    takugumavugut**\n" +
				"    takujumagatta**\n" +
				"    takujumavunga**\n" +
				"    takujumajuit\n" +
				"  Gold Standard relatives  (** = found):\n" +
				"    takujumaguvit\n" +
				"    takujumajuq\n" +
				"    takujumajut\n" +
				"    takugumajunga**\n" +
				"    takugumavugut**\n" +
				"    takujumagatta**\n" +
				"    takujumavunga**\n"
				)
			;
	}
}
