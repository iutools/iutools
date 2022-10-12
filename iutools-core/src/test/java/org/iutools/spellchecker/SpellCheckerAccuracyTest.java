package org.iutools.spellchecker;

import static org.junit.Assert.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import ca.nrc.testing.AssertRuntime;
import org.iutools.corpus.*;
import org.iutools.corpus.elasticsearch.CompiledCorpus_ES;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.corpus.CompiledCorpusTest;
import org.iutools.corpus.WordInfo;
import org.junit.jupiter.api.*;

import ca.nrc.datastructure.Pair;
import ca.nrc.string.StringUtils;
import ca.nrc.testing.AssertNumber;

public class SpellCheckerAccuracyTest {

    SpellChecker checkerLargeDict = null;

    private static DecimalFormat df2 = new DecimalFormat("#.##");

    private static final Pattern pattWordDescr =
        Pattern.compile("(^[^\\s\\(]+)");
    private TestInfo testInfo;


    protected SpellChecker makeLargeDictChecker() throws Exception {
        SpellChecker checker = new SpellChecker();
        return checker;
    }

    protected SpellChecker makeEmptyDictChecker() throws Exception {
        String indexName = CompiledCorpusTest.testIndex;
		 CorpusTestHelpers.deleteCorpusIndex(indexName);

		 CompiledCorpus corpus = new CompiledCorpusRegistry().makeCorpus(indexName);
        SpellChecker checker = new SpellChecker(indexName);

        return checker;
    }

    private static final SpellCheckerExample[]
        correctlySpelledWords = new SpellCheckerExample[] {

    };

    //
    // With a few exceptions, the examples below are a subset of the 200 most
    // frequent spelling mistakes in the Hansard.
    //
    // This subset excludes:
    // - Proper nouns
    // - Words borrowed from English (ex: minista = minister)
    //
    private static final SpellCheckerExample[]
            examples_MostFrequenMisspelledWords = new SpellCheckerExample[] {

            // NEED-IMPROVEMENT: Examples with ranking > 5,
            //   EVEN if we assume the correction is in the dict
            //
            new SpellCheckerExample("nakuqmi")
                    .isMisspelled("nakurmi").setMaxRank(1),

            new SpellCheckerExample("nunavungmi")
                    .isMisspelled("nunavummi").setMaxRank(1),

            new SpellCheckerExample("nunavuumik")
                    .isMisspelled("nunavummik").setMaxRank(1),

            new SpellCheckerExample("nunavuumit")
                    .isMisspelled("nunavummit").setMaxRank(3),

            new SpellCheckerExample("ugaalautaa")
                    .isMisspelled("uqaalautaa").setMaxRank(1),

            // NEED-IMPROVEMENT: Examples with ranking > 5
            //   ONLY if we don't assume the correction is in dict

            new SpellCheckerExample("qallunaatitut")
                    .isMisspelled("qallunaaqtitut").setMaxRank(1, -1),

            new SpellCheckerExample("tamaini")
                    .isMisspelled("tamainni").setMaxRank(1, -1),

            new SpellCheckerExample("nniaqamangittulirijiit")
                    .isMisspelled("aanniaqamangittulirijiit")
                    .setMaxRank(1, -1),

            new SpellCheckerExample("nniaqamangittulirinirmut")
                    .isMisspelled("aanniaqamangittulirinirmut")
                    .setMaxRank(1, -1),


            // OK: Examples with ranking <= 5

            new SpellCheckerExample("akitujutinut")
                    .isMisspelled("akitujuutinut").setMaxRank(1),

            new SpellCheckerExample("arragumi")
                    .isMisspelled("arraagumi").setMaxRank(1),

            new SpellCheckerExample("asuillaak")
                    .isMisspelled("asuilaak").setMaxRank(1),

            new SpellCheckerExample("iksivauitaaq")
                    .isMisspelled("iksivautaaq", "iksivautaak", "issivautaaq", "issivautaak", "itsivautaaq", "itsivautaak")
                    .setMaxRank(1),

            new SpellCheckerExample("iksivautap")
                    .isMisspelled("iksivautaup").setMaxRank(1),

            new SpellCheckerExample("immaqa")
                    .isMisspelled("immaqaa").setMaxRank(1),

            new SpellCheckerExample("katimajit")
                    .isMisspelled("katimajiit").setMaxRank(1),

            new SpellCheckerExample("katimmajjutiksaq")
                    .isMisspelled("katimajjutiksaq").setMaxRank(1),

            new SpellCheckerExample("kiinaujatigut")
                    .isMisspelled("kiinaujaqtigut").setMaxRank(1),

            new SpellCheckerExample("kiinaujat")
                    .isMisspelled("kiinaujait").setMaxRank(1),

            new SpellCheckerExample("maligaliqtit")
                    .isMisspelled("maligaliqtiit").setMaxRank(1),

            new SpellCheckerExample("maligatigut")
                    .isMisspelled("maligaqtigut").setMaxRank(1),

            new SpellCheckerExample("nigiani")
                    .isMisspelled("niggiani").setMaxRank(1),

            new SpellCheckerExample("nniaqtulirinirmut")
                    .isMisspelled("aanniaqtulirinirmut").setMaxRank(1),

            new SpellCheckerExample("nunavumi")
                    .isMisspelled("nunavummi").setMaxRank(1),

            new SpellCheckerExample("nunavumiut")
                    .isMisspelled("nunavummiut").setMaxRank(1),

            new SpellCheckerExample("nunavumut")
                    .isMisspelled("nunavummut").setMaxRank(1),

            new SpellCheckerExample("nunavutmi")
                    .isMisspelled("nunavummi").setMaxRank(1),

            new SpellCheckerExample("pigiaqtitat")
                    .isMisspelled("pigiaqtitait").setMaxRank(1),

            new SpellCheckerExample("sulikkanniiq")
                    .isMisspelled("sulikkanniq").setMaxRank(1),

            new SpellCheckerExample("takkua")
                    .isMisspelled("taakkua").setMaxRank(1),

            new SpellCheckerExample("tamakkuninnga")
                    .isMisspelled("tamakkuninga").setMaxRank(1),

            new SpellCheckerExample("tamatuminnga")
                    .isMisspelled("tamatuminga").setMaxRank(1),

            new SpellCheckerExample("tamatumunnga")
                    .isMisspelled("tamatumunga").setMaxRank(1),

            new SpellCheckerExample("tanna")
                    .isMisspelled("taanna").setMaxRank(1),

            new SpellCheckerExample("tavani")
                    .isMisspelled("tavvani").setMaxRank(1),

            new SpellCheckerExample("uvalu")
                    .isMisspelled("uvvalu").setMaxRank(1),

            new SpellCheckerExample("nniaqtulirijikkunnut")
                    .isMisspelled("aanniaqtulirijikkunnut").setMaxRank(1),

            new SpellCheckerExample("immaqaqai")
                    .isMisspelled("immaqaaqai").setMaxRank(1),

            new SpellCheckerExample("taimak")
                    .isMisspelled("taimaak").setMaxRank(1),
    };

    //
    // These examples were collected from a small set of handpicked
    // web pages in Inuktut.
    //
    // For each page, we collected all the words tagged as errors by the
    // Spell Checker and verified them to the best of our ability.
    //
    private static final SpellCheckerExample[]
            examples_RandomPageSample = new SpellCheckerExample[] {
            // NEEDS-IMPROVEMENT: False positives
            //   i.e. words that the spell checker tags as mis-spelled
            //   eventhough they are correctly spelled.
            //
            new SpellCheckerExample("juusi")
                    .notMisspelled(),

            new SpellCheckerExample("siqinnisiurnimut")
                    .notMisspelled(),

            new SpellCheckerExample("pigiannganirmut")
                    .notMisspelled(),

            new SpellCheckerExample("uppiritsiatatinnik")
                    .notMisspelled(),

            new SpellCheckerExample("nunalipaujakkut")
                    .notMisspelled(),

            new SpellCheckerExample("pijiraqtitsinasuaqpattuni")
                    .notMisspelled(),

            new SpellCheckerExample("katujjiqatigiigutaujumi")
                    .notMisspelled(),

            new SpellCheckerExample("katujjiqatigiigutaujuq")
                    .notMisspelled(),

            new SpellCheckerExample("amisurukkanniqullugit")
                    .notMisspelled(),

            new SpellCheckerExample("aup")
                    .notMisspelled(),

            new SpellCheckerExample("pirajangniuvuq")
                    .notMisspelled(),

            new SpellCheckerExample("pirajangnirmut")
                    .notMisspelled(),

            new SpellCheckerExample("pirajangniujuni")
                    .notMisspelled(),

            new SpellCheckerExample("pirajaktailimanirmut")
                    .notMisspelled(),

            new SpellCheckerExample("qanutigi")
                    .notMisspelled(),

            new SpellCheckerExample("nalunaikkutanganni")
                    .notMisspelled(),

            new SpellCheckerExample("pasijaujumik")
                    .notMisspelled(),

            new SpellCheckerExample("qaujinasuarningani")
                    .notMisspelled(),

            new SpellCheckerExample("saanngajunnaqpuq")
                    .notMisspelled(),

            new SpellCheckerExample("akiliititaunajarningani")
                    .isMisspelled(),

            new SpellCheckerExample("anullaksirnirmut")
                    .notMisspelled(),

            new SpellCheckerExample("aquttunnailliniujumik")
                    .notMisspelled(),


            // NEEDS-IMPROVEMENT: Examples for which we only know
            //   whether or not they are mis-spelled (need to
            //   figure out what the correct spellings are)

            new SpellCheckerExample("angijuqqaaqaqtutik")
                    .isMisspelled()
                    .setMaxRank(1),

            new SpellCheckerExample("kanatilimaamik")
                    .isMisspelled(),

            new SpellCheckerExample("suulur")
                    .isMisspelled(),

            new SpellCheckerExample("ujaranniarvimmi")
                    .isMisspelled(),


            // NEEDS-IMPROVEMENT: rank > 5 or null
            new SpellCheckerExample("piliriqatigiinik")
                    .isMisspelled("piliriqatigiinnik").setMaxRank(1),

            // OK: rank <= 5
            new SpellCheckerExample("aanniaqarnngittulirijikkut")
                    .isMisspelled("aanniaqanngittulirijikkut")
                    .setMaxRank(1),

            new SpellCheckerExample("angijuqqaaqaqtutik")
                    .setMaxRank(1)
                    .isMisspelled("angajuqqaaqaqtutik"),

            new SpellCheckerExample("maliklugu")
                    .setMaxRank(3)
                    .isMisspelled("maliglugu"),

            new SpellCheckerExample("pivagiijainiq")
                    .setMaxRank(1)
                    .isMisspelled("pivagiijarniq"),

            new SpellCheckerExample("qassigasangnut")
                    .setMaxRank(1)
                    .isMisspelled("qassigalangnut"),

            new SpellCheckerExample("qaujisarutinginniklu").setMaxRank(1)
                    .isMisspelled("qaujisarutinginniglu"),

            new SpellCheckerExample("qaritaujarmuaqtiqtaujuni")
                    .setMaxRank(1)
                    .isMisspelled("qaritaujamuaqtitaujuni"),

            new SpellCheckerExample("silataaniingaaqtulirinirmut")
                    .setMaxRank(1)
                    .isMisspelled("silataaninngaaqtulirinirmut"),

            new SpellCheckerExample("sivunnganit")
                    .setMaxRank(1)
                    .isMisspelled("sivuninganit", "sivurnganit"),

            new SpellCheckerExample("tukimuaktittiniaqtumik")
                    .setMaxRank(1)
                    .isMisspelled("tukimuaqtittiniaqtumik"),

            new SpellCheckerExample("tukimuaktiungmata")
                    .setMaxRank(1)
                    .isMisspelled("tukimuaqtiungmata"),

            new SpellCheckerExample("upalungaijanirmut")
                    .setMaxRank(1)
                    .isMisspelled("upalungaijarnirmut"),

            new SpellCheckerExample("uqaujjigiarutiniklu")
                    .setMaxRank(1)
                    .isMisspelled("uqaujjigiarutiniglu"),
    };

    static TestInfo prevTestInfo;

    @BeforeEach
    public void setUp(TestInfo testInfo) {
    	this.testInfo = testInfo;
    }

    @AfterEach
    public void tearDown() {

        prevTestInfo = this.testInfo;
    }

    @Test
    public void test__Evaluate__QuickEvaluation(TestInfo testInfo)
        throws Exception {

        // This test runs the evaluation on the first 10 examples
        // of the examples_MostFrequenMisspelledWords data set
        //
        EvaluationParameters parameters =
            new EvaluationParameters(testInfo)

//            .setFocusOnExample("tavani")

            .setVerbosity(1)
            .setExamples(examples_MostFrequenMisspelledWords)
            .setStopAfterNcases(10)
            .setLoadCorrectWordInDict(true)

            .setFPRate(0.0, 0.0)
            .setFNRate(0.0, 0.0)

//            .setPercentFoundInTopN(1.0)
            .setPercentFoundInTopN(1.0)
            .setTolerance(0.02)
                    
            .setPercTopSuggestionOK(0.91)
                    
            .setAverageRank(1.18)
            .setAvgRankTolerance(0.2)
            ;

        SpellChecker checker = makeLargeDictChecker();
        evaluateCheckerOnExamples(checker,  parameters);
    }

    @Test
    public void test__Evaluate__MostFrequentMisspelledWords__AssumingWordIsInDict(TestInfo testInfo)
            throws Exception {
        EvaluationParameters parameters =
            new EvaluationParameters(testInfo)

            // use setFocusOnExample() to run just one word out of the
            // data set.
//            .setFocusOnExample("pigiaqtitat")

            .setVerbosity(5)
            .setExamples(examples_MostFrequenMisspelledWords)
            .setLoadCorrectWordInDict(true)

            .setPercentFoundInTopN(1.0)
            .setTolerance(0.01)
            .setPercTopSuggestionOK(0.97)
            .setAverageRank(1.07)
            .setAvgRankTolerance(0.1)
        ;

        SpellChecker checker = makeLargeDictChecker();
        evaluateCheckerOnExamples(checker, parameters);
    }

    @Test
    public void test__Evaluate__MostFrequentMisspelledWords__WIHOUT_AssumingWordIsInDict(
        TestInfo testInfo) throws Exception {

        EvaluationParameters parameters =
            new EvaluationParameters(testInfo)
            // Use setFocusOnExample to run just one word from the data set
//            .setFocusOnExample("tavani")

            .setVerbosity(1)
            .setExamples(examples_MostFrequenMisspelledWords)
            .setLoadCorrectWordInDict(false)

            .setPercentFoundInTopN(1.0)
            .setTolerance(0.01)

            .setPercTopSuggestionOK(0.97)

            .setAverageRank(1.08)
            .setAvgRankTolerance(0.1)
            ;

        evaluateCheckerOnExamples(makeLargeDictChecker(), parameters);

    }

    @Test
    public void test__Evaluate__RandomPageSample__LargeDictionary(
        TestInfo testInfo) throws Exception {
        EvaluationParameters parameters =
            new EvaluationParameters(testInfo)
            .setFocusOnExample("piliriqatigiinik")

            .setVerbosity(2)
            .setExamples(examples_RandomPageSample)
            .setLoadCorrectWordInDict(true)

            .setFPRate(0.52, 0.01)
            .setFNRate(0.0, 0.0)

            .setPercentFoundInTopN(1.0 + 1.0)
            .setTolerance(0.01)
            .setPercTopSuggestionOK(0.90 + 1.0)
            .setAverageRank(-1.23)
            .setAvgRankTolerance(0.1)
            ;

    }

    private void evaluateCheckerOnExamples(SpellChecker spellChecker,
        EvaluationParameters parameters) throws Exception {


        SpellCheckerEvaluator evaluator =
            new SpellCheckerEvaluator(spellChecker);
        evaluator.setVerbose(parameters.verbosity);
        evaluator.run(
            parameters.examples, parameters.focusOnExample,
            parameters.loadCorrectWordInDict, parameters.stopAfterNcases);


        int numExamples = evaluator.totalExamples();
        Assertions.assertTrue(
            numExamples > 0,
            "No examples were evaluated!\nMaybe you set 'focusOnExample' to a word that is not in the list of examples?");

        int N = 5;
        assertEvaluationAsExpected(evaluator, N, parameters);

        if (parameters.focusOnExample != null) {
            Assertions.fail("The test was only carried out on word "+parameters.focusOnExample+".\n" +
                "Don't forget to set focusOnExample=null to run the test on all words");
        }
    }

    private void assertEvaluationAsExpected(
        SpellCheckerEvaluator evaluator, Integer N, EvaluationParameters parameters) throws SpellCheckerException {

        String errMess = "";

        errMess += checkFalsePositiveRate(evaluator, parameters.FPRate, parameters.toleranceFPRate);
        errMess += checkFalseNegativeRate(evaluator, parameters.FNRate, parameters.toleranceFNRate);
        errMess += checkPercentInTopN(evaluator, N, parameters.percentFoundInTopN, parameters.tolerance);;
        errMess += checkPercentWithTopSuggestionOK(evaluator, parameters);
        errMess += checkAverageRank(evaluator, parameters.averageRank, parameters.avgRankTolerance);
        errMess += checkExamplesWithWorseRank(evaluator);
        errMess += checkExamplesWithBetterRank(evaluator);
        errMess += checkAverageRuntime(evaluator, parameters);

        if (!errMess.matches("^\\s*$")) {
            fail(errMess);
        }
    }

    private String checkAverageRuntime(SpellCheckerEvaluator evaluator,
       EvaluationParameters parameters) throws SpellCheckerException {
        String errMess = "";
        try {
            AssertRuntime.runtimeHasNotChanged(
                evaluator.averageSecsPerCase(), parameters.runtimePercTolerance,
                "avg word spell checking time", parameters.testInfo);
        } catch (AssertionError e) {
            errMess = e.getMessage();
        } catch (IOException e) {
            throw new SpellCheckerException(e);
        }
        return "\n"+errMess+"\n";
    }


    private String checkFalsePositiveRate(SpellCheckerEvaluator evaluator,
                                          Double expFPRate, Double toleranceFPRate) {

        String errMess = "";
        if (expFPRate != null) {
            Double gotFPRate = evaluator.falsePositiveRate();
            try {
                AssertNumber.performanceHasNotChanged("False Positive Rate",
                        gotFPRate, expFPRate, toleranceFPRate,
                        false);
            } catch (AssertionError e) {
                errMess = e.getMessage();
            }
        }
        return "\n"+errMess+"\n";
    }

    private String checkFalseNegativeRate(SpellCheckerEvaluator evaluator,
                                          Double expFNRate, Double toleranceFNRate) {

        String errMess = "";
        if (expFNRate != null) {
            Double gotFNRate = evaluator.falseNegativeRate();
            try {
                AssertNumber.performanceHasNotChanged("False Negative Rate",
                        gotFNRate, expFNRate, toleranceFNRate,
                        false);
            } catch (AssertionError e) {
                errMess = e.getMessage();
            }
        }
        return errMess;
    }

    private String checkPercentInTopN(SpellCheckerEvaluator evaluator, int N,
        double expPercentFoundInTopN, double tolerance) {

        String errMess = "";

        List<Pair<Integer,Double>> histogram = evaluator.correctSpellingRankHistogramRelative();
        double gotPercentFoundInTopN = 0.0;
        for (int rankFound=0; rankFound < N; rankFound++) {
            if (histogram.size() > rankFound) {
                Pair<Integer,Double> histEntry = histogram.get(rankFound);
                gotPercentFoundInTopN += histEntry.getSecond();
            }
        }

        double delta = gotPercentFoundInTopN - expPercentFoundInTopN;
        if (Math.abs(delta) > tolerance) {
            if (delta < 0) {
                errMess =
                        "Significant DECREASE found for the percentage of words with an acceptable correction in the top "+N+
                                "\n  Got: "+gotPercentFoundInTopN+"\n  Exp: "+expPercentFoundInTopN+"\n  Delta: "+delta
                ;
            } else {
                errMess =
                        "Significant INCREASE found for the percentage of words with an acceptable correction in the top "+N+
                                "\n  Got: "+gotPercentFoundInTopN+"\n  Exp: "+expPercentFoundInTopN+"\n  Delta: "+delta+
                                "\n\nYou should probably change the expectations for that test so we don't loose that improvement in the future."
                ;
            }
        }

        if (!errMess.isEmpty()) {
            errMess += "\n\n---------------\n\n";
            errMess = "\n"+errMess;
        }

        return errMess;
    }

    private String checkPercentWithTopSuggestionOK(
        SpellCheckerEvaluator evaluator, EvaluationParameters parameters) {
        String errMess = "";

        double gotPercent = evaluator.percWithCorrectTopSuggestion();

        double delta =
            gotPercent - parameters.percTopSuggestionOK;
        if (Math.abs(delta) > parameters.tolerance) {
            if (delta < 0) {
                errMess =
                    "Significant DECREASE found for the percentage of words with a correct spelling in top first position.\n"+
                    "\n  Got: "+gotPercent+"\n  Exp: "+parameters.percTopSuggestionOK+"\n  Delta: "+delta
                ;
            } else {
                errMess =
                    "Significant INCREASE found for the percentage of words with a correct spelling in top first position.\n"+
                    "\n  Got: "+gotPercent+"\n  Exp: "+parameters.percTopSuggestionOK+"\n  Delta: "+delta+
                    "\n\nYou should probably change the expectations for that test so we don't loose that improvement in the future."
                ;
            }
        }

        if (!errMess.isEmpty()) {
            errMess += "\n\n---------------\n\n";
            errMess = "\n"+errMess;
        }

        return errMess;
    }


    private String checkAverageRank(SpellCheckerEvaluator evaluator, Double expAverageRank, Double avgRankTolerance) {
        String errMess = "";

        double expMin = expAverageRank - avgRankTolerance;
        double expMax = expAverageRank + avgRankTolerance;
        if (evaluator.averageRank() == null) {
            errMess = "Average rank was null!!!";
        } else {
            if (evaluator.averageRank() > expMax) {
                errMess =
                        "The average rank was higher than expected.\n"+
                                "  got: "+evaluator.averageRank()+
                                " (exp <= "+expMax+")";
            } else if (evaluator.averageRank() < expMin) {
                errMess =
                        "Significant improvement found in the average rank.\n"+
                                "You might want to decrease the expectation so we don't loose that gain in the future.\n"+
                                "  got: "+evaluator.averageRank()+
                                " (exp >= "+expMin+")";
            }
        }

        if (!errMess.isEmpty()) {
            errMess += "\n\n--------------------------\n\n";
        }

        return errMess;
    }

    private String checkExamplesWithWorseRank(SpellCheckerEvaluator evaluator) throws SpellCheckerException {
        String errMess = "";
        if (evaluator.examplesWithBadRank.size() > 0) {
            errMess =
                    "\nThere were examples for which the rank of the first correct suggestion was WORSE than the expected maximum.\n"+
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
                errMess +=
                    exampleWithBadRankDetails(
                        evaluator.checker.corpus, example,
                        topCandidates, rank);
            }
        }

        return errMess;
    }

    private String exampleWithBadRankDetails(
	 CompiledCorpus corpus, SpellCheckerExample example,
	 List<String> topCandidates, Integer gotRank) throws SpellCheckerException {
        String errMess =
            "  "+wordDetails(example.wordToCheck, corpus)+
            ": rank="+gotRank+
            " (exp <= "+example.maxRankAssumingInDict+")\n"+
            "  Correctly spelled forms:\n";

        Iterator<String> iter = example.acceptableCorrections.iterator();
        while (iter.hasNext()) {
            String word = iter.next();
            errMess += "     "+wordDetails(word, corpus)+"\n";
        }
        errMess += "  Top candidates were:\n";
        iter = topCandidates.iterator();
        while (iter.hasNext()) {
            String word = iter.next();
            errMess += "     "+wordDetails(word, corpus)+"\n";
        }

        errMess += "\n";

        return errMess;
    }

    private String wordDetails(String wordDescr, CompiledCorpus corpus) throws SpellCheckerException {

        String word = wordDescr;
        Matcher matcher = pattWordDescr.matcher(wordDescr);
        if (matcher.find()) {
            word = matcher.group(1);
        }
        String details = word;
        long freq = 0;
        String decomp = null;
        try {
            WordInfo winfo = corpus.info4word(word);
            if (winfo != null) {
                freq = winfo.frequency;;
                decomp = winfo.topDecompositionStr;
            }
        } catch (CompiledCorpusException e) {
            throw new SpellCheckerException(e);
        }

        details += "("+freq+"): "+decomp;

        return details;
    }

    private String checkExamplesWithBetterRank(SpellCheckerEvaluator evaluator) {
        String errMess = "";
        if (evaluator.examplesWithBetterRank.size() > 0) {
            errMess =
                    "\nThere were examples for which the rank of the first correct suggestion was BETTER than the expected maximum.\n"+
                            "List of such examples below.\n\n";
            for (SpellCheckerExample example: evaluator.examplesWithBetterRank.keySet()) {
                Pair<Integer,List<String>> problem =
                        evaluator.examplesWithBetterRank.get(example);
                String word = example.wordToCheck;
                Integer rank = problem.getFirst();
                List<String> topCandidates =
                        problem.getSecond()
                                .stream()
                                .limit(20)
                                .collect(Collectors.toList());
                errMess += "  "+word+": rank="+rank+
                        " (exp <= "+example.maxRankAssumingInDict+")\n"+
                        "  Correctly spelled forms: "+
                        StringUtils.join(example.acceptableCorrections.iterator(), ", ")+"\n"+
                        "  Top candidates were: "+
                        StringUtils.join(topCandidates.iterator(), ", ")+"\n\n"
                ;
            }
        }

        return errMess;
    }

    //////////////////////
    // TEST HELPERS
    //////////////////////

    public static class EvaluationParameters {

        public int verbosity = 1;

        public Integer stopAfterNcases  = null;
        public String focusOnExample = null;

        public double FPRate = 0.0;
        public Double toleranceFPRate = 0.0;
        public Double FNRate = 0.0;
        public Double toleranceFNRate = 0.0;

        public Double percentFoundInTopN = 1.0;
        public double tolerance = 0.0;
        public double percTopSuggestionOK = 1.0;
        public double averageRank = 1;
        public double avgRankTolerance = 0;

        // By default, we allow the rutime to change by at most 25%
        public Double runtimePercTolerance = 0.25;

        public boolean loadCorrectWordInDict = false;
        public SpellCheckerExample[] examples = null;
        private TestInfo testInfo = null;

        public EvaluationParameters(TestInfo _tInfo) {
            this.testInfo = _tInfo;
        }

        public EvaluationParameters setStopAfterNcases(Integer N) {
            this.stopAfterNcases = N;
            return this;
        }

        public EvaluationParameters setFocusOnExample(String focusOn) {
            this.focusOnExample = focusOn;
            return this;
        }

        public EvaluationParameters setVerbosity(int _verbosity) {
            this.verbosity = _verbosity;
            return this;
        }

        public EvaluationParameters setPercentFoundInTopN(double percent) {
            this.percentFoundInTopN = percent;
            return this;
        }

        public EvaluationParameters setTolerance(double _tolerance) {
            this.tolerance = _tolerance;
            return this;
        }

        public EvaluationParameters setPercTopSuggestionOK(double percent) {
            this.percTopSuggestionOK = percent;
            return this;
        }

        public EvaluationParameters setAverageRank(double rank) {
            this.averageRank = rank;
            return this;
        }

        public EvaluationParameters setAvgRankTolerance(double tolerance) {
            this.avgRankTolerance = tolerance;
            return this;
        }

        public EvaluationParameters setLoadCorrectWordInDict(boolean load) {
            this.loadCorrectWordInDict = load;
            return this;
        }

        public EvaluationParameters setExamples(SpellCheckerExample[] _examples) {
            this.examples = _examples;
            return this;
        }

        public EvaluationParameters setFPRate(Double rate, Double tolerance) {
            this.FPRate = rate;
            this.toleranceFPRate = tolerance;
            return this;
        }

        public EvaluationParameters setFNRate(Double rate, Double tolerance) {
            this.FNRate = rate;
            this.toleranceFNRate = tolerance;
            return this;
        }

        public EvaluationParameters setFuntimePercTolerance(Double percTolerance) {
            this.runtimePercTolerance = percTolerance;
            return this;
        }
    }
}
