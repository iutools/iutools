package ca.inuktitutcomputing.iutools;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import org.junit.Test;

import ca.inuktitutcomputing.config.IUConfig;
import ca.inuktitutcomputing.iutools.CorpusDocument_File;
import ca.inuktitutcomputing.iutools.CorpusReader;
import ca.inuktitutcomputing.iutools.CorpusReader_Directory;

/**
 * Unit test for simple App.
 */
public class CorpusReader_DirectoryTest 
{
    /**
     * Rigorous Test :-)
     * @throws Exception 
     */
	@Test
	public void test__CompiledCorpus__Synopsis() throws Exception {
	}

	@Test
    public void test__getFiles() throws Exception
    {
		CorpusReader corpusReader = new CorpusReader_Directory();
        String corpusDir = IUConfig.getIUDataPath()+"/src/test/CorpusReaderDirectory";
		Iterator<CorpusDocument_File> files = (Iterator<CorpusDocument_File>) ((CorpusReader_Directory) corpusReader).getFiles(corpusDir);
		Vector<String> fileNamesV = new Vector<String>();
		while (files.hasNext())
			fileNamesV.add(files.next().id);
		String[] fileNames = fileNamesV.toArray(new String[] {});
		Arrays.sort(fileNames);
		String[] expected = new String[] {
				corpusDir+"/fichier.doc", corpusDir+"/fichier.pdf", corpusDir+"/fichier.txt" };
		assertArrayEquals("The filenames returned are not correct.",expected,fileNames);
	}
	
	@Test
	public void test__getContents() throws Exception {
		CorpusReader corpusReader = new CorpusReader_Directory();
        String corpusDir = IUConfig.getIUDataPath()+"/src/test/CorpusReaderDirectory";
		Iterator<CorpusDocument_File> files = (Iterator<CorpusDocument_File>) ((CorpusReader_Directory) corpusReader).getFiles(corpusDir);
		String expected = "inuit tuktumik takulaaqtut sanallugu";
		while (files.hasNext()) {
			CorpusDocument_File f = files.next();
			String contenu = f.getContents().replaceAll("\\s+", " ").trim();
			assertEquals("The contents of the file "+f.id+" is not correct.",expected,contenu);
		}
	}
	

}
