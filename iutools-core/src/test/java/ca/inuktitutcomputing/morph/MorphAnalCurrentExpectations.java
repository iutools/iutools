package ca.inuktitutcomputing.morph;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides the "current" expectations for the morphological 
 * analyser on the cases provided in the 
 * MorphAnalGoldStandard.
 * 
 * By default, we assume that the top decomposition provided 
 * by the analyzer is the correct one for every word.
 * 
 * We only provide a different expectation for words where the 
 * analyzer "fails" in some sense. 
 * 
 * There are several ways in which the analyzer can fail for a word
 * 
 * - CORRECT_NOT_FIRST: The analyzer does include the correct 
 *     decomposition in the list, but it is not the first one.
 *     
 * - CORRECT_NOT_PRESENT: The analyzer does provide some analyses, but none of 
 *     them is the correct one.
 * 
 * - NO_DECOMPS: The analyzer completes without timeout but it 
 *     produces no decompositions at all
 *
 * - TIMEOUT: The analyzer times out before it can complete.
 * 
 *     
 */
public class MorphAnalCurrentExpectations {
	
	public static enum OutcomeType {
		SUCCESS, CORRECT_NOT_FIRST, CORRECT_NOT_PRESENT, 
		TIMEOUT, NO_DECOMPS};
	
	
	Map<String,OutcomeType> expFailures = 
		new HashMap<String,OutcomeType>();
	
	public MorphAnalCurrentExpectations() throws MorphologicalAnalyzerException {
		initMorphAnalCurrentExpectations();
	}

	public void expectFailure(String word, OutcomeType type) throws MorphologicalAnalyzerException {
		if (type == OutcomeType.SUCCESS) {
			throw new MorphologicalAnalyzerException(
				"Outcome "+OutcomeType.SUCCESS+" is not a type of failure");
		}
		expFailures.put(word, type);
	}

	public OutcomeType type4outcome(AnalysisOutcome outcome, 
			String correctDecomp) {
		
		OutcomeType type = null;
		
		if (outcome.decompositions == null || 
			outcome.decompositions.length == 0) {
			type = OutcomeType.NO_DECOMPS;
		}
		
		if (type == null) {
			// The Decomp produces some decompositions. 
			// What is the position of the correct one in that list?
			//
			Integer rank = outcome.decompRank(correctDecomp);
			if (rank == null) {
				type = OutcomeType.CORRECT_NOT_PRESENT;
			} else if (rank > 0) {
				type = OutcomeType.CORRECT_NOT_FIRST;
			} else {
				type = OutcomeType.SUCCESS;
			}
		}
		
		if (type == null) {
			type = OutcomeType.SUCCESS;
		}
		
		return type;
	}
	
	public OutcomeType expectedOutcome(String word) {
		OutcomeType outcome = OutcomeType.SUCCESS;
		if (expFailures.containsKey(word)) {
			outcome = expFailures.get(word);
		}
		return outcome;
	}
	
	private void initMorphAnalCurrentExpectations() throws MorphologicalAnalyzerException {
		
		// 
		// Words that produce the correct analysis, but not as the top 
		// alternative
		//
		expectFailure("amisuummata", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ajjigiinngittunut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("aulajjutinut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("piliriaksat", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("tullia", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimaniq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqti", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausirnik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("niqsunaqtuq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausilirinirmut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("taissumani", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("inulirijikkut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ministaa", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajiralaangukainnaqtunut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uuktuutigilugu", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("naammasaqtuit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("aulaninginnut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajiralaa", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("pivalliatittinirmut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("pijjutilik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajjutiksaq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("milianik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligarmik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("qanuittuni", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ivvit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("inulirijikkullu", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimautitsa", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("timiujut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunaliujuq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("titiraqsimajunut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunaliujut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("timiujuq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("taimannganit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("timimigut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqalimaaqtauninga", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("milianit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ilangi", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqartii", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqtinut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ilinniarvimmi", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausingit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausirmik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligassaq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliurti", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajingannit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("tukisinaliqtissimajuq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ilinniarviup", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("pigiaqtitat", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimatillugit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("kingulliqpaami", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("qitingani", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqtilimaat", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("missaanut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligarnit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("qautamaat", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausiit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliriji", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("akunialuk", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunaliujunit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqtilimaanut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katittugit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("aippaanik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ilagiaruti", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("aippaanit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajingit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("titiqqakkuvik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligarnik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ilagiarutit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("innatuqarnut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("atuagarmik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunaliujunik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ajjigiinngittunik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("amisuuningit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausirnut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("aglattiu", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("itsivautaa", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligatsait", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunalinnut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("titiqqak", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausiq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("pattatuqtut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("pillugit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ajjaqsiji", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqtiuqatiga", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausiksangit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqsualuk", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimatuinnaqtillugit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katitsutik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("titiqqaq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligarmi", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausituinnakkut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maliganga", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausikkut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligarmut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("piliriviit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("illulirijirjuakkut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("qaujigiarutit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqtiit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaksait", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("titiraqsimajunik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ilagiarutiit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqtinik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausiqtigut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("titiraqtii", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ilinniarviit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("kiinaujatigut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("akulliq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunalingnut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uuttuutigillugu", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("innatuqait", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqtikkut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligatigut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("titiqqakkut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqsualuup", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqalimaaqtauninginnut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katilimaaqtugit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajiralaangit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajiit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajinginnit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimaji", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaup", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ajjigiinngittut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqtiujut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("itsivautaaq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqtiujuq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nakuqmi", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajinginnik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("apiqqutit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqsualummut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaujuq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("titiraqsimajut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausinga", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("innatuqarnik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqtimut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("imaimmat", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("titiraqti", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("qanuittunik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajjutiksait", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunalinnik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligassait", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqtiup", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("pijjutiqaqtut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausiksait", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunalinginni", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("pijjutiqaqtuq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maannaujukkut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("akiraqtuqtut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("akuni", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajiralaani", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunnguani", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("piluaqtumit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("iksivautaa", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunaliit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("kingullirmi", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("pigiaqtitait", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausirijanga", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ilitarijauningit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ii", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqalimaarniq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("iglulirijirjuakkut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("tukiliuqtausimajuq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nigiani", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajilimaat", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nanulik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaksaq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqtiuqatikka", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunalilimaat", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("kingulliqpaaq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("aulanirmut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("naukkut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausitsangit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimautiminingit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqalimaarvik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliurtiit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunalinnit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("qaujimajuinnaugatta", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("tamani", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katillugit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ilulingit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausiksat", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligait", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqtinit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("inulirijikkunnut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("illulirijikkut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("titiqqanik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajjutissaq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("piqujaksat", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajingita", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ammaluttauq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("iglulirijikkut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajinginnut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ukiurtartumi", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("mamianaq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqsualuit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("naalautikkut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("amittuq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajiralaanguinnaqtut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("iksivautaaq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("titiqqanit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maannauju", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimavimmi", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunalingni", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunalingnit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajiuqataujut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajinut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("issivautaa", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunalinnu", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunalinni", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ajjigiinngittunit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("titiqqatigut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajiralaanut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("saniani", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("aaqqiumainnarutinut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("tanna", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("inuusilirijikkunullu", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliurtiup", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("pijjutiqaqtunik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("piluaqtumi", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("titiqqamik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("miksaanut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqtii", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ukua", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ilagiarut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("matuirutimut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligalirinirmut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaliuqtit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ilulingita", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunaliujuni", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimautinik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajiralaat", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("mitsaanut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("pigiarutiksanit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("naliqqangit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("qautamaamut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ilinniarvik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("titiraqsimaningit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nalliani", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("marrunnik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligaksanit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajiujut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("qattinik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("atuagaq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("issivautaaq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("atuqtuksait", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("atuagarnik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ajjaqsijii", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("sivuniksami", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimmajjutiksaq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("taitsumani", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("paktaqtuqtut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("unnusakkut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("atuqtuksanut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("kingulliqpaamik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("atuagait", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("illumi", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("naammasaqtut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("kingulliqpaamit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunalittinni", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqalimaaqtauningit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ilaannikkut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ikajuutit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("piluaqtumik", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uuttuutigilugu", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maani", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("pinasuarusiulauqtumi", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ukiuqtaqtumi", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajiralaanit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("maligatsaq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("katimajjutiksanut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("uqausissangit", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("qallunaatitut", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ammalu", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("sulijuq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("asingi", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("illuni", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("ilitaqsiniq", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("taanna", OutcomeType.CORRECT_NOT_FIRST);
		expectFailure("nunaliujunut", OutcomeType.CORRECT_NOT_FIRST);
		
		// 
		// Words that produce some analyses, but none of them is the correct 
		// one.
		//
		expectFailure("imaimmat", OutcomeType.CORRECT_NOT_PRESENT);
		expectFailure("iksivautaaq", OutcomeType.CORRECT_NOT_PRESENT);
		expectFailure("mamianaq", OutcomeType.CORRECT_NOT_PRESENT);
		expectFailure("marrunnik", OutcomeType.CORRECT_NOT_PRESENT);
		expectFailure("nalliani", OutcomeType.CORRECT_NOT_PRESENT);
		expectFailure("nunnguani", OutcomeType.CORRECT_NOT_PRESENT);
		expectFailure("paktaqtuqtut", OutcomeType.CORRECT_NOT_PRESENT);
		expectFailure("pigiaqtitat", OutcomeType.CORRECT_NOT_PRESENT);
		expectFailure("piliriaksat", OutcomeType.CORRECT_NOT_PRESENT);
		expectFailure("piqujaksat", OutcomeType.CORRECT_NOT_PRESENT);
		expectFailure("taaksumunga", OutcomeType.CORRECT_NOT_PRESENT);		
		expectFailure("taimannganit", OutcomeType.CORRECT_NOT_PRESENT);
		expectFailure("titiqqak", OutcomeType.CORRECT_NOT_PRESENT);
		expectFailure("titiqqaq", OutcomeType.CORRECT_NOT_PRESENT);
		expectFailure("titiqqat", OutcomeType.CORRECT_NOT_PRESENT);		
		expectFailure("ukiuqtaqtumi", OutcomeType.CORRECT_NOT_PRESENT);
		expectFailure("ukiurtartumi", OutcomeType.CORRECT_NOT_PRESENT);
		expectFailure("uqausiksat", OutcomeType.CORRECT_NOT_PRESENT);

		// 
		// Words that produce no decomposition at all
		//
		expectFailure("issivautaaq", OutcomeType.NO_DECOMPS);
		expectFailure("itsivautaaq", OutcomeType.NO_DECOMPS);
		expectFailure("katimajit", OutcomeType.NO_DECOMPS);
		expectFailure("katimmajjutiksaq", OutcomeType.NO_DECOMPS);
		expectFailure("kiinaujatigut", OutcomeType.NO_DECOMPS);
		expectFailure("maligaliuqtit", OutcomeType.NO_DECOMPS);
		expectFailure("maligatigut", OutcomeType.NO_DECOMPS);
		expectFailure("milianik", OutcomeType.NO_DECOMPS);
		expectFailure("milianit", OutcomeType.NO_DECOMPS);
		expectFailure("nakuqmi", OutcomeType.NO_DECOMPS);
		expectFailure("naliqqangit", OutcomeType.NO_DECOMPS);
		expectFailure("nigiani", OutcomeType.NO_DECOMPS);
		expectFailure("niqsunaqtuq", OutcomeType.NO_DECOMPS);
		expectFailure("qallunaatitut", OutcomeType.NO_DECOMPS);
		expectFailure("qattinik", OutcomeType.NO_DECOMPS);
		expectFailure("tamani", OutcomeType.NO_DECOMPS);
		expectFailure("tanna", OutcomeType.NO_DECOMPS);
		expectFailure("titiqqakkut", OutcomeType.NO_DECOMPS);
		expectFailure("titiqqakkuvik", OutcomeType.NO_DECOMPS);
		expectFailure("titiqqamik", OutcomeType.NO_DECOMPS);
		expectFailure("titiqqanik", OutcomeType.NO_DECOMPS);
		expectFailure("titiqqanit", OutcomeType.NO_DECOMPS);
		expectFailure("titiqqatigut", OutcomeType.NO_DECOMPS);
	}
}
