package ca.inuktitutcomputing.unitTests.applications;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.StringTokenizer;

import applications.DecomposeHansardTest;
import junit.framework.TestCase;

public class DecomposeHansardTestTest extends TestCase {
	
	public void test_locateFile__Case_read() {
		String file = "ressources/gstForTests.txt";
		String filePath = DecomposeHansardTest.locateFile(file);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filePath));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			assertTrue(false);
		}
		StringTokenizer st;
		/*
		 * aippaanik aippaanik {aippa:aippaq/1n}{anik:nganik/tn-acc-s-4s}
		 * aippaanit aippaanit {aippa:aippaq/1n}{anit:nganit/tn-abl-s-4s}
		 * aippanga aippanga {aippa:aippaq/1n}{nga:nga/tn-nom-s-4s}
		 */
		String expected[][] = {
				{ "aippaanik", "aippaanik", "{aippa:aippaq/1n}{anik:nganik/tn-acc-s-4s}" },
				{ "aippaanit", "aippaanit", "{aippa:aippaq/1n}{anit:nganit/tn-abl-s-4s}" },
				{ "aippanga", "aippanga", "{aippa:aippaq/1n}{nga:nga/tn-nom-s-4s}" }
		};
		int i=0;
		try {
			while ((st=DecomposeHansardTest.readLineST(br)) != null) {
			    boolean noProcessing = false;
			    String wordId = st.nextToken();
				String wordToBeAnalyzed = st.nextToken();
				String goldStandardDecomposition = st.nextToken();
				assertEquals((i+1)+"- Mot incorrect : '", expected[i][0], wordId);
				assertEquals((i+1)+"- Décomposition : '", expected[i][2], goldStandardDecomposition);
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}		
	
	}
	
	public void test_locateFile__Case_write() {
		String file = "outputFiles/faForTests.txt";
		String filePath = DecomposeHansardTest.locateFile(file);
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(filePath));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			assertTrue(false);
		}
		try {
			bw.write("xyz");
			bw.newLine();
			bw.close();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(filePath));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			assertTrue(false);
		}
		
	    //  blabla
		
		String expected2[] = {
				"xyz"
		};
		int i=0;
		StringTokenizer st;
		try {
			while ((st=DecomposeHansardTest.readLineST(br)) != null) {
			    String word = st.nextToken();
				assertEquals("2."+(i+1)+"- Ligne : '", expected2[i], word);
				i++;
			}
		} catch (IOException e) {
			e.printStackTrace();
			assertTrue(false);
		}	
		
		
	}
	
	
}
