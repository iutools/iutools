package ca.inuktitutcomputing.morph.expAlain;

import java.util.ArrayList;
import java.util.List;

import ca.inuktitutcomputing.morph.Decomposition;

public class DecompositionState {
	
	public static enum Step {
		EXTEND_CHOICE_TREE, BACKTRACK, MOVE_DEEPEST_CURSOR, DONE, PROCESS_PARTIAL_DECOMP}
	
	String _wordToDecompose = null;
	public Step nextStep = null;
	
	/** Tree of possible choices for the morpheme sequence */
	List<List<WrittenMorpheme>> choiceTree = 
			new ArrayList<List<WrittenMorpheme>>();
	
	/** Position of the cursor at each level of the choice tree */
	List<Integer> cursors = new ArrayList<Integer>();
	
	// Decomposition currently being built
	public Decomposition currDecomp = null;
	
	List<Decomposition> allDecompositions = new ArrayList<Decomposition>();

	public static DecompositionState initialState(String word) {
		DecompositionState state = new DecompositionState();
		state.setWord(word);
		
		return state;
	}

	private DecompositionState setWord(String word) {
		this._wordToDecompose = word;
		
		return this;
	}

	public List<Decomposition> getAllDecompositions() {
		return null;
	}

	public void acceptCurrentDecomposition() {
		allDecompositions.add(currDecomp);
	}

	public String remainingChars() {
		// TODO Auto-generated method stub
		return null;
	}

	public void extendChoiceTree(List<WrittenMorpheme> choices) {
		choiceTree.add(choices);
	}
	
	public boolean moveDeepestLevelCursor() {
		boolean hadMoreOptions = true;
		int deepestLevel = choiceTree.size();
		
		cursors.add(deepestLevel, cursors.get(deepestLevel) + 1);
		if (cursors.get(deepestLevel) >= choiceTree.get(deepestLevel).size()) {
			// We moved the cursor past the last option of the deepest level
			// in the choice tree
			//
			hadMoreOptions = false;
		}		
		
		return hadMoreOptions;
	}

	public void removeDepeestLevel() {
		choiceTree.remove(choiceTree.size()-1);
	}
	
	
	/**
	 * Returns the morpheme currently selected at the deepest level of the 
	 * choice tree.
	 * 
	 * If we are at the very beginning of the analysis, returns "1".
	 * 
	 * If cursor is passed the last option of the deepest level, returns null.
	 * 
	 * @return
	 */
	public WrittenMorpheme deepestChoice() {
		WrittenMorpheme choice = WrittenMorpheme.head;
		if (choiceTree != null && !choiceTree.isEmpty()) {
			choice = null;
			int depth = choiceTree.size();
			List<WrittenMorpheme> deepestLevel = choiceTree.get(depth);
			int deepestCursor = cursors.get(depth);
			if (deepestCursor < deepestLevel.size()) {
				choice = deepestLevel.get(deepestCursor);
			}
		}
		
		return choice;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("  Word: ");
		builder.append(_wordToDecompose);
		builder.append("\n");
		
		builder.append("  Step: ");
		builder.append(nextStep);
		builder.append("\n");
		
		
		builder.append("  Choices Tree:\n");
		if ( choiceTree.isEmpty()) {
			builder.append("    EMPTY\n");
		} else {
			for (int ii=0; ii < choiceTree.size(); ii++) {
				List<WrittenMorpheme> level = choiceTree.get(ii);
				builder.append("    Level ");
				builder.append(ii);
				builder.append(": [");
				int currentChoice = cursors.get(ii);
				for (int cursor=0; cursor < level.size(); cursor++) {
					if (cursor == currentChoice) {
						builder.append(">>");
					}
					builder.append(level.get(cursor));
					if (cursor == currentChoice) {
						builder.append("<<");
					}
				}
				builder.append("]");
				
				if (currentChoice >= level.size()) {
					builder.append(">><<");
				}
				builder.append("\n");
			}
		}		
		return builder.toString();
	}
}
