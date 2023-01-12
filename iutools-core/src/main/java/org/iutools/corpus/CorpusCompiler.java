package org.iutools.corpus;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.nrc.dtrc.elasticsearch.ElasticSearchException;
import ca.nrc.dtrc.stats.FrequencyHistogram;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.iutools.concordancer.Alignment;
import org.iutools.utilities.StopWatch;
import ca.nrc.ui.commandline.ProgressMonitor;
import ca.nrc.ui.commandline.ProgressMonitor_Terminal;
import ca.nrc.ui.commandline.UserIO;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.morph.r2l.DecompositionState;
import org.iutools.script.TransCoder;
import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import org.iutools.datastructure.trie.StringSegmenterException;
import ca.nrc.json.PrettyPrinter;
import org.iutools.text.segmentation.IUTokenizer;
import org.iutools.utilities.StopWatchException;
import org.json.JSONObject;

public class CorpusCompiler {

	private ObjectMapper mapper;
	private UserIO user_io;

	public static enum CompileWhat {FREQUENCIES, DECOMPOSITIONS, ALL};

	/** Directory where outputs and progress status are stored */
	private File _outputDir;

	/** Name of the CompiledCorpus_ES where compiled info is to be stored */
	public String corpusName;

	/** Current progress status of the corpus compilation */
	public CorpusCompilationProgress progress = new CorpusCompilationProgress();

	protected boolean verbose = true;
	public int stopAfter = -1;
	public int saveFrequency = 30;

	protected long wordCounter = 0;
	protected long retrievedFileWordCounter = -1;


	private CompiledCorpus _corpus = null;
	private FrequencyHistogram<String> wordFreqs = new FrequencyHistogram<String>();

	private long lastSaveMSecs = 0;

	ProgressMonitor_Terminal progressMonitor = null;

	IUTokenizer tokenizer = new IUTokenizer();

	public CorpusCompiler(File _outputDir) throws CorpusCompilerException {
		init_CorpusCompiler(_outputDir);
	}

	private void init_CorpusCompiler(File _outputDir)
		throws CorpusCompilerException {
		this._outputDir = _outputDir;
		readProgressFile();
		if (progress.corpusName != null) {
			this.corpusName = progress.corpusName;
		}
		lastSaveMSecs = StopWatch.nowMSecs();
		this.mapper = new ObjectMapper();
		return;
	}

	public CorpusCompiler setTMFilesDir(File _corpusDir) throws CorpusCompilerException {

		if (progress.corpusTextsRoot != null &&
			!progress.corpusTextsRoot.toString()
				.equals(_corpusDir.toString())) {
				throw new CorpusCompilerException(
					"The input corpus directory did not correspond to the value "+
					"found in the progress file: "+progressFile()+
					"\n   Received value        : "+_corpusDir+
					"\n   Value in progress file : "+progress.corpusTextsRoot
				);
			}
		this.progress.corpusTextsRoot = _corpusDir;
		saveProgress();
		return this;
	}

	public CorpusCompiler setCorpusName(String _corpusName)
		throws CorpusCompilerException {
		if (progress.corpusName != null &&
			!_corpusName.equals(progress.corpusName)) {
			throw new CorpusCompilerException(
				"The corpus name did not correspond to the value "+
				"found in the progress file: "+progressFile()+
				"\n   Received value        : "+_corpusName+
				"\n   Value in pogress file : "+progress.corpusName
			);
		}
		this.progress.corpusName = _corpusName;
		this.corpusName = _corpusName;
		saveProgress();
		return this;
	}

	public CorpusCompiler setUserIO(UserIO _user_io) {
		this.user_io = _user_io;
		return this;
	}

	public void compile() throws CorpusCompilerException {
		compile((String)null, (File)null);
	}

	public void compile(String _corpName, File txtDir) throws CorpusCompilerException {

		if (txtDir != null) {
			setTMFilesDir(txtDir);
		}
		if (_corpName != null) {
			setCorpusName(_corpName);
		}

		while (progress.currentPhase != CorpusCompilationProgress.Phase.DONE) {
			boolean stop = performNextStep();
			writeProgressFile();
			if (stop) {
				break;
			}
		}
		uponDone();
	}

	private boolean performNextStep() throws CorpusCompilerException {
		boolean stop = false;
		if (progress.currentPhase == null) {
			progress.currentPhase = CorpusCompilationProgress.Phase.COMPUTE_WORD_FREQUENCIES;
		}
		toConsole(true,
			"\n=============================\nStarting phase: "+progress.currentPhase);
		CorpusCompilationProgress.Phase oldPhase = progress.currentPhase;
		if (progress.currentPhase == CorpusCompilationProgress.Phase.COMPUTE_WORD_FREQUENCIES) {
			/**
			 * In this phase, we analyze a series of tm.json files to compute
			 * and generate a FrequencyHistogram that provides the word
			 * frequencies.
			 */
			phaseCompileFreqsCorpusFiles();
		} else if (progress.currentPhase == CorpusCompilationProgress.Phase.GENERATE_CORP_FILE_NO_DECOMPS) {
			/**
			 * In this phase, we use the this.wordFreqs map to generate two
			 * files:
			 * - words.txt: Contains all the words, one word per line.
			 * - corpus.nodecomps.json: A JSON file with WordInfo records
			 *     for every word, with just the 'word' and 'frequency'
			 *     fields filled in (no information about morphological
			 *     decompositions).
			 */
			phaseGenerateCorpFileNoDecomps();
		} else if (progress.currentPhase == CorpusCompilationProgress.Phase.CHECK_DECOMPS_FILE) {
			/**
			 * In this phase, we check for the existence of a decompositions
			 * file decomps.json.
			 *
			 * - If the file exists, we proceed with the next phase.
			 * - Otherwise, we terminate the compilation process with some
			 *     instructions on how to generate the decomps.json file.
			 */
			stop = phaseCheckDecompsFile();
		} else if (progress.currentPhase == CorpusCompilationProgress.Phase.GENERATE_CORP_FILE_WITH_DECOMPS) {
			/**
			 * In this phase, we generate a final <corpusName>.json file which
			 * contains the same WordInfo as the corpus.nodecomps.json file,
			 * but this time with the morphological decomposition fields filled
			 * in.
			 */
			phaseGenerateCorpFileWithDecomps();
		} else {
			throw new CorpusCompilerException("Phase not yet supported: "+progress.currentPhase);
		}
		toConsole(true,
			"Completed phase: "+oldPhase+
			"\n=============================\n");

		return stop;
	}

	public  void phaseCompileFreqsCorpusFiles() throws CorpusCompilerException {

		toConsole("   Compiling word frequencies from documents in "+
			corpusTextsRoot()+"\n\n");

		progress.currentPhase = CorpusCompilationProgress.Phase.COMPUTE_WORD_FREQUENCIES;
		wordCounter = 0;
		compileFreqsDirectory(progress.corpusTextsRoot);
		printWordsFile();
		printWordFreqsFile();

		progress.currentPhase = CorpusCompilationProgress.Phase.GENERATE_CORP_FILE_NO_DECOMPS;
	}

	/**
	 * In this phase, we use the this.wordFreqs map to generate two
	 * files:
	 * - words.txt: Contains all the words, one word per line.
	 * - corpus.nodecomps.json: A JSON file with WordInfo records
	 *     for every word, with just the 'word' and 'frequency'
	 *     fields filled in (no information about morphological
	 *     decompositions).
	 */
	private void phaseGenerateCorpFileNoDecomps() throws CorpusCompilerException {
		toConsole(
			"   Computing the corpus JSON file (no decomps) from words frequency histogram: "+
			wordFreqsMapFile()+"\n\n");

		progress.currentPhase = CorpusCompilationProgress.Phase.GENERATE_CORP_FILE_NO_DECOMPS;

		readWordFreqsMap();

		File corpFile = corpusNoDecompsFile();
		FileWriter fw = null;
		try {
			fw = new FileWriter(corpFile);
		} catch (IOException e) {
			throw new CorpusCompilerException("Could not open corpus file (no decomps) for writing: "+corpFile, e);
		}

		try {
			fw.write(
				"bodyEndMarker=BLANK_LINE\n"+
				"class=org.iutools.corpus.WordInfo\n\n");
			ObjectMapper mapper = new ObjectMapper();
			for (String word: wordFreqs.allValues()) {
				Long freq = wordFreqs.frequency(word);
				JSONObject jObj = new JSONObject()
					.put("word", word)
					.put("frequency", freq);
					;
				fw.write(jObj.toString() + "\n");
			}
		} catch (IOException  e) {
			throw new CorpusCompilerException("Error while writing to corpus file (no decomps) for writing: " + corpFile, e);
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				throw new CorpusCompilerException("Error closing corpus file (no decomps) for writing: " + corpFile, e);
			}
		}

		wordFreqsMapFile().delete();

		progress.currentPhase = CorpusCompilationProgress.Phase.CHECK_DECOMPS_FILE;
	}

	/**
	 * In this phase, we check for the existence of a decompositions
	 * file decomps.json.
	 *
	 * - If the file exists, we proceed with the next phase.
	 * - Otherwise, we terminate the compilation process with some
	 *     instructions on how to generate the decomps.json file.
	 */
	private boolean phaseCheckDecompsFile() throws CorpusCompilerException {
		boolean stop = false;
		File decompsFile = decompositionsFile();
		if (!decompsFile.exists()) {
			toConsole(true,
		"Decompositions file does not exist:\n   "+decompsFile+"\n"+
				"\nPlease compute the decomps file and put it under the above name.\n"+
				"You can compute the decomps file with the following commands:\n\n" +
				"\n"+
				"# Note: The version number may differ.\n"+
				"set IUTOOLS_CORE_EXECS=/path/to/iutools/iutools-core/target/iutools-core-1.0.2\n"+
				"cat \""+wordsFile()+ "\" | \\\n" +
				"   java -Xmx18g -cp \"${IUTOOLS_CORE_EXECS}/iutools-core.jar:${IUTOOLS_CORE_EXECS}/lib/*\" \\\n" +
				"   org.iutools.cli.CLI segment_iu --pipeline --timeout-secs 3\n" +
				"\n" +
				"NOTE: This may take a very long time so we recommend that you split the words file into small chunks and process each chunk with the above command on a cluster of machines. "+
				"You may decrease running time by decreasing the value of --timeout-secs, but then more words will end up having empty decompositions."
				);
			stop = true;
		} else {
			progress.currentPhase = CorpusCompilationProgress.Phase.GENERATE_CORP_FILE_WITH_DECOMPS;
		}
		return stop;
	}

	/**
	 * In this phase, we generate a file corpus.withdecomps.json which
	 * contains the same WordInfo as the corpus.nodecomps.json file,
	 * but this time with the morphological decomposition fields filled
	 * in.
	 */
	private void phaseGenerateCorpFileWithDecomps() throws CorpusCompilerException {
		File decompsFile = corpusFinalFile();
		File corpusFile = corpusWithDecompsFile();

		toConsole(
			true,
			"Creating final JSON file (with decomps) for corpus "+corpusName+". File path: \n"+
			decompsFile);

		BufferedReader decompsFileReader = null;
		FileWriter corpusFileWriter = null;
		try {
			try {
				corpusFileWriter = new FileWriter(corpusFile);
			} catch (IOException e) {
				throw new CorpusCompilerException(
					"Problem opening corpus file for writing: "+corpusFile, e);
			}
			long numWords = -1;
			try {
				numWords = Files.lines(decompsFile.toPath()).count();
				decompsFileReader = new BufferedReader(new FileReader(decompsFile));
			} catch (IOException e) {
				throw new CorpusCompilerException(
					"Problem opening decompositions file for reading: "+decompsFile, e);
			}

			ProgressMonitor monitor =
				new ProgressMonitor_Terminal(numWords,
					progress.currentPhase.toString(), 30);
			phaseGenerateCorpFileWithDecomps(decompsFileReader, corpusFileWriter, monitor);

		} catch (CompiledCorpusException e) {
			e.printStackTrace();
		} finally {
			try {
				if (decompsFileReader != null) {
					decompsFileReader.close();
				}
				if (corpusFileWriter != null) {
					corpusFileWriter.close();
				}
			} catch (IOException e) {
				throw new CorpusCompilerException(e);
			}
		}

		corpusNoDecompsFile().delete();

		progress.currentPhase = CorpusCompilationProgress.Phase.DONE;

		return;
	}

	private void phaseGenerateCorpFileWithDecomps(
		BufferedReader decompsFileReader, FileWriter corpusFileWriter,
		ProgressMonitor progMonitor)
		throws CorpusCompilerException, CompiledCorpusException {

		try {
			corpusFileWriter.write(
				"bodyEndMarker=BLANK_LINE\n" +
					"class=org.iutools.corpus.WordInfo\n\n");
		} catch (IOException e) {
			throw new CorpusCompilerException("Problem writing to corpus file.", e);
		}

		Map<String,Object> wordResult = null;
		while (true) {
			try {
				String lineJson = decompsFileReader.readLine();
				if (lineJson == null) {
					break;
				}
				progMonitor.stepCompleted();
				wordResult = mapper.readValue(lineJson, Map.class);
			} catch (IOException e) {
				throw new CorpusCompilerException(
					"Problem reading or parsing line from decomps file", e);
			}
			Map<String,Object> winfoMap = (Map) (wordResult.get("winfo"));
			try {
				String winfoJson = winfoMap2json(winfoMap);
				corpusFileWriter.write(winfoJson + "\n\n");
			} catch (IOException e) {
				throw new CorpusCompilerException("Problem writing updated word info to file", e);
			}
		}
	}

	private String winfoMap2json(Map<String, Object> winfoMap) throws CorpusCompilerException {
		String json = null;
		try {
			String mapJson  = mapper.writeValueAsString(winfoMap);
			WordInfo winfo = mapper.readValue(mapJson, WordInfo.class);
			json = winfo.toJson();
		} catch (JsonProcessingException | ElasticSearchException e) {
			throw new CorpusCompilerException(e);
		}
		return json;
	}

	private void uponDone() throws CorpusCompilerException {
		toConsole("   Cleaning up intermediate files.\n");
		cleanupIntermediatFiles();
		toConsole(
			"\n\nCompilation of corpus "+progress.corpusName+" is done.\n\n"+
			"The compiled corpus file is located at:\n\n"+
			corpusFinalFile()
		);
	}

	private void cleanupIntermediatFiles() throws CorpusCompilerException {
		try {
			File[] toCleanup = new File[] {
				wordFreqsMapFile(), corpusNoDecompsFile(),
				corpusWithDecompsFile(), decompositionsFile(),
			};
			for (File aFile: toCleanup) {
				if (aFile.exists()) {
					aFile.delete();
				}
			}
		} catch (CorpusCompilerException e) {
			throw new CorpusCompilerException(e);
		}
	}


	private void readWordFreqsMap() throws CorpusCompilerException {
		try {
			System.out.println("   Reading words frequency histogram ...");
			File freqsFile = wordFreqsMapFile();
			if (freqsFile.length() != 0) {
				Map<String, Long> freqsMap = new ObjectMapper()
				.readValue(wordFreqsMapFile(), Map.class);
				wordFreqs = FrequencyHistogram.fromMap(freqsMap);
			}
		} catch (IOException e) {
			throw new CorpusCompilerException(
				"Could not read word frequencies from file: "+wordFreqsMapFile(),
				e);
		}

	}

	private void printWordsFile() throws CorpusCompilerException {
		File wordsF = wordsFile();
		FileWriter fw = null;
		try {
			fw = new FileWriter(wordsF);
		} catch (IOException e) {
			throw new CorpusCompilerException(
				"Could not open words file: "+wordsF, e);
		}
		try {
			for (String word: wordFreqs.allValues()) {
				fw.write(word+"\n");
			}
		} catch (IOException e) {
			throw new CorpusCompilerException(
				"Could not write to words file: "+wordsF, e);

		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				throw new CorpusCompilerException(
					"Could not close words file: "+wordsF, e);
			}
		}
	}

	private void printWordFreqsFile() throws CorpusCompilerException{
		FileWriter fw = null;
		File freqsFile = null;
		try {
			freqsFile = wordFreqsMapFile();
			mapper.writeValue(freqsFile, wordFreqs.toMap());
		} catch (JsonGenerationException e) {
			throw new CorpusCompilerException("Problem writing word freqs file: "+freqsFile, e);
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					throw new CorpusCompilerException("Could not close word frequencies file: "+freqsFile);
				}
			}
		}
	}

	private void compileFreqsDirectory(File dir) throws CorpusCompilerException {
		Logger logger = LogManager.getLogger("CompiledCorpus_ES.processDirectory");
		
		Collection<File> tmFiles = tmFilesInDir(dir);
		long totalAlignments = countAlignments(tmFiles);
		progressMonitor =
				new ProgressMonitor_Terminal(totalAlignments/10000,
					"Compiling frequency of words TM files");
		progressMonitor.refreshEveryNSecs = 60;

		for (File aFile: tmFiles) {
			compileWordFreqsInTmFile(aFile);
		}

		CorpusReader_Directory corpusReader = new CorpusReader_Directory();
		Iterator<CorpusDocument_File> files =
			(Iterator<CorpusDocument_File>) corpusReader.getFiles(dir.toString());
		while (files.hasNext()) {
			CorpusDocument_File corpusDocumentFile = files.next();
			File file = new File(corpusDocumentFile.id);
			logger.debug("file: "+file.getAbsolutePath());
			if (file.isDirectory()) {
				compileFreqsDirectory(new File(corpusDocumentFile.id));
			} else {
				compileFreqsFile(corpusDocumentFile);
			}
		}
	}

	private void compileWordFreqsInTmFile(File tmFile) throws CorpusCompilerException {
		int alignNum = 0;
		ObjectStreamReader reader = null;
		try {
			reader = new ObjectStreamReader(tmFile);
			Alignment algnmt = null;
			String currAlignDescr = null;
			StopWatch sw = new StopWatch().start();
			while ((algnmt = (Alignment)reader.readObject()) != null) {
				alignNum++;
				if (alignNum % 10000 == 0) {
					progressMonitor.stepCompleted();
				}
				String iuText = algnmt.sentence4lang("iu");
				iuText = TransCoder.ensureRoman(iuText);
				List<String> words = tokenizer.tokenize(iuText);
				for (String word: words) {
					wordFreqs.updateFreq(word);
				}
				if (sw.totalTime(TimeUnit.SECONDS) > 30) {
					printWordFreqs();
					sw.reset();
				}
			}
		} catch (IOException | ClassNotFoundException | ObjectStreamReaderException | StopWatchException e) {
			throw new CorpusCompilerException(e);
		}
	}

	private void printWordFreqs() {
		System.out.println("== Compiled words stats");
		System.out.println("  Total words      : "+wordFreqs.allValues().size());
		System.out.println("  Total occurences : "+wordFreqs.totalOccurences());
	}


	private long countAlignments(Collection<File> tmFiles) throws CorpusCompilerException {
		long totalAlignments = 0;
		for (File aFile: tmFiles) {
			try {
				System.out.println("   Counting alignments in TM file: "+aFile+"\n    (may take a few minutes)");
				ObjectStreamReader reader = new ObjectStreamReader(aFile);
				while (true) {
					Alignment align = (Alignment) reader.readObject();
					if (align == null) {
						break;
					}
					totalAlignments++;
				}
			} catch (IOException | ClassNotFoundException | ObjectStreamReaderException e) {
				throw new CorpusCompilerException(e);
			}
		}
		return totalAlignments;
	}

	private Collection<File> tmFilesInDir(File dir) throws CorpusCompilerException {
		Collection<File> tmFiles = FileUtils.listFiles(dir, new WildcardFileFilter("*.tm.json"), null);
		return tmFiles;
	}

	public CorpusCompiler setVerbose(boolean _verbose) {
		this.verbose = _verbose;
		return this;
	}

	public CompiledCorpus corpus() throws CorpusCompilerException {
		if (_corpus == null) {
			try {
				_corpus = new CompiledCorpusRegistry().makeCorpus(corpusName);
			} catch (CompiledCorpusException | CompiledCorpusRegistryException e) {
				throw new CorpusCompilerException(e);
			}
		}
		return this._corpus;
	}

	public void compileFreqsDocumentContents(
			BufferedReader br, File docFile, CompileWhat what) throws CorpusCompilerException {
		String docFilePath = null;
		if (docFile != null) {
			docFilePath = docFile.toString();
		}
		try {
			compileFreqsDocumentContents(br, docFilePath, what);
		} catch (CompiledCorpusException | StringSegmenterException | LinguisticDataException e) {
			throw new CorpusCompilerException(e);
		}

	}

	private void compileFreqsFile(CorpusDocument_File file)
		throws CorpusCompilerException {
		String fileAbsolutePath = file.id;
		progress.fileBeingProcessed = file;
		toConsole("compiling document "+new File(fileAbsolutePath).getName()+"\n");
		compileFreqsDocumentContents();
		if ( !progress.filesCompiled.contains(fileAbsolutePath) ) {
			progress.filesCompiled.add(fileAbsolutePath);
		}
	}

	public void compileFreqsDocumentContents() throws CorpusCompilerException {
		Logger logger = LogManager.getLogger("CorpusCompiler.processDocumentContents");
		progress.currentFileWordCounter = 0;
		String contents;
		BufferedReader reader = null;
		try {
			reader = progress.fileBeingProcessed.contentsReader();
			String line;
			while ((line = reader.readLine()) != null) {
				IUTokenizer iuTokenizer = new IUTokenizer();
				List<String> words = iuTokenizer.tokenize(line);
				compileFreqsWords(words.toArray(new String[] {}));
			}
		} catch (CorpusDocumentException | IOException e) {
			throw new CorpusCompilerException(e);
		}
	}

	public void compileFreqsDocumentContents(
		BufferedReader bufferedReader, String fileAbsolutePath,
		CompileWhat what)
		throws CompiledCorpusException, StringSegmenterException, LinguisticDataException, CorpusCompilerException {

		Logger logger = LogManager.getLogger("CompiledCorpus_ES.processDocumentContents");
		String line;
		progress.currentFileWordCounter = 0;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				logger.debug("line: '"+line+"'");
				String[] words = extractWordsFromLine(line);
				compileFreqsWords(words);
			}
			bufferedReader.close();
		} catch (IOException e) {
			try {
				bufferedReader.close();
			} catch (IOException e1) {
				throw new CompiledCorpusException(e);
			}
			throw new CompiledCorpusException(e);
		}
	}

	private static boolean isInuktitutWord(String string) {
		Pattern p = Pattern.compile("[agHijklmnpqrstuv&]+");
		Matcher m = p.matcher(string);
		if (m.matches()) {
			p = Pattern.compile("[aiu]+");
			m = p.matcher(string);
			if (m.find()) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}

	private void compileFreqsWords(String[] words)
		throws CorpusCompilerException {
    	Logger logger = LogManager.getLogger("CompiledCorpus_ES.toknizeWords");
		logger.debug("words: "+words.length);
		for (int n = 0; n < words.length; n++) {
			String word = words[n];
			String wordInRomanAlphabet = TransCoder.unicodeToRoman(word);
			logger.debug("wordInRomanAlphabet: " + wordInRomanAlphabet);
			if (!isInuktitutWord(wordInRomanAlphabet)) {
				continue;
			}
			++wordCounter;

			if (progress.fileBeingProcessed == null || !progress.filesCompiled.contains(progress.fileBeingProcessed.id)) {
				++progress.currentFileWordCounter;
				if (retrievedFileWordCounter != -1) {
					if (progress.currentFileWordCounter < retrievedFileWordCounter) {
						continue;
					} else {
						retrievedFileWordCounter = -1;
					}
				}

				incrementWordFreq(wordInRomanAlphabet);

				// this line allows to make the compiler stop at a given point (for tests
				// purposes only)
				if (stopAfter != -1 && wordCounter == stopAfter) {
					throw new CorpusCompilerException(
					"processDocumentContents:: Simulating an error during trie compilation of corpus.");
				}

				if (needsSaving()) {
					saveProgress();
				}
//				if (saveFrequency > 0 && wordCounter % saveFrequency == 0) {
//					toConsole("[INFO]     --- saving compilation progress---" + "\n");
//					try {
//						logger.debug("size of trie: " + getCorpus().trie.getSize());
//					} catch (TrieException e) {
//						throw new CompiledCorpusException(e);
//					}
//					saveProgress();
//				}
			}
		}
	}

	private boolean needsSaving() throws CorpusCompilerException {
		boolean answer = false;
		if (saveFrequency > 0) {
			long nowMSecs = StopWatch.nowMSecs();
			long elapsedMSecs =
				StopWatch.elapsedMsecsSince(lastSaveMSecs);
			long elapsedSecs = elapsedMSecs / 1000;
			if (elapsedSecs  > saveFrequency) {
				answer = true;
			}
		}
		return answer;
	}

	private void saveProgress() throws CorpusCompilerException {
		boolean done = progress.currentPhase == CorpusCompilationProgress.Phase.DONE;
		if (done || progress.currentPhase == CorpusCompilationProgress.Phase.COMPUTE_WORD_FREQUENCIES) {
			saveWordFrequencies();
		}
		writeProgressFile();
		lastSaveMSecs = StopWatch.nowMSecs();
	}

	private void saveWordFrequencies() throws CorpusCompilerException {
		File freqsF = wordFreqsMapFile();
		try {
			new ObjectMapper().writeValue(freqsF, wordFreqs.toMap());
		} catch (IOException e) {
			throw new CorpusCompilerException(
				"Could not save word frequencies file: "+freqsF, e);
		}
	}

	private void incrementWordFreq(String word) {
		Long oldFreq = wordFreqs.frequency(word);
		if (oldFreq == null) {
			oldFreq = new Long(0);
		}
		wordFreqs.updateFreq(word);
	}

	private static String[] extractWordsFromLine(String line) {
		Logger logger = LogManager.getLogger("org.iutools.corpus.CorpusCompiler.extractWordsFromLine");
		line = line.replace('.', ' ');
		line = line.replace(',', ' ');
		logger.debug("line= '"+line+"'");
		String[] words = line.split("\\s+");
		logger.debug("words= "+PrettyPrinter.print(words));
		if (words.length!=0) {
			if (words[0].equals("")) {
				int n=words.length-1;
				String[] newWords=new String[n];
				System.arraycopy(words,1,newWords,0,n);
				words = newWords;
			}
		}
		logger.debug("words= "+PrettyPrinter.print(words));
		return words;
	}

	public void updateWordDecompositions()
			throws CorpusCompilerException {
		File decompsFile = decompositionsFile();
		ObjectStreamReader reader = null;
		try {
			reader = new ObjectStreamReader(decompsFile);
			reader.setEndOfBodyMarker("NEW_LINE");
		} catch (FileNotFoundException | ObjectStreamReaderException e) {
			throw new CorpusCompilerException(
				"Cannot open the decompositions file "+decompsFile, e);
		}

		ProgressMonitor progMonitor =
			makeLinesProgressMonitor(
				decompsFile,
				"Updating morphological decomp of words");

		int lineCounter = 0;
		try {
			while (true) {
				lineCounter++;
				progMonitor.stepCompleted();
				Map<String, Object> wordDecompInfo;
				try {
					wordDecompInfo = (Map<String, Object>) reader.readObject();
				} catch (ClassNotFoundException | IOException | ObjectStreamReaderException e) {
					throw new CorpusCompilerException(
						"Error reading Map<Strin,Object> from decompositions file " +
						decompsFile, e);
				}
				if (wordDecompInfo == null) {
					break;
				}

				String word = (String) wordDecompInfo.get("word");
				WordInfo winfo = _corpus.info4word(word);
				if (winfo == null) {
					toConsole("Skipping word " + word);
				} else {
					toConsole("Updating decompositions for word " + word);
					List<String> decompStrings = (List<String>) wordDecompInfo.get("decompositions");
					String[][] decomps = DecompositionState.decomps2morphemes(decompStrings);
					Integer totalDecomps = new Integer(0);
					if (decomps != null && decomps.length > 0) {
						totalDecomps = new Integer(decomps.length);
						int sampleSize = Math.min(decomps.length, 9);
						decomps = Arrays.copyOfRange(decomps, 0, sampleSize);
					}
					winfo.setDecompositions(decomps, totalDecomps);
				}
			}

			// This will force recreation of the morphemes ngram trie
			toConsole("Regenerating the morpheme ngram trie. This could take a while...");
			_corpus.getMorphNgramsTrie();
			toConsole("DONE regenerating the morpheme ngram trie.");
		} catch (CompiledCorpusException  e) {
			throw new CorpusCompilerException(e);
		} finally {
		}
	}

	private void writeJsonFileHeaders(FileWriter fileWriter) throws CorpusCompilerException {
		try {
			fileWriter.write("bodyEndMarker=BLANK_LINE\n" +
					"class=org.iutools.corpus.WordInfo\n\n");
		} catch (IOException e) {
			throw new CorpusCompilerException(e);
		}
	}

	private void writeWinfo(WordInfo winfo, FileWriter fileWriter) throws CorpusCompilerException {
		String json = PrettyPrinter.print(winfo);
		try {
			fileWriter.write(json+"\n\n");
		} catch (IOException e) {
			throw new CorpusCompilerException(e);
		}
	}

	private ProgressMonitor makeLinesProgressMonitor(File file, String mess) throws CorpusCompilerException {
		ProgressMonitor_Terminal monitor = null;
		try {
			System.out.println("\nCounting lines in file:\n  "+file);
			long lineCount = Files.lines(file.toPath()).count();
			System.out.println("There are "+lineCount+" to be processed in Counting lines in file:\n  "+file);
			monitor = new ProgressMonitor_Terminal(lineCount, mess, 30);
		} catch (IOException e) {
			throw new CorpusCompilerException(e);
		}
		return monitor;
	}

	private void readProgressFile()
		throws CorpusCompilerException {
		File progFile = progressFile();
		CorpusCompilationProgress prog =
				new CorpusCompilationProgress(getOutputDir());
		if (progFile != null) {
			if (!progFile.exists() || progFile.length() == 0) {
				progress = new CorpusCompilationProgress();
			} else {
				try {
					progress =
						new ObjectMapper()
							.readValue(progFile, CorpusCompilationProgress.class);
				} catch (IOException e) {
					throw new CorpusCompilerException(
						"Could not parse progress file " + progFile, e);
				}
			}
		}
		return;
	}

	private void writeProgressFile() throws CorpusCompilerException {
		File progFile = progressFile();
		if (progFile != null) {
			if (!progFile.exists()) {
				try {
					progFile.createNewFile();
				} catch (IOException e) {
					throw new CorpusCompilerException(
							"Unable to create progress file: " + progFile, e);
				}
			}
			try {
				new ObjectMapper().writeValue(progFile, progress);
			} catch (IOException e) {
				throw new CorpusCompilerException(
						"Could write progress status to file " + progFile, e);
			}
		}

		return;
	}

	public CorpusCompiler setOutputDir(File _outputDir) {
		this._outputDir = _outputDir;
		return this;
	}

	private File getOutputDir() throws CorpusCompilerException {
		if (_outputDir != null && !_outputDir.exists()) {
			try {
				Files.createDirectories(_outputDir.toPath());
			} catch (IOException e) {
				throw new CorpusCompilerException(
					"Could not create compilation directory: "+_outputDir, e);
			}
		}
		return _outputDir;
	}

	private File progressFile() throws CorpusCompilerException {
		File progF = null;
		if (null != getOutputDir()) {
			progF = new File(getOutputDir(), "progress.json");
		}
		return progF;
	}

	public File wordsFile() throws CorpusCompilerException {
		File wordsF = new File(getOutputDir(), "words.txt");
		return wordsF;
	}

	private File wordFreqsMapFile() throws CorpusCompilerException {
		File freqsF = new File(getOutputDir(), "wordfreqs.json");
		return freqsF;
	}

	private File corpusNoDecompsFile() throws CorpusCompilerException {
		File corpFile = new File(getOutputDir(), "corpus.nodecomps.json");
		return corpFile;
	}

	private File corpusWithDecompsFile() throws CorpusCompilerException {
		File corpFile = new File(getOutputDir(), "corpus.withdecomps.json");
		return corpFile;
	}

	public File decompositionsFile() throws CorpusCompilerException {
		return decompositionsFile(null);
	}

	public File decompositionsFile(Boolean failIfNotExist) throws CorpusCompilerException {
		if (failIfNotExist == null) {
			failIfNotExist = false;
		}
		File decompsF = new File(getOutputDir(), "decomps.json");
		if (!decompsF.exists() && failIfNotExist) {
			throw new CorpusCompilerException("Decompositions file does not exist:\n"+decompsF);
		}
		return decompsF;
	}

	public File corpusFinalFile() throws CorpusCompilerException {
		File corpF = new File(getOutputDir(), corpusName+".json");
		return corpF;
	}

	public void toConsole(String message) {
		toConsole(null, message);
	}

	public void toConsole(Boolean force, String message) {
		if (force == null) {
			force = false;
		}
		if (verbose || force) {
			System.out.print(message+"\n");
		}
	}

	public File corpusTextsRoot() {
		File root = null;
		if (progress != null) {
			root = progress.corpusTextsRoot;
		}
		return root;
	}
}
