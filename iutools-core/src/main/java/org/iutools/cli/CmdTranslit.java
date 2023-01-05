package org.iutools.cli;

import java.io.File;
import java.nio.file.Path;

import ca.nrc.ui.commandline.CommandLineException;
import org.iutools.bin.TranslitDOCFileParagraphs;
import org.iutools.script.TransCoder;

public class CmdTranslit extends ConsoleCommand {

	@Override
	public String getUsageOverview() {
		return "Transliterate Legacy inuktitut to Unicode.";
	}

	public CmdTranslit(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public void execute() throws Exception {
		Path inputFile = getInputFile(false);
		String content = getContent(false);
		String font = getFont(false);
				
		boolean interactive = false;
		if (content == null && inputFile == null) {
			interactive = true; // assume 'content' will be typed at prompt
			if (font==null) {
				echo("The Legacy font must be specified with the option -font.");
				return;
			}
		} else if (content != null) {
			if (font==null) {
				echo("The Legacy font must be specified with the option -font.");
				return;
			}
			String transliterated = TransCoder.legacyToUnicode(content,"tunngavik");
			echo(transliterated);
		} else {
			File file = inputFile.toFile();
			if ( !file.exists() ) {
				echo("The file '"+inputFile+"' does not exist.");
				return;
			} else {
				TranslitDOCFileParagraphs.translit(inputFile.toString());
			}
		} 
		
		while (true) {
			if (interactive) {
				content = prompt("Enter Inuktut text");
				if (content == null) break;
				String transliterated = TransCoder.legacyToUnicode(content,"tunngavik");
				echo(transliterated);
			} 

			if (!interactive) break;
		}
	}
}
