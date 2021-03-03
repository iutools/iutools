package org.iutools.cli;

import ca.nrc.ui.commandline.CommandLineException;
import ca.nrc.ui.commandline.UserIO;
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.config.IUConfig;

import java.nio.file.Path;
import java.nio.file.Paths;

public class CmdLoadTranslationMemory extends ConsoleCommand {
	public CmdLoadTranslationMemory(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public void execute() throws Exception {
		Path tmFile = getInputFile(false);

		if (tmFile == null) {
			String tmFileStr = IUConfig.getIUDataPath("data/translation-memories/nrc-nunavut-hansard.tm.json");
			tmFile = Paths.get(tmFileStr);
		}

		UserIO userIO = getUserIO();
		if (!userIO.verbosityLevelIsMet(UserIO.Verbosity.Level1)) {
			userIO.setVerbosity(UserIO.Verbosity.Level1);
		}
		new TranslationMemory()
			.setUserIO(userIO)
			.loadFile(tmFile);

		return;
	}

	@Override
	public String getUsageOverview() {
		return "Load a translation memory from a JSON file.";
	}
}
