package ca.pirurvik.iutools;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.config.IUConfig;
import ca.nrc.config.ConfigException;

public class CompiledCorpusRegistry {
	
	private static Map<String,CompiledCorpus> registry = new HashMap<String,CompiledCorpus>();
	
	@JsonIgnore
	public static CompiledCorpus getCorpus() throws CompiledCorpusRegistryException {
		return getCorpus("");
	}

	@JsonIgnore
	public static CompiledCorpus getCorpus(File corpusJsonFPath) throws CompiledCorpusRegistryException {
		String corpName = "FILE="+corpusJsonFPath.toString();
		return getCorpus(corpName);
	}
	
	@JsonIgnore
	public static CompiledCorpus getCorpus(String corpusName) throws CompiledCorpusRegistryException {
		if (corpusName.equals("")) {
			corpusName = "default";
		}
		CompiledCorpus corpus = null;
		if (registry.containsKey(corpusName)) {
			corpus = registry.get(corpusName);
		} else {
			if (corpusName.equals("default")) {
				corpus = makeDefaultCorpus();
				registry.put(corpusName, corpus);
			} else {
				throw new CompiledCorpusRegistryException("Unknown corpus name: "+corpusName);
			}
		}
		
		return corpus;
	}

	private static CompiledCorpus makeDefaultCorpus() throws CompiledCorpusRegistryException  {
		CompiledCorpus corpus = null;
		String trieFPath;
		try {
			trieFPath = IUConfig.getIUDataPath("data/tries/trie_compilation-HANSARD-1999-2002---single-form-in-terminals.json");
		} catch (ConfigException e) {
			throw new CompiledCorpusRegistryException(e);
		}
		
		if (! new File(trieFPath).exists()) {
			throw new CompiledCorpusRegistryException("Did not find the large corpus compilation file. Please download it and place it in "+
					trieFPath+". You can download the file from "+
					"https://www.dropbox.com/s/ka3cn778wgs1mk4/trie_compilation-HANSARD-1999-2002---single-form-in-terminals.json?dl=0");
		}
		try {
			corpus = CompiledCorpus.createFromJson(trieFPath);
			
		} catch (Exception e) {
			throw new CompiledCorpusRegistryException("Could not read compiled corpus from file: "+trieFPath, e);
		}
			
		
		return corpus;
	}

}
