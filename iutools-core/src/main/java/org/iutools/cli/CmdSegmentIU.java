package org.iutools.cli;

import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import ca.nrc.ui.commandline.CommandLineException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.iutools.corpus.WordInfo;
import org.iutools.morph.Decomposition;
import org.iutools.morph.r2l.MorphologicalAnalyzer_R2L;
import org.iutools.morph.MorphologicalAnalyzer;
import ca.nrc.debug.Debug;

public class CmdSegmentIU extends ConsoleCommand {
	
	Scanner stdinScanner = new Scanner(System.in);
	ObjectMapper mapper = new ObjectMapper();
	Mode mode = null;
	boolean lenient = false;

	public CmdSegmentIU(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Decompose an Inuktut word into its morphemes.";
	}

	@Override
	public void execute() throws Exception {
		Logger mLogger = LogManager.getLogger("ca.inuktitutcomputing.core.console.SegmentIU.execute");

		mode = getMode(ConsoleCommand.OPT_WORD);
		lenient = getExtendedAnalysis();
		Long timeoutMSecs = getTimeoutMSecs();
		MorphologicalAnalyzer_R2L morphAnalyzer = new MorphologicalAnalyzer_R2L();
		morphAnalyzer.setTimeout(timeoutMSecs);
		
		mLogger.trace("invoked with mode="+mode+", lenient="+lenient+
			", timeoutMsecs="+timeoutMSecs);
				
		while (true) {
			WordInfo winfo = null;
			try {
				winfo = nextInputWord();
				if (winfo == null) {
					mLogger.trace("No more words");
					break;
				}
				long start = System.currentTimeMillis();	
				mLogger.trace("Working on word="+winfo.word+"(@"+start+" msecs)");

				Decomposition[] decs =
					morphAnalyzer.decomposeWord(winfo.word,lenient);
				winfo.setDecompositions(decs);
				long elapsed = System.currentTimeMillis() - start;
				printDecompositions(winfo, elapsed);
				mLogger.trace("DONE Working on word="+winfo.word+"(@"+start+" msecs)");
				
			} catch (Exception e) {
				if (mode == Mode.PIPELINE) {
					// In pipeline mode, we print exceptions on STDOUT
					// so that every line of input has a corresponding line of 
					// output
					//
					// Note: This assumes that no exception is raised AFTER the
					//   winfo's decompositions have been printed to STDOUT.
					//
					printExceptionResult(winfo, e);
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
		
		MorphologicalAnalyzer.shutdownExecutorPool();
		
		mLogger.trace("Done!");	
	}

	private void printExceptionResult(WordInfo winfo, Exception e)
			throws ConsoleException {
		
		WordResult result = new WordResult(winfo);
		if (e instanceof TimeoutException) {
			result.setTimedout(true);
		} else {
			result.setException(e);
		}
		
		printWordResult(result);
	}

	private void printDecompositions(WordInfo winfo, long elapsedMSecs) throws ConsoleException {
		if (mode == Mode.PIPELINE) {
			printDecompositionsPipeline(winfo, elapsedMSecs);
		} else {
			printDecompositionsUserModes(winfo, elapsedMSecs);
		}
	}

	private void printDecompositionsUserModes(WordInfo winfo, long elapsedMSecs) throws ConsoleException {
		String[][] decs = winfo.decompositionsSample;
		if (decs == null) {
			echo("Morphological analyzer failed");
		} else if (decs.length == 0) {
			echo("No decompositions found");
		} else {
			for (String[] aDec: decs) {
				echo(String.join(", ", aDec));
			}
		}
	}

	private void printDecompositionsPipeline(WordInfo winfo, long elapsed) throws ConsoleException {
		try {
			WordResult result = new WordResult(winfo, lenient, elapsed);
			printWordResult(result);
		} catch (Exception e) {
			throw new ConsoleException(e);
		}
	}

	private void printWordResult(WordResult result) throws ConsoleException {
		String output;
		try {
			output = WordInfo.jsonWriter.writeValueAsString(result.winfo);
		} catch (JsonProcessingException e) {
			throw new ConsoleException(e);
		}
		echo(output);
	}

	private WordInfo nextInputWord() throws CommandLineException {
		String wordInput = null;
		if (mode == Mode.SINGLE_INPUT) {
			wordInput = getWord();
		} else if (mode == Mode.INTERACTIVE) {
			wordInput = prompt("Enter Inuktut word");
		} else if (mode == Mode.PIPELINE) {
			wordInput = null;
			if (stdinScanner.hasNext()) {
				wordInput = stdinScanner.nextLine();
				System.out.println("--** nextInputWord: wordInput="+wordInput);
			}
		}

		WordInfo winfo = null;
		if (!wordInput.matches("^\\s*$")) {
			if (wordInput.matches("^\\s*\\{.*\\}\\s*^")) {
				try {
					winfo = mapper.readValue(wordInput, WordInfo.class);
				} catch (JsonProcessingException e) {
					throw new CommandLineException(e);
				}
			} else {
				winfo = new WordInfo(wordInput);
			}
		}
		
		return winfo;
	}

	public static class WordResult {
		public WordInfo winfo = null;;
		public String exception = null;
		public Long elapsedMSecs = null;
		public boolean lenient = false;
		public boolean timedOut = false;
		
		public WordResult(WordInfo _winfo) throws ConsoleException {
			initializeWordResult(_winfo, null, null, null);
		}

		public WordResult(WordInfo _winfo, Exception exc)
				throws ConsoleException {
			initializeWordResult(_winfo, null, null, exc);
		}

		public WordResult(WordInfo _winfo, Boolean _lenient, Long elapsed) throws ConsoleException {
			initializeWordResult(_winfo, elapsed, _lenient, null);
		}

		public WordResult(WordInfo _winfo, Long elapsed, Exception exc) throws ConsoleException {
			initializeWordResult(_winfo, elapsed, null, exc);
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

		private void initializeWordResult(WordInfo _winfo,
			Long elapsed, Boolean _lenient, Exception exc) throws ConsoleException {
			this.winfo = _winfo;
			this.elapsedMSecs = elapsed;			
			if (_lenient != null) {
				this.lenient = _lenient;
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
