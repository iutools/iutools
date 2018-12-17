package ca.inuktitutcomputing.core.console;

import java.util.Scanner;

import ca.nrc.ui.commandline.SubCommand;

public abstract class ConsoleCommand extends SubCommand {
	
	public static final String OPT_CORPUS_DIR = "corpus-dir";
	public static final String OPT_CORPUS_NAME = "corpus-name";
	public static final String OPT_COMP_FILE = "comp-file";
	public static final String OPT_MORPHEMES = "morphemes";
	public static final String OPT_WORD = "word";
	public static final String OPT_FROM_SCRATCH = "from-scratch";
	
	
	public ConsoleCommand(String name) {
		super(name);
	}
	
	protected String getCompilationFile() {
		String tFile = getOptionValue(ConsoleCommand.OPT_COMP_FILE, true);
		if (!tFile.endsWith("json")) {
			tFile = tFile + ".json";
		}
		return tFile;
	}

	protected String getCorpusDir() {
		String dir = getOptionValue(ConsoleCommand.OPT_CORPUS_DIR, true);
		return dir;
	}

	protected String[] getMorphemes() {
		return getMorphemes(true);
	}
	
	protected String[] getMorphemes(boolean failIfAbsent) {
		String morphStr = getOptionValue(ConsoleCommand.OPT_MORPHEMES, failIfAbsent);
		String[] morphSeq = null;
		if (morphStr != null) {
			morphSeq = morphStr.split("\\s+");
		}
		
		return morphSeq;		
	}
	
	protected String getWord() {
		return getWord(true);
	}
	
	protected String getWord(boolean failIfAbsent) {
		String wordStr = getOptionValue(ConsoleCommand.OPT_WORD, failIfAbsent);
		return wordStr;		
	}
	
	protected static String prompt(String mess) {
		mess = "\n" + mess + " ('q' to quit).";
		System.out.print(mess+"\n> ");
		Scanner input = new Scanner(System.in);
        String resp = input.nextLine();
        if (resp.matches("^\\s*q\\s*$")) {
        	resp = null;
        }
		return resp;
	}
	


}
