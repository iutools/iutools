package ca.pirurvik.iutools;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.config.IUConfig;
import ca.nrc.config.ConfigException;

public class CompiledCorpusRegistry {
	
	private static Map<String,CompiledCorpus> corpusCache = new HashMap<String,CompiledCorpus>();
	private static Map<String,String> registry = new HashMap<String,String>();
	
	static {
		try {
			String Hansard19992002_compilationFilePath = IUConfig.getIUDataPath("data/tries/trie_compilation-HANSARD-1999-2002---single-form-in-terminals.json");
			registry.put("Hansard1999-2002", Hansard19992002_compilationFilePath);
			registry.put("default", Hansard19992002_compilationFilePath);
		} catch (ConfigException e) {
		}
	}
	
	@JsonIgnore
	public static CompiledCorpus getCorpus() throws CompiledCorpusRegistryException {
		return getCorpus("default");
	}

	@JsonIgnore
	public static CompiledCorpus getCorpus(String corpusName) throws CompiledCorpusRegistryException {
		if (corpusName==null)
			corpusName = "default";
		CompiledCorpus corpus = null;
		if (corpusCache.containsKey(corpusName)) {
			corpus = corpusCache.get(corpusName);
		} else {
			if (corpusName.startsWith("FILE=") || registry.containsKey(corpusName)) {
				String filePath;
				if (corpusName.startsWith("FILE=")) {
					filePath = corpusName.replace("FILE=", "");
				} else {
					filePath = registry.get(corpusName);
				}
				corpus = makeCorpus(filePath);
				corpusCache.put(corpusName, corpus);
			} else {
				throw new CompiledCorpusRegistryException("Unknown corpus name: "+corpusName);
			}
		}
		
		return corpus;
	}
	
	@JsonIgnore
	public static CompiledCorpus getCorpus(File corpusJsonFPath) throws CompiledCorpusRegistryException {
		String corpusName = "FILE="+corpusJsonFPath.toString();
		return getCorpus(corpusName);
	}
	

	public static void addCorpus(String corpusName, String compilationFilePath) throws CompiledCorpusRegistryException {
		addCorpus(corpusName,compilationFilePath,false);
	}
	public static void addCorpus(String corpusName, String compilationFilePath, boolean makeItDefault) throws CompiledCorpusRegistryException {
		if (registry.containsKey(corpusName)) {
			if ( !registry.get(corpusName).equals(compilationFilePath) ) {
				throw new CompiledCorpusRegistryException("The name '"+corpusName+"' is already associated with a different compilation file.");
			}
		} else {
			registry.put(corpusName, compilationFilePath);
		}
		if (makeItDefault) {
			registry.put("default", compilationFilePath);
		}
	}
	

	
	
	private static CompiledCorpus makeCorpus(String corpusJsonFPath) throws CompiledCorpusRegistryException  {
		CompiledCorpus corpus = null;
		if (! new File(corpusJsonFPath).exists()) {
			throw new CompiledCorpusRegistryException("Did not find the corpus compilation file. Please retrieve it and place it in "+
					corpusJsonFPath+".");
		}
		try {
			corpus = CompiledCorpus.createFromJson(corpusJsonFPath);
			
		} catch (Exception e) {
			throw new CompiledCorpusRegistryException("Could not read compiled corpus from file: "+corpusJsonFPath, e);
		}
		
		return corpus;
	}
	
	public static Map<String,String> getRegistry() {
		return registry;
	}
	public static Map<String,CompiledCorpus> getCorpusCache() {
		return corpusCache;
	}

}
