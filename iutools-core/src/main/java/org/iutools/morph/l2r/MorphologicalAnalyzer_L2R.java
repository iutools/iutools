package org.iutools.morph.l2r;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

import org.iutools.datastructure.trie.Trie;
import org.iutools.linguisticdata.*;
import org.iutools.morph.Decomposition;
import org.iutools.morph.MorphologicalAnalyzerAbstract;
import org.iutools.morph.MorphologicalAnalyzerException;
import ca.nrc.config.ConfigException;
import ca.nrc.datastructure.Pair;
import org.iutools.datastructure.trie.TrieException;
import org.iutools.datastructure.trie.TrieNode;
import org.iutools.datastructure.trie.Trie_InMemory;
import ca.nrc.json.PrettyPrinter;
import org.iutools.morph.r2l.StateGraphForward;

/*
 * Idée générale : à l'aide de structures Trie pour les formes de surface
 * des morphèmes
 */

public class MorphologicalAnalyzer_L2R extends MorphologicalAnalyzerAbstract {

	private Trie_InMemory root_trie = null;
	private Trie_InMemory affix_trie = null;

	public MorphologicalAnalyzer_L2R() throws TrieException, IOException, ConfigException {
		Trie_InMemory[] tries = SurfaceFormsHandler.loadSurfaceFormsTries();
		root_trie = tries[0];
		affix_trie = tries[1];
	}

	@Override
	protected Decomposition[] doDecompose(String word, Boolean lenient) throws MorphologicalAnalyzerException, TimeoutException {
		List<Decomposition> decomps = null;
		try {
			decomps = analyze(word);
		} catch (LinguisticDataException e) {
			throw new MorphologicalAnalyzerException(e);
		}

		return decomps.toArray(new Decomposition[0]);
	}

	/**
	 * Decomposition of an Inuktitut word into ints morphemic components.
	 *
	 * @param word String
	 * @return a List of DecompositionTree objects
	 * @throws LinguisticDataException
	 * @throws MorphologicalAnalyzerException
	 */
	List<Decomposition> analyze(String word) throws LinguisticDataException, MorphologicalAnalyzerException {
		/*
			For each root:
				for each affix:
					check if affix acceptable
					if affix is acceptable
						continue with next affix
		 */
		Logger logger = Logger.getLogger("MorphologicalAnalyzer_L2R.findAllPossibleSequencesOfMorphemes");

		List<DecompositionTree> decompositionTrees = decomposeWord_Tree(word);
		List<Decomposition> prunedDecompositions = combineMorphemes(decompositionTrees);

		return prunedDecompositions;
	}

	public List<DecompositionTree> decomposeWord_Tree(String word) throws MorphologicalAnalyzerException, LinguisticDataException {
		Logger tLogger = Logger.getLogger("org.iutools.morph.l2r.MorphologicalAnalyzer_L2R.decomposeWord");
		List<DecompositionTree> decompositionTrees = new ArrayList<DecompositionTree>();
		StateGraphForward.State initialStateOfAnalysis = StateGraphForward.initialState;
		List<String> possibleRoots = findRoot(word);
		tLogger.trace("possibleRoots = "+possibleRoots);
		Gson gson = new Gson();
		Iterator<String> iterRoot = possibleRoots.iterator();
		int i = 1;
		while (iterRoot.hasNext()) {
			String rootComponentInJson = iterRoot.next();
			tLogger.trace("Looking at Looking at rootComponentInJson="+rootComponentInJson);
			SurfaceFormInContext rootComponent = gson.fromJson(rootComponentInJson, SurfaceFormInContext.class);
			Morpheme morpheme = LinguisticData.getInstance().getMorpheme(rootComponent.morphemeId);
			StateGraphForward.State stateOfAnalysisAfterRoot = initialStateOfAnalysis.nextState(morpheme);
			String remainingPartOfWord = word.substring(rootComponent.surfaceForm.length());
			if (remainingPartOfWord.length()==0
					&& rootComponent.finalIsDifferentThanCanonical()) {
				tLogger.debug(rootComponent.morphemeId+" > rejeté – cause : no more chars and ending is not canonical");
				continue;
			}
			String stem = rootComponent.surfaceForm;
			tLogger.debug("-------------\nroot "+(i++)+". "+rootComponent.surfaceForm);
			List<DecompositionTree> list = analyzeRemainingForAffixes(stem,remainingPartOfWord,rootComponent,stateOfAnalysisAfterRoot);
			if (list != null) {
				DecompositionTree decTree = new DecompositionTree(rootComponent);
				decTree.addAllBranches(list);
				decompositionTrees.add(decTree);
			}
		}
		tLogger.trace("exiting");

		return decompositionTrees;
	}

	/**
	 * Convert a list of DecompositionTree objects into a list of Decomposition objects.
	 *
	 * @param decompositionTrees A List of DecompositionTree objects
	 * @return A List of Decomposition objects
	 */
	List<Decomposition> combineMorphemes(List<DecompositionTree> decompositionTrees) {
		Logger logger = Logger.getLogger("MorphologicalAnalyzer_L2R.combineMorphemes");
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
						//logger.debug("morphemeId: "+morphemeId);
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
		Logger logger = Logger.getLogger("MorphologicalAnalyzer_L2R.makeRegexp");
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
	    	Logger logger = Logger.getLogger("MorphologicalAnalyzer_L2R.DecompositionComparator.compare");
	    	logger.debug(a.decompSpecs +" VS "+b.decompSpecs);
	    	logger.debug(a.components().length+" VS "+b.components().length);
	    	if (a.components().length < b.components().length)
	    		return -1;
	    	else if (a.components().length > b.components().length)
				return 1;
	    	else {
		    	logger.debug(a.getSurfaceForm(a.components()[0])+" VS "+b.getSurfaceForm(b.components()[0]));
	    		if (a.getSurfaceForm(a.components()[0]).length() < b.getSurfaceForm(b.components()[0]).length())
	    			return 1;
	    		else if (a.getSurfaceForm(a.components()[0]).length() > b.getSurfaceForm(b.components()[0]).length())
	    			return -1;
	    		else {
	    			int lastComponentIndex = a.components().length-1;
		    		if (a.getMorphemeId(a.components()[lastComponentIndex]).length() < b.getMorphemeId(b.components()[lastComponentIndex]).length())
		    			return -1;
		    		else if (a.getMorphemeId(a.components()[lastComponentIndex]).length() > b.getMorphemeId(b.components()[lastComponentIndex]).length())
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
			SurfaceFormInContext precedingMorpheme,
			StateGraphForward.State stateOfAnalysisAfterPrecedingMorpheme) throws LinguisticDataException, MorphologicalAnalyzerException {
		Logger tLogger = Logger.getLogger("org.iutools.morph.l2r.MorphologicalAnalyzer_L2R.analyzeRemainingForAffixes");
		tLogger.trace("precedingMorpheme: "+precedingMorpheme.morphemeId);
		tLogger.trace("remainingPartOfWord: "+remainingPartOfWord);
		Gson gson = new Gson();
		List<DecompositionTree> fullList = null;
		
		if (remainingPartOfWord.length()==0) {
			fullList = new ArrayList<DecompositionTree>();
		}

		List<String> possibleAffixes = findAffix(remainingPartOfWord);
		tLogger.trace("Nb. possibleAffixes= "+possibleAffixes.size());
		
		Iterator<String> iterAffix = possibleAffixes.iterator();
		
		while (iterAffix.hasNext()) {
			String affixSurfaceForm = iterAffix.next();
			tLogger.trace("affixSurfaceForm= "+affixSurfaceForm);
			SurfaceFormInContext affixComponent;
			affixComponent = gson.fromJson(affixSurfaceForm, SurfaceFormInContext.class);
			List<DecompositionTree> list = processPossibleAffix(affixComponent,stem,remainingPartOfWord,precedingMorpheme,stateOfAnalysisAfterPrecedingMorpheme);
			if (list != null) {
				DecompositionTree decTree = new DecompositionTree(affixComponent);
				tLogger.trace("decTree: "+decTree.toStr());
				decTree.addAllBranches(list);
				if (fullList==null)
					fullList = new ArrayList<DecompositionTree>();
				fullList.add(decTree);
			}
		}
		
		return fullList;
	}
	

	private List<DecompositionTree> processPossibleAffix(
			SurfaceFormInContext affixComponent,
			String stem,
			String remainingPartOfWord,
			SurfaceFormInContext precedingMorpheme,
			StateGraphForward.State stateOfAnalysisAfterPrecedingMorpheme) throws LinguisticDataException, MorphologicalAnalyzerException {
		Logger tLogger = Logger.getLogger("org.iutools.morph.l2r.MorphologicalAnalyzer_L2R.processPossibleAffix");
		tLogger.trace("\n-------\naffix: "+affixComponent);
		tLogger.trace("remainingPartOfWord= '"+remainingPartOfWord+"'");

		String localRemainingPartOfWord = remainingPartOfWord.substring(affixComponent.surfaceForm.length());
		if (localRemainingPartOfWord.length()==0
				&& affixComponent.finalIsDifferentThanCanonical()) {
			tLogger.trace(affixComponent.morphemeId+" après "+stem+" > rejeté – cause : no more chars and ending is not canonical");
			return null;
		}

		Morpheme morpheme = LinguisticData.getInstance().getMorpheme(affixComponent.morphemeId);
		StateGraphForward.State nextStateAfterMorpheme = stateOfAnalysisAfterPrecedingMorpheme.nextState(morpheme);
//		boolean morphemeAcceptedInThisState = morphemeComponent.validateAssociativityWithPrecedingMorpheme(stateOfAnalysisAfterPrecedingMorpheme);
		boolean morphemeAcceptedInThisState = nextStateAfterMorpheme != null;
		if ( !morphemeAcceptedInThisState ) {
			tLogger.trace(affixComponent.morphemeId+" après "+stem+" > rejeté – cause : validateAssociativityWithPrecedingMorpheme a échoué");
			return null;
		}
		boolean componentContextIsValidated = affixComponent.validateWithStem(precedingMorpheme);
		if (!componentContextIsValidated) {
			tLogger.trace(affixComponent.morphemeId+" après "+stem+" > rejeté – cause : validateWithStem a échoué");
			return null;
		}
		boolean constraintsAreValidated = affixComponent.validateConstraints(precedingMorpheme);
		if (!constraintsAreValidated) {
			tLogger.trace(affixComponent.morphemeId+" après "+stem+" > rejeté – cause : valideConstraints a échoué");
			return null;
		}
		tLogger.trace(affixComponent.morphemeId+" après "+stem+" > *** accepté");
		String localStem = remainingPartOfWord.substring(0,affixComponent.surfaceForm.length());

		List<DecompositionTree> list = analyzeRemainingForAffixes(localStem,localRemainingPartOfWord,affixComponent, nextStateAfterMorpheme);
		tLogger.trace("\n>>>"+PrettyPrinter.print(list)+"\n\n");
		return list;
	}

	protected List<String> findRoot(String string) throws MorphologicalAnalyzerException {
		String[] chars = string.split("");
		List<String> possibleRoots = findMorpheme(chars, root_trie);

		return possibleRoots;
	}	
	
	protected List<String> findAffix(String string) throws MorphologicalAnalyzerException {
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
	 * @throws MorphologicalAnalyzerException 
	 */

	@SuppressWarnings("unchecked")
	private List<String> findMorpheme(String[] chars, Trie_InMemory trie) throws MorphologicalAnalyzerException {
		Logger logger = Logger.getLogger("MorphologicalAnalyzer_L2R.findMorpheme");
		List<Pair<String,String>> pairsOfRoots = new ArrayList<Pair<String,String>>();
		List<String> morphemeSurfaceFormsInContextInJsonFormat = new ArrayList<String>();
		ArrayList<String> currentKey = new ArrayList<String>();
		String currentChar = null;
		int charCounter;
		// Parse the morpheme
		for (charCounter = 0; charCounter < chars.length; charCounter++) {
			TrieNode terminalNodeForCurrentKeys = null;
			currentChar = chars[charCounter];
			currentKey.add(currentChar);
			logger.trace("currentKey: "+currentKey);
			try {
				terminalNodeForCurrentKeys = trie.node4keys(currentKey.toArray(new String[]{}), Trie.NodeOption.TERMINAL, Trie.NodeOption.NO_CREATE);
			} catch (TrieException e) {
				throw new MorphologicalAnalyzerException(e);
			}
			if (terminalNodeForCurrentKeys != null) {
				// this is a complete morpheme
				logger.trace("*** Found terminal node: "+terminalNodeForCurrentKeys.surfaceForm);
				Set<String> surfaceForms = terminalNodeForCurrentKeys.getSurfaceForms().keySet();
				Iterator<String> itsf = surfaceForms.iterator();
				String currentKeyAsString = currentKey.toString();
				while (itsf.hasNext()) {
					String surfaceForm = itsf.next();
					pairsOfRoots.add(new Pair<String,String>(currentKeyAsString,surfaceForm));
					morphemeSurfaceFormsInContextInJsonFormat.add(surfaceForm);
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
		MorphologicalAnalyzer_L2R analyzer = new MorphologicalAnalyzer_L2R();
		System.out.println("Nb. roots in trie = "+analyzer.root_trie.totalTerminals());
		System.out.println("Nb. affixes in trie = "+analyzer.affix_trie.totalTerminals());

		String word;
		while ( true ) {
			System.out.print("\nEnter a word (or $ to quit): ");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					System.in));


			try {
				word = reader.readLine();
				if (word.equals("$")) {
					break;
				}
				List<Decomposition> decompositions = analyzer.analyze(word);
				if (decompositions.size() == 0)
					System.out.println("Aucune décomposition");
				else for (int id = 0; id < decompositions.size(); id++)
					System.out.println(">>> " + decompositions.get(id).toStr());

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	


}
