package org.iutools.morphrelatives;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.iutools.corpus.CompiledCorpusRegistry;

import java.util.ArrayList;
import java.util.List;

public class PerformanceExpectations {

	public String corpusName = CompiledCorpusRegistry.defaultCorpusName;
	public boolean computeStatsOverSurfaceForms;
	public double targetPrecision = -1;
	public double targetRecall = -1;
	public double precRecTolerance;
	public Double runtimePercTolerance = 0.10;
	public String focusOnWord;
	public boolean verbose;

	public List<Object[]> wordOutcomes = new ArrayList<Object[]>();

	public PerformanceExpectations() {
		init_PerformanceExpectations();
	}

	private void init_PerformanceExpectations() {
		defineWordOutcomes();
	}

	private void defineOutcome(String word,
										int goodRelsProduced, int relsProduced, int gsRels) {
		wordOutcomes.add(new Object[]{word, goodRelsProduced, relsProduced, gsRels});
	}

	public Pair<String, Triple<Integer, Integer, Integer>>
	outcome4word(String word) {

		Pair<String, Triple<Integer, Integer, Integer>> outcome = null;

		Object[] wordOutcome = null;
		for (Object[] outcomeArr : wordOutcomes) {
			String outcomeWord = (String) outcomeArr[0];
			if (word.equals(outcomeWord)) {
				outcome = Pair.of(
				word,
				Triple.of(
				(Integer) outcomeArr[1],
				(Integer) outcomeArr[2],
				(Integer) outcomeArr[3]
				)
				);
			}
		}

		return outcome;
	}

	public static Pair<String, Triple<Integer, Integer, Integer>>
	outcomeArr2Tuple(Object[] outcomeArr) {

		String word = (String) outcomeArr[0];
		Triple<Integer, Integer, Integer> wordStats =
		Triple.of(
		(Integer) outcomeArr[1],
		(Integer) outcomeArr[2],
		(Integer) outcomeArr[3]);

		return Pair.of(word, wordStats);
	}

	public PerformanceExpectations setComputeStatsOverSurfaceForms(
	boolean _computeStatsOverSurfaceForms) {
		this.computeStatsOverSurfaceForms = _computeStatsOverSurfaceForms;
		return this;
	}

	public PerformanceExpectations setTargetPrecision(
	double _targetPrecision) {
		this.targetPrecision = _targetPrecision;
		return this;
	}

	public PerformanceExpectations setTargetRecall(double _targetRecall) {
		this.targetRecall = _targetRecall;
		return this;
	}

	public Pair<Double, Double> targetPrecRecall() {

		int totalWords = wordOutcomes.size();
		int totalGoodRelsProduced = 0;
		int totalRelsProduced = 0;
		int totalRelsGS = 0;
		for (Object[] outcome : wordOutcomes) {
			String word = (String) outcome[0];
			totalGoodRelsProduced += (Integer) outcome[1];
			totalRelsProduced += (Integer) outcome[2];
			totalRelsGS += (Integer) outcome[3];
		}

		Double targetPrec = 0.0;
		if (totalRelsProduced > 0) {
			targetPrec = 1.0 * totalGoodRelsProduced / totalRelsProduced;
		}

		Double targetRecall = 0.0;
		if (totalRelsGS > 0) {
			targetRecall = 1.0 * totalGoodRelsProduced / totalRelsGS;
		}

		return Pair.of(targetPrec, targetRecall);
	}

	public PerformanceExpectations setPrecRecTolerance(double _precRecallTolerance) {
		this.precRecTolerance = _precRecallTolerance;
		return this;
	}

	public PerformanceExpectations setRuntimePercTolerance(
	double _percTolerance) {
		this.runtimePercTolerance = _percTolerance;
		return this;
	}

	public PerformanceExpectations setFocusOnWord(String _word) {
		this.focusOnWord = _word;
		return this;
	}

	public PerformanceExpectations setVerbosity(boolean _verbose) {
		this.verbose = _verbose;
		return this;
	}

	public PerformanceExpectations setCorpusName(String _corpusName) {
		this.corpusName = _corpusName;
		return this;
	}

	private PerformanceExpectations defineWordOutcomes() {
		// Define what we currently expect to happen for each word in the
		// GS.
		defineOutcome("takujumaguvit", 4, 5, 7);
		defineOutcome("qarasaujakkut", 0, 0, 1);
		defineOutcome("qaujisaqtikkut", 4, 5, 5);
		defineOutcome("ilinniaqtumut", 0, 5, 5);
		defineOutcome("mikinniqsanut", 3, 5, 8);
		defineOutcome("silattusarvimmi", 1, 5, 8);
		defineOutcome("pivalliajut", 0, 5, 1);
		defineOutcome("akigiliutinajaqtuq", 4, 5, 6);
		defineOutcome("qakugukkanniq", 0, 0, 1);
		defineOutcome("nalunairsivut", 4, 5, 6);
		defineOutcome("atuinnautittinirmit", 2, 5, 8);
		defineOutcome("ilagiisakkunnut", 0, 0, 2);
		defineOutcome("tuksiraummut", 4, 5, 8);
		defineOutcome("atuqtauniarput", 4, 5, 8);
		defineOutcome("piliriaksamik", 4, 5, 8);
		defineOutcome("iqqanaijarvingmi", 3, 5, 8);
		defineOutcome("atuinnaujuq", 2, 5, 7);
		defineOutcome("ilagiiksakkut", 0, 5, 1);
		defineOutcome("pijitsiraqtuq", 0, 0, 8);
		defineOutcome("iqqanaijaqtinut", 5, 5, 7);
		defineOutcome("ikajuutiqarnirminik", 4, 5, 5);
		defineOutcome("inuulisaijiit", 2, 5, 5);
		defineOutcome("qikiqtaalungmi", 1, 5, 6);
		defineOutcome("tunisimatsiaqtut", 0, 5, 2);
		defineOutcome("pijittirautinginnik", 0, 0, 4);
		defineOutcome("ilinniarusiaqtaatittilaarngata", 0, 0, 3);
		defineOutcome("ujararniarvinu", 5, 5, 6);
		defineOutcome("nalliukkumaanik", 0, 0, 1);
		defineOutcome("uvattinnut", 0, 5, 5);
		defineOutcome("ulurianaqtuliit", 0, 0, 3);
		defineOutcome("tuttarvilingmik", 5, 5, 8);
		defineOutcome("ilaliutisimatuinnalirtuni", 0, 0, 6);
		defineOutcome("atuinnaujumi", 4, 5, 7);
		defineOutcome("pujurninganut", 0, 0, 1);
		defineOutcome("qaujisarnirmut", 3, 5, 5);
		defineOutcome("uqausiqarniq", 0, 5, 5);
		defineOutcome("nunaqaqqaasimajut", 0, 5, 2);
		defineOutcome("attanaqtunik", 1, 5, 4);
		defineOutcome("uqausiksaqapiit", 0, 0, 3);
		defineOutcome("katimajinginnut", 4, 5, 6);
		defineOutcome("isumaksaqsiurunnaqullugillu", 0, 0, 6);
		defineOutcome("kiggaqtuq&ugit", 0, 5, 6);
		defineOutcome("iliqqusiliriniq", 1, 2, 2);
		defineOutcome("iqqanaijarvinut", 4, 5, 8);
		defineOutcome("nunamiutait", 0, 5, 1);
		defineOutcome("iktajaralik", 0, 0, 3);
		defineOutcome("attarnaqtumi", 2, 5, 5);
		defineOutcome("ikajuutiksanik", 4, 5, 7);
		defineOutcome("tusaqtitsijjutiit", 0, 5, 5);
		defineOutcome("umiarjuarmut", 3, 5, 5);
		defineOutcome("tusagaksalirijikkunnit", 2, 5, 4);
		defineOutcome("tusaqtitsigiatsaq", 1, 5, 3);
		defineOutcome("attanajjaiqsisimanirmik", 2, 5, 5);
		defineOutcome("sanajauvallialiqtillugu", 0, 5, 6);
		defineOutcome("uqsualummuuqtumu", 0, 5, 3);
		defineOutcome("pijunnautinginnik", 4, 5, 8);
		defineOutcome("piviqaqtittisuunguvuq", 5, 5, 8);
		defineOutcome("pijitsirautikkut", 0, 0, 9);
		defineOutcome("qaujimajautsiaqtuq", 2, 5, 6);
		defineOutcome("nangminirijauqataujuq", 5, 5, 8);
		defineOutcome("tusaqtitsijariaksaq", 1, 5, 5);
		defineOutcome("qaujikkaijunnaramik", 5, 5, 10);
		defineOutcome("sivulininngaaqtuliriniq", 0, 0, 1);
		defineOutcome("uvvaluunniit", 0, 2, 1);
		defineOutcome("kisuruluujalungnik", 0, 0, 4);
		defineOutcome("unatarninginni", 0, 5, 6);
		defineOutcome("ukiuqtaqturmiutanut", 0, 0, 3);
		defineOutcome("saqqittivut", 0, 0, 1);
		defineOutcome("nalunaiqsimajumi", 4, 5, 6);
		defineOutcome("piliriqatautittiniq", 0, 5, 4);
		defineOutcome("sanajulirinirmut", 4, 5, 6);
		defineOutcome("piqasiujjigiaqarniaqtuq", 0, 0, 4);
		defineOutcome("tunisinirmit", 5, 5, 6);
		defineOutcome("piliriangulaaqpuq", 4, 5, 8);
		defineOutcome("ilisarijausimajjutimik", 1, 5, 6);
		defineOutcome("unataqtutsalirijingita", 2, 5, 5);
		defineOutcome("aqiarurlungniq", 0, 0, 3);
		defineOutcome("avatilirinirmuungajunik", 1, 5, 3);
		defineOutcome("piisiipiilingnik", 0, 0, 1);
		defineOutcome("tukisigiarutiksanit", 4, 5, 8);
		defineOutcome("parnaummit", 2, 5, 6);
		defineOutcome("aaqqigiaqsinirmit", 0, 0, 3);
		defineOutcome("pilirianguniujuq", 4, 5, 6);
		defineOutcome("kaanturaaktaaqtitauvut", 0, 5, 4);
		defineOutcome("ullurummitarnamik", 1, 5, 7);
		defineOutcome("sunakkutaaqaqtitsijiit", 0, 0, 1);
		defineOutcome("quviasugutiqarnirmut", 3, 5, 6);
		defineOutcome("piruqtittiqullugit", 1, 5, 3);
		defineOutcome("silarjuamilu", 4, 5, 7);
		defineOutcome("tangmaarniup", 0, 5, 3);
		defineOutcome("niuviqatautillugit", 0, 5, 3);
		defineOutcome("ilinniarvinganni", 3, 5, 5);
		defineOutcome("ilinniarutauvattumik", 0, 5, 9);
		defineOutcome("manimatittinirmit", 0, 0, 2);
		defineOutcome("nunataarnirmut", 2, 5, 5);
		defineOutcome("kuapuriisakkut", 0, 0, 1);
		defineOutcome("ikiaqqivingmi", 0, 0, 2);
		defineOutcome("qanuittuugajarmangaat", 0, 5, 4);
		defineOutcome("quviasuttut", 4, 5, 7);
		defineOutcome("akinnamiutat", 0, 0, 7);
		defineOutcome("nunatuinnarmut", 0, 5, 5);

		return this;
	}
}
