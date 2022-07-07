package org.iutools.morph.l2rAlain;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import ca.nrc.config.ConfigException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.iutools.config.IUConfig;
import org.iutools.datastructure.trie.TrieException;
import org.iutools.datastructure.trie.TrieNode;
import org.iutools.datastructure.trie.Trie_InMemory;
import org.iutools.datastructure.trie.visitors.TrieNodeVisitor;
import org.iutools.linguisticdata.MorphemeException;
import org.iutools.morph.MorphologicalAnalyzerException;
import org.iutools.morph.l2r.SurfaceFormsHandler;

public class MorphemeWrittenForms {
	
	private static MorphemeWrittenForms _singleton = null;
	
	private Trie_InMemory root_trie = null;
	private Trie_InMemory affix_trie = null;

	Map<String,List<WrittenMorpheme>> surf2MatchedMorpheme =
			new HashMap<String,List<WrittenMorpheme>>();

	List<String> _surfaceFormsSorted = null;

	static ObjectMapper mapper = new ObjectMapper();
	
	private MorphemeWrittenForms() throws MorphologicalAnalyzerException {
		readSurfaceFormTries();

		try {
			TrieNodeVisitor visitor = new SurfaceFormsTrieVisitor(this);
			affix_trie.traverseNodes(visitor);
			root_trie.traverseNodes(visitor);
		} catch (TrieException e) {
			throw new MorphologicalAnalyzerException(e);
		}
		writeForms();
	}

	private void readSurfaceFormTries() throws MorphologicalAnalyzerException {
		Trie_InMemory[] tries = new Trie_InMemory[0];
		try {
			tries = SurfaceFormsHandler.loadSurfaceFormsTries();
		} catch (ConfigException | IOException e) {
			throw new MorphologicalAnalyzerException(
			"could not load the surface form tries", e);
		}
		root_trie = tries[0];
		affix_trie = tries[1];
	}

	private void writeForms() throws MorphologicalAnalyzerException {
		File surfFormsFile = surfaceFormsFile("all_forms.json").toFile();
		try(FileWriter fw = new FileWriter(surfFormsFile)) {
			Map<String,Object> obj = null;
			try {
				fw.write("bodyEndMarker=NEW_LINE\n");
				fw.write("class=java.util.Map\n\n");

				for (String aWrittenForm: this.surfaceForms()) {
					List<WrittenMorpheme> morphSet = this.surf2MatchedMorpheme.get(aWrittenForm);
					List<String> morphsSorted = sortWrittenMorphIDs(morphSet);
					obj = new HashMap<String,Object>();
					obj.put("surfaceForm", aWrittenForm);
					obj.put("morphIDs", morphsSorted);
					String json = mapper.writeValueAsString(obj);
					fw.write(json+"\n");
				}
			} catch (IOException e) {
				String mess = "Error raised while writing to surface forms file "+
					surfFormsFile;
				if (obj != null) {
					mess += "\nWas trying to write object "+obj;
				}
				throw new MorphologicalAnalyzerException(mess);
			}
		} catch (IOException e) {
			throw new MorphologicalAnalyzerException(
				"Could open surface forms file for writing: "+
				surfFormsFile.toString());
		}
	}

	private List<String> sortWrittenMorphIDs(Collection<WrittenMorpheme> writtenMorphs) {
		List<String> sortedIDs = new ArrayList<String>();
		for (WrittenMorpheme aMorpheme: writtenMorphs) {
			sortedIDs.add(aMorpheme.morphID);
		}
		Collections.sort(sortedIDs);

		return sortedIDs;
	}

	public static Path surfaceFormsFile() throws MorphologicalAnalyzerException {
		return surfaceFormsFile(null);
	}

	public static Path surfaceFormsFile(String fileName) throws MorphologicalAnalyzerException {
		Path filePath = null;

		String fileRelpath = "data/linguistic_data/morphemes_surface_forms.json";
		try {
			filePath = Paths.get(IUConfig.getIUDataPath(fileRelpath));
		} catch (ConfigException e) {
			throw new MorphologicalAnalyzerException(
				"Could not get surface forms data file "+fileRelpath,  e);
		}

		if (!filePath.toFile().exists()) {
			filePath.toFile().getParentFile().mkdirs();
			try {
				filePath.toFile().createNewFile();
			} catch (IOException e) {
				throw new MorphologicalAnalyzerException("Could not create surface forms file: "+filePath);
			}
		}

		return filePath;
	}

	public static synchronized MorphemeWrittenForms getInstance() 
			throws MorphologicalAnalyzerException {
		if (_singleton == null) {
			generateSingleton();
		}
		return _singleton;
	}

	/**
	 * Generate the singleton instance.
	 * 
	 * Note: The method is synchronized to prevent the possibility that two 
	 * threads will try to instantiate it at the same time.
	 * 
	 * @return
	 * @throws MorphologicalAnalyzerException
	 */
	private synchronized static void generateSingleton() 
			throws MorphologicalAnalyzerException {
		Logger tLogger = 
			LogManager.getLogger("ca.inukitutcomputing.morph.expAlain.MorphemeWrittenForms.generateSingleton");
		
		// Make sure that singleton has not already been created by another 
		// thread while we were waiting for the method 'sync'
		//
		if (_singleton == null) {
			_singleton = new MorphemeWrittenForms();
		}
	}

	private void addForm(WrittenMorpheme aForm) 
			throws MorphologicalAnalyzerException {
		Logger tLogger = LogManager.getLogger("org.iutools.morph.l2rAlain.MorphemeWritternForm.addForm");
		if (tLogger.isTraceEnabled()) {
			tLogger.trace("Adding form: " + aForm.toString());
		}
		if (aForm != null && aForm.writtenForm != null &&
			!aForm.writtenForm.isEmpty()) {
			updateAllSurfFormsRegex(aForm);
			updateSurf2MatchedMorphemeMap(aForm);
		}
	}

	private void updateAllSurfFormsRegex(WrittenMorpheme aForm) throws MorphologicalAnalyzerException {
		String regex = aForm.regex();
	}

	private void updateSurf2MatchedMorphemeMap(WrittenMorpheme aForm) {
		String written = aForm.writtenFormWithHeadConstraints();
		if (!surf2MatchedMorpheme.containsKey(written)) {
			surf2MatchedMorpheme
				.put(written, new ArrayList<WrittenMorpheme>());
		}

		List<WrittenMorpheme> matchingMorphemes = surf2MatchedMorpheme.get(written);
		if (!matchingMorphemes.contains(aForm)) {
			surf2MatchedMorpheme.get(written).add(aForm);
		}
	}
	
	/**
	 * Find all morphemes that can attach to a particular morpheme, and 
	 * whose surface form can match the start of a string.
	 * 
	 * @param attachTo
	 * @param matchSurfForm
	 * @return
	 * @throws MorphologicalAnalyzerException
	 */
	public List<WrittenMorpheme> morphemesThatCanFollow(
			WrittenMorpheme attachTo, 
			String matchSurfForm) throws MorphemeException {

		String[] matchFormsWithConstraint = new String[] {
			attachTo.resultsIn + matchSurfForm,
			"X" + matchSurfForm
		};
		int len = matchFormsWithConstraint[0].length();

		List<WrittenMorpheme> morphemes = new ArrayList<WrittenMorpheme>();
		while (len > 0) {
			for (String aFormWithConstraint: matchFormsWithConstraint) {
				String formToTry = aFormWithConstraint.substring(0, len);
				if (surf2MatchedMorpheme.containsKey(formToTry)) {
					morphemes.addAll(surf2MatchedMorpheme.get(formToTry));
				}
			}
			len--;
		}

		// Remove duplicate morphemes
		List<WrittenMorpheme> noDups = new ArrayList<WrittenMorpheme>();
		Set<String> alreadySeen = new HashSet<String>();
		for (WrittenMorpheme morph: morphemes) {
			if (!alreadySeen.contains(morph.toString())) {
				noDups.add(morph);
			}
			alreadySeen.add(morph.toString());
		}

		return noDups;
	}

	public List<String> surfaceForms() {
		if (_surfaceFormsSorted == null) {
			_surfaceFormsSorted = new ArrayList<String>();
			_surfaceFormsSorted.addAll(surf2MatchedMorpheme.keySet());

			//  We sort the list of allSurfRegexps so that the longest
			//  forms come first. That way, the resulting regexp will match
			//  the LONGEST possible form.
			//
			// In case of tie, we sort alphabetically, so forms are always
			// traversed in a predictable order
			//
			Collections.sort(_surfaceFormsSorted,
				(f1, f2) -> {
					int comp = f1.compareTo(f2);
					if (comp == 0) {
						comp = f1.compareTo(f2);
					}
					return comp;
				});
		}
		return _surfaceFormsSorted;
	}

	public static class SurfaceFormsTrieVisitor extends TrieNodeVisitor {

		private final MorphemeWrittenForms writtenFormsRegistry;

		public SurfaceFormsTrieVisitor(MorphemeWrittenForms _writtenFormsRegistry) {
			super();
			this.writtenFormsRegistry = _writtenFormsRegistry;
		}

		@Override
		public void visitNode(TrieNode node) throws TrieException {
			try {
				for (String writtenFormJson: node.getSurfaceForms().keySet()) {
					Map<String,String> fields = new HashMap<String,String>();
					fields = mapper.readValue(writtenFormJson, fields.getClass());
					WrittenMorpheme morphForm =
						new WrittenMorpheme(fields.get("morphemeId"), fields.get("surfaceForm"));
					writtenFormsRegistry.addForm(morphForm);
				}
			} catch (RuntimeException | IOException | MorphologicalAnalyzerException | MorphemeException e) {
				throw new TrieException(e);
			}
		}
	}
}
