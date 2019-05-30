package ca.inuktitutcomputing.core.console;

import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import com.google.gson.Gson;

import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.inuktitutcomputing.data.Morpheme;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphInuk;
import ca.pirurvik.iutools.CompiledCorpus;
import ca.pirurvik.iutools.MorphemeExtractor;
import ca.pirurvik.iutools.MorphemeExtractor.Words;
import ca.pirurvik.iutools.SpellChecker;

public class CmdLookForMorpheme extends ConsoleCommand {

	public CmdLookForMorpheme(String name) {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Return words containing morpheme X.";
	}

	@Override
	public void execute() throws Exception {
		String morpheme = getMorpheme(false);
		//String dictionaryFilePathname = getDictFile(true);
		//File dictionaryFile = new File(dictionaryFilePathname);
		String compiledCorpusFilePath = getCompilationFile();
		FileReader fr = new FileReader(compiledCorpusFilePath);
		CompiledCorpus compiledCorpus = new Gson().fromJson(fr, CompiledCorpus.class);
		fr.close();
		
		MorphemeExtractor morphExtr = new MorphemeExtractor();
		morphExtr.useCorpus(compiledCorpus);
		
		//morphExtr.useDictionary(dictionaryFile);
		
		LinguisticDataSingleton.getInstance("csv");

		boolean interactive = false;
		List<MorphemeExtractor.Words> words = null;
		if (morpheme == null) {
			interactive = true;
		} else {
			words = morphExtr.wordsContainingMorpheme(morpheme);
		}

		while (true) {
			if (interactive) {
				morpheme = prompt("Enter Inuktut morpheme (nominal form)");
				if (morpheme == null) break;
				words = null;
				try {
					words = morphExtr.wordsContainingMorpheme(morpheme);
				} catch (Exception e) {
					throw e;
				}
			}
			
			if (words != null && words.size() > 0) {
				Iterator<MorphemeExtractor.Words> itWords = words.iterator();
				int nIt = 1;
				while (itWords.hasNext()) {
					MorphemeExtractor.Words wordsForMorpheme = itWords.next();
					String morphemeWithId = wordsForMorpheme.morphemeWithId;
					String[] wordsList = wordsForMorpheme.words.toArray(new String[] {});
					Arrays.sort(wordsList);
					echo("\nMORPHEME ID: "+morphemeWithId+
							"               "+
							"\""+Morpheme.getMorpheme(morphemeWithId).englishMeaning+"\""+
							"               "+
							"["+wordsList.length+" words]"+"\n");
					echo(String.join("; ", wordsList));
				}
			}
			
			if (!interactive) break;				
		}

	}

}
