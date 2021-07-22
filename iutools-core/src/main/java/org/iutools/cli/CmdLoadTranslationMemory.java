package org.iutools.cli;

import ca.nrc.data.file.FileGlob;
import ca.nrc.ui.commandline.CommandLineException;
import ca.nrc.ui.commandline.UserIO;
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.config.IUConfig;

import java.io.File;

public class CmdLoadTranslationMemory extends ConsoleCommand {
	public CmdLoadTranslationMemory(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public void execute() throws Exception {
		File tmFilesDir = getInputDir();
		if (tmFilesDir == null) {
			tmFilesDir = new File(IUConfig.getIUDataPath("data/translation-memories/"));
		}

		UserIO userIO = getUserIO();
		if (!userIO.verbosityLevelIsMet(UserIO.Verbosity.Level1)) {
			userIO.setVerbosity(UserIO.Verbosity.Level1);
		}
		TranslationMemory tm =
			new TranslationMemory()
				.setUserIO(userIO);


		for (File tmFile: FileGlob.listFiles(tmFilesDir.toString()+"/*.tm.json")) {
			tm.loadFile(tmFile.toPath());
		}

		return;
	}

	@Override
	public String getUsageOverview() {
		return "Load a translation memory from a JSON file.";
	}
}
