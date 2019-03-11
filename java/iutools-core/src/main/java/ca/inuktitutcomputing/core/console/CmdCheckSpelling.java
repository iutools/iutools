package ca.inuktitutcomputing.core.console;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import ca.inuktitutcomputing.core.SpellChecker;
import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphInuk;

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
		String word = getWord(false);
		String checkerFilePathname = getDictFile(true);
		File checkerFile = new File(checkerFilePathname);
		int maxCorrections = 5;
		
		List<String> suggestions = null;
		SpellChecker checker = new SpellChecker();
		checker.readFromFile(checkerFile);

		boolean interactive = false;
		if (word == null) {
			interactive = true;
		} else {
			suggestions = checker.correct(word, maxCorrections);
		}

		while (true) {
			if (interactive) {
				word = prompt("Enter Inuktut word");
				if (word == null) break;
				suggestions = null;
				try {
					suggestions = checker.correct(word, maxCorrections);
				} catch (Exception e) {
					throw e;
				}
			}
			
			if (suggestions != null && suggestions.size() > 0) {
				Iterator<String> itSugg = suggestions.iterator();
				int nIt = 0;
				while (itSugg.hasNext()) {
					echo(nIt+". "+itSugg.next());
				}
			}
			
			if (!interactive) break;				
		}

	}

}
