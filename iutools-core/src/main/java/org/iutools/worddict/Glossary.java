package org.iutools.worddict;

import ca.nrc.config.ConfigException;
import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.config.IUConfig;
import org.iutools.utilities.StopWatch;
import org.iutools.utilities.StopWatchException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** Human-generated glossary of en-iu terms
 *
 * For more info on how to use this class, see DOCUMENTATION tests in test case
 * GlossaryTest.
 * */

public class Glossary {

	public Map<String, List<GlossaryEntry>> term2entries =
		new HashMap<String,List<GlossaryEntry>>();

	private static Glossary _singleton = null;

	private static ObjectMapper mapper = new ObjectMapper();


	public Glossary() throws GlossaryException {
		return;
	}

	public static synchronized Glossary get() throws GlossaryException {
		if (_singleton == null) {
//			_singleton = loadFromCache();
			if (_singleton == null) {
				_singleton = loadFromGlossaryFiles();
			}
		}
		return _singleton;
	}

	private static Path cacheFilePath() throws GlossaryException {
		Path cacheFile = null;
		try {
			cacheFile = new IUConfig().workspaceFile("glossaryCache.json");
		} catch (ConfigException e) {
			throw new GlossaryException(e);
		}
		return cacheFile;
	}

	private static Glossary loadFromCache() throws GlossaryException {
		Logger logger = LogManager.getLogger("org.iutools.worddict.Glossary.loadFromCache");
		Glossary gloss = null;
		StopWatch sw = new StopWatch().start();
		try {
			Path cacheFile = cacheFilePath();
			logger.trace("cacheFile="+cacheFile);
			if (cacheFile.toFile().exists() && cacheFile.toFile().length() != 0L) {
				gloss = mapper.readValue(cacheFile.toFile(), Glossary.class);
			}
		} catch (IOException e) {
			throw new GlossaryException(e);
		}
		try {
			logger.trace("Loading took "+sw.totalTime(TimeUnit.SECONDS)+" secs");
		} catch (StopWatchException e) {
			e.printStackTrace();
		}
		return gloss;
	}

	private static Glossary loadFromGlossaryFiles() throws GlossaryException {
		Logger logger = LogManager.getLogger("org.iutools.worddict.Glossary.loadFromGlossaryFiles");
		StopWatch sw = new StopWatch().start();
		Glossary gloss = new Glossary();
		File[] glossFiles = glossFiles = glossFilesToLoad();
		for (File glossFile: glossFiles) {
			gloss.loadFile(glossFile);
		}
		saveToCache(gloss);

		try {
			logger.trace("Loading took "+sw.totalTime(TimeUnit.SECONDS)+" secs");
		} catch (StopWatchException e) {
			e.printStackTrace();
		}

		return gloss;
	}

	private static void saveToCache(Glossary gloss) throws GlossaryException {
		try {
			Path cacheFile = cacheFilePath();
			mapper.writeValue(cacheFile.toFile(), gloss);
		} catch (GlossaryException | IOException e) {
			throw new GlossaryException(e);
		}
		return;
	}

	private static File[] glossFilesToLoad() throws GlossaryException {
		String[] fileNames = new String[] {
//			"Dorais 1978",
//			"EDU 2000 (rev. 2019)",
//			"NAC Kadlun-Jone & Angalik (1996)",
//			"NAC Kublu (2005)",
//			"SCHNEIDER",
//			"tusaalanga",
			"wpGlossary",
		};
		File[] files = new File[fileNames.length];
		for (int ii=0; ii < files.length; ii++) {
			try {
				files[ii] = new IUConfig().glossaryFPath(fileNames[ii]+".gloss.json").toFile();
			} catch (ConfigException e) {
				throw new GlossaryException(e);
			}
		}
		return files;
	}


	private Glossary loadFile(File file) throws GlossaryException {
		try {
			ObjectStreamReader reader = new ObjectStreamReader(file);
			GlossaryEntry entry = null;
			while (true) {
				entry = (GlossaryEntry) reader.readObject();
				if (entry == null) {
					break;
				}
				onNewGlossaryEntry(entry);
			}
		} catch (IOException | ClassNotFoundException | ObjectStreamReaderException e) {
			throw new GlossaryException("Problem reading glossary file: "+file);
		}
		return this;
	}

	private void onNewGlossaryEntry(GlossaryEntry newEntry) {
		Logger logger = LogManager.getLogger("org.iutools.worddict.Glossary.onNewGlossaryEntry");
		if (logger.isTraceEnabled()) {
			logger.trace("Read glossary entry: "+newEntry);
		}
		for (String lang: newEntry.availableLanguages()) {
			for (String term: newEntry.termsInLang(lang)) {
				String key = keyFor(lang, term);
				if (!term2entries.containsKey(key)) {
					term2entries.put(key, new ArrayList<GlossaryEntry>());
				}
				List<GlossaryEntry> existingEntries = term2entries.get(key);
				existingEntries.add(newEntry);
			}
		}
	}

	private String keyFor(String lang, String term) {
		return lang+":"+term.toLowerCase();
	}

	public List<GlossaryEntry> entries4word(String lang, String word) {
		List<GlossaryEntry> entries = new ArrayList<GlossaryEntry>();
		String key = keyFor(lang,word);
		if (term2entries.containsKey(key)) {
			entries = term2entries.get(keyFor(lang,word));
		}

		return entries;
	}
}