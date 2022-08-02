package org.iutools.cli;

import ca.nrc.data.file.FileGlob;
import ca.nrc.ui.commandline.CommandLineException;
import ca.nrc.ui.commandline.UserIO;
import org.iutools.concordancer.tm.TMFactory;
import org.iutools.concordancer.tm.TranslationMemory;
import org.iutools.config.IUConfig;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class CmdLoadTranslationMemory extends ConsoleCommand {
	private Pattern fileRegex;
	private File tmFilesDir;

	public CmdLoadTranslationMemory(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public void execute() throws Exception {
		tmFilesDir = getInputDir();
		fileRegex = getFileRegexp();
		if (tmFilesDir == null) {
			tmFilesDir = new File(IUConfig.getIUDataPath("data/translation-memories/"));
		}

		UserIO userIO = getUserIO();
		if (!userIO.verbosityLevelIsMet(UserIO.Verbosity.Level1)) {
			userIO.setVerbosity(UserIO.Verbosity.Level1);
		}
		TranslationMemory tm = new TMFactory().makeTM()
			.setUserIO(userIO);
		
		List<File> files = tmFilesToLoad();
		if (files.size() > 0) {
			tm.delete();
		}
		for (File tmFile: files) {
			tm.loadFile(tmFile.toPath());
		}

		return;
	}

	private List<File> tmFilesToLoad() {
		List<File> toLoad = new ArrayList<File>();
		echo("TM files to load:");
		boolean atLeastOne = false;
		echo(1);
		try {
			File[] files = FileGlob.listFiles(tmFilesDir.toString()+"/*.tm.json");
			for (File aFile: files) {
				if (this.fileRegex == null ||
					this.fileRegex.matcher(aFile.toString()).find()) {
					toLoad.add(aFile);
					atLeastOne = true;
					echo(aFile.toString());
				}
			}
		} finally {
			if (! atLeastOne) {
				echo("None");
			}
			echo(-1);
		}
		return toLoad;
	}

	@Override
	public String getUsageOverview() {
		return "Load a translation memory from a JSON file.";
	}
}
