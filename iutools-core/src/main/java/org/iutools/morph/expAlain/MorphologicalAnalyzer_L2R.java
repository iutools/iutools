package org.iutools.morph.expAlain;

import java.util.List;

import org.apache.log4j.Logger;

import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.morph.MorphologicalAnalyzerException;
import org.iutools.morph.MorphologicalAnalyzerAbstract;
import org.iutools.morph.expAlain.DecompositionState.Step;

public class MorphologicalAnalyzer_L2R 
				extends MorphologicalAnalyzerAbstract {

	public MorphologicalAnalyzer_L2R() throws LinguisticDataException {
		super();
	}

//	@Override
//	public DecompositionSimple[] decomposeWord(String word)
//			throws TimeoutException, MorphologicalAnalyzerException,
//			LinguisticDataException {
//		DecompositionSimple finalState = decompose(word);
//		List<DecompositionSimple> decompsLst = finalState.allDecompositions;
//		DecompositionSimple[] decomps =
//			decompsLst.toArray(new DecompositionSimple[decompsLst.size()]);
//		
//		return decomps;
//	}

		
	@Override
	public org.iutools.morph.r2l.DecompositionState[] doDecompose(String word, Boolean extendedAnalysis)
		throws MorphologicalAnalyzerException {
		if (extendedAnalysis == null) {
			extendedAnalysis = true;
		}

		DecompositionState finalState;
		try {
			finalState = decompose(word);
		} catch (MorphologicalAnalyzerException e) {
			throw new MorphologicalAnalyzerException(e);
		}
		List<org.iutools.morph.r2l.DecompositionState> decompsLst = finalState.allDecompositions;
		org.iutools.morph.r2l.DecompositionState[] decomps =
			decompsLst.toArray(new org.iutools.morph.r2l.DecompositionState[decompsLst.size()]);
		
		return decomps;
	}
	
	DecompositionState decompose(String word)
			throws MorphologicalAnalyzerException {

		DecompositionState state = initState(word);
		while (state.nextStep != Step.DONE) {
			doStep(state);
		}
				
		return state;
	}

	DecompositionState initState(String word) {
		DecompositionState state = DecompositionState.initialState(word);
		state.nextStep = Step.EXTEND_CHOICE_TREE;
		return state;
	}
	
	private void doStep(DecompositionState state)
			throws MorphologicalAnalyzerException {
		Logger tLogger = Logger.getLogger("ca.inukitutcomputing.morph.expAlain.MorphologicalAnalyzer_L2R.doStep");
		
		Step step = state.nextStep;
		
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("Upon entry, state is\n"+state.toString());
		}
		
		if (step == Step.EXTEND_CHOICE_TREE) {
			doExtendChoiceTree(state);
		} else if (step == Step.BACKTRACK) {
			doBacktrack(state);
		} else if (step == Step.MOVE_DEEPEST_CURSOR) {
			doMoveDeepestCursor(state);
		} else if (step == Step.PROCESS_PARTIAL_DECOMP) {
			doProcessPartialDecomp(state);
		}
		
		state.prevStep = step;
		
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("Upon exit, state is\n"+state.toString());
		}
	}

	/**
	 * Process the partial decomposition currently being built, and determine 
	 * the next step.
	 * 
	 * @param state
	 * @return
	 */
	private boolean doProcessPartialDecomp(DecompositionState state) {
		Logger tLogger = Logger.getLogger("ca.inukitutcomputing.morph.expAlain.MorphologicalAnalyzer_L2R.doProcessPartialDecomp");
		
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("Upon entry, state is\n"+state.toString());
		}

		boolean isValid = true;
		Step next = null;
		
		isValid = checkLastTwoMorphemesValidity(state);
		
		String remainingChars = state.remainingChars();
		if (isValid && remainingChars.isEmpty()) {
			// Found a valid complete decomposition.
			//
			// Add it to the list of all valid decompositions, then
			// try next morpheme option at the deepest level of the choice tree.
			//
			if (tLogger.isTraceEnabled()) {
				tLogger.trace("Found complete, valid decomposition");
			}
			
			state.onNewCompleteDecomposition();
			next = Step.MOVE_DEEPEST_CURSOR;
		}
		
		if (next == null && isValid && !remainingChars.isEmpty()) {
			// Partial decomp is valid so far. 
			// Next, try to extend it 
			//
			next = Step.EXTEND_CHOICE_TREE;
		}
		
		if (next == null && !isValid) {
			// Partial decomp is NOT valid. 
			// Try next morpheme option at the deepest level of the choice tree.
			//
			next = Step.MOVE_DEEPEST_CURSOR;
		}
				
		state.nextStep = next;
		
		return isValid;
	}

	private boolean checkLastTwoMorphemesValidity(DecompositionState state) {
		boolean isValid = true;
		int depth = state.choiceTree.size();
		if (depth > 1) {
			WrittenMorpheme lastMorpheme = state.choiceAtLevel(depth);
			WrittenMorpheme prevMorpheme = state.choiceAtLevel(depth-1);
			isValid = 
				MorphoPhonoRules.getInstance()
					.canJoin(prevMorpheme, lastMorpheme);
		}
		
		return isValid;
	}

	/**
	 * Change the morpheme selected for the deepest level of the decomp
	 * currently being built.
	 * 
	 * @param state
	 */
	private void doMoveDeepestCursor(DecompositionState state) {
		Step next = null;
		boolean success = state.moveDeepestLevelCursor();
		if (success) {
			// The deepest level still had one option. 
			// Next step is to process the new partial decomposition obtained
			// by thus moving the cursor
			//
			next = Step.PROCESS_PARTIAL_DECOMP;
		} else {
			// The deepest level did not have any more options.
			// Next step is therefore to backtrack.
			//
			next = Step.BACKTRACK;
		}
		
		state.nextStep = next;
	}

	/**
	 * Backtrack in the choice tree, until we find a level that still has 
	 * some options.
	 * 
	 * @param state
	 */
	private void doBacktrack(DecompositionState state) {
		while (true) {
			// Keep removing the deepest level in the choice tree until we 
			// reach a level that still has some options
			//
			state.removeDepeestLevel();
			boolean deepestLevelHasMoreOptions = state.moveDeepestLevelCursor();
			if (state.choiceTree.isEmpty() || deepestLevelHasMoreOptions) {
				break;
			}
		}
		
		if (state.choiceTree.isEmpty()) {
			// We have backtracked to the very first level and there were 
			// no more choices there.
			//
			state.nextStep = Step.DONE;
		} else {
			state.nextStep = Step.PROCESS_PARTIAL_DECOMP;
		}
	}

	/**
	 * Add a new level in the choice tree.
	 * 
	 * @param state
	 * @throws MorphologicalAnalyzerException
	 */
	private void doExtendChoiceTree(DecompositionState state)
			throws MorphologicalAnalyzerException {
		Logger tLogger = Logger.getLogger("ca.inukitutcomputing.morph.expAlain.MorphologicalAnalyzer_L2R.doExtendChoiceTree");
		
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("Upon entry, state=\n"+state.toString());
		}
		
		state.extendChoiceTree(nextLevelChoices(state));
		state.nextStep = Step.MOVE_DEEPEST_CURSOR;
	}

	/**
	 * Generate a list of options for the next level in the choice tree.
	 * 
	 * @param state
	 * @return
	 * @throws MorphologicalAnalyzerException
	 */
	private List<WrittenMorpheme> nextLevelChoices(DecompositionState state)
			throws MorphologicalAnalyzerException {
		
		Logger tLogger = Logger.getLogger("ca.inukitutcomputing.morph.expAlain.MorphologicalAnalyzer_L2R.nextLevelChoices");
		
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("Upon entry, state=\n"+state.toString());
		}
		
		// TODO Generate a list of choices for the next level in the choice tree
		//
		// The choices should be morphemes that:
		// - Can attach to the deepestChoice() morpheme
		// - Has a written form that matches the begging of the remaining chars
		//   of the word (i.e. chars that have not yet been matched by the 
		//   current partial decomposition)
		//
		WrittenMorpheme attachTo = state.deepestChoice();
		String matchSurfForm = state.remainingChars();		
		List<WrittenMorpheme> nextChoices = MorphemeWrittenForms.getInstance().morphemesThatCanFollow(attachTo, matchSurfForm);
		
		return nextChoices;
	}

//	@Override
//	public DecompositionSimple[] decomposeWord(String word)
//			throws TimeoutException, MorphologicalAnalyzerException {
//		// TODO Auto-generated method stub
//		return null;
//	}

//	@Override
//	public DecompositionSimple[] decomposeWord(String word, Boolean extendedAnalysis)
//			throws TimeoutException, MorphologicalAnalyzerException {
//		// TODO Auto-generated method stub
//		return null;
//	}
}
