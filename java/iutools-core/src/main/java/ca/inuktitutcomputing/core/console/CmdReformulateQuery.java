package ca.inuktitutcomputing.core.console;

import java.io.FileReader;

import com.google.gson.Gson;

import ca.inuktitutcomputing.core.CompiledCorpus;
import ca.inuktitutcomputing.core.Reformulator;
import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphInuk;
import ca.inuktitutcomputing.script.Roman;
import ca.inuktitutcomputing.script.Syllabics;

public class CmdReformulateQuery extends ConsoleCommand {

	public CmdReformulateQuery(String name) {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Return a list of most useful reformulations of an inuktitut word.";
	}

	@Override
	public void execute() throws Exception {
		String word = getWord(false);
		String latin;
		String[] reformulations = null;
		
		String compilationFilePath = getCompilationFile();
		FileReader fr = new FileReader(compilationFilePath);
		CompiledCorpus compiledCorpus = new Gson().fromJson(fr, CompiledCorpus.class);
		fr.close();
		Reformulator reformulator = new Reformulator(compiledCorpus);
		
		LinguisticDataSingleton.getInstance("csv");

		boolean interactive = false;
		if (word == null) {
			interactive = true;
		} else {
			if (Syllabics.allInuktitut(word))
				latin = Syllabics.transcodeToRoman(word);
			else
				latin = word;
			reformulations = reformulator.getReformulations(word);
		}

		while (true) {
			if (interactive) {
				word = prompt("Enter Inuktut word");
				if (word == null) break;
				if (Syllabics.allInuktitut(word))
					latin = Syllabics.transcodeToRoman(word);
				else
					latin = word;
				reformulations = null;
				try {
					reformulations = reformulator.getReformulations(latin);
				} catch (Exception e) {
					throw e;
				}
			}
			
			if (reformulations != null && reformulations.length > 0) {
				if (Syllabics.allInuktitut(word)) {
					String[] syllRefs = new String[reformulations.length];
					for (int i=0; i<reformulations.length; i++)
						syllRefs[i] = Roman.transcodeToUnicode(reformulations[i], null);
					echo(String.join("; ", syllRefs)+"\n");
				}
				echo(String.join("; ", reformulations));
			}
			
			if (!interactive) break;				
		}

	}
	

}
