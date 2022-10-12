package org.iutools.cli;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import ca.nrc.ui.commandline.CommandLineException;
import org.iutools.corpus.CompiledCorpus;
import org.iutools.corpus.CompiledCorpusRegistry;

import org.iutools.linguisticdata.Morpheme;
import org.iutools.morphemedict.MorphDictionaryEntry;
import org.iutools.morphemedict.MorphemeDictionary;
import org.iutools.morphemedict.MorphWordExample;

public class CmdLookForMorpheme extends ConsoleCommand {

	public CmdLookForMorpheme(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Return words containing morpheme X.";
	}

	@Override
	public void execute() throws Exception {
		String morpheme = getMorpheme(false);
		String corpusName = getCorpusName(true);

		CompiledCorpus compiledCorpus =
			new CompiledCorpusRegistry().getCorpus(corpusName);

		MorphemeDictionary morphExtr = new MorphemeDictionary();
		morphExtr.useCorpus(compiledCorpus);
		
		//morphExtr.useDictionary(dictionaryFile);
		
		boolean interactive = false;
		List<MorphDictionaryEntry> words = null;
		if (morpheme == null) {
			interactive = true;
		} else {
			words = morphExtr.search(morpheme);
		}

		while (true) {
			if (interactive) {
				morpheme = prompt("Enter Inuktut morpheme (nominal form)");
				if (morpheme == null) break;
				words = null;
				try {
					words = morphExtr.search(morpheme);
				} catch (Exception e) {
					throw e;
				}
			}
			
			if (words != null && words.size() > 0) {
				MorphemeDictionary.MorphExampleComparator comparator = morphExtr.new MorphExampleComparator();
				Iterator<MorphDictionaryEntry> itWords = words.iterator();
				int nIt = 1;
				while (itWords.hasNext()) {
					MorphDictionaryEntry wordsForMorpheme = itWords.next();
					String morphemeWithId = wordsForMorpheme.morphemeWithId;
					MorphWordExample[] wordsAndFreqs = wordsForMorpheme.words.toArray(new MorphWordExample[] {});
					Arrays.sort(wordsAndFreqs, comparator);

					String[] wordList = new String[wordsAndFreqs.length];
					for (int iWF=0; iWF<wordsAndFreqs.length; iWF++) {
						wordList[iWF] = wordsAndFreqs[iWF].word + "(" + wordsAndFreqs[iWF].getScore() + ")";
					}
					
					echo("\nMORPHEME ID: "+morphemeWithId+
							"        "+
							"\""+Morpheme.getMorpheme(morphemeWithId).englishMeaning+"\""+
							"               "+
							"["+wordList.length+" words]"+"\n");
					echo(String.join("; ", wordList));
				}
			}
			
			if (!interactive) break;				
		}

	}

}

