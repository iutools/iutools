package ca.inuktitutcomputing.core.console;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.inuktitutcomputing.applications.Decompose;
import ca.inuktitutcomputing.data.LinguisticDataSingleton;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphInuk;
import ca.inuktitutcomputing.script.Syllabics;

public class CmdGist extends ConsoleCommand {

	public CmdGist(String name) {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Return the 'gist' of a series of inuktitut words.";
	}

	@Override
	public void execute() throws Exception {
		String content = getContent(false);
		String inputFile = getInputFile(false);
		String[] words = null;
		String latin = null;
		Decomposition[] decs = null;
		
		LinguisticDataSingleton.getInstance("csv");

		boolean interactive = false;
		if (content == null && inputFile == null) {
			interactive = true; // assume 'content' will be typed at prompt
		} else if (content != null) {
			words = content.split("\\s+");
		} else {
			File file = new File(inputFile);
			if ( !file.exists() ) {
				echo("The file '"+inputFile+"' does not exist.");
				return;
			} else {
				BufferedReader br = new BufferedReader(new FileReader(file));
				content = "";
				String line;
				while ( (line = br.readLine()) != null )
					content += " "+line;
				br.close();
				words =content.split("\\s+");
			}
		}
		
		Pattern pattern = Pattern.compile("^(.+?)---(.+)$");
		
		while (true) {
			if (interactive) {
				content = prompt("Enter Inuktut words");
				if (content == null) break;
				words = content.split("\\s+");
			} else {
				echo("Processing: "+String.join(" ", words));
			}
			
			for (String word : words) {
				if (word.equals("")) continue;
				boolean syllabic = false;
				try {
					if (Syllabics.allInuktitut(word)) {
						latin = Syllabics.transcodeToRoman(word);
						syllabic = true;
					}
					else
						latin = word;
					decs = MorphInuk.decomposeWord(latin);
					if (decs != null && decs.length > 0) {
						Decomposition dec = decs[0];
						String[] meaningsOfParts = Decompose.getMeaningsInArrayOfStrings(dec.toStr2(),"en",true,false);
						ArrayList<String> wordGistParts = new ArrayList<String>();
						for (int i=0; i<meaningsOfParts.length; i++) {
							Matcher matcher = pattern.matcher(meaningsOfParts[i]);
							matcher.matches();
							wordGistParts.add(matcher.group(1)+": "+matcher.group(2));
						}
						String wordToDisplay = syllabic? word+" ("+latin.toUpperCase()+")" : latin.toUpperCase();
						String wordGist = "<<<< " +wordToDisplay.toUpperCase()+"= "+
								String.join(" || ", wordGistParts.toArray(new String[] {}))+">>>>";
						echo(wordGist+"  ",false);
					}
				} catch (Exception e) {
					throw e;
				}
			}
			
			if (!interactive) break;				
		}
		
		echo("");

	}

}
