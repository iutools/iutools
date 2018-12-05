package ca.inuktitutcomputing.core.console;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import ca.inuktitutcomputing.core.console.Console;
import ca.nrc.ui.commandline.CommandLineException;

public class ConsoleTest {
	
	/******************************************
	 * DOCUMENTATION TESTS
	 * @throws ConsoleException 
	 ******************************************/
	
	@Test
	public void test__Console__Synopsis() {
		try {
			//
			// Use Console to launch an operation.
			//
			// On the command line:
			// java -cp ... ca.inuktitutcomputing.console.Console name arguments*
			//
			// where name: name of the operation (for example: search_trie)
			//       arguments: one or more arguments, depending on the operation
			//
			String trieFilePath = "/Users/benoitfarley/Documents/git_repositories/iutools/java/iutools-data/src/test/trie_dump_with_error.txt";
			Console.main(new String[]{"search_trie","-trie-file",trieFilePath});
		} catch (Exception e) {
		}
	}
	
	@Test(expected=ConsoleException.class)
	public void test__Console__search_trie__file_with_error() throws ConsoleException {
		try {
			String trieFilePath = "/Users/benoitfarley/Documents/git_repositories/iutools/java/iutools-data/src/test/trie_dump_with_error.txt";
			Console.main(new String[]{"search_trie","-trie-file",trieFilePath});
		} catch(Exception e) {
			if (e.getClass().equals(ConsoleException.class))
				throw (ConsoleException)e;
		} 
	}


}
