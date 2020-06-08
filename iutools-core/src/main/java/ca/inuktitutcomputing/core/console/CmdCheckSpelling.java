package ca.inuktitutcomputing.core.console;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.pirurvik.iutools.corpus.CompiledCorpus_InMemory;
import ca.pirurvik.iutools.corpus.CompiledCorpusRegistry;
import ca.pirurvik.iutools.edit_distance.EditDistanceCalculatorFactory;
import ca.pirurvik.iutools.spellchecker.ScoredSpelling;
import ca.pirurvik.iutools.spellchecker.SpellChecker;
import ca.pirurvik.iutools.spellchecker.SpellingCorrection;

public class CmdCheckSpelling extends ConsoleCommand {

	public CmdCheckSpelling(String name) {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Return N words closest to submitted word.";
	}

	@Override
	public void execute() throws Exception {
		SpellChecker checker = new SpellChecker();

		String word = getWord(false);
		String compiledCorpusFilePathname = getCompilationFile(false);
		if (compiledCorpusFilePathname!=null) {
			File compiledCorpusFile = new File(compiledCorpusFilePathname);
			checker.setDictionaryFromCorpus(compiledCorpusFile);
		} else {
			String corpusName = getCorpusName(false);
			checker.setDictionaryFromCorpus(corpusName);
		}
		
		String maxCorrectionsOpt = getMaxCorr(false);
		int maxCorrections = maxCorrectionsOpt==null ? 5 : Integer.parseInt(maxCorrectionsOpt);
		EditDistanceCalculatorFactory.DistanceMethod editDistanceAlgorithm
				= getEditDistanceAlgorithm(false);
		
		if (editDistanceAlgorithm!=null)
			checker.setEditDistanceAlgorithm(editDistanceAlgorithm);
		System.out.println("Using edit distance algorithm "+checker.editDistanceCalculator.getClass().getName());
		
		SpellingCorrection corr = null;

		boolean interactive = false;
		if (word == null) {
			interactive = true;
		} else {
			corr = checker.correctWord(word, maxCorrections);
		}

		while (true) {
			if (interactive) {
				word = prompt("Enter Inuktut word");
				if (word == null) break;
				corr = null;
				try {
					corr = checker.correctWord(word, maxCorrections);
				} catch (Exception e) {
					throw e;
				}
			}
			
			List<ScoredSpelling> suggestions = corr.getScoredPossibleSpellings();
			if (suggestions != null && suggestions.size() > 0) {
				Iterator<ScoredSpelling> itSugg = suggestions.iterator();
				int nIt = 1;
				while (itSugg.hasNext()) {
					echo((nIt++)+". "+itSugg.next());
				}
			}
			
			if (!interactive) break;				
		}
	}
}
