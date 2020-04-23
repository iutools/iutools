package ca.inuktitutcomputing.nunhansearch;

import static org.junit.Assert.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

import org.junit.Test;

import ca.nrc.config.ConfigException;


public class ProcessQueryTest {

	@Test
	public void test_grep() throws IOException, ConfigException {
		ProcessQuery queryProcessor = new ProcessQuery();
		File file = File.createTempFile("abc", "");
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		bw.write("abba:1:30\nblah:3:50:95:200\ngluck:2:123:167\nbloh:1:45");
		bw.close();

		String query = "blah";
		String result = queryProcessor.grep(query,file);
		String expected = "blah:3:50:95:200";
		assertEquals(expected,result);
		
		query = "*luc*";
		expected = "gluck:2:123:167";
		result = queryProcessor.grep(query,file);
		assertEquals(expected,result);
		
		query = "ab*";
		expected = "abba:1:30";
		result = queryProcessor.grep(query,file);
		assertEquals(expected,result);
		
		query = "*ah";
		expected = "blah:3:50:95:200";
		result = queryProcessor.grep(query,file);
		assertEquals(expected,result);
		
		query = "bl*";
		expected = "blah:3:50:95:200\nbloh:1:45";
		result = queryProcessor.grep(query,file);
		assertEquals(expected,result);
	}
	
	@Test
	public void test_getAlignments() throws IOException, ConfigException {
		File file = File.createTempFile("abc", "");
		BufferedWriter bw = new BufferedWriter(new FileWriter(file));
		String line1 = "19990401:: Une ligne avec le mot blah@----@ xyz_blah\n";
		bw.write(line1);
		String line2 = "19990402:: Autre ligne avec abba, etc.@----@ xyz_abba\n";
		bw.write(line2);
		String line3 = "19990403:: Autre ligne avec blah, etc.@----@ xyz_blah_etc\n";
		bw.write(line3);
		bw.close();
		ProcessQuery queryProcessor = new ProcessQuery();
		queryProcessor.setAlignedSentencesFile(file.getAbsolutePath());

		HashMap<String,Long[]> distAbba = new HashMap<String,Long[]>();
		distAbba.put("abba", new Long[] {(long)line1.length()});
		String[] alignments = queryProcessor.getAlignments(distAbba);
		assertEquals(1,alignments.length);
		assertEquals(line2.replaceAll("\r", "").replaceAll("\n", ""),alignments[0]);
		
		HashMap<String,Long[]> distBlah = new HashMap<String,Long[]>();
		distBlah.put("blah", new Long[] {(long)0,(long)line1.length()+(long)line2.length()});
		alignments = queryProcessor.getAlignments(distBlah);
		assertEquals(2,alignments.length);
		assertEquals(line1.replaceAll("\r", "").replaceAll("\n", ""),alignments[0]);
		assertEquals(line3.replaceAll("\r", "").replaceAll("\n", ""),alignments[1]);
		
		
	}

}
