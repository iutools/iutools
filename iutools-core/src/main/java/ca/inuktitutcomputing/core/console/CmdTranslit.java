package ca.inuktitutcomputing.core.console;

import java.io.File;

import ca.inuktitutcomputing.applications.TranslitDOCFileParagraphs;
import ca.inuktitutcomputing.script.TransCoder;

public class CmdTranslit extends ConsoleCommand {

	@Override
	public String getUsageOverview() {
		return "Transliterate Legacy inuktitut to Unicode.";
	}

	public CmdTranslit(String name) {
		super(name);
	}

	@Override
	public void execute() throws Exception {
		String inputFile = getInputFile(false);
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
			File file = new File(inputFile);
			if ( !file.exists() ) {
				echo("The file '"+inputFile+"' does not exist.");
				return;
			} else {
				TranslitDOCFileParagraphs.translit(inputFile);
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
