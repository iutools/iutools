package ca.inuktitutcomputing.core.console;

import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;

public class CmdSegmentIU extends ConsoleCommand {

	public CmdSegmentIU(String name) {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Decompose an Inuktut word into its morphemes.";
	}

	@Override
	public void execute() throws Exception {
		String word = getWord(false);
		Decomposition[] decs = null;
		
		MorphologicalAnalyzer morphAnalyzer = new MorphologicalAnalyzer();

		boolean interactive = false;
		if (word == null) {
			interactive = true;
		} else {
			decs = morphAnalyzer.decomposeWord(word);
		}

		while (true) {
			if (interactive) {
				word = prompt("Enter Inuktut word");
				if (word == null) break;
				decs = null;
				try {
					decs = morphAnalyzer.decomposeWord(word);
				} catch (Exception e) {
					throw e;
				}
			}
			
			if (decs != null && decs.length > 0) {
				for (Decomposition dec : decs) {
					echo(dec.toStr2());
				}
			}
			
			if (!interactive) break;				
		}

	}

}
