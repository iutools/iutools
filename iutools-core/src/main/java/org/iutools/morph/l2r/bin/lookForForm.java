package org.iutools.morph.l2r.bin;

import ca.nrc.config.ConfigException;
import org.iutools.datastructure.trie.Trie;
import org.iutools.datastructure.trie.TrieException;
import org.iutools.datastructure.trie.TrieNode;
import org.iutools.datastructure.trie.Trie_InMemory;
import org.iutools.morph.l2r.SurfaceFormsHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class lookForForm {
    private static Trie_InMemory root_trie = null;
    private static Trie_InMemory affix_trie = null;

    public static void main(String[] args) throws IOException, ConfigException {
        Trie_InMemory[] tries = SurfaceFormsHandler.loadSurfaceFormsTries();
        root_trie = tries[0];
        affix_trie = tries[1];

        String input;
        boolean stop = false;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        while ( !stop ) {
            System.out.print("\nEnter a string (or $ to quit): ");
            input = reader.readLine();
            if (input.equals("$")) {
                stop = true;
                continue;
            }
            String[] nodeKeys = input.split("");
            List<String> nodeKeysAsList = new ArrayList<>(Arrays.asList(nodeKeys));
//            nodeKeys.add(TrieNode.TERMINAL_SEG);
            try {
                TrieNode nodeAsRoot = root_trie.node4keys(nodeKeys, Trie.NodeOption.TERMINAL, Trie.NodeOption.NO_CREATE);
                if (nodeAsRoot != null)
                    System.out.println("\nRoot surface form: " + nodeAsRoot.getTerminalSurfaceForm());
                else
                    System.out.println("\nNo entry as a root form.");
                TrieNode nodeAsAffix = affix_trie.node4keys(nodeKeys, Trie.NodeOption.TERMINAL, Trie.NodeOption.NO_CREATE);
                if (nodeAsAffix != null) {
//                    System.out.println("\nAffix surface form: " + nodeAsAffix.getTerminalSurfaceForm());
                    HashMap<String,Long> mapOfSurfaceForms = nodeAsAffix.getSurfaceForms();
                    String[] surfaceForms = mapOfSurfaceForms.keySet().toArray(new String[]{});
                    System.out.println();
                    for (String surfaceForm : surfaceForms) {
                        System.out.println("Affix surface form: " + surfaceForm);
                    }
                } else
                    System.out.println("\nNo entry as an affix form.");
            } catch (TrieException e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
    }
}
