package ca.pirurvik.iutools;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.config.IUConfig;
import ca.nrc.config.ConfigException;

public class CompiledCorpusRegistry {
	
	private static Map<String,CompiledCorpus> corpusCache = new HashMap<String,CompiledCorpus>();
	private static Map<String,File> registry = new HashMap<String,File>();
	public static String defaultCorpusName = "Hansard1999-2002";
	
	static {
		try {
			String Hansard19992002_compilationFilePath = IUConfig.getIUDataPath("data/compiled-corpuses/compiled-corpus-HANSARD-1999-2002---single-form-in-terminals.json");
			registry.put("Hansard1999-2002", new File(Hansard19992002_compilationFilePath));
		} catch (ConfigException e) {
			throw new ExceptionInInitializerError(e);
		}
	}
	
	public static void registerCorpus(String corpusName, File jsonFile) throws CompiledCorpusRegistryException {
		if (registry.containsKey(corpusName) 
				&&  !registry.get(corpusName).equals(jsonFile) ) {
				throw new CompiledCorpusRegistryException("The name '"+corpusName+"' is already associated with a different compilation file.");
		} else if ( !jsonFile.exists() ) {
			throw new CompiledCorpusRegistryException("The file "+jsonFile.getAbsolutePath()+" does not exist.");
		} else {
			registry.put(corpusName, jsonFile);
		}
	}
	
	
	@JsonIgnore
	public static CompiledCorpus getCorpus() throws CompiledCorpusRegistryException {
		return getCorpus(defaultCorpusName);
	}

	@JsonIgnore
	public static CompiledCorpus getCorpus(String corpusName) throws CompiledCorpusRegistryException {
		if (corpusName==null) corpusName = defaultCorpusName;
		CompiledCorpus corpus = null;
		if (corpusCache.containsKey(corpusName)) {
			corpus = corpusCache.get(corpusName);
		} else {
			if (!registry.containsKey(corpusName)) {
				throw new CompiledCorpusRegistryException("Unknown corpus name: "+corpusName);
			} else {
				String jsonFilePath = registry.get(corpusName).toString();
				corpus = makeCorpus(jsonFilePath);
				corpusCache.put(corpusName, corpus);
			}
		}
		
		return corpus;
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
	
	public static Map<String,File> getRegistry() {
		return registry;
	}
	public static Map<String,CompiledCorpus> getCorpusCache() {
		return corpusCache;
	}

}
