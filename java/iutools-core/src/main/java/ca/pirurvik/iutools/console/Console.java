package ca.pirurvik.iutools.console;

import java.util.Arrays;
import java.util.HashMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.fasterxml.jackson.databind.ObjectMapper;

import ca.nrc.datastructure.trie.Trie;
import ca.nrc.datastructure.trie.TrieException;
import ca.nrc.datastructure.trie.TrieNode;
import ca.nrc.datastructure.trie.TrieReader;
import ca.nrc.json.PrettyPrinter;

/**
 * Some basic commands:

compile_trie -input-dir /path/to/dir -trie-file /path/to/trie/file.json

search_trie_one -trie-file /path/to/trie/file.json -text text

search_trie -trie-file /path/to/trie/file.json
    This one will then keep prompting you for words to search in a trie

 * @author benoitfarley
 *
 */

public class Console {
	
	private static HashMap<String,Class[]> methodsArgClasses = 
			new HashMap<String,Class[]>();
	
	public static void main(String[] args) throws ConsoleException {
		methodsArgClasses.put("test", new Class[]{String.class, String.class});
		methodsArgClasses.put("search_trie", new Class[]{String.class, String.class});
		methodsArgClasses.put("search_trie_one", new Class[]{String.class, String.class, String.class, String.class});

		Class<?> c = Console.class;
		String methodName = args[0];
		try {
			Class<?>[] paramTypes = methodsArgClasses.get(methodName);
			Method method
			  = Console.class.getMethod(
					  methodName, paramTypes);
			String[] params = (String[]) Arrays.copyOfRange(args, 1, args.length);
			method.invoke(null, params);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
			MiscellaneousConsoleException ce = new MiscellaneousConsoleException(e.getMessage());
			throw ce;
		} catch (InvocationTargetException e) {
			ConsoleException ce = (ConsoleException) e.getCause();
			throw ce;
		}
	}
	
	public static void test(String arg1, String arg2) {
		System.out.println("test - "+arg1);
		System.out.println("test - "+arg2);
	}

	public static void search_trie_one(String arg1Name, String arg1Value, String arg2Name, String arg2Value) throws ConsoleException {
		String trieFilePath = null;
		String searchType = null;
		String searchText = null;
		
		if ( arg1Name.equals("-trie-file") ) {
			if ( arg2Name.equals("-text") || arg2Name.equals("-surfaceform") ) {
				trieFilePath = arg1Value;
				if (arg2Name.equals("-text"))
					searchType = "text";
				else
					searchType = "surfaceform";
			} else {
				throw new InvalidArgumentConsoleException("'"+arg2Name+"' is not a valid argument to search_trie_one.");
			}
		} else if ( arg1Name.equals("-text") || arg1Name.equals("-surfaceform") ) {
			if ( arg2Name.equals("-trie-file") ) {
				trieFilePath = arg1Value;
				if (arg1Name.equals("-text"))
					searchType = "text";
				else
					searchType = "surfaceform";
			} else {
				throw new InvalidArgumentConsoleException("'"+arg2Name+"' is not a valid argument to search_trie_one.");
			}
		} else {
			throw new InvalidArgumentConsoleException("'"+arg1Name+"' is not a valid argument to search_trie_one.");
		}
		Trie trie = null;
		trie = readTrie(trieFilePath);
		System.out.println("trie: "+trie.getClass().getName());
		System.out.println(__search_trie(trie,searchType,searchText));
	}

	private static Object __search_trie(Trie trie, String searchType, String searchText) {
		if (searchType.equals("surfaceform")) {

		} else {
			
		}
		return null;
	}

	public static void search_trie(String argName, String argValue) throws ConsoleException {
		if (!argName.equals("-trie-file"))
			throw new InvalidArgumentConsoleException("First argument '"+argName+"' is unknown to the method search_trie.");
		String trieFilePath = argValue;
		File f = new File(trieFilePath);
		if(!f.exists() || !f.isFile()) { 
		    throw new UnknownFileConsoleException("Second argument '"+argName+"' does not refer to an existing file.");
		}
		Trie trie = readTrie(trieFilePath);
		
		InputStream is = null;
		BufferedReader br = null;
		try {
			is = System.in;
			br = new BufferedReader(new InputStreamReader(is));
			String line = "";
			while ( line != null) {
				System.out.print("Enter a word ['q' to quit]: ");
				System.out.flush();
				line = br.readLine();
				if (line.equalsIgnoreCase("q")) {
					break;
				}
				System.out.println("Searching for : " + line);
				System.out.flush();
			
				String[] segments = null;
				segments = new ObjectMapper().readValue(line, segments.getClass());
				TrieNode trieNode = trie.getNode(segments);
				System.out.println("frequency of the whole word: "+trieNode.getFrequency());
				System.out.println(PrettyPrinter.print(trieNode));
				TrieNode rootNode = trie.getNode(new String[]{segments[0]});
				System.out.println("root morpheme: "+rootNode.getText());
				System.out.println("frequency of the root: "+rootNode.getFrequency());
				TrieNode mostFrequentTerminal = rootNode.getMostFrequentTerminal();
				System.out.println("most frequent word with this root: "+mostFrequentTerminal.getText()+
						" ["+mostFrequentTerminal.getFrequency()+" occurrence(s)]");
				System.out.println("node of most frequent word: "+PrettyPrinter.print(mostFrequentTerminal));
				//System.out.println("surface form of most frequent word: "+((TrieNode_IUMorpheme)mostFrequentTerminal).getSurfaceForm());
				System.out.println("\n");
			}
		} catch (IOException ioe) {
			System.out.println("Exception while reading input " + ioe);
		} catch (Exception e) {
			System.out.println("Exception while getting the node.");
		}
		finally {
			// close the streams using close method
			try {
				if (br != null) {
					br.close();
				}
			}
			catch (IOException ioe) {
				System.out.println("Error while closing stream: " + ioe);
			}
		}
		
	}

	
	public static Trie readTrie(String trieFilePath) throws ConsoleException {
		File f = new File(trieFilePath);
		if(!f.exists() || !f.isFile()) { 
		    throw new UnknownFileConsoleException("'"+trieFilePath+"' is not a file.");
		}
		TrieReader trieReader = new TrieReader();
		Trie trie = null;
		try {
			trie = trieReader.read(trieFilePath);
		} catch (TrieException e) {
			throw new ConsoleException("Exception while reading trie file into a trie. "+e.getMessage());
		}
		return trie;
	}
	

}
