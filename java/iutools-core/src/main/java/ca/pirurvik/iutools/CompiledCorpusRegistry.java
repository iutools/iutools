package ca.pirurvik.iutools;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.config.IUConfig;
import ca.nrc.config.ConfigException;

public class CompiledCorpusRegistry {
	
	private static Map<String,CompiledCorpus> registry = new HashMap<String,CompiledCorpus>();
	private static Map<String,String> corpusCompilationFPaths = new HashMap<String,String>();
	
	static {
		try {
			// Add newly compiled corpi here: define a name and specify the path
			// where the compilation file is located (should be in iutools-data)
			corpusCompilationFPaths.put("Hansard1999-2002", IUConfig.getIUDataPath("data/tries/trie_compilation-HANSARD-1999-2002---single-form-in-terminals.json"));
		} catch (ConfigException e) {
		}
	}
	
	@JsonIgnore
	public static CompiledCorpus getCorpus() throws CompiledCorpusRegistryException {
		return getCorpus("default");
	}

	@JsonIgnore
	public static CompiledCorpus getCorpus(String corpusName) throws CompiledCorpusRegistryException {
		CompiledCorpus corpus = null;
		if (registry.containsKey(corpusName)) {
			corpus = registry.get(corpusName);
		} else {
			if (corpusName.equals("default")) {
				corpus = makeCorpus();
				registry.put(corpusName, corpus);
			} else if (corpusName.startsWith("FILE=") || corpusCompilationFPaths.containsKey(corpusName)) {
				String filePath;
				String corpName;
				if (corpusName.startsWith("FILE=")) {
					String corpusJsonFPathAndName = corpusName.replace("FILE=", "");
					String[] pathAndName = corpusJsonFPathAndName.split(";NAME=");
					filePath = pathAndName[0];
					corpName = pathAndName[1];
				} else {
					corpName = corpusName;
					filePath = corpusCompilationFPaths.get(corpName);
				}
				corpus = makeCorpus(filePath,corpName);
				registry.put(corpName, corpus);
			} else {
				throw new CompiledCorpusRegistryException("Unknown corpus name: "+corpusName);
			}
		}
		
		return corpus;
	}
	
	@JsonIgnore
	public static CompiledCorpus getCorpus(File corpusJsonFPath, String corpusName) throws CompiledCorpusRegistryException {
		if (corpusName.equals("")) {
			throw new CompiledCorpusRegistryException("The name of the corpus is empty string.");
		}
		String corpName = "FILE="+corpusJsonFPath.toString()+";NAME="+corpusName;
		return getCorpus(corpName);
	}
	

	
	
	private static CompiledCorpus makeCorpus() throws CompiledCorpusRegistryException {
		String corpusJsonFPath;
		try {
			corpusJsonFPath = IUConfig.getIUDataPath("data/tries/trie_compilation-HANSARD-1999-2002---single-form-in-terminals.json");
			if (! new File(corpusJsonFPath).exists()) {
				throw new CompiledCorpusRegistryException("Did not find the Hansard corpus compilation file. Please download it and place it in "+
						corpusJsonFPath+". You can download the file from "+
						"https://www.dropbox.com/s/ka3cn778wgs1mk4/trie_compilation-HANSARD-1999-2002---single-form-in-terminals.json?dl=0");
			}
		} catch (ConfigException e) {
			throw new CompiledCorpusRegistryException(e);
		}
		
		return makeCorpus(corpusJsonFPath,"default");
	}

	private static CompiledCorpus makeCorpus(String corpusJsonFPath,String corpName) throws CompiledCorpusRegistryException  {
		CompiledCorpus corpus = null;
		if (! new File(corpusJsonFPath).exists()) {
			throw new CompiledCorpusRegistryException("Did not find the corpus compilation file. Please retrieve it and place it in "+
					corpusJsonFPath+".");
		}
		try {
			corpus = CompiledCorpus.createFromJson(corpusJsonFPath);
			corpus.setName(corpName);
			
		} catch (Exception e) {
			throw new CompiledCorpusRegistryException("Could not read compiled corpus from file: "+corpusJsonFPath, e);
		}
		
		return corpus;
	}

}
