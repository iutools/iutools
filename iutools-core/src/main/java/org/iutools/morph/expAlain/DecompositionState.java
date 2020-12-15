package org.iutools.morph.expAlain;

import java.util.ArrayList;
import java.util.List;

import org.iutools.morph.Decomposition;

public class DecompositionState {
	
	public static enum Step {
		EXTEND_CHOICE_TREE, BACKTRACK, MOVE_DEEPEST_CURSOR, DONE, PROCESS_PARTIAL_DECOMP}
	
	String _wordToDecompose = null;
	public Step nextStep = null;
	public Step prevStep = null;
	
	/** Tree of possible choices for the morpheme sequence */
	List<List<WrittenMorpheme>> choiceTree = 
			new ArrayList<List<WrittenMorpheme>>();
	
	/** Position of the cursor at each level of the choice tree */
	List<Integer> cursors = new ArrayList<Integer>();
	
	List<Decomposition> allDecompositions = new ArrayList<Decomposition>();
	
	List<List<WrittenMorpheme>> _allDecomposition_asWrittenMorphemes =
		new ArrayList<List<WrittenMorpheme>>();

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

	public void onNewCompleteDecomposition() {
		List<WrittenMorpheme> decomp = currentDecomposition();
		_allDecomposition_asWrittenMorphemes.add(decomp);
	}

	private List<WrittenMorpheme> currentDecomposition() {
		List<WrittenMorpheme> decomp = new ArrayList<WrittenMorpheme>();
		for (int ii=0; ii < choiceTree.size(); ii++) {
			WrittenMorpheme morph = currentChoiceAtLevel(ii);
			decomp.add(morph);
		}
		return decomp;
	}

	private WrittenMorpheme currentChoiceAtLevel(int levelNum) {
		List<WrittenMorpheme> level = choiceTree.get(levelNum);
		WrittenMorpheme choice = null;
		int cursor = cursors.get(levelNum);
		if (cursor >= 0 && cursor < level.size()) {
			choice = level.get(cursor);
		}
		
		return choice;
	}

	public String remainingChars() {
		int totalMatched = 0;
		for (int ii=0; ii < choiceTree.size(); ii++) {
			totalMatched += choiceAtLevel(ii).writtenForm.length();
		}
		String remaining = _wordToDecompose.substring(totalMatched);
		return remaining;
	}

	public void extendChoiceTree(List<WrittenMorpheme> choices) {
		choiceTree.add(choices);
		setDeepestLevelCursor(-1);
	}
	
	private void setDeepestLevelCursor(int pos) {
		if (!choiceTree.isEmpty()) {
			cursors.add(choiceTree.size()-1, -1);
		}
	}

	public boolean moveDeepestLevelCursor() {
		boolean hadMoreOptions = true;
		if (choiceTree.isEmpty()) {
			hadMoreOptions = false;
		} else {
			int deepestLevel = choiceTree.size()-1;
			
			cursors.add(deepestLevel, cursors.get(deepestLevel) + 1);
			if (cursors.get(deepestLevel) >= choiceTree.get(deepestLevel).size()) {
				// We moved the cursor past the last option of the deepest level
				// in the choice tree
				//
				hadMoreOptions = false;
			}		
		}
		
		return hadMoreOptions;
	}

	public void removeDepeestLevel() {
		if (!choiceTree.isEmpty()) {
			choiceTree.remove(choiceTree.size()-1);
			cursors.remove(cursors.size()-1);
		}
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
			choice = choiceAtLevel(choiceTree.size()-1);
		}
		
		return choice;
	}
	
	public WrittenMorpheme choiceAtLevel(int levelNum) {
		WrittenMorpheme choice = null;
		if (choiceTree != null && choiceTree.size() > levelNum) {
			List<WrittenMorpheme> level = choiceTree.get(levelNum);
			int levelCursor = cursors.get(levelNum);
			if (levelCursor < level.size() && levelCursor >= 0) {
				choice = level.get(levelCursor);
			}
		}
		
		return choice;
		
	}

	public String toString() {
		StringBuilder builder = new StringBuilder();
		
		builder.append("  Word: ");
		builder.append(_wordToDecompose);
		builder.append("\n");

		builder.append("  Step (current, previous):\n");
		builder.append("    ");
		builder.append(nextStep);
		builder.append(", ");
		builder.append(prevStep);
		builder.append("\n");		
		
		toString_choiceTree(builder);
		
		toString_allDecompositions(builder);
		
		return builder.toString();
	}

	private void toString_choiceTree(StringBuilder builder) {
		builder.append("  Choices Tree:\n");
		if ( choiceTree.isEmpty()) {
			builder.append("    EMPTY\n");
		} else {
			for (int ii=0; ii < choiceTree.size(); ii++) {
				List<WrittenMorpheme> level = choiceTree.get(ii);
				int levelCurrChoice = cursors.get(ii);
				builder.append("    Level ");
				builder.append(ii);
				builder.append(": ");
				if (levelCurrChoice < 0) {
					builder.append(">><<");
				}
				builder.append("[");
				for (int cursor=0; cursor < level.size(); cursor++) {
					if (cursor == levelCurrChoice) {
						builder.append(">>");
					}
					builder.append(level.get(cursor));
					if (cursor == levelCurrChoice) {
						builder.append("<<");
					}
				}
				builder.append("]");
				
				if (levelCurrChoice >= level.size()) {
					builder.append(">><<");
				}
				builder.append("\n");
			}
		}
	}
	
	private void toString_allDecompositions(StringBuilder builder) {
		builder.append("  Current valid decompositions:\n");
		if (allDecompsAsWrittenMorphemes().isEmpty()) {
			builder.append("    EMPTY");
		} else {
			for (List<WrittenMorpheme> decomp: allDecompsAsWrittenMorphemes()) {
				builder.append("    [");
				for (WrittenMorpheme morph: decomp) {
					builder.append(morph);
					builder.append(", ");
				}
				builder.append("]\n");
			}
		}
		builder.append("\n");
	}	

	public List<List<WrittenMorpheme>> allDecompsAsWrittenMorphemes() {
		return _allDecomposition_asWrittenMorphemes;
	}
}
