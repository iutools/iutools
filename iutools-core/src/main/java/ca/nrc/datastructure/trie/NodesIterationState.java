package ca.nrc.datastructure.trie;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * State of iteration through a Trie
 */
public class NodesIterationState {

    public List<String> nextNodeKeys;

    public static enum Step {
        EXTEND_CHOICE_TREE, BACKTRACK, MOVE_DEEPEST_CURSOR, DONE;
    }

    public Step nextStep = null;
    public Step prevStep = null;

    /** Tree of possible choices for the Node keys */
    List<List<String>> choiceTree =
            new ArrayList<List<String>>();

    /** Position of the cursor at each level of the choice tree */
    List<Integer> cursors = new ArrayList<Integer>();

    public NodesIterationState(String[] startKeys) {
        for (int ii=0; ii < startKeys.length; ii++) {
            List<String> singleChoice = new ArrayList<String>();
            singleChoice.add(startKeys[ii]);
            choiceTree.add(singleChoice);
            cursors.add(new Integer(0));
        }
        this.nextStep = Step.EXTEND_CHOICE_TREE;

    }

    List<String> currentNodeKeys() {
        List<String> keys = new ArrayList<String>();
        for (int ii=0; ii < choiceTree.size(); ii++) {
            String iiKey = currentChoiceAtLevel(ii);
            keys.add(iiKey);
        }
        return keys;
    }

    public void setCurrentNodeAsNext() {
        nextNodeKeys = currentNodeKeys();
    }

    protected String currentChoiceAtLevel(int levelNum) {
        List<String> level = choiceTree.get(levelNum);
        String choice = null;
        int cursor = cursors.get(levelNum);
        if (cursor >= 0 && cursor < level.size()) {
            choice = level.get(cursor);
        }

        return choice;
    }

    public void extendChoiceTree(List<String> choices) {
        nextNodeKeys = null;
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
        nextNodeKeys = null;
        if (!choiceTree.isEmpty()) {
            choiceTree.remove(choiceTree.size()-1);
            cursors.remove(cursors.size()-1);
        }
    }


    /**
     * Returns the key currently selected at the deepest level of the
     * choice tree.
     *
     * If we are at the very beginning of the analysis, returns "1".
     *
     * If cursor is passed the last option of the deepest level, returns null.
     *
     * @return
     */
    public String deepestChoice() {
        String choice = null;
        if (choiceTree != null && !choiceTree.isEmpty()) {
            choice = choiceAtLevel(choiceTree.size()-1);
        }

        return choice;
    }

    public String choiceAtLevel(int levelNum) {
        String choice = null;
        if (choiceTree != null && choiceTree.size() > levelNum) {
            List<String> level = choiceTree.get(levelNum);
            int levelCursor = cursors.get(levelNum);
            if (levelCursor < level.size() && levelCursor >= 0) {
                choice = level.get(levelCursor);
            }
        }

        return choice;

    }

    public boolean isTerminalNode() {
        boolean answer = (deepestChoice().equals(TrieNode.TERMINAL_SEG));
        return answer;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();

        builder.append("\n");

        builder.append("  Step (current, previous):\n");
        builder.append("    ");
        builder.append(nextStep);
        builder.append(", ");
        builder.append(prevStep);
        builder.append("\n");

        toString_choiceTree(builder);

        return builder.toString();
    }

    private void toString_choiceTree(StringBuilder builder) {
        builder.append("  Choices Tree:\n");
        if ( choiceTree.isEmpty()) {
            builder.append("    EMPTY\n");
        } else {
            for (int ii=0; ii < choiceTree.size(); ii++) {
                List<String> level = choiceTree.get(ii);
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
}
