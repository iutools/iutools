package org.iutools.worddict;

import ca.nrc.config.ConfigException;
import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.config.IUConfig;
import org.iutools.text.IUWord;
import org.iutools.text.WordException;
import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.text.segmentation.Token;
import org.iutools.utilities.StopWatch;
import org.iutools.utilities.StopWatchException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public Set<String> allLanguages = new HashSet<>();

	private static IUTokenizer tokenizer = new IUTokenizer();

	static Pattern pattParseTermDescr = Pattern.compile("^((iu|en|fr):)(.*)$");

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
			"iutools-loanWords",
			"iutools-locations",
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

	public static Pair<String,String> parseTermDescription(String termDescr) throws GlossaryException {
		String lang = null;
		String term = null;
		Matcher matcher = pattParseTermDescr.matcher(termDescr);
		if (!matcher.find()) {
			term = termDescr;
		} else {
			if (!matcher.group(2).isEmpty()) {
				lang = matcher.group(2);
			}
			term = matcher.group(3);
		}

		return Pair.of(lang, term);
	}


	public Glossary loadFile(File file) throws GlossaryException {
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

	private void onNewGlossaryEntry(GlossaryEntry newEntry) throws GlossaryException {
		Logger logger = LogManager.getLogger("org.iutools.worddict.Glossary.onNewGlossaryEntry");
		if (logger.isTraceEnabled()) {
			logger.trace("Read glossary entry: "+newEntry);
		}

		if (isInAtLeastOneSupportedDialect(newEntry) && isValid_TEMPORARY(newEntry)) {
			for (String lang: newEntry.availableLanguages()) {
				allLanguages.add(lang);
				for (String term: newEntry.termsInLang(lang)) {
					String key = null;
					try {
						key = keyFor(lang, term);
					} catch (GlossaryException e) {
						throw new GlossaryException(e);
					}
					if (!term2entries.containsKey(key)) {
						term2entries.put(key, new ArrayList<GlossaryEntry>());
					}
					List<GlossaryEntry> existingEntries = term2entries.get(key);
					existingEntries.add(newEntry);
				}
			}
		}
	}

	private boolean isInAtLeastOneSupportedDialect(GlossaryEntry newEntry) {
		boolean answer = false;
		String [] availableDialects = newEntry.dialects;
		if (availableDialects == null) {
			answer = true;
		} else {
			for (String aDialect: availableDialects) {
				aDialect = aDialect.toLowerCase();
//				System.out.println("--** isInAtLeastOneSupportedDialect: aDialect="+aDialect);
				if (!aDialect.matches("(east greenland|inuinnaqtun|labrador|north greenland|nunatsiavummiutut|nattilingmiutut|paallirmiutut|west greenland)")) {
					answer = true;
					break;
				}
			}
		}
//		System.out.println("--** isInAtLeastOneSupportedDialect: returning answer="+answer);

		return answer;
	}

	private boolean isValid_TEMPORARY(GlossaryEntry newEntry) {
		Boolean valid = null;

		if (valid == null) {
			for (String lang : newEntry.availableLanguages()) {
				allLanguages.add(lang);
				for (String term : newEntry.termsInLang(lang)) {
					// Some glossaries provided by Stephane (Dorais, NAC Kadlun-Jone & Angalik) may contain some chars that
					// are not dealt with correctly by the Transcoder (ex: y, _).
					// For now, just ignore those words, until we get more info from Stephane about that char.
					//
					if (term.matches(".*[_].*")) {
						valid = false;
						break;
					}
				}
			}
		}

		if (valid == null) {
			valid = true;
		}
		return valid;
	}

	protected static String keyFor(String lang, String term) throws GlossaryException {
		String key = null;
		if (!lang.equals("iu")) {
			key = lang+":"+term.toLowerCase();
		} else {
			key = keyForIUTerm(term);
		}
		return key;
	}

	private static String keyForIUTerm(String term) throws GlossaryException {
		String key = null;
		try {
			key = "";
			tokenizer.tokenize(term);
			for (Token token: tokenizer.getAllTokens()) {
				if (token.isWord) {
					key += new IUWord(token.text).inRoman();
				} else {
					key += token.text;
				}
			}
		} catch (WordException e) {
			throw new GlossaryException(e);
		}
		key = "iu:"+key;
		return key;
	}

	public List<GlossaryEntry> entries4word(String lang, String word) throws GlossaryException {
		List<GlossaryEntry> entries = new ArrayList<GlossaryEntry>();
		String key = keyFor(lang,word);
		if (term2entries.containsKey(key)) {
			entries = term2entries.get(keyFor(lang,word));
		}

		return entries;
	}

	public Set<String> allTermDescriptions() throws GlossaryException {
		return allTermDescriptions((String)null);
	}

	public Set<String> allTermDescriptions(String inLang) throws GlossaryException {
		Set<String> terms = new HashSet<>();
		for (String termWithLang: term2entries.keySet()) {
			Boolean add = true;
			if (inLang != null) {
				try {
					Pair<String, String> parsed = parseTermDescription(termWithLang);
					if (parsed == null || !parsed.getLeft().equals(inLang)) {
						add = false;
					}
				} catch (GlossaryException e) {
					throw new GlossaryException(e);
				}
			}
			if (add) {
				terms.add(termWithLang);
			}
		}
		return terms;
	}

	public Set<String> allTerms() throws GlossaryException {
		return allTerms((String)null);

	}

	public Set<String> allTerms(String lang) throws GlossaryException {
		Set<String> terms = new HashSet<>();
		for (String termDescr: allTermDescriptions(lang)) {
			Pair<String,String> parsed = parseTermDescription(termDescr);
			String term = parsed.getRight();
			if (term != null && !term.isEmpty()) {
				terms.add(term);
			}
		}
		return terms;
	}
}