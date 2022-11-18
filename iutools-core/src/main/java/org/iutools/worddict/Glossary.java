package org.iutools.worddict;

import ca.nrc.config.ConfigException;
import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import org.iutools.config.IUConfig;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Human-generated glossary of en-iu terms
 *
 * For more info on how to use this class, see DOCUMENTATION tests in test case
 * GlossaryTest.
 * */

public class Glossary {

	private Map<String, List<GlossaryEntry>> term2entries =
		new HashMap<String,List<GlossaryEntry>>();

	private static Glossary _singleton;
	static {
		try {
			_singleton = new Glossary();
		} catch (GlossaryException e) {
			throw new RuntimeException("Could not create singleton instance");
		}
	}

	public static Glossary get() {
		return _singleton;
	}

	private Glossary() throws GlossaryException {
		String[] glossFiles = null;
		try {
			glossFiles = new String[] {
				IUConfig.getIUDataPath("data/glossaries/wpGlossary.json")
			};
			for (String glossFile: glossFiles) {
				loadFile(new File(glossFile));
			}
		} catch (ConfigException e) {
			throw new GlossaryException("Problem reading glossary files "+String.join(", ", glossFiles), e);
		}
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
		for (String lang: newEntry.availableLanguages()) {
			for (String term: newEntry.termsInLang(lang)) {
				String key = keyFor(lang, term);
				if (!term2entries.containsKey(key)) {
					term2entries.put(key, new ArrayList<GlossaryEntry>());
				}
				term2entries.get(key).add(newEntry);
			}
		}
	}

	private String keyFor(String lang, String term) {
		return lang+":"+term;
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