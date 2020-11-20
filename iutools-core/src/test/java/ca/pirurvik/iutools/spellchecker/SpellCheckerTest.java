package ca.pirurvik.iutools.spellchecker;


import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ca.inuktitutcomputing.config.IUConfig;
import ca.nrc.datastructure.Pair;
import ca.nrc.testing.*;
import ca.pirurvik.iutools.corpus.CompiledCorpus;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;

import ca.pirurvik.iutools.corpus.RW_CompiledCorpus;
import ca.pirurvik.iutools.corpus.WordInfo;
import ca.pirurvik.iutools.text.segmentation.IUTokenizer;
import org.junit.*;

import static ca.nrc.testing.AssertIterator.assertContainsAll;

public class SpellCheckerTest {

	private static enum TestOption {ENABLE_PARTIAL_CORRECTIONS};

	private static final String emptyCorpusName = "empty-corpus";

	protected SpellChecker checkerSyll = null;
	
	// Note: These are not "real" correct words in Inuktut.
	//  Just pretending that they are by putting them in the
	//  spell checker's explicitly correct dictionary
	//
	protected static final String[] explicitlyCorrectWordsLatin = new String[] {
		"inuktut", "inukttut", "inuk", "inukutt", "inukshuk", 
		"nunavut", "inuktitut"
	};

	protected static final String[] correctWordsNonExplicit = new String[] {
		"maligaliuqti", "juu", "niruaqtaukkannilauqpuq", "niruavigjuarnautillugu",
		"utupiri", "tallimanganni", "maligalirvingmit", "nunavummi"
	};

	String[] misspelledWords =
		new String[] {
			"nakuqmi", "nunavungmi", "nunavuumik", "nunavuumit",
			"ugaalautaa"};


	@Before
	public void setUp() throws Exception {
		// Make sure the ES indices are empty for the empty corpus name
		clearESIndices(new SpellChecker(emptyCorpusName, false));
		return;
	}

	protected SpellChecker largeDictChecker() throws Exception {
		SpellChecker checker = new SpellChecker(CompiledCorpusRegistry.defaultCorpusName);
		return checker;
	}

	protected SpellChecker smallDictChecker() throws Exception {
		SpellChecker checker = new SpellChecker(emptyCorpusName, false);
		return checker;
	}

	private void clearESIndices(SpellChecker checker) throws Exception {
		if (!checker.corpusIndexName().equals(emptyCorpusName)) {
			throw new Exception(
					"You are only allowed to clear the ES index that corresponds to a corpus that is meant to be initially empty!!");
		}

		CompiledCorpus corpus = checker.corpus;
		corpus.deleteAll(true);

		corpus = checker.explicitlyCorrectWords;
		corpus.deleteAll(true);

		Thread.sleep(100);

		return;
	}

	protected SpellChecker makeCheckerEmptyDict() throws Exception {
		SpellChecker checker =
			new SpellChecker(
				emptyESCorpus().getIndexName());
		checker.setVerbose(false);
		return checker;
	}

	protected CompiledCorpus largeESCorpus() throws Exception {
		CompiledCorpus corpus =
			RW_CompiledCorpus.read(largeESCorpusFile());
		return corpus;
	}

	protected File largeESCorpusFile() throws Exception {
		File corpusFile = new File(IUConfig.getIUDataPath("data/compiled-corpuses/HANSARD-1999-2002.ES.json"));
		return corpusFile;
	}

	protected CompiledCorpus emptyESCorpus() throws Exception {
		CompiledCorpus corpus = new CompiledCorpus("empty-corpus");
		corpus.deleteAll(true);
		return corpus;
	}

	protected SpellChecker largeDictCheckerWithTestWords() throws Exception {
		SpellChecker checker = largeDictChecker();
		checker.setVerbose(false);
		addCorrectWordsLatin(checker);
		return checker;
	}

	protected SpellChecker smallDictCheckerWithTestWords() throws Exception {
		SpellChecker checker = smallDictChecker();
		checker.setVerbose(false);
		addCorrectWordsLatin(checker);
		return checker;
	}

	private void addCorrectWordsLatin(SpellChecker checker) throws Exception {
		for (String aWord: explicitlyCorrectWordsLatin) {
			checker.addExplicitlyCorrectWord(aWord);
		}

		if (checker instanceof SpellChecker) {
			// Sleep a bit to allow the ElasticSearch index to synchronize
			Thread.sleep(100);
		}
	}

	@Test(expected=SpellCheckerException.class)
	public void test__SpellChecker__Synopsis() throws Exception {
		//
		// Before you can use a spell checker, you must first build its
		// dictionary of known words. This can be done in 2 ways:
		//
		// - directly by adding correct words to the dictionary;
		// - by specifying a corpus, the words of which will be added to the 
		//   dictionary (Note: in this case, we assume that words are correctly
		//   spelled if and only if they decompose)
		//
		
		SpellChecker checker = largeDictCheckerWithTestWords();
		checker.setVerbose(false);
		
		// 
		// For example
		//
		checker.addExplicitlyCorrectWord("inuktut");
		checker.addExplicitlyCorrectWord("inuk");
		checker.addExplicitlyCorrectWord("inuksuk");
		checker.addExplicitlyCorrectWord("nunavut");
		checker.addExplicitlyCorrectWord("1988-mut");
		// etc...
		
		// OR
		//
		// Note: In this case, we don't assume that all words contained 
		//   in the corpus are correctly spelled. Instead, we use the 
		//   SpellChecker to determine if they are or not.
		//
		checker.setDictionaryFromCorpus("a_corpus_name");

		//
		// You can then use the checker to see if a word is mis-spelled, and if so, 
		// get a list of plausible corrections for that word
		//
		String wordWithError = "inusuk";
		int nCorrections = 5;
		SpellingCorrection correction = checker.correctWord(wordWithError, nCorrections);
		if (correction.wasMispelled) {
			List<String> possibleCorrectSpellings = correction.getPossibleSpellings();
			// This is the longest leading string of the word which might 
			// be correctly spelled
			String correctLead = correction.getCorrectLead();
			// This is the longest tailing string of the word which might 
			// be correctly spelled
			String correctTail = correction.getCorrectTail();
		}
		
		//
		// You can also get corrections for all words in a text
		//
		String text = "inuit inusuk nunnavut";
		List<SpellingCorrection> corrections2 = checker.correctText(text, nCorrections);
	}
	
		
	/**********************************
	 * VERIFICATION TESTS
	 **********************************/
	
	@Test
	public void test__addCorrectWord__HappyPath() throws Exception {
		SpellChecker checker = makeCheckerEmptyDict();
		Thread.sleep(1000);
		checker.setVerbose(false);
		
		String word = "inukkkutttt";
		Assert.assertTrue(
	"Initially, word "+word+" should have been deemed mis-spelled",
			checker.isMispelled(word));
		
		checker.addExplicitlyCorrectWord(word);
		Thread.sleep(1000);
		Assert.assertFalse(
	"After being explicitly labelled as correct, word "+word+
			" should NOT have been deemed mis-spelled",
			checker.isMispelled(word));
	}
	
	@Test 
	public void test__wordsContainingSequ() throws Exception {
		String seq = "nuk";
		
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		Iterator<String> wordsWithSeq = checker.wordsContainingNgram(seq);
		String[] expected = new String[] {"inukshuk","inuk","inuktut"};
		assertContainsAll(
			"The list of words containing sequence "+seq+" was not as expected",
			expected, wordsWithSeq);
	}
	
	@Test 
	public void test__wordsContainingNgram__Case_considering_extremities() throws Exception {
		SpellChecker checker = largeDictCheckerWithTestWords();
		checker.setVerbose(false);
		checker.addExplicitlyCorrectWord("inuktitut");
		checker.addExplicitlyCorrectWord("inuksuk");
		checker.addExplicitlyCorrectWord("inuttitut");
		checker.addExplicitlyCorrectWord("inakkut");
		checker.addExplicitlyCorrectWord("takuinuit");
		checker.addExplicitlyCorrectWord("taku");
		checker.addExplicitlyCorrectWord("intakuinuit");
		
		String seq;
		String[] expected;
		String[] unexpected;
		Iterator<String> wordsWithSeq;
		
		seq = "inukt";
		wordsWithSeq = checker.wordsContainingNgram(seq);
		expected = new String[] {"inuktitut","inuktaluk","inuktigut"};
		assertContainsAll(
		"The list of words containing sequence "+seq+" was not as expected",
			expected, wordsWithSeq);

		seq = "^inukt";
		wordsWithSeq = checker.wordsContainingNgram(seq);
		expected = new String[] {"inuktitut","inuktaluk","inuktigut"};
		assertContainsAll(
			"The list of words containing sequence "+seq+" was not as expected",
			expected, wordsWithSeq);
		// This word contains inukt, but not at the start of the word
		unexpected = new String[] {"qaujijumatuinnaqtungainuktituunganingit"};
		AssertIterator.assertContainsNoneOf(
				"The list of words containing sequence "+seq+" was not as expected",
				unexpected, wordsWithSeq);

		seq = "itut$";
		wordsWithSeq = checker.wordsContainingNgram(seq);
		expected = new String[] {"inuktitut","inuttitut"};
		assertContainsAll(
			"The list of words containing sequence "+seq+" was not as expected",
			expected, wordsWithSeq);
		// This word contains itut, but not at the end
		unexpected = new String[] {"jiiqatigiituta"};
		AssertIterator.assertContainsNoneOf(
				"The list of words containing sequence "+seq+" was not as expected",
				unexpected, wordsWithSeq);

		seq = "^taku$";
		wordsWithSeq = checker.wordsContainingNgram(seq);
		expected = new String[] {"taku"};
		assertContainsAll(
			"The list of words containing sequence "+seq+" was not as expected",
			expected, wordsWithSeq);

		// This word contains taku, but it is not bounded by start and end of word
		unexpected = new String[] {"aktakuniglu"};
		AssertIterator.assertContainsNoneOf(
			"The list of words containing sequence "+seq+" was not as expected",
			unexpected, wordsWithSeq);
	}
	
	
	@Test 
	public void test__firstPassCandidates_TFIDF() throws Exception {
		SpellChecker checker = smallDictCheckerWithTestWords();
		
		String badWord = "inukkshuk";
		List<ScoredSpelling> candidates =
			checker.candidatesWithSimilarNgrams(badWord, false);
	
		String[] expected = new String[] {
			"inuk", "inukshuk", "inuktitut", "inukttut", "inuktut",
			"inukutt"};
		AssertSpellingCorrection
			.candidatesEqual(expected, candidates);
	}

	@Test
	public void test__correctWord__CorrectLeadAndTailOverlap() throws Exception {
		SpellChecker checker = largeDictCheckerWithTestWords();

		checker.enablePartialCorrections();
		
		// The correct lead ('ujaranni') and tail ('nniarvimmi') overlap by 
		// several characters ('nni'). In this case, we can't show the badly 
		// spelled middle part, because there isn't one. Yet, we know that 
		// the word is mis-spelled, so something must be wrong in that middle
		// part
		//
		String word = "ujaranniarvimmi";

		String[] expSuggestions = new String[]{
			"ujara[nni]arvimmi",
			"ujararniarvimmi",
			"ujararniarvimmik",
			"ujararniarvimmit",
			"ujarattarniarvimmi",
			"ujararniarvimmut"
		};

		if (!(checker instanceof SpellChecker)) {
			// For some reason, the list of suggestions is slightly different
			// for ES vs InMemory
			//
			expSuggestions = new String[]{
				"ujara[nni]arvimmi",
				"ujararniarvimmi",
				"ujararniarvimmik",
				"ujararniarvimmit",
				"ujararniarvingmi",
				"ujarattarniarvimmi"			};
			}

		SpellingCorrection gotCorrection = checker.correctWord(word, 5);

		AssertSpellingCorrection.assertThat(gotCorrection,
				  "Correction for word 'inukshuk' was wrong")
			.wasMisspelled()
			.providesSuggestions(expSuggestions)
			;
	}

	@Test 
	public void test__correctWord__roman__MispelledInput() throws Exception {
		SpellChecker checker = smallDictCheckerWithTestWords();
		checker.enablePartialCorrections();
		String word = "inuktigtut";
		SpellingCorrection gotCorrection = checker.correctWord(word, 5);
		
		AssertSpellingCorrection.assertThat(gotCorrection,
				  "Correction for word 'inukshuk' was wrong")
			.wasMisspelled()
			.providesSuggestions(
				new String[] {
				  "inukti[g]tut",
				  "inuktitut",
				  "inuktut",
				  "inukttut",
				  "inukutt",
				  "inuk"})
			;
	}

	@Test 
	public void test__correctWord__roman__CorrectlySpellendInput() throws Exception {
		SpellChecker checker = largeDictCheckerWithTestWords();

		String word = "inuksuk";
		SpellingCorrection gotCorrection = checker.correctWord(word, 5);
		assertCorrectionOK(gotCorrection, word, true);
	}

	@Test 
	public void test__correctWord__syllabic__MispelledInput() throws Exception {
		String[] correctWordsLatin = new String[] {"inuktut", "nunavummi", "inuk", "inuksut", "nunavuumi", "nunavut"};
		SpellChecker checker = smallDictCheckerWithTestWords();
		checker.setVerbose(false);
		for (String aWord: correctWordsLatin) checker.addExplicitlyCorrectWord(aWord);
		String word = "ᓄᓇᕗᖕᒥ";
		SpellingCorrection gotCorrection = checker.correctWord(word, 5);
		assertCorrectionOK(gotCorrection, word, false,
			new String[] {"ᓄᓇᕗᒻᒥ", "ᓄᓇᕘᒥ", "ᓄᓇᕗᑦ" });
	}
	
	@Test 
	public void test__correctWord__number__ShouldBeDeemedCorrectlySpelled() throws Exception {
		SpellChecker checker = largeDictCheckerWithTestWords();
		checker.setVerbose(false);
		String word = "2019";
		SpellingCorrection gotCorrection = checker.correctWord(word, 5);
		assertCorrectionOK(gotCorrection, word, true, new String[] {});
	}

	@Test 
	public void test__correctWord__numeric_term_mispelled() throws Exception {
		String[] correctWordsLatin = new String[] {
			"inuktut", "nunavummi", "inuk", "inuksut", "nunavuumi", "nunavut",
			"1988-mut", "$100-mik", "100-nginnik", "100-ngujumik"
		};
		SpellChecker checker = smallDictCheckerWithTestWords();
		checker.setVerbose(false);
		for (String aWord: correctWordsLatin) checker.addExplicitlyCorrectWord(aWord);
		String word = "1987-muti";
		SpellingCorrection gotCorrection = checker.correctWord(word, 5);
		
		// TODO-June2020: This test used to only return "1987-mut"
		//   but with Alain's recent refactorings, it now returns 
		//   a list of 5 possibilities. Waiting for Benoit to 
		//   confirm whether or not those new results make sense
		//
//		String[] expCorrection = new String[] { "1987-mut" };
		String[] expCorrection = new String[] {
			"1987-mut", "1987-muarluti", "1987-muttauq", "1987-kulummut",
			"1987-tuinnaulluti"};
		if (checker instanceof SpellChecker) {
			// 2020-10-01-AD:
			// For some reason, the ES spell checker only produces
			// the one suggestion (which happens to be the correct one).
			// Don't have time or patience to figure out why.
			expCorrection = new String[] { "1987-mut" };
		}
		assertCorrectionOK(
			gotCorrection, word, false, expCorrection);
	}
	

	@Test 
	public void test__correctWord__ninavut() throws Exception {
		SpellChecker checker = smallDictCheckerWithTestWords();
		
		String word = "ninavut";
		checker.setVerbose(false);
		SpellingCorrection gotCorrection = checker.correctWord(word, 5);
		assertCorrectionOK(gotCorrection, word, false, 
				new String[] { "nunavut"});
	}
	
	@Test  
	public void test__correctText__roman() throws Exception  {
		String text = "inuktut ninavut inuit inuktut";
		
//		SpellChecker checker = makeCheckerLargeDict();
		SpellChecker checker = smallDictCheckerWithTestWords();
		
		List<SpellingCorrection> gotCorrections = checker.correctText(text);
		
		// Note we skip every other "correction" because they are just blank spaces
		// between words
		
		int wordNum = 0;
		int ii = 2*wordNum;
		SpellingCorrection wordCorr = gotCorrections.get(ii);
		assertCorrectionOK("Correction for word #"+wordNum+"="+wordCorr.orig+" was not as expected", wordCorr,  "inuktut", true);
		

		wordNum = 1;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		Assert.assertTrue("Word #"+wordNum+"="+wordCorr.orig+" should have deemed MISPELLED", wordCorr.wasMispelled);
		AssertObject.assertDeepEquals("Corrections for word#"+wordNum+"="+wordCorr.orig+" were not as expected", 
				new String[] {"nunavut"}, 
				wordCorr.getPossibleSpellings());

		wordNum = 2;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		Assert.assertFalse("Word #"+ii+"="+wordCorr.orig+" should have deemd correctly spelled", wordCorr.wasMispelled);

		wordNum = 3;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		Assert.assertFalse("Word #"+ii+"="+wordCorr.orig+" should have deemd correctly spelled", wordCorr.wasMispelled);
	}	
	


	@Test  
	public void test__correctText__syllabic() throws Exception  {
		SpellChecker checker = smallDictCheckerWithTestWords();
		
		String text = "ᐃᓄᑦᒧᑦ ᑕᑯᔪᖅ ᐃᒡᓗᑦᒥᒃ ᐊᕐᕌᒍᒥ";
		List<SpellingCorrection> gotCorrections = checker.correctText(text);
		
		
		int wordNum = 0;
		int ii = 2*wordNum;
		SpellingCorrection wordCorr = gotCorrections.get(ii);
		assertCorrectionOK("Correction for word #"+wordNum+"="+wordCorr.orig+" was not as expected",
				wordCorr, "ᐃᓄᑦᒧᑦ", false, 
				// Note: The correct spelling is not in this list of corrections, but that's OK (kinda). 
				//   This test mostly aims at testing the mechanics of SpellText.
				//  				
				new String[] {
					"ᐃᓄᒃᑐᑦ",
				  	"ᐃᓄᑯᑦᑦ",
				  	"ᐃᓄᒃᑎᑐᑦ",
				  	"ᐃᓄᒃ",
					"ᐃᓄᒃᑦᑐᑦ"
				});
		
		wordNum = 1;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		assertCorrectionOK("Correction for word #"+wordNum+"="+wordCorr.orig+" was not as expected",
				wordCorr, "ᑕᑯᔪᖅ", true);

		wordNum = 2;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		assertCorrectionOK("Correction for word #"+wordNum+"="+wordCorr.orig+" was not as expected",
				wordCorr, "ᐃᒡᓗᑦᒥᒃ", false, 
				// Note: The correct spelling is not in this list of corrections, but that's OK (kinda). 
				//   This test mostly aims at testing the mechanics of SpellText.
				//  				
				new String[] {});

		wordNum = 3;
		ii = 2*wordNum;
		wordCorr = gotCorrections.get(ii);
		assertCorrectionOK("Correction for word #"+wordNum+"="+wordCorr.orig+" was not as expected",
				wordCorr, "ᐊᕐᕌᒍᒥ", true);
	}	


	@Test 
	public void test__correctText_ampersand() throws Exception {
		SpellChecker checker = largeDictCheckerWithTestWords();
		String text = "inuktut sinik&uni";
		List<SpellingCorrection> gotCorrections = checker.correctText(text);
		Assert.assertEquals("The number of corrections is not as expected.",3,gotCorrections.size());
	}
	
	@Test 
	public void test__isMispelled__CorreclySpelledWordFromCompiledCorpus() throws Exception  {
		String word = "inuktitut";
		Assert.assertFalse("Word "+word+" should have been deemed correctly spelled", 
				largeDictCheckerWithTestWords().isMispelled(word));
	}

	@Test 
	public void test__isMispelled__CorreclySpelledWordNOTFromCompiledCorpus() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		String word = "inuktut";
		Assert.assertFalse("Word "+word+" should have been deemed correctly spelled", 
				checker.isMispelled(word));
	}

	@Test 
	public void test__isMispelled__CorreclySpelledWordNumber() throws Exception  {
		String word = "2018";
		Assert.assertFalse("Word "+word+" should have been deemed correctly spelled", 
			largeDictCheckerWithTestWords().isMispelled(word));
	}

	@Test 
	public void test__isMispelled__MispelledWordFromCompiledCorpus() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		String word = "inukkkkutt";
		Assert.assertTrue("Word "+word+" should have been deemed mis-spelled", 
				checker.isMispelled(word));
	}

	@Test 
	public void test__isMispelled__MispelledWordNOTFromCompiledCorpus() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		String word = "inuktuttt";
		Assert.assertTrue("Word "+word+" should have been deemed mis-spelled", 
				checker.isMispelled(word));
	}
	
	@Test 
	public void test__isMispelled__WordIsSingleInuktitutCharacter() throws Exception  {
//		SpellChecker checker = makeCheckerLargeDict();
		SpellChecker checker = smallDictCheckerWithTestWords();
		
		String word = "ti";
		Assert.assertFalse("Word "+word+" should have been deemed correctly spelled", 
				checker.isMispelled(word));
		word = "t";
		Assert.assertFalse("Word "+word+" should have been deemed correctly spelled", checker.isMispelled(word));
		word = "o";
		Assert.assertTrue("Word "+word+" should have been deemed mis-spelled", checker.isMispelled(word));
	}
	
	@Test 
	public void test__isMispelled__WordIsNumericTermWithValidEnding() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		String word = "1988-mut";
		Assert.assertFalse("Word "+word+" should have been deemed correctly spelled", checker.isMispelled(word));
		word = "1988-muti";
		Assert.assertTrue("Word "+word+" should have been deemed correctly spelled", checker.isMispelled(word));
	}
	
	@Test 
	public void test__WordContainsMoreThanTwoConsecutiveConsonants() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		String word = "imglu";
		Assert.assertTrue("Word "+word+" should have been acknowledged as having more than 2 consecutive consonants", 
				checker.wordContainsMoreThanTwoConsecutiveConsonants(word));
		word = "inglu";
		Assert.assertFalse("Word "+word+" should have been acknowledged as not having more than 2 consecutive consonants", 
				checker.wordContainsMoreThanTwoConsecutiveConsonants(word));
		word = "innglu";
		Assert.assertTrue("Word "+word+" should have been acknowledged as having more than 2 consecutive consonants", 
				checker.wordContainsMoreThanTwoConsecutiveConsonants(word));
	}
	
	@Test 
	public void test__wordIsNumberWithSuffix() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		String word = "34-mi";
		String[] numericTermParts = checker.splitNumericExpression(word);
		Assert.assertTrue("Word "+word+" should have been acknowledged as a number-based word", numericTermParts != null);
		Assert.assertEquals("The 'number' part is not as expected.", "34-", numericTermParts[0]);
		Assert.assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "$34,000-mi";
		numericTermParts = checker.splitNumericExpression(word);
		Assert.assertTrue("Word "+word+" should have been acknowledged as a number-based word", numericTermParts != null);
		Assert.assertEquals("The 'number' part is not as expected.", "$34,000-", numericTermParts[0]);
		Assert.assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "4:30-mi";
		numericTermParts = checker.splitNumericExpression(word);
		Assert.assertTrue("Word "+word+" should have been acknowledged as a number-based word", numericTermParts != null);
		Assert.assertEquals("The 'number' part is not as expected.", "4:30-", numericTermParts[0]);
		Assert.assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "5.5-mi";
		numericTermParts = checker.splitNumericExpression(word);
		Assert.assertTrue("Word "+word+" should have been acknowledged as a number-based word", numericTermParts != null);
		Assert.assertEquals("The 'number' part is not as expected.", "5.5-", numericTermParts[0]);
		Assert.assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "5,500.33-mi";
		numericTermParts = checker.splitNumericExpression(word);
		Assert.assertTrue("Word "+word+" should have been acknowledged as a number-based word", numericTermParts != null);
		Assert.assertEquals("The 'number' part is not as expected.", "5,500.33-", numericTermParts[0]);
		Assert.assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "bla";
		numericTermParts = checker.splitNumericExpression(word);
		Assert.assertTrue("Word "+word+" should have been acknowledged as a number-based word", numericTermParts == null);
		word = "34–mi";
		numericTermParts = checker.splitNumericExpression(word);
		Assert.assertTrue("Word "+word+" should have been acknowledged as a number-based word", numericTermParts != null);
		Assert.assertEquals("The 'number' part is not as expected.", "34–", numericTermParts[0]);
		Assert.assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		word = "40−mi";
		numericTermParts = checker.splitNumericExpression(word);
		Assert.assertTrue("Word "+word+" should have been acknowledged as a number-based word", numericTermParts != null);
		Assert.assertEquals("The 'number' part is not as expected.", "40−", numericTermParts[0]);
		Assert.assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts[1]);
		}
	
	@Test 
	public void test__assessEndingWithIMA() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		String ending = "mi";
		boolean goodEnding = checker.assessEndingWithIMA(ending);
		Assert.assertTrue("Ending "+ending+" should have been acknowledged as valid word ending", goodEnding);
		ending = "mitiguti";
		goodEnding = checker.assessEndingWithIMA(ending);
		Assert.assertFalse("Ending "+ending+" should have been rejected as valid word ending", goodEnding);
		ending = "gumut";
		goodEnding = checker.assessEndingWithIMA(ending);
		Assert.assertFalse("Ending "+ending+" should have been rejected as valid word ending", goodEnding);
	}
	
	@Test 
	public void test__wordIsPunctuation() throws Exception  {
		SpellChecker checker = largeDictCheckerWithTestWords();
		
		String word = "-";
		Assert.assertTrue("Word "+word+" should have been acknowledged as punctuation", checker.wordIsPunctuation(word));
		word = "–";
		Assert.assertTrue("Word "+word+" should have been acknowledged as punctuation", checker.wordIsPunctuation(word));
	}
	
	@Test 
	public void test__addWord__HappyPath() throws Exception {
		SpellChecker checker = largeDictCheckerWithTestWords();
		String word = "tamainni";
		assertWordUnknown(word, checker);
		
		checker.addExplicitlyCorrectWord(word);
		assertWordIsKnown(word, checker);
	}	
	
	@Test
	public void test__SpeedTest__AllOKWords() throws Exception {
		// Spell checking should be pretty fast for words that are
		// correctly spelled
		//
		double expAvgSecs = 0.2;
		speedTest(
		"All correctly spelled words",
			correctWordsNonExplicit, expAvgSecs);
	}


	@Test
	public void test__SpeedTest__AllMisspelledWords__DisablePartialCorrections() throws Exception {
		// Spell checking is somewhat slow when dealing with words that are
		// mis-spelled. This is even true if we do not enable computation of
		// partial corrections.
		//
		double expAvgSecs = 1.7;
		speedTest(
			"All misspelled words",
			misspelledWords, expAvgSecs);
	}

	@Test
	public void test__SpeedTest__AllMisspelledWords__EnablePartialCorrections() throws Exception {
		double expAvgSecs = 2.0;
		speedTest(
			"All misspelled words",
			misspelledWords, expAvgSecs, TestOption.ENABLE_PARTIAL_CORRECTIONS);
	}

	private void speedTest(String dataSetName, String[] words,
		double expMaxAvgSecs, TestOption... options) throws Exception {
		SpellChecker checker = largeDictCheckerWithTestWords();

		double totalSecs = 0;
		int totalWords = words.length;
		if (totalWords == 0) {
			throw new SpellCheckerException("Empty data set");
		}
		boolean enablePartialCorrections =
			Arrays.stream(options)
				.anyMatch(TestOption.ENABLE_PARTIAL_CORRECTIONS::equals);
			checker.setPartialCorrectionEnabled(enablePartialCorrections);
			for (String word: words) {
				Long start = System.currentTimeMillis();
				SpellingCorrection gotCorrection = checker.correctWord(word);
				long end = System.currentTimeMillis();
				double elapsedSecsThisWord =
					1.0 * (end - start) 	/ 1000;
				totalSecs += elapsedSecsThisWord;
				totalWords++;
			}

			String baseMess =
				"With partial correction set to "+
				enablePartialCorrections+"...\n"+
				"On dataset: "+dataSetName+"...\n";

			Double gotAvgSecs= avgSecs(totalSecs, totalWords);
			AssertNumber.isLessOrEqualTo(
				baseMess +
				"SpellChecker performance was MUCH lower than expected.\n" +
				"Note: This test may fail on occasion depending on the speed " +
				"and current load of your machine.",
				gotAvgSecs, expMaxAvgSecs);
		}

	private Double avgSecs(double totalSecs, int totalWords) {
		double avg = 0.0;
		if (totalWords != 0) {
			avg = totalSecs / totalWords;
			BigDecimal bd = BigDecimal.valueOf(avg);
			bd = bd.setScale(4, RoundingMode.HALF_UP);
			avg = bd.doubleValue();
		}
		return avg;
	}

	@Test 
	public void test__computeCorrectPortions__HappyPath() throws Exception {
		SpellChecker checker = smallDictCheckerWithTestWords();
		
		String badWord = "inuktiqtut";
		SpellingCorrection correction = 
				new SpellingCorrection(badWord, new String[0], true);
		checker.computeCorrectPortions(badWord, correction);
		AssertSpellingCorrection.assertThat(correction, "")
			.highlightsIncorrectTail("inukti")
			.highlightsIncorrectLead("tut")
			.highlightsIncorrectMiddle("inukti[q]tut")
		;
	}
	
	@Test 
	public void test__leadRespectsMorphemeBoundaries__HappyPath() throws Exception {
		String word = "inuktitut";
		SpellChecker checker = smallDictCheckerWithTestWords();
		
		String leadChars = "inuk";
		Assert.assertTrue(
				"Lead morphemes for word "+word+" SHOULD have matched "+leadChars, 
				checker.leadRespectsMorphemeBoundaries(leadChars, word));
		
		leadChars = "inukt";
		Assert.assertFalse(
				"Lead morphemes for word "+word+" should NOT have matched "+leadChars, 
				checker.leadRespectsMorphemeBoundaries(leadChars, word));
	}

	@Test 
	public void test__tailRespectsMorphemeBoundaries__HappyPath() throws Exception {
		String word = "inuktitut";
		SpellChecker checker = smallDictCheckerWithTestWords();
		
		String tailChars = "tut";
		Assert.assertTrue(
				"Tail morphemes for word "+word+" SHOULD have matched "+tailChars, 
				checker.tailRespectsMorphemeBoundaries(tailChars, word));
		
		tailChars = "itut";
		Assert.assertFalse(
				"Tail morphemes for word "+word+" should NOT have matched "+tailChars, 
				checker.tailRespectsMorphemeBoundaries(tailChars, word));
	}

	@Test
	public void test__winfosContainingNgram__HappyPath() throws Exception {
		SpellChecker checker = largeDictCheckerWithTestWords();
		String ngram = "^nuna";
		WordInfo[] expWinfos = new WordInfo[] {
			new WordInfo("nunavut")
		};
		Iterator<WordInfo> iter = checker.winfosContainingNgram(ngram);
		AssertIterator.assertContainsAll(
			"List of words containing ngram "+ngram+" did not contain expected words",
			expWinfos, iter);
	}

	/**********************************
	 * TEST HELPERS
	 **********************************/
	
	private boolean containsNumericTerm(String numericTerm, SpellChecker checker) {
		String[] numericTermParts = checker.splitNumericExpression(numericTerm);
		boolean answer = (numericTermParts != null);
		return answer;
	}

	private void assertCorrectionOK(String mess, SpellingCorrection wordCorr, String expOrig, boolean expOK) throws Exception {
		assertCorrectionOK(mess, wordCorr, expOrig, expOK, null);
		
	}
	
	private void assertCorrectionOK(SpellingCorrection wordCorr, String expOrig, 
			boolean expOK, String[] expSpellings) throws Exception {
		assertCorrectionOK("", wordCorr, expOrig, expOK, expSpellings, 
				null, null);
	}
	
	private void assertCorrectionOK(SpellingCorrection wordCorr, String expOrig, 
			boolean expOK) throws Exception {
		assertCorrectionOK("", wordCorr, expOrig, expOK, new String[] {});
	}
	
	private void assertCorrectionOK(String mess, SpellingCorrection wordCorr, 
			String expOrig, boolean expOK, String[] expSpellings) 
					throws Exception {
		assertCorrectionOK(mess, wordCorr, 
				expOrig, expOK, expSpellings, null, null);
	}
	
	private void assertCorrectionOK(String mess, SpellingCorrection wordCorr, 
			String expOrig, boolean expOK, String[] expTopSpellings,
			String expCorrLead, String expCorrTail) throws Exception {
		if (expTopSpellings == null) {
			expTopSpellings = new String[] {};
		}
		Assert.assertEquals(
			mess+"\nThe input word was not as expected",
			expOrig, wordCorr.orig);
		Assert.assertEquals(
			mess+"\nThe correctness status was not as expected",
			expOK, !wordCorr.wasMispelled);

		List<String> gotTopSpellings = wordCorr.getPossibleSpellings();
		gotTopSpellings =
			gotTopSpellings
				.subList(
					0,
					Math.min(gotTopSpellings.size(), expTopSpellings.length));
		AssertObject.assertDeepEquals(
				mess+"\nThe list of spellings was not as expected", 
				expTopSpellings, gotTopSpellings);
		
		if (expCorrLead != null) {
			Assert.assertEquals(
					"Longest Correctly Spelled leading string was not as expected", 
					expCorrLead, wordCorr.getCorrectLead());
		}

		if (expCorrTail != null) {
			Assert.assertEquals(
					"Longest Correctly tailing string was not as expected", 
					expCorrLead, wordCorr.getCorrectTail());
		}
	}

	private void assertWordIsKnown(String word, SpellChecker checker)
		throws Exception {
		Assert.assertTrue("Spell checker dictionary did not know about word '"+word+"'",
			checker.knowsWord(word));
	}

	private void assertWordUnknown(String word, SpellChecker checker)
		throws SpellCheckerException {
		Assert.assertFalse("Spell checker dictionary should NOT have known about word '"+word+"'", 
			checker.knowsWord(word));
	}

	protected List<String> wordsInText(String text) {
		IUTokenizer iutokenizer = new IUTokenizer();
		List<String> words = iutokenizer.tokenize(text);
		return words;
	}
}
