package org.iutools.cli;

import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.morph.Decomposition;
import org.iutools.morph.MorphologicalAnalyzer;
import org.iutools.morph.MorphologicalAnalyzerAbstract;
import ca.nrc.debug.Debug;

public class CmdSegmentIU extends ConsoleCommand {
	
	Scanner stdinScanner = new Scanner(System.in);
	Mode mode = null;
	boolean lenient = false;
	
	public CmdSegmentIU(String name) {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Decompose an Inuktut word into its morphemes.";
	}

	@Override
	public void execute() throws Exception {
		Logger mLogger = Logger.getLogger("ca.inuktitutcomputing.core.console.SegmentIU.execute");
		mode = getMode(ConsoleCommand.OPT_WORD);
		lenient = getExtendedAnalysis();
		Long timeoutMSecs = getTimeoutMSecs();
		MorphologicalAnalyzer morphAnalyzer = new MorphologicalAnalyzer();
		morphAnalyzer.setTimeout(timeoutMSecs);
		
		mLogger.trace("invoked with mode="+mode+", lenient="+lenient+
			", timeoutMsecs="+timeoutMSecs);
				
		while (true) {
			String word = null;
			try {
				word = nextInputWord();
				if (word == null) {
					mLogger.trace("No more words");
					break;
				}
				long start = System.currentTimeMillis();	
				mLogger.trace("Working on word="+word+"(@"+start+" msecs)");

				Decomposition[] decs = 
					morphAnalyzer.decomposeWord(word,lenient);
				long elapsed = System.currentTimeMillis() - start;
				printDecompositions(word, decs, elapsed);
				mLogger.trace("DONE Working on word="+word+"(@"+start+" msecs)");
				
			} catch (Exception e) {
				if (mode == Mode.PIPELINE) {
					// In pipeline mode, we print exceptions on STDOUT
					// so that every line of input has a corresponding line of 
					// output
					//
					// Note: This assumes that no exception is raised AFTER the
					//   word's decompositions have been printed to STDOUT.
					//
					printExceptionResult(word, e);
				} else {
					if (e instanceof TimeoutException) {
						onCommandTimeout((TimeoutException)e);
					} else {
						throw e;
					}
				}
			} finally {
				if (mode == Mode.SINGLE_INPUT) {
					break;
				}
			}
		}
		
		MorphologicalAnalyzerAbstract.shutdownExecutorPool();
		
		mLogger.trace("Done!");	
	}

	private void printExceptionResult(String word, Exception e) 
			throws ConsoleException {
		
		WordResult result = new WordResult(word);
		if (e instanceof TimeoutException) {
			result.setTimedout(true);
		} else {
			result.setException(e);
		}
		
		printWordResult(result);
	}

	private void printDecompositions(String word, Decomposition[] decs, 
			long elapsedMSecs) throws ConsoleException {
		if (mode == Mode.PIPELINE) {
			printDecompositionsPipeline(word, decs, elapsedMSecs);
		} else {
			printDecompositionsUserModes(decs, elapsedMSecs);
		}
	}

	private void printDecompositionsUserModes(Decomposition[] decs, 
			long elapsedMSecs) throws ConsoleException {
		if (decs == null) {
			echo("Morphological analyzer failed");
		} else if (decs.length == 0) {
			echo("No decompositions found");
		} else {
			for (Decomposition aDec: decs) {
				try {
					echo(aDec.toStr2());
				} catch (LinguisticDataException e) {
					throw new ConsoleException(e);
				}
			}
		}
	}

	private void printDecompositionsPipeline(String _word, Decomposition[] decs, 
			long elapsed) throws ConsoleException {
		try {
			WordResult result = new WordResult(_word, decs, lenient, elapsed);
			printWordResult(result);
		} catch (Exception e) {
			throw new ConsoleException(e);
		}
	}

	private void printWordResult(WordResult result) throws ConsoleException {
		String output;
		try {
			output = new ObjectMapper().writeValueAsString(result);
		} catch (JsonProcessingException e) {
			throw new ConsoleException(e);
		}
		echo(output);
	}

	private String nextInputWord() {
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

	public static class WordResult {
		public String word = null;;
		public String[] decompositions = new String[0];
		public String exception = null;
		public Long elapsedMSecs = null;
		public boolean lenient = false;
		public boolean timedOut = false;
		
		public WordResult(String _word) throws ConsoleException {
			initializeWordResult(_word, null, null, null, null);
		}

		public WordResult(String _word, Exception exc) 
				throws ConsoleException {
			initializeWordResult(_word, null, null, null, exc);
		}

		public WordResult(String _word, Decomposition[] decomps, 
				Boolean _lenient, Long elapsed) throws ConsoleException {
			initializeWordResult(_word, decomps, elapsed, _lenient, null);
		}

		public WordResult(String _word, Decomposition[] decomps, 
				Long elapsed, Exception exc) throws ConsoleException {
			initializeWordResult(_word, decomps, elapsed, null, exc);
		}
		
		public WordResult setTimedout(boolean flag) {
			this.timedOut  = flag;
			return this;
		}
		
		public WordResult setException(Exception exc) {
			if (exc != null) {
				this.exception = singleLineMessage(exc);
			}
			return this;
		}

		private void initializeWordResult(String _word, Decomposition[] decomps, 
			Long elapsed, Boolean _lenient, Exception exc) throws ConsoleException {
			this.word = _word;
			this.elapsedMSecs = elapsed;			
			if (_lenient != null) {
				this.lenient = _lenient;
			}
			if (decomps != null) {
				this.decompositions = new String[decomps.length];
				for (int ii=0; ii < decomps.length; ii++) {
					try {
						this.decompositions[ii] = decomps[ii].toStr2();
					} catch (LinguisticDataException e) {
						throw new ConsoleException(e);
					}
				}
			}
			setException(exc);
		}

		private String singleLineMessage(Exception exc) {
			String mess = exc.getMessage()+"\n"+Debug.printCallStack();
			mess = mess.replaceAll("[\n\r]+", "\\n");
			return mess;
		}
	}
}
