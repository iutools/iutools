package ca.inuktitutcomputing.morph.exp;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import ca.inuktitutcomputing.data.LinguisticDataAbstract;
import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.inuktitutcomputing.data.SurfaceFormInContext;
import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.pirurvik.iutools.CompiledCorpusException;

public class appCreateFormTrees {

	public static void main(String[] args) throws Exception {
		String type = args[0]; // "root" or "affix"
		LinguisticDataSingleton.getInstance("csv");
		Trie trie = prepareTrie(type);
		saveTrieToFile(type, trie);
	}

	public static void saveTrieToFile(String type, Trie trie) throws Exception {
		FileWriter saveFileWriter = null;
		String saveFilePathname = "inuktitutFormTrie-" + type + ".json";
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
		System.out.println(type + "s " + "saved in " + saveFile.getAbsolutePath()+"; trie size: "+trie.getSize());
	}


	private static Trie prepareTrie(String type) throws FormGeneratorException {
		System.out.println("Preparation of "+type+" trie...");
		FormGenerator formGenerator = new FormGenerator();
		Gson gson = new Gson();
		String[] morphemeIds;
		if (type.equals("root"))
			morphemeIds = LinguisticDataAbstract.getAllBasesIds();
		else
			morphemeIds = LinguisticDataAbstract.getAllAffixesIds();

		Trie trie = new Trie();
		for (int i = 0; i < morphemeIds.length; i++) {
			try {
				String morphemeId = morphemeIds[i];
				System.out.println(String.format("%04d. ", i) + type+": " + morphemeId);
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

}
