package ca.inuktitutcomputing.core.console;

import java.util.Scanner;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.data.LinguisticDataException;
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
		Logger logger = Logger.getLogger("CmdSegmentIU.execute");
		Mode mode = getMode(ConsoleCommand.OPT_WORD);
		boolean doExtendedAnalysis = getExtendedAnalysis();
		MorphologicalAnalyzer morphAnalyzer = new MorphologicalAnalyzer();
		String word = nextInputWord(mode);
		while (true) {
			Decomposition[] decs = null;
			
			try {
				decs = morphAnalyzer.decomposeWord(word,doExtendedAnalysis);
			} catch (Exception e) {
				if (mode == Mode.PIPELINE) {
					printException(e);
				} else {
					throw(e);
				}
			}
			printDecompositions(decs, mode);
			word = nextInputWord(mode);
			if (mode == Mode.SINGLE_INPUT || word == null) {
				break;
			}
		}
	}

	private void printException(Exception e) {
		echo("EXCEPTION RAISED: "+e.getMessage());
	}

	private void printDecompositions(Decomposition[] decs, Mode mode) 
			throws LinguisticDataException {
		String decsStr = "null";
		String sep = "\n";
		if (mode == Mode.PIPELINE) {
			sep = " ";
		}
		if (decs != null) {
			decsStr = "";
			int decsCount = 0;
			for (Decomposition aDecomp: decs) {
				decsCount++;
				if (decsCount > 1) {
					decsStr += sep;
				}
				try {
					decsStr += aDecomp.toStr2();
				} catch (LinguisticDataException e) {
					if (mode == Mode.PIPELINE) {
						printException(e);
						break;
					} else {
						throw e;
					}
				}
			}
		}		
		echo(decsStr);
	}

	private String nextInputWord(Mode mode) {
		String word = null;
		if (mode == Mode.SINGLE_INPUT) {
			word = getWord();
		} else if (mode == Mode.INTERACTIVE) {
			word = prompt("Enter Inuktut word");			
		} else if (mode == Mode.PIPELINE) {
			Scanner scanner = new Scanner(System.in);
			word = null;
			if (scanner.hasNext()) {
				word = scanner.nextLine();
			}
		}
		
		return word;
	}

}
