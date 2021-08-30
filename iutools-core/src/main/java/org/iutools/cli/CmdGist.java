package org.iutools.cli;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.nrc.ui.commandline.CommandLineException;
import org.iutools.bin.Decompose;
import org.iutools.morph.Decomposition;
import org.iutools.morph.r2l.MorphologicalAnalyzer_R2L;
import org.iutools.script.Syllabics;
import ca.nrc.ui.commandline.UserIO.Verbosity;

public class CmdGist extends ConsoleCommand {

	public CmdGist(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Return the 'gist' of a series of inuktitut words.";
	}

	@Override
	public void execute() throws Exception {
		String content = getContent(false);
		Path inputFile = getInputFile(false);
		Verbosity verbose = getVerbosity(); // 0: dense layout  1: pretty-print
		if (verbose==Verbosity.Level0)
			verbose = Verbosity.Level2;
		String[] words = null;
		String[] lines = null;
		String latin = null;
		Decomposition[] decs = null;
		
		MorphologicalAnalyzer_R2L morphAnalyzer = new MorphologicalAnalyzer_R2L();

		boolean interactive = false;
		if (content == null && inputFile == null) {
			interactive = true; // assume 'content' will be typed at prompt
		} else if (content != null) {
//			words = content.split("\\s+");
			lines = content.split("\n");
		} else {
			if ( !inputFile.toFile().exists() ) {
				echo("The file '"+inputFile+"' does not exist.");
				return;
			} else {
				ArrayList<String> arrayLines = new ArrayList<String>();
				BufferedReader br = new BufferedReader(new FileReader(inputFile.toFile()));
				content = "";
				String line;
				while ( (line = br.readLine()) != null ) {
					content += " "+line;
					arrayLines.add(line);
				}
				br.close();
				lines = arrayLines.toArray(new String[] {});
			}
		}

		Pattern pattern = Pattern.compile("^(.+?)---(.+)$");
		
		while (true) {
			if (interactive) {
				content = prompt("Enter Inuktut words");
				if (content == null) break;
				lines = content.split("\n");
			} else {
			}
			
			for (int iLine=0; iLine<lines.length; iLine++) {	
			
			String line = lines[iLine];
			words = line.split("\\s+");
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
					decs = morphAnalyzer.decomposeWord(latin);
					if (decs != null && decs.length > 0) {
						Decomposition dec = decs[0];
						String[] meaningsOfParts = Decompose.getMeaningsInArrayOfStrings(dec.toString(),"en",true,false);
						ArrayList<String> wordGistParts = new ArrayList<String>();
						for (int i=0; i<meaningsOfParts.length; i++) {
							Matcher matcher = pattern.matcher(meaningsOfParts[i]);
							matcher.matches();
							wordGistParts.add(matcher.group(1)+": "+matcher.group(2));
						}
						String[] wordParts = wordGistParts.toArray(new String[] {});
						String wordToDisplay = syllabic? word+" ("+latin.toUpperCase()+")" : latin.toUpperCase();
						String wordGist = generateGist(wordToDisplay,wordGistParts,verbose);
						if (verbose==Verbosity.Level1)
							echo(wordGist+"  ",false);
						else
							echo(wordGist+"\n\n",false);
					}
				} catch (Exception e) {
					throw e;
				}
			}
			if (iLine != lines.length-1 || interactive) echo("");
			}
			
			if (!interactive) break;				
		}
		
		echo("");

	}

	private String generateGist(String wordToDisplay, ArrayList<String> wordParts, Verbosity verbose) {
		if (verbose==Verbosity.Level1)
			return "<<<< " +wordToDisplay.toUpperCase()+"= "+String.join(" || ", wordParts)+">>>>";
		else {
			/*
			 * ᐋᓐᓂᐊᖃᕐᓇᙱᑦᑐᓕᕆᔨᒃᑯᑦ (AANNIAQARNANNGITTULIRIJIKKUT)
					sickness
					to have, to possess
					to prevent
					one who/something that does the action
					manipulation: 'to work with, on'
					one whose job is; agent
					group, family, people related to
			 */
			return wordToDisplay+"\n    "+String.join("\n    ", wordParts);
				
		}
	}

}
