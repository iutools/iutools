package org.iutools.corpus;

import ca.nrc.config.ConfigException;
import ca.nrc.data.file.ObjectStreamReader;
import ca.nrc.data.file.ObjectStreamReaderException;
import ca.nrc.file.ResourceGetter;
import org.iutools.config.IUConfig;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CorpusSqlHandlerTest {

	CorpusSqlHandler corpusSqlHandler;

	@Before
	public void setUp() throws Exception {
		String userName = IUConfig.getConfigProperty("org.iutools.database.username", false);
		String password = IUConfig.getConfigProperty("org.iutools.database.password", false);
		String dbName = IUConfig.getConfigProperty("org.iutools.database.dbname", false);
		corpusSqlHandler = new CorpusSqlHandler(userName,password,dbName);
	}
	
	@After
    public void tearDown() throws Exception {
		if (corpusSqlHandler.connection != null) { corpusSqlHandler.closeConnection(); }
    }

	/////////////////////////////////
	// DOCUMENTATION TESTS
	/////////////////////////////////
	
	@Test
	public void test__CorpusSqlHandler__Synopsis() throws Exception {
	}
	
	///////////////////////////
	// VERIFICATION TESTS
	///////////////////////////
	@Test
	public void test__CorpusSqlHandler_getConnection() throws Exception {
		String userName = IUConfig.getConfigProperty("org.iutools.database.username", false);
		String password = IUConfig.getConfigProperty("org.iutools.database.password", false);
		String dbName = IUConfig.getConfigProperty("org.iutools.database.dbname", false);
		CorpusSqlHandler corpusSqlHandler = new CorpusSqlHandler(userName,password,dbName);
		Connection connection = corpusSqlHandler.openConnection();
		Assert.assertNotNull("The connection should have returned not null.",connection);
		corpusSqlHandler.closeConnection();
	}

	@Test
	public void test__query() throws Exception {
		Connection con = corpusSqlHandler.openConnection();
		String query = "SELECT * FROM CorpusData WHERE noid > 0 ORDER BY noid LIMIT 1";
		try {
			Statement stmt = con.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if (rs.next()) {
				int noid = Integer.parseInt(rs.getString("noid"));
				String word = rs.getString("word");
				String expectedWord = "Haajsan";
				Assert.assertEquals("The field 'noid' should contain the value 1.", 1, noid);
				Assert.assertEquals("The field 'word' should contain the value '"+expectedWord+"'.", expectedWord, word);
			} else {
				Assert.fail("There should have been a succesfull return.");
			}
			if (rs.next()) {
				Assert.fail("There should have been only 1 result.");
			}

		} catch (SQLException e) {
			System.out.println("Error: " + e.getMessage());
		}
	}

	@Test
	public void test__getInfoForWord__nonexisting_word() throws Exception {
		corpusSqlHandler.openConnection();
		String nonexistingWord = "blah";
		WordInfo wordInfo = corpusSqlHandler.getInfoForWord(nonexistingWord);
		Assert.assertNull("There should be no record for the word '"+nonexistingWord+"'.",wordInfo);
	}

	@Test
	public void test__getInfoForWord__existing_word() throws Exception {
		corpusSqlHandler.openConnection();
		String existingWord = "Haakinningatauq";
		WordInfo wordInfo = corpusSqlHandler.getInfoForWord(existingWord);
		Assert.assertNotNull("There should be a record for the word '"+existingWord+"'.",wordInfo);
		String word = wordInfo.word;
		Assert.assertEquals("The field 'word' should contain the value '"+existingWord+"'.", existingWord, word);
	}

	@Test
	public void test__get_word_decompositions_from_wordinfo__existing_word() throws Exception {
		corpusSqlHandler.openConnection();
		String existingWord = "Haakiqataurataalaurmata";
		String[][] expectedDecompositions = {
				{"Haakiq/1v","qatau/1vv","rataaq/1vv","lauq/1vv","mata/tv-caus-4p"}
		};
		WordInfo wordInfo = corpusSqlHandler.getInfoForWord(existingWord);
		String[][] decompositions = wordInfo.decompositionsSample;
		Assert.assertArrayEquals("The decompositions are not as expected.", expectedDecompositions, decompositions);
	}

	@Test
	public void test__get_word_decompositions_directly__existing_word() throws Exception {
		corpusSqlHandler.openConnection();
		String existingWord = "Haakiliutitillugit";
		String[][] expectedDecompositions = {
				{"Haakiq/1v","liq/1vv","uti/1vv","tillugit/tv-part-3p"}
		};
		String[][] decompositions = corpusSqlHandler.getDecompositionsForWord(existingWord);
		Assert.assertArrayEquals("The decompositions are not as expected.", expectedDecompositions, decompositions);
	}

	@Test
	public void test__get_word_decompositions_directly__nonexisting_word() throws Exception {
		corpusSqlHandler.openConnection();
		String nonexistingWord = "blah";
		String[][] expectedDecompositions = {};
		String[][] decompositions = corpusSqlHandler.getDecompositionsForWord(nonexistingWord);
		Assert.assertArrayEquals("The decompositions are not as expected.", expectedDecompositions, decompositions);
	}

	@Test
	public void test__iterate_over_words() throws Exception {
		corpusSqlHandler.openConnection();
		CorpusSqlHandler.WordIterator<WordInfo> iterator = corpusSqlHandler.getWordIterator();
		String[] expectedFirstFiveWords = new String[] {
				"Haajsan","Haaki","Haakikkanniriaqalaaqpugut","Haakiliutinirmik","Haakiliutitillugit",
				"Haakimik","Haakinningatauq","Haakinniqalaurata","Haakiqataurataalaurmata","Haakiqatiqarniaratta"};
		boolean end = false;
		int i = 0;
		while ( !end ) {
			WordInfo wi = iterator.next();
			if (wi != null) {
				String word = wi.word;
				Assert.assertEquals("Wrong word in iterator position " + iterator.getIndex(),
						expectedFirstFiveWords[i], word);
				i++;
				if (i==expectedFirstFiveWords.length) {
					end = true;
				}
			} else {
				end = true;
			}
		}
		Assert.assertEquals("The iterator should have gone through "+
				expectedFirstFiveWords.length+" words.",expectedFirstFiveWords.length,i);
	}

	@Test
	public void test__iterate_over_all_words() throws Exception {
		corpusSqlHandler.openConnection();
		CorpusSqlHandler.WordIterator<WordInfo> iterator = corpusSqlHandler.getWordIterator();
		String[] expectedFirstFiveWords = new String[] {
				"Haajsan","Haaki","Haakikkanniriaqalaaqpugut","Haakiliutinirmik","Haakiliutitillugit",
				"Haakimik","Haakinningatauq","Haakinniqalaurata","Haakiqataurataalaurmata","Haakiqatiqarniaratta"};
		boolean end = false;
		int i = 0;
		while ( !end ) {
			WordInfo wi = iterator.next();
			if (wi != null) {
				String word = wi.word;
				System.out.println("word: "+word);
				Assert.assertEquals("Wrong word in iterator position " + iterator.getIndex(),
						expectedFirstFiveWords[i], word);
				i++;
			} else {
				end = true;
			}
		}
		Assert.assertEquals("The iterator should have gone through "+
				expectedFirstFiveWords.length+" words.",expectedFirstFiveWords.length,i);
	}

	@Test
	public void test__getWordsWithNoDecomposition() throws SQLException, ClassNotFoundException, ConfigException {
		corpusSqlHandler.openConnection();
		String[] wordsWithNoDecomposition = corpusSqlHandler.getWordsWithNoDecomposition();
		String[] expected = new String[]{
				"Haajsan","Haaki",
				"Haakimik","Haakinningatauq","Haakinniqalaurata"};
		Assert.assertEquals("The number of words without decomposition is wrong.", expected.length,
				wordsWithNoDecomposition.length);
		Assert.assertArrayEquals("The words without decomposition are not as expected.", expected, wordsWithNoDecomposition);
	}

	@Test
	public void test__getWordsWithDecompositions() throws SQLException, ClassNotFoundException, ConfigException {
		corpusSqlHandler.openConnection();
		String[] wordsWithNoDecomposition = corpusSqlHandler.getWordsWithDecompositions();
		String[] expected = new String[]{
				"Haakikkanniriaqalaaqpugut","Haakiliutinirmik","Haakiliutitillugit",
				"Haakiqataurataalaurmata","Haakiqatiqarniaratta"};
		Assert.assertEquals("The number of words with decompositions is wrong.", expected.length,
				wordsWithNoDecomposition.length);
		Assert.assertArrayEquals("The words with decompositions are not as expected.", expected, wordsWithNoDecomposition);
	}

	@Test
	public void test__getNbWordsWithNgram__with_and_without_decomposition() throws SQLException, ClassNotFoundException, ConfigException {
		corpusSqlHandler.openConnection();
		String ngram = "nni";
		int nbWordsWithNgram = corpusSqlHandler.getNbWordsWithNgram(ngram);
		int expected = 3;
		Assert.assertEquals("1. The number of words with the ngram '"+ngram+"' is not as expected.",expected,nbWordsWithNgram);
		ngram = "blah";
		nbWordsWithNgram = corpusSqlHandler.getNbWordsWithNgram(ngram);
		expected = 0;
		Assert.assertEquals("2. The number of words with the ngram '"+ngram+"' is not as expected.",expected,nbWordsWithNgram);
	}
	@Test
	public void test__getNbWordsWithNgram__with_decompositions_only() throws SQLException, ClassNotFoundException, ConfigException {
		corpusSqlHandler.openConnection();
		String ngram = "nni";
		int nbWordsWithNgram = corpusSqlHandler.getNbWordsWithNgram(ngram,true);
		int expected = 1;
		Assert.assertEquals("1. The number of words with the ngram '"+ngram+"' is not as expected.",expected,nbWordsWithNgram);
		ngram = "blah";
		nbWordsWithNgram = corpusSqlHandler.getNbWordsWithNgram("blah");
		expected = 0;
		Assert.assertEquals("2. The number of words with the ngram '"+ngram+"' is not as expected.",expected,nbWordsWithNgram);
	}

	@Test
	public void test__getWordsWithNgram__with_and_without_decomposition() throws SQLException, ClassNotFoundException, ConfigException {
		corpusSqlHandler.openConnection();
		String ngram = "nni";
		String[] wordsWithNgram = corpusSqlHandler.getWordsWithNgram(ngram);
		String[] expected = new String[]{
				"Haakikkanniriaqalaaqpugut", "Haakinningatauq","Haakinniqalaurata"};
		Assert.assertEquals("The number of words with the ngram '"+ngram+"' is wrong.", expected.length,
				wordsWithNgram.length);
		Assert.assertArrayEquals("The words with the ngram '"+ngram+"' are not as expected.", expected, wordsWithNgram);
	}

	@Test
	public void test__getWordsWithNgram__with_decompositions() throws SQLException, ClassNotFoundException, ConfigException {
		corpusSqlHandler.openConnection();
		String ngram = "nni";
		String[] wordsWithNgram = corpusSqlHandler.getWordsWithNgram(ngram,true);
		String[] expected = new String[]{
				"Haakikkanniriaqalaaqpugut"};
		Assert.assertEquals("The number of words with the ngram '"+ngram+"' which have decompositions is wrong.", expected.length,
				wordsWithNgram.length);
		Assert.assertArrayEquals("The words with the ngram '"+ngram+"' which have decompositions are not as expected.", expected, wordsWithNgram);
	}

	@Test
	public void test__readCorpus() throws IOException, ObjectStreamReaderException, ClassNotFoundException {
		String fPath = ResourceGetter.getResourcePath("org/iutools/corpus/testdata/smallCorpus.json");
		File f = new File(fPath);
		ObjectStreamReader reader = new ObjectStreamReader(f);
		WordInfo wordInfo = (WordInfo)reader.readObject();
		Assert.assertTrue("The first element in the json file should not be null.",wordInfo != null);
		Assert.assertEquals("","Haajsan",wordInfo.word);
	}



	////////////////////////////
	// TEST HELPERS
	////////////////////////////

}
