package org.iutools.morph.exp;

import ca.nrc.config.ConfigException;
import ca.nrc.file.ResourceGetter;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;
import com.google.gson.stream.JsonReader;
import org.apache.log4j.Logger;
import org.iutools.config.IUConfig;
import org.iutools.corpus.CompiledCorpusException;
import org.iutools.datastructure.trie.TrieException;
import org.iutools.datastructure.trie.Trie_InMemory;
import org.iutools.linguisticdata.LinguisticData;
import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.linguisticdata.SurfaceFormInContext;
import org.iutools.morph.exp.FormGenerator;
import org.iutools.morph.exp.FormGeneratorException;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class SurfaceFormsHandler {

    private static boolean verbose = false;

    /**
     * Compile a "trie" structure for all possible surface forms of the morpheme type given as argument.
     *
     * @param type a string indicating which morpheme type to compile, either "root" or "affix"
     * @return a "trie" structure
     * @throws FormGeneratorException
     */
    public static Trie_InMemory compileSurfaceFormsTrieForMorphemeType(String type) throws FormGeneratorException {
        Logger logger = Logger.getLogger("SurfaceFormsHandler.compileSurfaceFormsTrieForMorphemeType");
        logger.debug("type= "+type);
        if (verbose) System.out.println("Preparation of "+type+" trie...");
        FormGenerator formGenerator = new FormGenerator();
        Gson gson = new Gson();
        String[] morphemeIds;
        if (type.equals("root"))
            morphemeIds = LinguisticData.getInstance().getAllBasesIds();
        else
            morphemeIds = LinguisticData.getInstance().getAllAffixesIds();
        logger.debug("Nb. morphemes = "+morphemeIds.length);
        Trie_InMemory trie = new Trie_InMemory();
        for (int i = 0; i < morphemeIds.length; i++) {
            try {
                String morphemeId = morphemeIds[i];
                if (verbose) System.out.println(String.format("%04d. ", i) + type+": " + morphemeId);
                List<SurfaceFormInContext> morphemeFormsInContext = formGenerator.run(morphemeId);
                for (int irf = 0; irf < morphemeFormsInContext.size(); irf++) {
                    SurfaceFormInContext surfaceFormInContext = morphemeFormsInContext.get(irf);
                    trie.add(surfaceFormInContext.surfaceForm.split(""), gson.toJson(surfaceFormInContext));
                }
            } catch (TrieException | LinguisticDataException e) {
                throw new FormGeneratorException(e);
            }
        }

        return trie;
    }


    public static void saveTrieToFile(String type, Trie_InMemory trie) throws Exception {
        FileWriter saveFileWriter = null;
        String saveFilePathname = "inuktitutFormTrie2-" + type + ".json";
        File saveFile = new File(saveFilePathname);
        try {
            saveFileWriter = new FileWriter(saveFilePathname);
        } catch (IOException e1) {
            throw e1;
        }
        Gson gson = new Gson();
        try {
            gson.toJson(trie, saveFileWriter);
            saveFileWriter.flush();
            saveFileWriter.close();
        } catch (JsonIOException | IOException e) {
            throw new CompiledCorpusException(e);
        }
        if (verbose) System.out.println(type + "s " + "saved in " + saveFile.getAbsolutePath()+"; trie size: "+trie.totalTerminals());
    }

    public static Trie_InMemory[] loadSurfaceFormsTries() throws ConfigException, IOException {
        String affixFullPathname = getMorphemeTrieFilePath("inuktitutFormTrie-affix.json");
        String rootFullPathname = getMorphemeTrieFilePath("inuktitutFormTrie-root.json");
        JsonReader affixReader;
        affixReader = new JsonReader(new FileReader(affixFullPathname));
        JsonReader rootReader;
        rootReader = new JsonReader(new FileReader(rootFullPathname));
        Gson gson = new Gson();
        Trie_InMemory affix_trie = (Trie_InMemory)gson.fromJson(affixReader, Trie_InMemory.class);
        Trie_InMemory root_trie = (Trie_InMemory)gson.fromJson(rootReader, Trie_InMemory.class);
        return new Trie_InMemory [] { root_trie, affix_trie };
    }

    protected static String getMorphemeTrieFilePath(String fName) throws ConfigException, IOException {
        String filePath = "org/iutools/linguisticdata/surfaceForms/"+fName;
        String fullFilePath = ResourceGetter.getResourcePath(filePath);

        return fullFilePath;
    }


    public static void setVerbose(boolean value) {
        verbose = value;
    }

}
