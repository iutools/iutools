package ca.pirurvik.iutools.corpus;

import ca.nrc.datastructure.trie.*;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class RW_CompiledCorpus_v2Mem extends RW_CompiledCorpus {
    protected static final String wordCharFname = "wordCharTrie.json";
    protected static final String charNgramsFname = "charNgrams.json";
    protected static final String morphNgramsFname = "morphNgramsTrie.json";

    @Override
    protected void writeCorpus(CompiledCorpus corpus, File savePath) throws CompiledCorpusException {
        CompiledCorpus_v2Mem memCorp = (CompiledCorpus_v2Mem)corpus;
        File corpusDir = memCorp.corpusDir;
        writeTrie((Trie_InMemory) memCorp.wordCharTrie, wordCharFile(memCorp));
        writeTrie((Trie_InMemory) memCorp.charNgramsTrie, charNgramsFile(memCorp));
        writeTrie((Trie_InMemory) memCorp.morphNgramsTrie, morphNgramsFile(memCorp));

        return;
    }

    protected void writeTrie(Trie_InMemory trie, File file) throws CompiledCorpusException {
        try {
            if (file.exists()) {
                backupTrieFile(file);
            }
            FileWriter fw = new FileWriter(file);
            gson.toJson(trie, fw);
            fw.flush();
            fw.close();
        } catch (IOException e) {
            throw new CompiledCorpusException("Error writing trie to JSON file "+file, e);
        }

        return;
    }

//    protected void writeTrie(Trie_InMemory trie, File file) throws CompiledCorpusException {
//        try {
//            if (file.exists()) {
//                backupTrieFile(file);
//            }
//            new Gson().writeValue(file, trie);
//        } catch (RW_TrieNodeException e) {
//            throw new CompiledCorpusException("Error writing trie to JSON file "+file, e);
//        }
//
//        return;
//    }
//

    private void backupTrieFile(File file) throws CompiledCorpusException {
        File backupFile = new File(file.toString()+".bak");
        try {
            Files.copy(file.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new CompiledCorpusException("Unable to backup trie file "+file, e);
        }
    }

    protected Trie_InMemory readTrie(File file)  throws CompiledCorpusException {
        Trie_InMemory value = null;
        if (fileIsEmpty(file)) {
            value = new Trie_InMemory();
        } else {
            try {
                JsonReader reader = new JsonReader(new FileReader(file));
                value = gson.fromJson(reader, Trie_InMemory.class);
            } catch (FileNotFoundException e) {
                throw new CompiledCorpusException("Error trie from JSON file", e);
            }
        }
        return value;
    }

    private boolean fileIsEmpty(File file) {
        boolean answer = true;
        if (file.exists() && file.length() > 0) {
            answer = false;
        }
        return answer;
    }

    @Override
    protected CompiledCorpus readCorpus(File savePath) throws CompiledCorpusException {
        CompiledCorpus_v2Mem corpus = new CompiledCorpus_v2Mem(savePath);
        if (savePath.exists()) {
            JsonReader reader = null;
            File file = null;

            try {
                file = wordCharFile(savePath);
                corpus.wordCharTrie = (Trie) readTrie(file);
                System.out.println("--** DONE reading trie");
            } catch (Exception e) {
                throw new CompiledCorpusException("Problem reading trie from JSON file "+file, e);
            }

            try {
                file = charNgramsFile(savePath);
                corpus.charNgramsTrie = (Trie) readTrie(file);
            } catch (Exception e) {
                throw new CompiledCorpusException("Problem reading trie from JSON file "+file, e);
            }

            try {
                file = morphNgramsFile(savePath);
                corpus.charNgramsTrie = (Trie) readTrie(file);
            } catch (Exception e) {
                throw new CompiledCorpusException("Problem reading trie from JSON file "+file, e);
            }
        }

        return corpus;
    }

    @Override
    protected CompiledCorpus newCorpus(File savePath) {
        return new CompiledCorpus_v2Mem(savePath);
    }

    protected File wordCharFile(CompiledCorpus_v2Mem corpus) {
        return wordCharFile(corpus.corpusDir);
    }

    protected File wordCharFile(File corpusDir) {
        File file = new File(corpusDir, wordCharFname);
        return file;
    }

    protected File charNgramsFile(CompiledCorpus_v2Mem corpus) {
        return charNgramsFile(corpus.corpusDir);
    }

    protected File charNgramsFile(File corpusDir) {
        File file = new File(corpusDir, charNgramsFname);
        return file;
    }

    protected File morphNgramsFile(CompiledCorpus_v2Mem corpus) {
        return morphNgramsFile(corpus.corpusDir);
    }

    protected File morphNgramsFile(File corpusDir) {
        File file = new File(corpusDir, morphNgramsFname);
        return file;
    }
}
