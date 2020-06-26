package ca.pirurvik.iutools.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.script.TransCoder;
import ca.inuktitutcomputing.utilities.StopWatch;
import ca.inuktitutcomputing.utilities.StopWatchException;
import ca.nrc.datastructure.trie.StringSegmenter;
import ca.nrc.datastructure.trie.StringSegmenterException;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.text.segmentation.IUTokenizer;

public class CorpusCompiler {

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

	public void compile(File corpusDirectory) throws CorpusCompilerException {
		try {
			compileCorpusFromScratch(corpusDirectory);
		} catch (CompiledCorpusException | StringSegmenterException e) {
			throw new CorpusCompilerException(e);
		}
	}
	
	public  void compileCorpusFromScratch(File corpusDirectory) throws CompiledCorpusException, StringSegmenterException {
		compileCorpus(corpusDirectory, true);
	}
	
	public  void compileCorpus(File corpusDirectory, boolean fromScratch) throws CompiledCorpusException, StringSegmenterException {
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
			
		process(corpusDirectory);
		
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

	public void processDocumentContents(BufferedReader br, File docFile) throws CorpusCompilerException {
		String docFilePath = null;
		if (docFile != null) {
			docFilePath = docFile.toString();
		}
		try {
			processDocumentContents(br, docFilePath);
		} catch (CompiledCorpusException | StringSegmenterException | LinguisticDataException e) {
			throw new CorpusCompilerException(e);
		}
		
	}
	
	public void toConsole(String message) {
		if (verbose) System.out.print(message);
	}
	
	/**
	 * Reads the corpus compiler in the state it was when it was
	 * interrupted while running.
	 * @throws CompiledCorpusException 
	 */
	public void resumeCompilation(File corpusDirectory) throws CompiledCorpusException {
		File jsonFilePath = new File(corpusDirectory, CompiledCorpus_InMemory.JSON_COMPILATION_FILE_NAME);
		corpus = createFromJson(jsonFilePath.toString());
	}

    public static CompiledCorpus_InMemory createFromJson(String jsonCompilationFilePathname) throws CompiledCorpusException {
    	try {
    		FileReader jsonFileReader = new FileReader(jsonCompilationFilePathname);
    		Gson gson = new Gson();
    		CompiledCorpus_InMemory compiledCorpus = gson.fromJson(jsonFileReader, CompiledCorpus_InMemory.class);
    		jsonFileReader.close();
    		return compiledCorpus;
    	} catch (FileNotFoundException e) {
    		throw new CompiledCorpusException("File "+jsonCompilationFilePathname+"does not exist. Could not create a compiled corpus.");
    	} catch (IOException e) {
    		throw new CompiledCorpusException(e);
    	}
    }
    
	private void deleteJSON(File corpusDirectory) {
		File saveFile = new File(corpusDirectory, CompiledCorpus_InMemory.JSON_COMPILATION_FILE_NAME);
		if (saveFile.exists())
			saveFile.delete();
	}

	private void process(File corpusDirectory) throws CompiledCorpusException, StringSegmenterException {
		toConsole("[INFO] --- compiling directory "+corpusDirectory+"\n");
		this.corpusDirectory = corpusDirectory;
		processDirectory(corpusDirectory);
	}
	
	private void processDirectory(File directory) throws CompiledCorpusException, StringSegmenterException {
		Logger logger = Logger.getLogger("CompiledCorpus.processDirectory");
    	CorpusReader_Directory corpusReader = new CorpusReader_Directory();
    	Iterator<CorpusDocument_File> files = 
    		(Iterator<CorpusDocument_File>) corpusReader.getFiles(directory.toString());
    	while (files.hasNext()) {
    		CorpusDocument_File corpusDocumentFile = files.next();
    		File file = new File(corpusDocumentFile.id);
    		logger.debug("file: "+file.getAbsolutePath());
    		if (file.isDirectory()) {
    			processDirectory(new File(corpusDocumentFile.id));
    		} else {
    			processFile(corpusDocumentFile);
    		}
    	}
	}

	private void processFile(CorpusDocument_File file) throws CompiledCorpusException, StringSegmenterException {
			String fileAbsolutePath = file.id;
			fileBeingProcessed = file;
			toConsole("[INFO] --- compiling document "+new File(fileAbsolutePath).getName()+"\n");
			processDocumentContents();
			if ( !this.filesCompiled.contains(fileAbsolutePath) )
				filesCompiled.add(fileAbsolutePath);
	}
	
	
	public void processDocumentContents()
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
				processWords(words.toArray(new String[] {}));
			}
		} catch (CompiledCorpusException e) {
			throw e;
		} catch (StringSegmenterException e) {
			throw e;
		} catch (Exception e) {
			throw new CompiledCorpusException(e);
		}
	}

	protected void processDocumentContents(String fileAbsolutePath) throws CompiledCorpusException, StringSegmenterException, LinguisticDataException, CorpusCompilerException {
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new FileReader(fileAbsolutePath));
		} catch (FileNotFoundException e) {
			throw new CompiledCorpusException(e);
		}
		processDocumentContents(bufferedReader,fileAbsolutePath);
	}
	
	public void processDocumentContents(BufferedReader bufferedReader, String fileAbsolutePath)
			throws CompiledCorpusException, StringSegmenterException, LinguisticDataException, CorpusCompilerException {
		Logger logger = Logger.getLogger("CompiledCorpus.processDocumentContents");
		String line;
		currentFileWordCounter = 0;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				logger.debug("line: '"+line+"'");
				String[] words = extractWordsFromLine(line);
				processWords(words);
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

	private void processWords(String[] words) throws CompiledCorpusException, StringSegmenterException, LinguisticDataException, CorpusCompilerException {
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

				processWord(wordInRomanAlphabet);

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

	private void processWord(String word) throws CompiledCorpusException, StringSegmenterException, LinguisticDataException, CorpusCompilerException {
    	processWord(word,false);
    }
	
    private void processWord(String word, boolean recompilingFailedWord) throws CompiledCorpusException, StringSegmenterException, LinguisticDataException, CorpusCompilerException {
    	Logger logger = Logger.getLogger("ca.pirurvik.iutools.corpus.CorpusCompiler.processWord");
    	
    	long start = 0;
    	TimeUnit tunit = TimeUnit.MILLISECONDS;
    	if (logger.isTraceEnabled()) {
    		try {
				start = StopWatch.now(tunit);
			} catch (StopWatchException e) {
				throw new CorpusCompilerException(e);
			}
    	}
		toConsole("[INFO]     "+wordCounter + "(" + currentFileWordCounter + "+). " + word + "... \n");
		
		getCorpus().addWordOccurence(word);
		
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
		File saveFilePathname = new File(corpusDirectory, CompiledCorpus_InMemory.JSON_COMPILATION_FILE_NAME);
		saveCompilerInJSONFile(saveFilePathname);
	}
	
	public void saveCompilerInJSONFile (File savePath) throws CompiledCorpusException {
		FileWriter saveFile = null;
		try {
			saveFile = new FileWriter(savePath);
		} catch (IOException e1) {
			throw new CompiledCorpusException(e1);
		}
		Gson gson = new Gson();
		long savedRetrievedFileWordCounter = this.retrievedFileWordCounter;
		this.retrievedFileWordCounter = this.currentFileWordCounter;
		try {
			gson.toJson(this, saveFile);
			saveFile.flush();
			saveFile.close();
			this.retrievedFileWordCounter = savedRetrievedFileWordCounter;
		} catch (JsonIOException | IOException e) {
			throw new CompiledCorpusException(e);
		}
		toConsole("saved in "+savePath);
	}

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
}
