package ca.inuktitutcomputing.unitTests.morph;

import ca.pirurvik.data.LinguisticDataAbstract;
import morph.Decomposition;
import morph.MorphInuk;
import junit.framework.TestCase;

public class MorphInukTest extends TestCase {

	public MorphInukTest (String sourceOfData) {
		LinguisticDataAbstract.init(sourceOfData);
	}
	
	public void testDecomposeWord_saqqitaujuq() {
		String word = "saqqitaujuq";
		Decomposition [] got_decs = MorphInuk.decomposeWord(word);
		String decsStr[] = new String[got_decs.length];
		String exp_decsStr[] = {
				"{saqqi:saqqik/1v}{ta:jaq/1vn}{u:u/1nv}{juq:juq/1vn}",
				"{saqqi:saqqik/1v}{ta:jaq/1vn}{u:u/1nv}{juq:juq/tv-ger-3s}"
		};
		for (int i=0; i<got_decs.length; i++) {
			String decStr = got_decs[i].toStr2();
			decsStr[i] = decStr;
		}
		for (int i=0; i<got_decs.length; i++) {
			assertEquals("Error: ",exp_decsStr[i],decsStr[i]);
		}
	}

	public void testDecomposeWord_tusaaji() {
		String word = "tusaaji";
		Decomposition [] got_decs = MorphInuk.decomposeWord(word);
		String decsStr[] = new String[got_decs.length];
		String exp_decsStr[] = {
				"{tusaaji:tusaaji/1n}",
				"{tusa:tusaq/1v}{a:a/1vv}{ji:ji/1vn}"
		};
		for (int i=0; i<got_decs.length; i++) {
			String decStr = got_decs[i].toStr2();
			decsStr[i] = decStr;
		}
		for (int i=0; i<got_decs.length; i++) {
			assertEquals("Error: ",exp_decsStr[i],decsStr[i]);
		}
	}
	
	public void testDecomposeWord_tusaajitiguuqtuq() {
		String word = "tusaajitiguuqtuq";
		Decomposition [] got_decs = MorphInuk.decomposeWord(word);
		String decsStr[] = new String[got_decs.length];
		System.err.println("Nb. got_decs: "+got_decs.length);
		String exp_decsStr[] = {
				"{tusaaji:tusaaji/1n}{tigut:tigut/tn-via-p}{uq:uq/1nv}{tuq:juq/1vn}",
				"{tusa:tusaq/1v}{a:a/1vv}{ji:ji/1vn}{tigut:tigut/tn-via-p}{uq:uq/1nv}{tuq:juq/1vn}",
				"",
				""
		};
		for (int i=0; i<got_decs.length; i++) {
			String decStr = got_decs[i].toStr2();
			decsStr[i] = decStr;
		}
		for (int i=0; i<got_decs.length; i++) {
			assertEquals("Error: ",exp_decsStr[i],decsStr[i]);
		}
	}
	
	public void testDecomposeWord_Case_assimilation_in_root() {
		// ll < bl - allur- < abluq-
		String word = "allurtuq";
		Decomposition [] got_decs = MorphInuk.decomposeWord(word);
		String decsStr[] = new String[got_decs.length];
		String exp_decsStr[] = {
				"{allur:abluq/1v}{tuq:juq/1vn}",
				"{allur:abluq/1v}{tuq:juq/tv-ger-3s}",
		};
		for (int i=0; i<got_decs.length; i++) {
			String decStr = got_decs[i].toStr2();
			decsStr[i] = decStr;
		}
		for (int i=0; i<got_decs.length; i++) {
			assertEquals("Error: ",exp_decsStr[i],decsStr[i]);
		}
	}

}
