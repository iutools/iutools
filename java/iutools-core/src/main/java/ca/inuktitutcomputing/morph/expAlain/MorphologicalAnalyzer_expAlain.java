package ca.inuktitutcomputing.morph.expAlain;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphInukException;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzerAbstract;
import ca.inuktitutcomputing.morph.expAlain.DecompositionState.Step;
import ca.inuktitutcomputing.morph.MorphInukException;

public class MorphologicalAnalyzer_expAlain 
				extends MorphologicalAnalyzerAbstract {

	public MorphologicalAnalyzer_expAlain() throws LinguisticDataException {
		super();
	}

	@Override
	public Decomposition[] decomposeWord(String word)
			throws TimeoutException, MorphInukException, 
			LinguisticDataException {
		DecompositionState finalState = decompose(word);
		List<Decomposition> decompsLst = finalState.allDecompositions;
		Decomposition[] decomps = 
			decompsLst.toArray(new Decomposition[decompsLst.size()]);
		
		return decomps;
	}

	@Override
	public Decomposition[] decomposeWord(String word, boolean extendedAnalysis)
			throws TimeoutException, MorphInukException, 
			LinguisticDataException, MorphInukException {
		DecompositionState finalState = decompose(word);
		List<Decomposition> decompsLst = finalState.allDecompositions;
		Decomposition[] decomps = 
			decompsLst.toArray(new Decomposition[decompsLst.size()]);
		
		return decomps;
	}
	
	private DecompositionState decompose(String word) 
			throws MorphInukException {
		
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
			throws MorphInukException {
		Logger tLogger = Logger.getLogger("ca.inukitutcomputing.morph.expAlain.doStep");
		
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("Upon entry, state is\n"+state.toString());
		}
		
		if (state.nextStep == Step.EXTEND_CHOICE_TREE) {
			doExtendChoiceTree(state);
		} else if (state.nextStep == Step.BACKTRACK) {
			doBacktrack(state);
		} else if (state.nextStep == Step.MOVE_DEEPEST_CURSOR) {
			doMoveDeepestCursor(state);
		} else if (state.nextStep == Step.PROCESS_PARTIAL_DECOMP) {
			doProcessPartialDecomp(state);
		}
		
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
		
		boolean isValid = true;
		Step next = null;
		{
			// Check if the sequence of choices currently made at each level 
			// is a valid sequence of morphemes. If not, set isValid = false;
			//
			// Code to be added later
		}
		
		// If we have matched the whole word, then add the current sequence of 
		// morphemes to the list of all analyses
		//
		if (isValid) {
			if (state.remainingChars().isEmpty()) {
				// Found a valid complete decomposition.
				// Add it to the list of all valid decompositions, then 
				state.acceptCurrentDecomposition();
				next = Step.MOVE_DEEPEST_CURSOR;
			} else {
				// Partial decomp is valid so far. 
				// Next, try to extend it 
				next = Step.EXTEND_CHOICE_TREE;
			}
		} else {
			// Partial decomp is not valid. Try next morpheme option for the
			// deepest level of the decomposition state
			//
			next = Step.MOVE_DEEPEST_CURSOR;
		}
		
		if (state.remainingChars().isEmpty()) {
			if (isValid) {
				
			}
			next = Step.MOVE_DEEPEST_CURSOR;
		} else {
			next = Step.MOVE_DEEPEST_CURSOR;
		}
		
		state.nextStep = next;
		
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
		boolean stop = false;
		while (stop) {
			// Keep removing the deepest level in the choice tree until we 
			// reach a level that still has some options
			//
			state.removeDepeestLevel();
			stop = state.moveDeepestLevelCursor();
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
	 * @throws MorphInukException 
	 */
	private void doExtendChoiceTree(DecompositionState state) 
			throws MorphInukException {
		state.extendChoiceTree(nextLevelChoices(state));
	}

	/**
	 * Generate a list of options for the next level in the choice tree.
	 * 
	 * @param state
	 * @return
	 * @throws MorphInukException 
	 */
	private List<WrittenMorpheme> nextLevelChoices(DecompositionState state) 
			throws MorphInukException {
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
}
