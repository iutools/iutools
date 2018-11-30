package ca.pirurvik.iutools.console;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

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
			// java -cp ... ca.pirurvik.iutools.console.Console name arguments*
			//
			// where name: name of the operation (for example: search_trie)
			//       arguments: one or more arguments, depending on the operation
			//
			Console.main(new String[]{"test","argument1","argument2"});
		} catch (ConsoleException e) {
		}
	}
	
	@Test
	public void test__Console__test() {
		try {
			//
			// Use Console to launch an operation.
			//
			// On the command line:
			// java -cp ... ca.pirurvik.iutools.console.Console name arguments*
			//
			// where name: name of the operation (for example: search_trie)
			//       arguments: one or more arguments, depending on the operation
			//
			Console.main(new String[]{"test","argument1","argument2"});
		} catch (ConsoleException e) {
			assertFalse("There should not be an exception with this call.",true);
		}
	}

	@Test(expected=MiscellaneousConsoleException.class)
	public void test__Console__unknown_method() throws MiscellaneousConsoleException {
		try {
			Console.main(new String[]{"wabadoo","whatever","argument2"});
		} catch(ConsoleException e) {
			if (e.getClass().equals(MiscellaneousConsoleException.class))
				throw (MiscellaneousConsoleException)e;
		}
	}

	@Test(expected=InvalidArgumentConsoleException.class)
	public void test__Console__search_trie__bad_argument() throws InvalidArgumentConsoleException {
		try {
			Console.main(new String[]{"search_trie","whatever","argument2"});
		} catch(ConsoleException e) {
			if (e.getClass().equals(InvalidArgumentConsoleException.class))
				throw (InvalidArgumentConsoleException)e;
		}
	}

	@Test(expected=UnknownFileConsoleException.class)
	public void test__Console__search_trie__unknown_file() throws UnknownFileConsoleException {
		try {
			Console.main(new String[]{"search_trie","-trie-file","argument2"});
		} catch(ConsoleException e) {
			if (e.getClass().equals(UnknownFileConsoleException.class))
				throw (UnknownFileConsoleException)e;
		}
	}

	@Test(expected=ConsoleException.class)
	public void test__Console__search_trie__file_with_error() throws ConsoleException {
		try {
			String trieFilePath = "/Users/benoitfarley/Documents/git_repositories/iutools/java/iutools-data/src/test/trie_dump_with_error.txt";
			Console.main(new String[]{"search_trie","-trie-file",trieFilePath});
		} catch(ConsoleException e) {
			if (e.getClass().equals(ConsoleException.class))
				throw (ConsoleException)e;
		} catch(Exception e) {
			e.getStackTrace();
		}
	}

	@Test
	public void test__Console__search_trie_one__good_arguments() {
		try {
			String trieFilePath = "/Users/benoitfarley/Documents/git_repositories/iutools/java/iutools-data/src/test/trie_dump.txt";
			Console.main(new String[]{"search_trie_one","-trie-file",trieFilePath,"-surfaceform","takulaaq"});
		} catch(ConsoleException e) {
			assertFalse("There should not be an exception with this call.",true);
		}
	}

}
