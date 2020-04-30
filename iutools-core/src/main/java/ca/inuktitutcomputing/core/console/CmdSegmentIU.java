package ca.inuktitutcomputing.core.console;

import java.util.Scanner;

import org.apache.log4j.Logger;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.morph.MorphologicalAnalyzer;
import ca.nrc.debug.Debug;

public class CmdSegmentIU extends ConsoleCommand {
	
	Scanner stdinScanner = new Scanner(System.in);
	
	public CmdSegmentIU(String name) {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Decompose an Inuktut word into its morphemes.";
	}

	@Override
	public void execute() throws Exception {
		Mode mode = getMode(ConsoleCommand.OPT_WORD);

		boolean doExtendedAnalysis = getExtendedAnalysis();
		MorphologicalAnalyzer morphAnalyzer = new MorphologicalAnalyzer();
		
		while (true) {
			try {
				String word = nextInputWord(mode);
				if (word == null) {
					break;
				}
				Decomposition[] decs = 
					morphAnalyzer.decomposeWord(word,doExtendedAnalysis);
				printDecompositions(decs, mode);
				if (mode == Mode.SINGLE_INPUT) {
					break;
				}
			} catch (Exception e) {
				if (mode == Mode.PIPELINE) {
					// In pipeline mode, we print exceptions on STDOUT
					// so that every line of input has a corresponding line of 
					// output
					//
					// Note: This assumes that no exception is raised AFTER the
					//   word's decompositions have been printed to STDOUT.
					//
					printException(e);
					System.err.println(Debug.printCallStack());
				} else {
					throw e;
				}
			}
		}
	}

	private void printException(Exception e) {
		String message = e.getMessage().replaceAll("[\n\r]+", "\\n");
		echo("EXCEPTION RAISED: "+message);
	}

	private void printDecompositions(Decomposition[] decs, Mode mode) 
			throws LinguisticDataException {
		String decsStr = "null";
		String sep = "\n";
		if (mode == Mode.PIPELINE) {
			sep = ", ";
		}
		if (decs != null) {
			decsStr = "";
			int decsCount = 0;
			for (Decomposition aDecomp: decs) {
				decsCount++;
				if (decsCount > 1) {
					decsStr += sep;
				}
				decsStr += aDecomp.toStr2();
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
			word = null;
			if (stdinScanner.hasNext()) {
				word = stdinScanner.nextLine();
			}
		}
		
		return word;
	}

}
