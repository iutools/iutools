package ca.inuktitutcomputing.core.console;

import java.io.FileReader;

import com.google.gson.Gson;

import ca.inuktitutcomputing.core.CompiledCorpus;
import ca.inuktitutcomputing.core.Reformulator;
import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphInuk;

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
			reformulations = reformulator.getReformulations(word);
		}

		while (true) {
			if (interactive) {
				word = prompt("Enter Inuktut word");
				if (word == null) break;
				reformulations = null;
				try {
					reformulations = reformulator.getReformulations(word);
				} catch (Exception e) {
					throw e;
				}
			}
			
			if (reformulations != null && reformulations.length > 0) {
				echo(String.join("; ", reformulations));
			}
			
			if (!interactive) break;				
		}

	}
	

}
