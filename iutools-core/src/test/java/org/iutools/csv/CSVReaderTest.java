package org.iutools.csv;

import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class CSVReaderTest {

	@Test
	public void test_contructor() throws IOException {
		File tempFile = File.createTempFile("temp",null);
		tempFile.deleteOnExit();
		FileWriter fw = new FileWriter(tempFile);
		fw.write("morpheme,type,meaning\n");
		fw.write("inuk,n,\"some text, with coma\"");
		fw.close();
		BufferedReader br = new BufferedReader(new FileReader(tempFile));
		CSVReader csvReader = new CSVReader(br);
		String [] gotFields = csvReader.fieldNames;
		Assert.assertEquals("Field no 1 is incorrect.","morpheme",gotFields[0]);
		Assert.assertEquals("Field no 2 is incorrect.","type",gotFields[1]);
		Assert.assertEquals("Field no 3 is incorrect.","meaning",gotFields[2]);
	}

	@Test
	public void test_readNext() throws IOException {
		File tempFile = File.createTempFile("temp",null);
		tempFile.deleteOnExit();
		FileWriter fw = new FileWriter(tempFile);
		fw.write("morpheme,type,meaning\n");
		fw.write("inuk,n,\"some text, with coma\"");
		fw.close();
		BufferedReader br = new BufferedReader(new FileReader(tempFile));
		CSVReader csvReader = new CSVReader(br);
		Map<String,String> gotData = csvReader.readNext();
		Assert.assertEquals("Field no 1 is incorrect.","inuk",gotData.get("morpheme"));
		Assert.assertEquals("Field no 2 is incorrect.","n",gotData.get("type"));
		Assert.assertEquals("Field no 3 is incorrect.","some text, with coma",gotData.get("meaning"));
	}

}
