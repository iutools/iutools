package ca.pirurvik.iutools.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.morph.Decomposition;
import ca.inuktitutcomputing.script.TransCoder;
import ca.inuktitutcomputing.utilities.StopWatch;
import ca.inuktitutcomputing.utilities.StopWatchException;
import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.text.segmentation.IUTokenizer;

public class CorpusCompiler {
	
//	public static enum CompilationOption {FROM_SCRATCH, FREQUENCIES_ONLY, LENIENT_DECOMPS};
	public static enum CompileWhat {FREQUENCIES, DECOMPOSITIONS, ALL};

	protected boolean verbose = true;
	public long saveFrequency;
	public int stopAfter = -1;
	
	protected long wordCounter = 0;
	protected List<String> filesCompiled = new ArrayList<String>();	
	protected long retrievedFileWordCounter = -1;	
	
	private CompiledCorpus corpus = null;
	
	private transient StringSegmenter segmenter = null;
	private File corpusDirectory;
	private CorpusDocument_File fileBeingProcessed;
	private int currentFileWordCounter;


	public CorpusCompiler(CompiledCorpus _corpus) {
		this.corpus = _corpus;
	}

	public void compileWordFrequencies(File corpusDirectory) throws CorpusCompilerException {
		try {
			compileCorpusFromScratch(corpusDirectory);
		} catch (CompiledCorpusException | StringSegmenterException e) {
			throw new CorpusCompilerException(e);
		}
	}
	
	public  void compileCorpusFromScratch(File corpusDirectory) throws CompiledCorpusException, StringSegmenterException {
		compileCorpus(corpusDirectory, true);
	}

	public void compileCorpus(File corpusDirectory, boolean fromScratch) 
			throws CompiledCorpusException, StringSegmenterException {
		CompileWhat what = CompileWhat.ALL;
		compileCorpus(corpusDirectory, fromScratch, what);
	}
	
	public  void compileCorpus(
		File corpusDirectory, boolean fromScratch, CompileWhat what) 
		throws CompiledCorpusException, StringSegmenterException {
		
		toConsole("[INFO] *** Compiling trie for documents in "+corpusDirectory+"\n");
		segmenter = getSegmenter(); //new StringSegmenter_IUMorpheme();
		
		if ( !fromScratch ) {
			if (this.canBeResumed(corpusDirectory)) {
				this.resumeCompilation(corpusDirectory);
			} else {
				// no json compilation in the corpus directory: will do as if from scratch
			}
		} else {
			this.deleteJSON(corpusDirectory);
		}
		
		wordCounter = 0;
			
		process(corpusDirectory, what);
		
		compileExtras();
		
		toConsole("[INFO] *** Compilation completed."+"\n");
		save(corpusDirectory);
	}
	
	public void setVerbose(boolean _verbose) {
		this.verbose = _verbose;
	}

	public CompiledCorpus getCorpus() {
		return this.corpus;
	}
	
	@SuppressWarnings("unchecked")
	@JsonIgnore
	public StringSegmenter getSegmenter() throws CompiledCorpusException {
		if (segmenter == null) {
			Class cls;
			try {
				cls = Class.forName(getCorpus().segmenterClassName);
			} catch (ClassNotFoundException e) {
				throw new CompiledCorpusException(e);
			}
			try {
				segmenter = (StringSegmenter) cls.newInstance();
			} catch (Exception e) {
				throw new CompiledCorpusException(e);
			}
		}
		return segmenter;
	}
	

	public boolean canBeResumed(File corpusDir) {
//		return getCorpus().canBeResumed(corpusDir.toString());
		return true;
	}

	public void processDocumentContents(BufferedReader br, File docFile, CompileWhat what) throws CorpusCompilerException {
		String docFilePath = null;
		if (docFile != null) {
			docFilePath = docFile.toString();
		}
		try {
			processDocumentContents(br, docFilePath, what);
		} catch (CompiledCorpusException | StringSegmenterException | LinguisticDataException e) {
			throw new CorpusCompilerException(e);
		}
		
	}
	
	public void toConsole(String message) {
		if (verbose) System.out.print(message+"\n");
	}
	
	/**
	 * Reads the corpus compiler in the state it was when it was
	 * interrupted while running.
	 * @throws CompiledCorpusException 
	 */
	public void resumeCompilation(File corpusDirectory) throws CompiledCorpusException {
		Class<? extends CompiledCorpus> corpusClass = CompiledCorpus_v2FS.class;
		File jsonFilePath = new File(corpusDirectory, CompiledCorpus_InMemory.JSON_COMPILATION_FILE_NAME);
		if (jsonFilePath.exists()) {
			corpus = RW_CompiledCorpus.read(
				jsonFilePath, CompiledCorpus_InMemory.class);
		} else {
			corpus = RW_CompiledCorpus.read(
				corpusDirectory, CompiledCorpus_v2FS.class);
		}
	}

//    public static CompiledCorpus_InMemory createFromJson(String jsonCompilationFilePathname) throws CompiledCorpusException {
//    	try {
//    		FileReader jsonFileReader = new FileReader(jsonCompilationFilePathname);
//    		Gson gson = new Gson();
//    		CompiledCorpus_InMemory compiledCorpus = gson.fromJson(jsonFileReader, CompiledCorpus_InMemory.class);
//    		jsonFileReader.close();
//    		return compiledCorpus;
//    	} catch (FileNotFoundException e) {
//    		throw new CompiledCorpusException("File "+jsonCompilationFilePathname+"does not exist. Could not create a compiled corpus.");
//    	} catch (IOException e) {
//    		throw new CompiledCorpusException(e);
//    	}
//    }
    
	private void deleteJSON(File corpusDirectory) {
		File saveFile = new File(corpusDirectory, CompiledCorpus_InMemory.JSON_COMPILATION_FILE_NAME);
		if (saveFile.exists())
			saveFile.delete();
	}

	private void process(File corpusDirectory) throws CompiledCorpusException, StringSegmenterException {
		process(corpusDirectory, null);
	}
	
	private void process(File corpDir, CompileWhat what) 
		throws CompiledCorpusException, StringSegmenterException {
		toConsole("[INFO] --- compiling directory "+corpDir+"\n");
		this.corpusDirectory = corpDir;
		processDirectory(corpDir, what);
	}
	
	
	private void processDirectory(File directory, CompileWhat what) 
		throws CompiledCorpusException, StringSegmenterException {
		Logger logger = Logger.getLogger("CompiledCorpus.processDirectory");
    	CorpusReader_Directory corpusReader = new CorpusReader_Directory();
    	Iterator<CorpusDocument_File> files = 
    		(Iterator<CorpusDocument_File>) corpusReader.getFiles(directory.toString());
    	while (files.hasNext()) {
    		CorpusDocument_File corpusDocumentFile = files.next();
    		File file = new File(corpusDocumentFile.id);
    		logger.debug("file: "+file.getAbsolutePath());
    		if (file.isDirectory()) {
    			processDirectory(new File(corpusDocumentFile.id), what);
    		} else {
    			processFile(corpusDocumentFile, what);
    		}
    	}
	}

	private void processFile(
		CorpusDocument_File file, CompileWhat what) 
		throws CompiledCorpusException, StringSegmenterException {
		String fileAbsolutePath = file.id;
		fileBeingProcessed = file;
		toConsole("[INFO] --- compiling document "+new File(fileAbsolutePath).getName()+"\n");
		processDocumentContents(what);
		if ( !this.filesCompiled.contains(fileAbsolutePath) )
			filesCompiled.add(fileAbsolutePath);
	}
	
	
	public void processDocumentContents(CompileWhat what)
			throws CompiledCorpusException, StringSegmenterException {
		Logger logger = Logger.getLogger("CorpusCompiler.processDocumentContents");
		currentFileWordCounter = 0;
		String contents;
		try {
//			contents = fileBeingProcessed.getContents();
			BufferedReader reader = fileBeingProcessed.contentsReader();
			String line;
			while ((line = reader.readLine()) != null) {
				IUTokenizer iuTokenizer = new IUTokenizer();
				List<String> words = iuTokenizer.tokenize(line);
				processWords(words.toArray(new String[] {}), what);
			}
		} catch (CompiledCorpusException e) {
			throw e;
		} catch (StringSegmenterException e) {
			throw e;
		} catch (Exception e) {
			throw new CompiledCorpusException(e);
		}
	}

//	protected void processDocumentContents(String fileAbsolutePath) throws CompiledCorpusException, StringSegmenterException, LinguisticDataException, CorpusCompilerException {
//		BufferedReader bufferedReader = null;
//		try {
//			bufferedReader = new BufferedReader(new FileReader(fileAbsolutePath));
//		} catch (FileNotFoundException e) {
//			throw new CompiledCorpusException(e);
//		}
//		processDocumentContents(bufferedReader,fileAbsolutePath);
//	}
	
	public void processDocumentContents(
		BufferedReader bufferedReader, String fileAbsolutePath, 
		CompileWhat what)
		throws CompiledCorpusException, StringSegmenterException, LinguisticDataException, CorpusCompilerException {
		
		Logger logger = Logger.getLogger("CompiledCorpus.processDocumentContents");
		String line;
		currentFileWordCounter = 0;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				logger.debug("line: '"+line+"'");
				String[] words = extractWordsFromLine(line);
				processWords(words, what);
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

	private void processWords(String[] words, CompileWhat what) throws CompiledCorpusException, StringSegmenterException, LinguisticDataException, CorpusCompilerException {
    	Logger logger = Logger.getLogger("CompiledCorpus.processWords");
		logger.debug("words: "+words.length);
		for (int n = 0; n < words.length; n++) {
			String word = words[n];
			String wordInRomanAlphabet = TransCoder.unicodeToRoman(word);
			logger.debug("wordInRomanAlphabet: " + wordInRomanAlphabet);
			if (!isInuktitutWord(wordInRomanAlphabet))
				continue;
			++wordCounter;

			if (fileBeingProcessed == null || !filesCompiled.contains(fileBeingProcessed.id)) {
				++currentFileWordCounter;
				if (retrievedFileWordCounter != -1) {
					if (currentFileWordCounter < retrievedFileWordCounter) {
						continue;
					} else {
						retrievedFileWordCounter = -1;
					}
				}

				processWord(wordInRomanAlphabet, what);

				// this line allows to make the compiler stop at a given point (for tests
				// purposes only)
				if (stopAfter != -1 && wordCounter == stopAfter) {
					throw new CompiledCorpusException(
							"processDocumentContents:: Simulating an error during trie compilation of corpus.");
				}
				if (saveFrequency > 0 && wordCounter % saveFrequency == 0) {
					toConsole("[INFO]     --- saving jsoned compiler ---" + "\n");
//					try {
//						logger.debug("size of trie: " + getCorpus().trie.getSize());
//					} catch (TrieException e) {
//						throw new CompiledCorpusException(e);
//					}
					save(this.corpusDirectory);
				}
			}
		}
	}

	private void processWord(String word, CompileWhat what) throws CompiledCorpusException, StringSegmenterException, LinguisticDataException, CorpusCompilerException {
    	Logger logger = Logger.getLogger("ca.pirurvik.iutools.corpus.CorpusCompiler.processWord");
    	
    	if (what == null) {
    		what = CompileWhat.ALL;
    	}
    	
    	long start = 0;
    	TimeUnit tunit = TimeUnit.MILLISECONDS;
    	if (logger.isTraceEnabled()) {
    		try {
				start = StopWatch.now(tunit);
			} catch (StopWatchException e) {
				throw new CorpusCompilerException(e);
			}
    	}
    	boolean frequenciesOnly = false;
    	if (what == CompileWhat.FREQUENCIES) {
    		frequenciesOnly = true;
    	}
    	
		toConsole("[INFO]     "+wordCounter + "(" + currentFileWordCounter + "+). " + word + "... \n");
		
		getCorpus().addWordOccurence(word, frequenciesOnly);
		
    	if (logger.isTraceEnabled()) {
    		try {
    			long end = StopWatch.now(tunit);
				logger.trace("processWord took "+
						StopWatch.elapsedSince(start, tunit)+" "+tunit);
			} catch (StopWatchException e) {
				throw new CorpusCompilerException(e);
			}
    	}
	
    	return;
	}

	protected void compileExtras() {
//		corpus.setNgramStats();
	}
	
	public void save(File corpusDirectory) throws CompiledCorpusException  {
		if (corpus instanceof CompiledCorpus_InMemory) {
			File saveFilePathname = new File(corpusDirectory, CompiledCorpus_InMemory.JSON_COMPILATION_FILE_NAME);
			RW_CompiledCorpus.write(corpus, saveFilePathname);
		} else {
			RW_CompiledCorpus.write(corpus, corpusDirectory);
		}
	}
	
//	public void saveCompilerInJSONFile (File savePath) throws CompiledCorpusException {
//		FileWriter saveFile = null;
//		try {
//			saveFile = new FileWriter(savePath);
//		} catch (IOException e1) {
//			throw new CompiledCorpusException(e1);
//		}
//		Gson gson = new Gson();
//		long savedRetrievedFileWordCounter = this.retrievedFileWordCounter;
//		this.retrievedFileWordCounter = this.currentFileWordCounter;
//		try {
//			gson.toJson(this, saveFile);
//			saveFile.flush();
//			saveFile.close();
//			this.retrievedFileWordCounter = savedRetrievedFileWordCounter;
//		} catch (JsonIOException | IOException e) {
//			throw new CompiledCorpusException(e);
//		}
//		toConsole("saved in "+savePath);
//	}

	private static String[] extractWordsFromLine(String line) {
		Logger logger = Logger.getLogger("ca.pirurvik.iutools.corpus.CorpusCompiler.extractWordsFromLine");
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

	public void updateWordDecompositions(File decompsFile) throws CompiledCorpusException {
		ObjectStreamReader reader = null;
		try {
			reader = new ObjectStreamReader(decompsFile);
			reader.setEndOfBodyMarker("NEW_LINE");
		} catch (FileNotFoundException | ObjectStreamReaderException e) {
			throw new CompiledCorpusException(
				"Cannot open the decompositions file "+decompsFile, e);
		}
		
		while (true) {
			Map<String,Object> wordDecompInfo;
			try {
				wordDecompInfo = (Map<String, Object>) reader.readObject();
			} catch (ClassNotFoundException | IOException | ObjectStreamReaderException e) {
				throw new CompiledCorpusException(
					"Error reading Map<Strin,Object> from decompositions file "+
					decompsFile, e);
			}
			if (wordDecompInfo == null) {
				break;
			}
			
			String word = (String) wordDecompInfo.get("word");			
			if (!corpus.containsWord(word)) {
				toConsole("Skipping word "+word);
			} else {
				toConsole("Updating decompositions for word "+word);
				List<String> decompStrings = (List<String>) wordDecompInfo.get("decompositions");
				String[][] decomps = Decomposition.decomps2morphemes(decompStrings);
				corpus.updateWordDecompositions(word, decomps);
			}
		}
		
		// This will force recreation of the morphemes ngram trie
		toConsole("Regenerating the morpheme ngram trie. This could take a while...");
		corpus.getMorphNgramsTrie();
		toConsole("DONE regenerating the morpheme ngram trie.");
	}
}
