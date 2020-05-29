package ca.inuktitutcomputing.morph.exp;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import ca.inuktitutcomputing.config.IUConfig;
import ca.inuktitutcomputing.data.LinguisticData;
import ca.inuktitutcomputing.data.LinguisticDataAbstract;
import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.inuktitutcomputing.data.Morpheme;
import ca.inuktitutcomputing.data.SurfaceFormInContext;
import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.Pair;
import ca.nrc.datastructure.trie.Trie_InMemory;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode_InMemory;
import ca.nrc.file.ResourceGetter;
import ca.nrc.json.PrettyPrinter;

/*
 * Idée générale : à l'aide de structures Trie pour les formes de surface
 * des morphèmes
 */

public class WordAnalyzer {

	private Trie_InMemory root_trie = null;
	private Trie_InMemory affix_trie = null;	
	
	public WordAnalyzer() throws IOException, ConfigException {
		prepareTries();
	}
	
	private void prepareTries() throws ConfigException, FileNotFoundException {
		String affixFullPathname = getMorphemeTrieFilePath("iuFormTrie-affix.json");
		String rootFullPathname = getMorphemeTrieFilePath("iuFormTrie-root.json");
		JsonReader affixReader;
		affixReader = new JsonReader(new FileReader(affixFullPathname));
		JsonReader rootReader;
		rootReader = new JsonReader(new FileReader(rootFullPathname));
		Gson gson = new Gson();
		affix_trie = (Trie_InMemory)gson.fromJson(affixReader, Trie_InMemory.class);
		root_trie = (Trie_InMemory)gson.fromJson(rootReader, Trie_InMemory.class);
	}
	

	public List<Decomposition> analyze(String word) throws LinguisticDataException {
		
		List<Decomposition> decompositions = findAllPossibleDecompositions(word);
				
		return decompositions;		
	}
	
	/**
	 * This method takes care of the morphophonological aspect of the word.
	 * Forms and actions of affixes in the context of the stem's final are dealt with.
	 * @param word String
	 * @return a List of DecompositionTree objects
	 * @throws LinguisticDataException 
	 */

	List<Decomposition> findAllPossibleDecompositions(String word) throws LinguisticDataException {
		Logger logger = Logger.getLogger("WordAnalyzer.findAllPossibleSequencesOfMorphemes");
		List<DecompositionTree> decompositionTrees = new ArrayList<DecompositionTree>();
		
		List<String> possibleRoots = findRoot(word);	
		
		Gson gson = new Gson();
		Iterator<String> iterRoot = possibleRoots.iterator();
		int i = 1;
		while (iterRoot.hasNext()) {
			String rootComponentInJson = iterRoot.next();
			SurfaceFormInContext rootComponent = gson.fromJson(rootComponentInJson, SurfaceFormInContext.class);
			String remainingPartOfWord = word.substring(rootComponent.surfaceForm.length());
			String stem = rootComponent.surfaceForm;
			logger.debug("-------------\nroot "+(i++)+". "+rootComponent.surfaceForm);
			List<DecompositionTree> list = analyzeRemainingForAffixes(stem,remainingPartOfWord,rootComponent);
			if (list != null) {
//				logger.debug("branches: "+PrettyPrinter.print(list));
				DecompositionTree decTree = new DecompositionTree(rootComponent);
				decTree.addAllBranches(list);
				decompositionTrees.add(decTree);
			}
		}
		HashMap<String,String[]> combinedMorphemesInDecompositions = new HashMap<String,String[]>();
		List<Decomposition> allDecomps = new ArrayList<Decomposition>();
		for (int id=0; id<decompositionTrees.size(); id++) {
			DecompositionTree decompTree = decompositionTrees.get(id);
			List<Decomposition> decomps = decompTree.toDecomposition();
			for (int idd=0; idd<decomps.size(); idd++) {
				Decomposition decomp = decomps.get(idd);
				logger.debug("------- decomp= "+decomp.toStr());
				if (decomp.validateForFinalComponent()) {
					allDecomps.add(decomp);
					String[] morphemeIds = decomp.getMorphemes();
					for (int imid=0; imid<morphemeIds.length; imid++) {
						String morphemeId = morphemeIds[imid];
						logger.debug("morphemeId: "+morphemeId);
						Morpheme morpheme = LinguisticData.getInstance().getMorpheme(morphemeId);
						String[] morphemeCombinationElements = morpheme.getCombiningParts();
						if (morphemeCombinationElements!=null && morphemeCombinationElements.length!=0) {
							combinedMorphemesInDecompositions.put(morphemeId, morphemeCombinationElements);
						}
					}
				}
			}
		}
		
		List<Decomposition> prunedDecomps = checkForCombinedElements(allDecomps,combinedMorphemesInDecompositions);
		
		prunedDecomps.sort(new DecompositionComparator());
		
		return prunedDecomps;
	}
	
	List<Decomposition> checkForCombinedElements(
			List<Decomposition> decompositions,
			HashMap<String, String[]> combinedMorphemesInDecompositions) {
		List<Decomposition> prunedDecompositions = new ArrayList<Decomposition>();
		String[] regexps = new String[combinedMorphemesInDecompositions.size()];
		Set<String> keys = combinedMorphemesInDecompositions.keySet();
		int iregxp = 0;
		Iterator<String> itkeys = keys.iterator();
		while (itkeys.hasNext()) {
			String key = itkeys.next();
			String[] combinedElements = combinedMorphemesInDecompositions.get(key);
			String regexp = makeRegexp(combinedElements);
			regexps[iregxp++] = regexp;
		}
		for (int id=0; id<decompositions.size(); id++) {
			boolean reject = false;
			for (int iregexp=0; iregexp<regexps.length; iregexp++) {
				Pattern p = Pattern.compile(regexps[iregexp]);
				Matcher mp = p.matcher(decompositions.get(id).toStr());
				if (mp.find()) {
					reject = true;
					break;
				}
			}
			if (!reject)
				prunedDecompositions.add(decompositions.get(id));
		}
		
		return prunedDecompositions;
	}

	private String makeRegexp(String[] combinedElements) {
		Logger logger = Logger.getLogger("WordAnalyzer.makeRegexp");
		String[] regexps = new String[combinedElements.length];
		for (int i=0; i<combinedElements.length; i++) {
			String element = combinedElements[i];
			logger.debug("element: "+element);
			String regexp = "\\{.+?\\:"+element+"\\}";
			regexps[i] = regexp;
		}
		
		return String.join(" ", regexps);
	}

	class DecompositionComparator  implements Comparator<Decomposition> {
	    @Override
	    public int compare(Decomposition a, Decomposition b) {
	    	Logger logger = Logger.getLogger("WordAnalyzer.DecompositionComparator.compare");
	    	logger.debug(a.expression+" VS "+b.expression);
	    	logger.debug(a.components.length+" VS "+b.components.length);
	    	if (a.components.length < b.components.length)
	    		return -1;
	    	else if (a.components.length > b.components.length)
				return 1;
	    	else {
		    	logger.debug(a.getSurfaceForm(a.components[0])+" VS "+b.getSurfaceForm(b.components[0]));
	    		if (a.getSurfaceForm(a.components[0]).length() < b.getSurfaceForm(b.components[0]).length())
	    			return 1;
	    		else if (a.getSurfaceForm(a.components[0]).length() > b.getSurfaceForm(b.components[0]).length())
	    			return -1;
	    		else {
	    			int lastComponentIndex = a.components.length-1;
		    		if (a.getMorphemeId(a.components[lastComponentIndex]).length() < b.getMorphemeId(b.components[lastComponentIndex]).length())
		    			return -1;
		    		else if (a.getMorphemeId(a.components[lastComponentIndex]).length() > b.getMorphemeId(b.components[lastComponentIndex]).length())
		    			return 1;
		    		else {
		    			return 0;
		    		}
	    		}
	    	}
	    }
	}

	
	private List<DecompositionTree> analyzeRemainingForAffixes(
			String stem, 
			String remainingPartOfWord, 
			SurfaceFormInContext precedingMorpheme
			) throws LinguisticDataException {
		Logger logger = Logger.getLogger("WordAnalyzer.analyzeRemainingForAffixes");
		logger.debug("precedingMorpheme: "+precedingMorpheme.surfaceForm);
		logger.debug("remainingPartOfWord: "+remainingPartOfWord);
		Gson gson = new Gson();
		List<DecompositionTree> fullList = null;
		
		if (remainingPartOfWord.length()==0) {
			fullList = new ArrayList<DecompositionTree>();
		}

		List<String> possibleAffixes = findAffix(remainingPartOfWord);
		
		Iterator<String> iterAffix = possibleAffixes.iterator();
		
		while (iterAffix.hasNext()) {
			String affixComponentInJson = iterAffix.next();
			SurfaceFormInContext affixComponent = gson.fromJson(affixComponentInJson, SurfaceFormInContext.class);
			List<DecompositionTree> list = processPossibleAffix(affixComponent,stem,remainingPartOfWord,precedingMorpheme);
			if (list != null) {
				DecompositionTree decTree = new DecompositionTree(affixComponent);
				decTree.addAllBranches(list);
				if (fullList==null)
					fullList = new ArrayList<DecompositionTree>();
				fullList.add(decTree);
			}
		}
		
		return fullList;
	}
	

	private List<DecompositionTree> processPossibleAffix(
			SurfaceFormInContext morphemeComponent, 
			String stem,
			String remainingPartOfWord,
			SurfaceFormInContext precedingMorpheme
			) throws LinguisticDataException {
		Logger logger = Logger.getLogger("WordAnalyzer.processPossibleAffix");		
		logger.debug("\naffix: "+morphemeComponent.surfaceForm+"; "+morphemeComponent.morphemeId);
		
		boolean componentContextIsValidated = morphemeComponent.validateWithStem(precedingMorpheme);
		if (!componentContextIsValidated) {
			logger.debug("rejeté – cause : validateWithStem a échoué");
			return null;
		}
		boolean associativityWithStemIsValidated = morphemeComponent.validateWithPrecedingMorpheme(precedingMorpheme);
		if (!associativityWithStemIsValidated) {
			logger.debug("rejeté – cause : validateWithPrecedingMorpheme a échoué");
			return null;
		}
		boolean constraintsAreValidated = morphemeComponent.validateConstraints(precedingMorpheme);
		if (!constraintsAreValidated) {
			logger.debug("rejeté – cause : valideConstraints a échoué");
			return null;
		}
		logger.debug("*** accepté");
		String localRemainingPartOfWord = remainingPartOfWord.substring(morphemeComponent.surfaceForm.length());
		String localStem = remainingPartOfWord.substring(0,morphemeComponent.surfaceForm.length());

		List<DecompositionTree> list = analyzeRemainingForAffixes(localStem,localRemainingPartOfWord,morphemeComponent);
		logger.debug("\n>>>"+PrettyPrinter.print(list)+"\n\n");
		return list;
	}
	
	


	protected List<String> findRoot(String string) {
		String[] chars = string.split("");
		List<String> possibleRoots = findMorpheme(chars, root_trie);

		return possibleRoots;
	}	
	
	protected List<String> findAffix(String string) {
		List<String> possibleAffixes = new ArrayList<String>();
		if (string.length()!=0) {
			String[] chars = string.split("");
			possibleAffixes = findMorpheme(chars, affix_trie);
		}

		return possibleAffixes;
	}
	
	
	
	/**
	 * 
	 * @param chars String[] array of characters 
	 * @param trie Trie structure for database morphemes 
	 * @return List of Pair<String,String> of 
	 *   - surface form
	 *   - morpheme id
	 */

	@SuppressWarnings("unchecked")
	private List<String> findMorpheme(String[] chars, Trie_InMemory trie) {
		
		List<Pair<String,String>> pairsOfRoots = new ArrayList<Pair<String,String>>();
		List<String> morphemeSurfaceFormsInContextInJsonFormat = new ArrayList<String>();
		ArrayList<String> currentKeys = new ArrayList<String>();
		String currentChar = null;
		int charCounter;
		// Parse the morpheme
		for (charCounter = 0; charCounter < chars.length; charCounter++) {
			TrieNode_InMemory nodeForCurrentKeys = null;
			currentChar = chars[charCounter];
			currentKeys.add(currentChar);
			String currentKeysAsString = String.join("", currentKeys.toArray(new String[] {}));
			nodeForCurrentKeys = trie.getNode(currentKeys.toArray(new String[] {}));
			nodeForCurrentKeys = trie.getNode(currentKeys.toArray(new String[] {}));
			if (nodeForCurrentKeys == null) {
				// no morpheme 'currentKeys'
				break;
			} else {
				// there is a morpheme 'currentKeys'
				ArrayList<String> searchForSlashNodeKeys = (ArrayList<String>) currentKeys.clone();
				searchForSlashNodeKeys.add("\\");
				TrieNode_InMemory terminalNode = trie.getNode(searchForSlashNodeKeys.toArray(new String[] {}));
				if (terminalNode != null) {
				// this is a complete morpheme
					Set<String> surfaceForms = terminalNode.getSurfaceForms().keySet();
					Iterator<String> itsf = surfaceForms.iterator();
					while (itsf.hasNext()) {
						String surfaceForm = itsf.next();
						pairsOfRoots.add(new Pair<String,String>(currentKeysAsString,surfaceForm));
						morphemeSurfaceFormsInContextInJsonFormat.add(surfaceForm);
					}
				} 
			}
		}
		
		return morphemeSurfaceFormsInContextInJsonFormat;
	}
	
	
	
	/**
	 * MAIN
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		WordAnalyzer analyzer = new WordAnalyzer();

		System.out.print("Enter a word: ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				System.in));
		String word;
		
		
		try {
			word = reader.readLine();
			List<Decomposition> decompositions = analyzer.analyze(word);
			if (decompositions.size()==0)
				System.out.println("Aucune décomposition");
			else for (int id=0; id<decompositions.size(); id++)
				System.out.println(">>> "+decompositions.get(id).toStr());
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private String getMorphemeTrieFilePath(String fName) throws ConfigException  {
		String dirPath = IUConfig.getIUDataPath("data/LanguageData");
		Path trieFPath = Paths.get(dirPath, fName);
		
		return trieFPath.toString();
	}
	
	

}
