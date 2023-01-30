/**
 * Left-to-right implementation of the morphological analyser.
 *
 * This class decomposes an Inuktitut word into morphemes, starting from the
 * end of the word, and moving towards the beginning.
 */

package org.iutools.morph.r2l;

import java.util.*;
import java.util.concurrent.TimeoutException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import org.iutools.linguisticdata.*;
import org.iutools.linguisticdata.constraints.Conditions;
import org.iutools.morph.*;
import org.iutools.script.Orthography;
import org.iutools.script.Roman;
import org.iutools.script.Syllabics;
import org.iutools.morph.r2l.Graph.State;
import org.iutools.phonology.Dialect;
import org.iutools.utilities.StopWatch;
import org.iutools.utilities1.Util;

//-------------------------------------------------
// The morphological analyzer uses the state graph.
//-------------------------------------------------


public class MorphologicalAnalyzer_R2L extends MorphologicalAnalyzer {

    private Hashtable<String, Graph.Arc[]> arcsByMorpheme = new Hashtable<>();
    private final static boolean USE_SYLLABICS = false;

    private static Cache<String, Decomposition[]>
    	decompsCache =
    		Caffeine.newBuilder().maximumSize(10000)
    		  .build();

	protected Set<DecompositionState> _decompsSoFar = new HashSet<>();

	/**
	 * The MorphologicalAnalyzer will raise this exception when it has found enough analyses.
	 */
	public static class MorphologicalAnalyzerDoneException extends Exception {}

    public MorphologicalAnalyzer_R2L() {
    	super();
		LinguisticData.getInstance();
    }

	/**
	 * Main method for decomposing a word.
	 *
	 * @param word             String  word to be analyzed
	 * @param extendedAnalysis Boolean when true, and if the word ends in a vowel, analyze also the words
	 *                                 word+'t', word+'k', word+'q'
	 * @return Decomposition[]         an array of Decomposition objects
	 * @throws MorphologicalAnalyzerException
	 * @throws TimeoutException
	 */
	@Override
	protected Decomposition[] doDecompose(String word, Boolean extendedAnalysis)
		throws MorphologicalAnalyzerException, TimeoutException {
		Logger logger = LogManager.getLogger("MorphoplogicalAnalyzer.doDecompose");
		if (extendedAnalysis == null) {
			extendedAnalysis = true;
		}

		Decomposition[] cachedDecomps = uncache(word, extendedAnalysis);
		if (cachedDecomps != null) {
			return cachedDecomps;
		}

		boolean decomposeCompositeRoot = false; // do not decompose composite root

		try {
			// Do the morphological analysis.
			List<DecompositionState> decStatesList =
				decomposeUntilTimeoutOrCompletion(word, extendedAnalysis, decomposeCompositeRoot);

			// A.
			// Eliminate decompositions that contain a sequence of suffixes
			// that corresponds to a composite suffix, in order to keep only
			// the decompositions with the composite suffix.
			DecompositionState[] decStatesArray =
				DecompositionState.removeCombinedSuffixes(decStatesList.toArray(new DecompositionState[0]));

			// B. Eliminate duplicate decompositions.
			decStatesArray = DecompositionState.removeMultiples(decStatesArray);

			// C.
			// Sort the decompositions according to the following rules:
			// 1. longest roots first
			// 2. minimum number of affixes
			Arrays.sort(decStatesArray);

			Decomposition[] decomps = DecompositionState.toDecompositionArray(decStatesArray);
			cache(decomps, word, extendedAnalysis);

			return decomps;

		} catch (LinguisticDataException | MorphologicalAnalyzerException | DecompositionException e) {
			throw new MorphologicalAnalyzerException(e);
		}

	}

	/**
	 * This method is called by doDecompose (see above).
	 * The Inuktitut word can be in syllabics or in the Roman alphabet at this point.
	 */
	private List<DecompositionState> decomposeUntilTimeoutOrCompletion(
		String wordToBeAnalyzed, Boolean extendedAnalysis, boolean decomposeCompositeRoot)
			throws TimeoutException, MorphologicalAnalyzerException {
		this._decompsSoFar = new HashSet<>();
		List<DecompositionState> decompsSoFar = new ArrayList<DecompositionState>();
		try {
			List<DecompositionState> decomps;
			if (Syllabics.containsInuktitut(wordToBeAnalyzed))
				wordToBeAnalyzed = Syllabics.transcodeToRoman(wordToBeAnalyzed);
			wordToBeAnalyzed = Util.enMinuscule(wordToBeAnalyzed);
			wordToBeAnalyzed = wordToBeAnalyzed.replaceAll("([iua])qk([iua])", "$1qq$2"); // to cope with error of transliteration

			if (wordToBeAnalyzed.charAt(wordToBeAnalyzed.length() - 1) != 'n') {
				// if the word does not end in 'n', analyze it as is.
				decomps = _decompose(wordToBeAnalyzed, decomposeCompositeRoot, decompsSoFar);
				decompsSoFar.addAll(decomps);
				// if the flag is set for treating possibly missing consonant, do it.
				if (extendedAnalysis && Roman.typeOfLetterLat(wordToBeAnalyzed.charAt(wordToBeAnalyzed.length() - 1)) == Roman.V) {
					List<DecompositionState> otherDecomps = _decomposeForFinalConsonantPossiblyMissing(wordToBeAnalyzed, decomposeCompositeRoot);
					decomps.addAll(otherDecomps);
					decompsSoFar.addAll(otherDecomps);
				}
			} else {
				// replace 'n' by 't' and analyze.
				decomps = _decomposeForFinalN(wordToBeAnalyzed, decomposeCompositeRoot);
				decompsSoFar.addAll(decomps);
			}
		} catch (MorphologicalAnalyzerDoneException e) {
			// If this exception is raised, it just means we stopped at some point where we had found enough decomps
			decompsSoFar.addAll(this._decompsSoFar);
		}

		return decompsSoFar;
	}

	private synchronized void cache(Decomposition[] decs, String word, boolean extendedAnalysis) {
		String key = cacheKeyFor(word, _stopAfterNDecomps, extendedAnalysis);
		decompsCache.put(key, decs);
	}


	private synchronized Decomposition[]  uncache(String word, boolean extendedAnalysis) {
		String key = cacheKeyFor(word, _stopAfterNDecomps, extendedAnalysis);
		Decomposition[] decomps = decompsCache.getIfPresent(key);
		return decomps;
	}


	/*
	 * If the word ends with the consonant 'n', we could be in the presence of a nasalized 't'; this happens often.
	 * Analyze the word with its final 'n' replaced by 't'.
	 */
	private List<DecompositionState> _decomposeForFinalN(String aWord, boolean decomposeCompositeRoot)
			throws TimeoutException, MorphologicalAnalyzerException, MorphologicalAnalyzerDoneException {

		String wordWithNReplaced = aWord.substring(0, aWord.length() - 1) + "t";
		List<DecompositionState> newDecomps = _decompose(wordWithNReplaced, decomposeCompositeRoot);
		if (newDecomps != null)
			for (int j = 0; j < newDecomps.size(); j++) {
				DecompositionState dec = (DecompositionState) newDecomps.get(j);
				dec.stem.term = aWord;
				Object[] morphemesOfDecomposition = dec.morphParts;
				if (morphemesOfDecomposition.length != 0) {
					AffixPartOfComposition lastAffix = (AffixPartOfComposition) morphemesOfDecomposition[morphemesOfDecomposition.length - 1];
					lastAffix.setTerme(lastAffix.getTerm().substring(0, lastAffix.getTerm().length() - 1) + "n");
				}
			}

		return newDecomps;
	}

    /*
     * It is often seen that Inuktitut words are written without their final consonant.
     * If the word ends with a vowel, there could be missing a consonant.
     * Add '*' at the end of the word in place of a missing consonant and analyze the word.
     */
    private List<DecompositionState> _decomposeForFinalConsonantPossiblyMissing(
    		String aWord, boolean decomposeCompositeRoot) throws TimeoutException, MorphologicalAnalyzerException, MorphologicalAnalyzerDoneException {
    	stpw.check(
    		"_decomposeForFinalConsonantPossiblyMissing -- upon entry, word="+
    		aWord+", decomposeCompositeRoot="+decomposeCompositeRoot);
		return _decompose(aWord + "*", false);
	}

	/**
	 * Decompositions are returned in the same order as they were found.
	 * At this stage, they are not sorted.
	 * Note: The argument decomposeCompositeRoot set to 'true' means
	 * that roots known as composite in the database must be decomposed.
	 *
	 * @param word String an Inuktitut word in the Roman alphabet
	 * @param decomposeCompositeRoot boolean if true, decompose composite roots into their components
	 * @param decompsSoFar List a list of Decomposition objects, possible null
	 * @return a list of Decomposition objects or null
	 */
	private List<DecompositionState> _decompose(String word,
															  boolean decomposeCompositeRoot, List<DecompositionState> decompsSoFar)
			throws TimeoutException, MorphologicalAnalyzerException, MorphologicalAnalyzerDoneException {

		Vector<AffixPartOfComposition> morphPartsInit = new Vector<AffixPartOfComposition>();
		Graph.State state = Graph.initialState;
		List<DecompositionState> decompositions = null;
		String simplifiedTerm = null;
		Conditions preCond = null;

		stpw = new StopWatch(millisTimeout, "Decomposing word="+word).start();
		Dialect.setStopWatch(stpw);
		MorphAnalyzerValidation.setStopWatch(stpw);
        if (!timeoutActive) stpw.disactivate(); // for debugging
		stpw.reset();

		arcsByMorpheme.clear();

		if (word != null) {
			// Simplify spelling: replace 'nng' by 'NN' and 'ng' by 'N'.
			simplifiedTerm = Orthography.simplifiedOrthography(word, USE_SYLLABICS);
			String transitivity = null;
			// TODO: add the boolean decomposeCompositeRoot as argument to __decompose_simplified_term__
			decompositions = __decompose_simplified_term__(simplifiedTerm, simplifiedTerm, simplifiedTerm,
					morphPartsInit, new Graph.State[] { state }, preCond, transitivity);
		}

		if (decompsSoFar != null) {
			decompsSoFar.addAll(decompositions);
		}

		return decompositions;
	}

	private List<DecompositionState> _decompose(String word,
  		boolean decomposeCompositeRoot) throws TimeoutException, MorphologicalAnalyzerException, MorphologicalAnalyzerDoneException {
		return _decompose(word, decomposeCompositeRoot, null);
	}

    //========================== __decompose_simplified_term__ ====================================
    // This is where the morphological analysis is done. RECURSIVE.
    //=============================================================================================

	/**
	 * Decomposition of an Inuktitut term.
	 *
	 * This method is called recursively with the remaining stem every time
	 * a candidate affix has been validated. The first time it is called, 'term' is the original word,
	 * the same as 'termOrig'. The next times, 'termOrig' is the stem remaining in front of the consumed
	 * affix's characters, and 'term' is that stem with its end contextualized, i.e. rebuilt according to the actions of the
	 * validated affixes. For example, when analyzing 'umiarjualiuq...' and 'liuq' is found a valid affix,
	 * the remaining stem 'umiarjua' becomes the value of 'termOrig' and 'term' is set iteratively to
	 * that value appended with a consonant since 'liuq' is known to delete the last consonant of the stem:
	 * 'umiarjuat', 'umiarjuak' and 'umiarjuaq'. This is done such that the term analyzed presents itself as
	 * if there had been no deletion, voicing or any other action, for the purpose of looking up the forms
	 * in the database's list of forms.
	 *
	 * @param term String the contextualized term to be analyzed, in simplified spelling; initially, the whole word
	 * @param termOrig String the real term to be analyzed, in simplified spelling.
	 * @param word String the original word being analyzed, in simplified spelling.
	 * @param morphParts Vector<AffixPartOfComposition> morphemes accepted so far
	 * @param states Graph.State[] the sequence of states so far
	 * @param preConds Conditions conditions to be met by the (last morpheme of the) stem
	 *                 i.e. next during the analysis
	 * @param transitivity String the state of the transitivity so far
	 */

	// TODO: add the boolean decomposeCompositeRoot as argument to __decompose_simplified_term__ or set it as a class attribute
    private List<DecompositionState> __decompose_simplified_term__(
			String term, String termOrig, String word,
			Vector<AffixPartOfComposition> morphParts,
			Graph.State[] states,
			Conditions preConds,
			String transitivity)
			throws TimeoutException, MorphologicalAnalyzerException, MorphologicalAnalyzerDoneException {

    	Logger logger = LogManager.getLogger("MorphologicalAnalyzer_R2L.__decompose_simplified_term__");
    	stpw.check("__decompose_simplified_term__ -- Upon entry");

		List<DecompositionState> completeAnalysis = new ArrayList<DecompositionState>();

        // 1. Check if the term to be analyzed could be a root (simple or composite) in the database.
        //    If that is the case, add the resulting decomposition(s) to the list of all the resulting decompositions.
        List<DecompositionState> analysesAsRoot = analyzeAsRoot(term,termOrig,
                word,morphParts,states, preConds, transitivity
                );
        completeAnalysis.addAll(analysesAsRoot);
        logger.debug("analysesAsRoot: "+analysesAsRoot.size());

        // 2. If the term could not be analyzed as a root, or if it could be analyzed as a root and the flag is set
		//    to further decompose a composite root into its parts, analyze the term as a sequence of morphemes.
        if (analysesAsRoot.size()==0 || decomposeCompositeRoot) {
			List<DecompositionState> analysesAsSequenceOfMorphemes = analyzeAsSequenceOfMorphemes(term,
					word, morphParts, states, preConds, transitivity
			);
			completeAnalysis.addAll(analysesAsSequenceOfMorphemes);
		}

        return completeAnalysis;
    }


	/*
	 * ===================== ANALYZE AS A SEQUENCE OF MORPHEMES ===========================
	 * Starting at the last character of the term, go back 1 character at a time
	 * until an affix is found. One looks up for affixes for the sequence of characters as
	 * read from the term and for any variation of that sequence with equivalent groups of
	 * consonants resulting from the assimilation of place (eg. kt<>tt, gv<>vv, etc.).
	 *
	 * When an affix candidate has been found, a branch is created and the analysis is
	 * continued. If more than 1 affix candidates were found, other branches are
	 * checked by analyzing the same remaining stem from these other candidates points
	 * of view.
	 *
	 * When those branches have all been processed, the analysis continues by eating up
	 * another character, as if affix(es) had not been found. This allows for all the
	 * possibilities for combining characters in morphemes of different lengths. For example,
	 * in a word containing 'lauqsima', 'sima' will be found first; in order to analyze
	 * 'lauqsima', one has to continue on another branch as if 'sima' had not been found.
	 *
	 * The character counter is stopped at 2 because there exists no root of 2 characters
	 * the last of which would be a consonant susceptible of being deleted by an affix.
	 */

	private List<DecompositionState> analyzeAsSequenceOfMorphemes(
			String simplifiedTerm,
			String word,
			Vector<AffixPartOfComposition> morphParts, State[] states,
			Conditions preCond, String transitivity) throws TimeoutException, MorphologicalAnalyzerException, MorphologicalAnalyzerDoneException {

    	stpw.check("analyzeAsSequenceOfMorphemes -- Upon entry");
    	Logger logger = LogManager.getLogger("MorphologicalAnalyzer_R2L.analyzeAsSequenceOfMorphemes");
    	logger.debug("++++++simplifiedTerm= "+simplifiedTerm);
        List<DecompositionState> completeAnalysis = new ArrayList<DecompositionState>();

        int positionAffix = 0; // position dans le mot
        int positionAffixStart = simplifiedTerm.length() - 1;
        //            if (term.charAt(term.length() - 1) == '*')
        //                positionAffixStart--;

		// from the end of the stem, backwards
        for (positionAffix = positionAffixStart; positionAffix > 1; positionAffix--) {
            String affixCandidate = simplifiedTerm.substring(positionAffix);
            String remainingStem = simplifiedTerm.substring(0, positionAffix);
            stpw.check("analyzeAsSequenceOfMorphemes -- position: "+positionAffix+
        			"; affixCandidate: "+affixCandidate+"; remainingStem: "+remainingStem);

			Vector<SurfaceFormOfAffix> formsOfAffixForOriginalSpelling = lookForForms(affixCandidate, USE_SYLLABICS); // USE_SYLLABICS set to false

            // It is possible that a dialectal difference in pronunciation shows up at the junction of 2 morphemes.
			// It can affect the end of the first morpheme, the beginning of the second, or both. It is also possible
			// that some dialectal changes happen inside the candidate affix. All possibilities are considered, including
			// Schneider's law.
			Vector<SurfaceFormOfAffix> otherFormsOfAffixResultingFromAssimilationOfPlace = new Vector<SurfaceFormOfAffix>();
			  Vector<String> dialectalSpellingVariationsOfAffix = null;
			  try {
				  dialectalSpellingVariationsOfAffix = Dialect.dialectalSpellingVariations(affixCandidate, remainingStem);
			  } catch (LinguisticDataException e) {
				  throw new MorphologicalAnalyzerException(e);
			  }
			  for (int k = 0; k < dialectalSpellingVariationsOfAffix.size(); k++) {
				Vector<SurfaceFormOfAffix> formsOfAffixForAVariationSpelling = lookForForms(dialectalSpellingVariationsOfAffix.elementAt(k), USE_SYLLABICS);
				otherFormsOfAffixResultingFromAssimilationOfPlace.addAll(formsOfAffixForAVariationSpelling);
			}

            /*
             * BRANCHING POINT
             * Analyze the remaining stem on the basis of each possible affix candidate.
             */


			// 1. Do the analysis on the basis of the candidates from the original string.
			List<DecompositionState> analysesOfOriginal = analyzeWithCandidateAffixes(
                		formsOfAffixForOriginalSpelling,
                		remainingStem,
                        affixCandidate, states, preCond, transitivity,
                        positionAffix, morphParts, word, true);
            completeAnalysis.addAll(analysesOfOriginal);

            // 2. Do the analysis on the basis of the candidates from the dialectal differences.
			List<DecompositionState> analysesOfDialectal = analyzeWithCandidateAffixes(
                		otherFormsOfAffixResultingFromAssimilationOfPlace,
                        remainingStem, affixCandidate, states, preCond,
                        transitivity, positionAffix, morphParts, word, false);
            completeAnalysis.addAll(analysesOfDialectal);

            // Continue the analysis of the term by eating up another character backwards.
        }

        return completeAnalysis;
	}

	/**
	 * Look in the database for all the objects of the class SurfaceFormOfAffix associated with a string of characters
	 * representing an affix. Those objects describe an affix in different contexts with the contextual actions.
	 *
	 * @param term String the string to be looked up in the database
	 * @param syllabic boolean indicates whether the term is in syllabics (true) or roman alphabet (false)
	 * @return a Vector<SurfaceFormOfAffix> possibly empty of SurfaceFormOfAffix objects
	 */
    public Vector<SurfaceFormOfAffix> lookForForms(String term, boolean syllabic) throws MorphologicalAnalyzerException {
    	String[] cons = syllabic ? Lexicon.consonantsSyl : Lexicon.consonants;
    	Vector<SurfaceFormOfAffix> formsFound = new Vector<SurfaceFormOfAffix>();
        if (term.endsWith("*")) {
            Vector<SurfaceFormOfAffix> formsFoundForTermWithAddedConsonant;
            String termWithoutStar, termWithConsonant;
            termWithoutStar = term.substring(0, term.length() - 1);
			for (String con : cons) {
				termWithConsonant = termWithoutStar + con;
				try {
					formsFoundForTermWithAddedConsonant = Lexicon.lookForForms(termWithConsonant, syllabic);
				} catch (LinguisticDataException e) {
					throw new MorphologicalAnalyzerException(e);
				}
				formsFound.addAll(formsFoundForTermWithAddedConsonant);
			}
        } else {
			  try {
				  formsFound = Lexicon.lookForForms(term,syllabic);
			  } catch (LinguisticDataException e) {
				  throw new MorphologicalAnalyzerException(e);
			  }
		  }

        return formsFound;
    }



    /**
     * Validate each possible form of affix and continue analyzing the remaining stem.
     *
     */
	@SuppressWarnings("unchecked")
	private List<DecompositionState> analyzeWithCandidateAffixes(
		Vector<SurfaceFormOfAffix> formsOfAffixFound,
		String stem,
		String affixCandidateOrig,
		Graph.State[] states,
		Conditions preConds,
		String transitivity,
		int positionAffix,
		Vector<AffixPartOfComposition> morphParts,
		String word,
		boolean notResultingFromDialectalPhonologicalTransformation)
			throws TimeoutException, MorphologicalAnalyzerException, MorphologicalAnalyzerDoneException {

		Logger logger = LogManager.getLogger("MorphologicalAnalyzer_R2L.analyzeWithCandidateAffixes");
		List<DecompositionState> completeAnalysis = new ArrayList<DecompositionState>();

		String keyStateIDs = computeStateIDs(states);

		List<SurfaceFormOfAffix> contextualForms = new ArrayList<>(formsOfAffixFound);

		//---------------------------------------
		// For each contextual form of affix submitted:
		//---------------------------------------
		for (SurfaceFormOfAffix contextualForm : contextualForms) {

			stpw.check("analyzeWithCandidateAffixes -- contextualForm: "+contextualForm.form);

			Affix affix = null;
			try {
				 affix = (Affix) contextualForm.getAffix().copyOf();
			} catch (CloneNotSupportedException | LinguisticDataException e1) {
				throw new MorphologicalAnalyzerException(e1);
			}
			Graph.Arc[] arcsFollowed = null;
			MorphAnalyzerValidation.ContextualResult[] validStemAffixCombinationsInContext = null;
			boolean conditionsMet, transitivityMet, sameAffixAsNext, samePosition;

			boolean validate =
			false  // affix not found at the same position as previous one
					;
			try {
				validate = (arcsFollowed = arcsSuivis(affix, states, keyStateIDs)) != null && // type of affix is accepted at this point
					  (conditionsMet = affix.meetsConditions(preConds, morphParts)) && // relayed specific conditions are met
				(validStemAffixCombinationsInContext = agreeWithContextAndActions(affixCandidateOrig, affix, stem, // affix accepted in the context
						positionAffix, contextualForm,
						notResultingFromDialectalPhonologicalTransformation)) != null &&
					  (transitivityMet = affix.meetsTransitivityCondition(transitivity)) && // transitivity is fine
					  (sameAffixAsNext = !sameAsNext(affix, morphParts)) && // not the same affix as the previous one
					  (samePosition = !samePosition(positionAffix, morphParts));
			} catch (LinguisticDataException  e) {
				throw new MorphologicalAnalyzerException(e);
			}
			//--------------------- validation -------------------------------------------------------
			if (validate) {

			Graph.State[] nextPossibleStates = new Graph.State[arcsFollowed.length];
				 for (int i=0; i<arcsFollowed.length; i++) {
					  Graph.State dest =  ((Graph.Arc)arcsFollowed[i]).getDestinationState();
					  nextPossibleStates[i] = (Graph.State)dest.clone();
				 }

				 Conditions newConditionsToBeMetByNextMorpheme = affix.getPrecCond();

				 // TODO: this is not done properly; has to be modified.
			String affixTransitivity = affix.getTransitivityConstraint();
			String newTransitivity;
			if (transitivity==null || transitivity.equals("n") ) {
				newTransitivity = null;
			} else if (affixTransitivity==null || affixTransitivity.equals("n") ) {
				newTransitivity = transitivity;
			} else {
				newTransitivity = null;
			}
		//				logger.debug("*** affix: "+affix.morpheme+" ("+affix.id+")");
		//				logger.debug("    transitivity: "+transitivity);
		//				logger.debug("    affixTransitivity: "+affixTransitivity);
		//				logger.debug("    newTransitivity: "+newTransitivity);

			for (MorphAnalyzerValidation.ContextualResult validStemAffixCombinationInContext : validStemAffixCombinationsInContext) {
				stpw.check("decomposeByAffixes -- affixes respecting context and actions: " + validStemAffixCombinationInContext.stemBeforeAffixAction);
				Vector<AffixPartOfComposition> newMorphparts = (Vector<AffixPartOfComposition>) morphParts.clone();
				AffixPartOfComposition partIro = (AffixPartOfComposition) validStemAffixCombinationInContext.affixPartOfComposition;
				partIro.arcs = arcsFollowed;
				newMorphparts.add(0, partIro); // morceau ajouté
				List<DecompositionState> analyses = __decompose_simplified_term__((String) validStemAffixCombinationInContext.stemBeforeAffixAction,
						(String) validStemAffixCombinationInContext.stemAfterAffixAction, word,
						newMorphparts,
						nextPossibleStates,
						newConditionsToBeMetByNextMorpheme,
						newTransitivity
				);
				completeAnalysis.addAll(analyses);
			}
			} // if (validation)

		} // for

		return completeAnalysis;
    }




	private String computeStateIDs(State[] states) {
        String keyStateIDs = "0";
		for (State state : states) keyStateIDs += "+" + state.id;

        return keyStateIDs;
	}




    /*
	 * ===================== ANALYZE AS A ROOT ===========================
     */
    @SuppressWarnings("unchecked")
	private Vector<DecompositionState> analyzeAsRoot(String term, String termOrig,
		 String word, Vector<AffixPartOfComposition> morphParts, Graph.State[] states,
		 Conditions preConds,
		 String transitivity) throws TimeoutException, MorphologicalAnalyzerException, MorphologicalAnalyzerDoneException {

    	Logger logger = LogManager.getLogger("MorphologicalAnalyzer_R2L.analyzeAsRoot");
        Vector<DecompositionState> allAnalyses = new Vector<DecompositionState>();

        String termICI = Orthography.orthographyICI(term, USE_SYLLABICS); // de-simplify the term (re: NN>nng; N>ng); needed to search the database
        String termOrigICI = Orthography.orthographyICI(termOrig, USE_SYLLABICS);

        /*
         * Enlever le '*' à la fin du terme, s'il s'y trouve à la suite d'une
         * tentative pour trouver des analyses au cas où la consonne finale du
         * mot à analyser aurait été omise.
         */
        if (termOrigICI.endsWith("*"))
            termOrigICI = termOrigICI.substring(0,termOrigICI.length()-1);

        /*
         * À ce point-ci, nous sommes au début du terme. À cause de la
         * récursivité au point de branchement, le terme en question sera ce qui
         * précède tout affixe trouvé. Cela ira donc du mot entier à la racine
         * réelle, en passant par plusieurs termes intermédiaires.
         *
         * On vérifie si cette partie initiale du mot est une racine connue.
         *
         * Chercher le TERME dans les racines.
         */
        Vector<Morpheme> lexs = null;
        Vector<String> newRootCandidates = null;
        lexs = lookForBase(termICI, USE_SYLLABICS);
        /*
         * Il est possible qu'une différence de prononciation dialectale se
         * produise dans un groupe de consonnes à la frontière de deux suffixes.
         * Il faut vérifier si le suffixe trouvé précédemment commence par une
         * consonne et si le candidat racine finit par une consonne et si ce
         * groupe de deux consonnes correspond à un autre groupe de consonnes.
         *
         * On cherche aussi des groupes de consonnes équivalents à l'intérieur
         * de la racine candidate. Toutes les possibilités sont retenues.
         */
		 try {
			 newRootCandidates = Dialect.newRootCandidates(termICI);
		 } catch (LinguisticDataException e) {
			 throw new MorphologicalAnalyzerException(e);
		 }
		 if (newRootCandidates != null)
            for (int k = 0; k < newRootCandidates.size(); k++) {
//                stpw.check("analyzeAsRoot -- newRootCandidate: "+((Base)newRootCandidates.elementAt(k)).morpheme);
                Vector<Morpheme> tr = lookForBase((String) newRootCandidates.elementAt(k), USE_SYLLABICS);
                if (tr != null)
                    if (lexs == null)
                        lexs = (Vector<Morpheme>) tr.clone();
                    else {
                        lexs.addAll(tr);
                    }
            }
        Vector<DecompositionState> rootAnalyses = checkRoots(lexs,word,termOrigICI,morphParts,states,
                preConds,transitivity);

        allAnalyses.addAll(rootAnalyses);

        return allAnalyses;
    }

    public Vector<Morpheme> lookForBase(String termICI, boolean isSyllabic) throws MorphologicalAnalyzerException {
    	Vector<Morpheme> basesFound = null;
    	try {
			if (termICI.endsWith("*")) {
				String[] cons = isSyllabic ? Lexicon.consonantsSyl : Lexicon.consonants;

				basesFound = new Vector<Morpheme>();
				String termICIWithoutStar = termICI.substring(0, termICI.length() - 1);
				for (String con : cons) {
					String termICIWithConsonant = termICIWithoutStar + con;
					Vector<Morpheme> morphemesFoundForWordWithAddedConsonant = Lexicon.lookForBase(termICIWithConsonant, isSyllabic);
					if (morphemesFoundForWordWithAddedConsonant != null) {
						basesFound.addAll(morphemesFoundForWordWithAddedConsonant);
					}
				}
				if (basesFound.size() == 0)
					basesFound = null;
			} else {
				basesFound = Lexicon.lookForBase(termICI, isSyllabic);
			}
		} catch (LinguisticDataException e) {
    		throw new MorphologicalAnalyzerException(e);
		}

    	return basesFound;
    }

    /**
     * Compute the root morphemes corresponding to the given lexemes.
     * @param lexs Vector<Object>
     * @param word String
     * @param termOrigICI String
     * @param morphParts Vector<AffixPartOfComposition>
     * @param states Graph.State[]
     * @param preConds Conditions
     * @param transitivity String
     * @return Vector<Decomposition>
     * @throws TimeoutException
     */
	private Vector<DecompositionState> checkRoots(Vector<Morpheme> lexs, String word, String termOrigICI,
		Vector<AffixPartOfComposition> morphParts, Graph.State[] states, Conditions preConds,
 		String transitivity) throws TimeoutException, MorphologicalAnalyzerException, MorphologicalAnalyzerDoneException {

        Vector<DecompositionState> rootAnalyses = new Vector<DecompositionState>();

        char typeBase = 0;

        if (lexs == null) {
            // RACINE UNKNOWN !!!
            // Pour le moment, on ne fait rien. On ne fait que
            // créer un vecteur vide.
            lexs = new Vector<Morpheme>();
        }

        //-------------------------------------------
        // Pour chaque base possible du vecteur lexs:----------------
        //-------------------------------------------

        for (int ib = 0; ib < lexs.size(); ib++) {
            // Chaque élément de lexs est un ensemble Object []
            // {Integer,Base}.
            Base root = (Base) lexs.elementAt(ib);

            stpw.check("checkRoots -- morpheme: "+root.morpheme);

            typeBase = root.type.charAt(0);

            if (typeBase == '?') {
                /*
                 * Si la racine est inconnue, on ajoute simplement une nouvelle
                 * décomposition à la liste des décompositions. (Note: ceci
                 * n'est pas effectué puisqu'on a mis en commentaire plus haut
                 * le traitement des racines inconnues.)
                 */
                DecompositionState res = new DecompositionState(word, new RootPartOfComposition(
                        termOrigICI, root, transitivity, null), morphParts
                        .toArray(new AffixPartOfComposition[] {}));
                onNewDecompFound(res);
                rootAnalyses.add(res);
            } else {
                /*
                 * Si la racine est connue : vérifier la validité du candidat.
                 */
            	Graph.Arc arcFollowed = checkValidityOfRoot(root,states,morphParts,preConds,transitivity);

                 if (arcFollowed != null) {
                    /*
                     * Toutes les conditions ont été respectées. Créer une
                     * nouvelle décomposition avec cette racine et les morphParts
                     * trouvés jusqu'ici.
                     */
                	Graph.Arc arc = arcFollowed.copy();
                    RootPartOfComposition mr = new RootPartOfComposition(
                            termOrigICI, root, transitivity, arc);
                    DecompositionState res = new DecompositionState(word, mr, morphParts
                            .toArray(new AffixPartOfComposition[] {}));
                    onNewDecompFound(res);
                    rootAnalyses.add(res);
                }
            }
        } // FOR

        return rootAnalyses;
    }

	private Graph.Arc checkValidityOfRoot(Morpheme root, Graph.State[] states,
		Vector<AffixPartOfComposition> morphParts, Conditions preConds,
		String transitivity) throws TimeoutException, MorphologicalAnalyzerException {
       	/* il faut vérifier si le type de la
         * racine correspond à un arc à partir de l'état actuel, et cet
         * arc doit conduire à l'état final (aucun arc partant de cet
         * état final). En principe, il ne devrait y avoir qu'un seul
         * arc accepté puisqu'on est rendu à la racine et qu'une racine
         * ne peut prendre qu'un seul arc.
         */

        /*
         * Il faut aussi vérifier que la finale de la base est une
         * lettre valide: une voyelle, un k, un q, un t.
         */
        //                          // *** Mis en quarantaine pour le moment ***
        //                                String motbase = racine.morpheme;
        //                                char dernierChar = motbase.charAt(motbase
        //                                        .length() - 1);
        //                                						if (dernierChar == 'a'
        //                                							|| dernierChar == 'i'
        //                                							|| dernierChar == 'u'
        //                                							|| dernierChar == 't'
        //                                							|| dernierChar == 'k'
        //                               							|| dernierChar == 'q') {

        /*
         * Il faut aussi que les conditions spécifiques soient
         * rencontrées. Il y a les conditions sur ce qui précède,
         * et les conditions sur ce qui peut suivre. Par exemple, si le suffixe
         * trouvé précédemment
         * exige de suivre immédiatement un nom au cas datif, le suffixe
         * ou la terminaison actuelle doit rencontrer cette contrainte.
         */
        /*
         * Vérifier si la transitivité imposée par le morphème trouvé
         * précédemment sur le radical, i.e. sur le morphème qui le
         * précède dans le mot (le candidat actuel) est respectée. Cette
         * valeur de transitivité imposée par le morphème trouvé est
         * indiquée dans le champ 'condTrans'.  Elle ne s'applique qu'aux
         * racines.
         */
        boolean accepted = false;

		String keyStateIDs = computeStateIDs(states);

        Graph.Arc arcFollowed = null;
        Graph.Arc[] arcsFollowed = arcsSuivis(root,states,keyStateIDs);
		if (arcsFollowed != null) {
			arcFollowed = arcToZero(arcsFollowed);
			if (arcFollowed != null) {
				boolean preConditionsMet = false;
				try {
					preConditionsMet = root.meetsConditions(preConds, morphParts);
				} catch (LinguisticDataException e) {
					throw new MorphologicalAnalyzerException(e);
				}
				if (preConditionsMet) {
					Conditions postConds = root.getNextCond();
					boolean postConditionsMet = true;
					if (morphParts.size()!=0) {
						try {
							postConditionsMet = morphParts.firstElement().getAffix().meetsConditions(postConds);
						} catch (LinguisticDataException e) {
							throw new MorphologicalAnalyzerException(e);
						}
					}
					if (postConditionsMet) {
						if (root.type.equals("v")) {
							boolean transitivityMet = root.meetsTransitivityCondition(transitivity);
							if (transitivityMet)
								accepted = true;
						} else {
							accepted = true;
						}
					}
				}
			}
        }

        return accepted?arcFollowed:null;
	}

	//-----------------------------------------------

	private Graph.Arc arcToZero(Graph.Arc[] arcsFollowed) throws TimeoutException {
		for (Graph.Arc arc : arcsFollowed) {
			stpw.check("arcToZero -- arc: " + arc.toString());
			if (arc.getDestinationState() == Graph.finalState) {
				return arc;
			}
		}
        return null;
    }


    /*
     * Vérifier si ce suffixe est le même que le dernier suffixe trouvé
     * précédemment. Cela permet d'éliminer certaines analyses, entre autres,
     * celles qui retournent le suffixe "a" d'action de groupe deux fois
     * lorsqu'on a un double "a" dans le mot.
     */
    private boolean sameAsNext(Morpheme morpheme, Vector<AffixPartOfComposition> partsAlreadyAnalyzed) throws MorphologicalAnalyzerException {
        boolean isSameAsNext = false;
        try {
			  if (partsAlreadyAnalyzed.size() != 0) {
				  Affix affPrec = ((AffixPartOfComposition) partsAlreadyAnalyzed.elementAt(0)).getAffix();
				  if (morpheme.id.equals(affPrec.id))
					  isSameAsNext = true;
			  }
		  } catch (LinguisticDataException e) {
        	throw new MorphologicalAnalyzerException(e);
		  }
        return isSameAsNext;
    }

    /*
     * Vérifier si ce suffixe est à la même position dans le mot que le
     * suffixe trouvé précédemment (celui qui le suit dans le mot dans l'analyse
     * courante). Cela permet d'éliminer certaines analyses, entre autres,
     * celles où le suffixe suivant, à cause de son action de suppression,
     * ajoute les caractères supprimés au radical, lesquels caractères sont
     * interprétés comme suffixe. Or un suffixe ne peut logiquement supprimer un
     * autre suffixe.
     */
    private boolean samePosition(int positionAffixInWord,
            Vector<AffixPartOfComposition> partsAlreadyAnalyzed) {
        boolean isAtSamePosition = false;
        if (partsAlreadyAnalyzed.size() != 0) {
            AffixPartOfComposition nextMorphpart = (AffixPartOfComposition) partsAlreadyAnalyzed
                .elementAt(0);
            if (nextMorphpart.getPosition() == positionAffixInWord)
                isAtSamePosition = true;
        }
        return isAtSamePosition;
    }


    /*
     * Vérifier si ce suffixe est permis en ce moment. Il doit
     * correspondre à un des arcs partant de l'état actuel.
     */

    private Graph.Arc[] arcsSuivis(Morpheme morpheme, Graph.State[] states,
		String keyStateIDs) throws TimeoutException, MorphologicalAnalyzerException {
		Graph.Arc[] arcsFollowed = null;
		try {
			String keyMorphemeStateIDs = morpheme.id + ":" + keyStateIDs;
			Graph.Arc[] arcsFollowedByHash = (Graph.Arc[]) arcsByMorpheme
			.get(keyMorphemeStateIDs);
			if (arcsFollowedByHash == null) {
				Vector<Graph.Arc> arcs = null;
				Vector<Graph.Arc> arcsFollowedV = new Vector<Graph.Arc>();
				for (State state : states) {
					stpw.check("arcsSuivis --- morpheme: " + morpheme.morpheme);
					arcs = state.verify(morpheme);
					arcsFollowedV.addAll(arcs);
				}
				if (arcsFollowedV.size() != 0) {
					arcsFollowed = (Graph.Arc[]) arcsFollowedV
					.toArray(new Graph.Arc[]{});
					arcsByMorpheme.put(keyMorphemeStateIDs, arcsFollowed);
				}
			} else {
				arcsFollowed = arcsFollowedByHash;
			}
		} catch (LinguisticDataException e) {
			throw new MorphologicalAnalyzerException(e);
		}
		return arcsFollowed;
	}



	/*
	* Vérifier si le contexte est respecté. La forme candidate
	* trouvée est associée à un contexte de radical et à des actions.
	* On vérifie si le radical et la forme de l'affixe correspondent à
	* ce contexte et à ces actions. Si c'est le cas, on retourne les
	* résultats possibles:
	*
	* a. radical sans les changements morphologiques causés par
	* l'affixe; b. un objet de classe MorceauAffixe contenant: 1. la
	* position de l'affixe dans le mot (la valeur de i); 2. un objet de
	* classe SurfaceFormOfAffix décrivant totalement l'affixe.
	*/
	private MorphAnalyzerValidation.ContextualResult[] agreeWithContextAndActions(
		String affixCandidateOrig,
		Affix affix,
		String stem,
		int positionAffixInWord,
		SurfaceFormOfAffix form,
		boolean notResultingFromDialectalPhonologicalTransformation)
	throws TimeoutException, MorphologicalAnalyzerException {
		MorphAnalyzerValidation.ContextualResult[] stemAffs = null;
		boolean checkStartOfConsonantsGroup = true;
		/*
		* Si la forme du candidat affixe est le résultat de changements
		* phonologiques, et si ces changements impliquent la consonne initiale,
		* on ne vérifiera pas la possibilité de changements phonologiques parce
		* qu'on ne veut pas que le groupe de consonne soit vérifié à nouveau.
		*/
		if (!notResultingFromDialectalPhonologicalTransformation) {
			if (Roman.isConsonant(form.form.charAt(0)) &&
					  Roman.isConsonant(affixCandidateOrig.charAt(0)) &&
					  form.form.charAt(0) != affixCandidateOrig.charAt(0) )
				 checkStartOfConsonantsGroup = false;
		}
		String context = (String) form.context;
		Action action1 = form.action1;
		Action action2 = form.action2;
		stemAffs = MorphAnalyzerValidation.validateContextActions(context, action1, action2,
				 stem, positionAffixInWord, affix, form, false,
				 checkStartOfConsonantsGroup,affixCandidateOrig);
		return stemAffs;
	}


	public static void removeFromCache(String word) {
		removeFromCache(word, null, null);
	}

	public static synchronized void removeFromCache(String word, Integer maxDecomps, Boolean extendedAnalyses) {
		if (extendedAnalyses == null) {
			extendedAnalyses = false;
		}

		String key = cacheKeyFor(word, maxDecomps, extendedAnalyses);

		decompsCache.invalidate(key);
	}


	private static String cacheKeyFor(String word, Integer maxDecomps, boolean extendedAnalyses) {
		String key = word;
		if (maxDecomps != null) {
			key += "/max="+maxDecomps;
		}
		if (extendedAnalyses) {
			key += "/extended";
		}
		return key;
	}

	/**
	 * Add decompositions to the list of decomps found so far.
	 */
	private void onNewDecompFound(DecompositionState newDecomp) throws MorphologicalAnalyzerDoneException {
		if (newDecomp.isComplete()) {
			_decompsSoFar.add(newDecomp);
		}
		if (_stopAfterNDecomps != null && _decompsSoFar.size() >= _stopAfterNDecomps) {
			// We are done.
			// Throw a MorphologicalAnalyzerDoneException so we stop processing
			// The exception will be caught at the level of doDecompose(), which will return
			// the list of decomps found so far.
			//
			throw new MorphologicalAnalyzerDoneException();
		}
	}
}