package ca.inuktitutcomputing.morph.exp;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import ca.inuktitutcomputing.data.LinguisticDataAbstract;
import ca.inuktitutcomputing.data.LinguisticDataException;
import ca.inuktitutcomputing.data.SurfaceFormInContext;
import ca.nrc.json.PrettyPrinter;

public class FormGeneratorTest {

	@Test
	public void test_run__Case_root() throws FormGeneratorException, IOException, LinguisticDataException {
		FormGenerator formGenerator = new FormGenerator();
		String morpheme = "malik/1v";
		List<SurfaceFormInContext> forms = formGenerator.run(morpheme);
		List<SurfaceFormInContext> expectedForms = 
				new ArrayList<SurfaceFormInContext>(
						Arrays.asList(
						new SurfaceFormInContext("malik","","",morpheme),
						new SurfaceFormInContext("mali","","",morpheme),
						new SurfaceFormInContext("malip","","",morpheme),
						new SurfaceFormInContext("malit","","",morpheme),
						new SurfaceFormInContext("malig","","",morpheme),
						new SurfaceFormInContext("maling","","",morpheme),
						new SurfaceFormInContext("malij","","",morpheme),
						new SurfaceFormInContext("malil","","",morpheme),
						new SurfaceFormInContext("maliv","","",morpheme)));
		
		for (int i =0; i<expectedForms.size(); i++) {
			assertTrue("The element "+expectedForms.get(i).surfaceForm+" with contextual constraint '"+expectedForms.get(i).contextualConstraintOnStem+"' is not contained in the returned forms.",forms.contains(expectedForms.get(i)));
		}
	}

	@Test
	public void test_run__Case_infix_gaq() throws FormGeneratorException, IOException, LinguisticDataException {
		FormGenerator formGenerator = new FormGenerator();
		String morpheme = "gaq/1vn";
		List<SurfaceFormInContext> forms = formGenerator.run(morpheme);
		List<SurfaceFormInContext> expectedForms = 
				new ArrayList<SurfaceFormInContext>(
						Arrays.asList(
						new SurfaceFormInContext("gaq","V","V",morpheme),
						new SurfaceFormInContext("ga","V","V",morpheme),
						new SurfaceFormInContext("gar","V","V",morpheme),
						new SurfaceFormInContext("gan","V","V",morpheme),
						new SurfaceFormInContext("gam","V","V",morpheme),
						new SurfaceFormInContext("gal","V","V",morpheme),
						
						new SurfaceFormInContext("gaq","V","t",morpheme),
						new SurfaceFormInContext("ga","V","t",morpheme),
						new SurfaceFormInContext("gar","V","t",morpheme),
						new SurfaceFormInContext("gan","V","t",morpheme),
						new SurfaceFormInContext("gam","V","t",morpheme),
						new SurfaceFormInContext("gal","V","t",morpheme),
						
						new SurfaceFormInContext("gaq","V","k",morpheme),
						new SurfaceFormInContext("ga","V","k",morpheme),
						new SurfaceFormInContext("gar","V","k",morpheme),
						new SurfaceFormInContext("gan","V","k",morpheme),
						new SurfaceFormInContext("gam","V","k",morpheme),
						new SurfaceFormInContext("gal","V","k",morpheme),
						
						new SurfaceFormInContext("gaq","V","q",morpheme),
						new SurfaceFormInContext("ga","V","q",morpheme),
						new SurfaceFormInContext("gar","V","q",morpheme),
						new SurfaceFormInContext("gan","V","q",morpheme),
						new SurfaceFormInContext("gam","V","q",morpheme),
						new SurfaceFormInContext("gal","V","q",morpheme)
						)
						);
				
		assertEquals("The number of forms returned is not correct.",expectedForms.size(),forms.size());
		for (int i =0; i<expectedForms.size(); i++) {
			assertTrue("The element "+expectedForms.get(i).surfaceForm+" is not contained in the returned forms.",forms.contains(expectedForms.get(i)));
		}
	}

	@Test
	public void test_run__Case_infix_juq() throws FormGeneratorException, IOException, LinguisticDataException {
		FormGenerator formGenerator = new FormGenerator();
		String morpheme = "juq/1vn";
		List<SurfaceFormInContext> forms = formGenerator.run(morpheme);
		List<SurfaceFormInContext> expectedForms = 
				new ArrayList<SurfaceFormInContext>(
						Arrays.asList(
						new SurfaceFormInContext("juq","V","V",morpheme),
						new SurfaceFormInContext("ju","V","V",morpheme),
						new SurfaceFormInContext("jur","V","V",morpheme),
						new SurfaceFormInContext("jun","V","V",morpheme),
						new SurfaceFormInContext("jum","V","V",morpheme),
						new SurfaceFormInContext("jul","V","V",morpheme),
						
						new SurfaceFormInContext("tuq","C","t",morpheme),
						new SurfaceFormInContext("tu","C","t",morpheme),
						new SurfaceFormInContext("tur","C","t",morpheme),
						new SurfaceFormInContext("tun","C","t",morpheme),
						new SurfaceFormInContext("tum","C","t",morpheme),
						new SurfaceFormInContext("tul","C","t",morpheme),
						
						new SurfaceFormInContext("tuq","C","k",morpheme),
						new SurfaceFormInContext("tu","C","k",morpheme),
						new SurfaceFormInContext("tur","C","k",morpheme),
						new SurfaceFormInContext("tun","C","k",morpheme),
						new SurfaceFormInContext("tum","C","k",morpheme),
						new SurfaceFormInContext("tul","C","k",morpheme),
						
						new SurfaceFormInContext("tuq","C","q",morpheme),
						new SurfaceFormInContext("tu","C","q",morpheme),
						new SurfaceFormInContext("tur","C","q",morpheme),
						new SurfaceFormInContext("tun","C","q",morpheme),
						new SurfaceFormInContext("tum","C","q",morpheme),
						new SurfaceFormInContext("tul","C","q",morpheme)
						)
						);
		
		assertEquals("The number of forms returned is not correct.",expectedForms.size(),forms.size());
		for (int i =0; i<expectedForms.size(); i++) {
			assertTrue("The element "+expectedForms.get(i).surfaceForm+" is not contained in the returned forms.",forms.contains(expectedForms.get(i)));
		}
	}

	@Test
	public void test_run__Case_infix_liuq() throws FormGeneratorException, IOException, LinguisticDataException {
		FormGenerator formGenerator = new FormGenerator();
		String morpheme = "liuq/1nv";
		List<SurfaceFormInContext> forms = formGenerator.run(morpheme);
		List<SurfaceFormInContext> expectedForms = 
				new ArrayList<SurfaceFormInContext>(
						Arrays.asList(
						new SurfaceFormInContext("liuq","V","V",morpheme),
						new SurfaceFormInContext("liu","V","V",morpheme),
						new SurfaceFormInContext("liur","V","V",morpheme),
						new SurfaceFormInContext("liun","V","V",morpheme),
						new SurfaceFormInContext("lium","V","V",morpheme),
						new SurfaceFormInContext("liul","V","V",morpheme),
						
						new SurfaceFormInContext("liuq","V","t",morpheme),
						new SurfaceFormInContext("liu","V","t",morpheme),
						new SurfaceFormInContext("liur","V","t",morpheme),
						new SurfaceFormInContext("liun","V","t",morpheme),
						new SurfaceFormInContext("lium","V","t",morpheme),
						new SurfaceFormInContext("liul","V","t",morpheme),
						
						new SurfaceFormInContext("liuq","V","k",morpheme),
						new SurfaceFormInContext("liu","V","k",morpheme),
						new SurfaceFormInContext("liur","V","k",morpheme),
						new SurfaceFormInContext("liun","V","k",morpheme),
						new SurfaceFormInContext("lium","V","k",morpheme),
						new SurfaceFormInContext("liul","V","k",morpheme),
						
						new SurfaceFormInContext("liuq","V","q",morpheme),
						new SurfaceFormInContext("liu","V","q",morpheme),
						new SurfaceFormInContext("liur","V","q",morpheme),
						new SurfaceFormInContext("liun","V","q",morpheme),
						new SurfaceFormInContext("lium","V","q",morpheme),
						new SurfaceFormInContext("liul","V","q",morpheme),
						
						new SurfaceFormInContext("tiuq","C","t",morpheme),
						new SurfaceFormInContext("tiu","C","t",morpheme),
						new SurfaceFormInContext("tiur","C","t",morpheme),
						new SurfaceFormInContext("tiun","C","t",morpheme),
						new SurfaceFormInContext("tium","C","t",morpheme),
						new SurfaceFormInContext("tiul","C","t",morpheme),
						
						new SurfaceFormInContext("siuq","C","t",morpheme),
						new SurfaceFormInContext("siu","C","t",morpheme),
						new SurfaceFormInContext("siur","C","t",morpheme),
						new SurfaceFormInContext("siun","C","t",morpheme),
						new SurfaceFormInContext("sium","C","t",morpheme),
						new SurfaceFormInContext("siul","C","t",morpheme),
						
						new SurfaceFormInContext("jjiuq","V","q",morpheme),
						new SurfaceFormInContext("jjiu","V","q",morpheme),
						new SurfaceFormInContext("jjiur","V","q",morpheme),
						new SurfaceFormInContext("jjiun","V","q",morpheme),
						new SurfaceFormInContext("jjium","V","q",morpheme),
						new SurfaceFormInContext("jjiul","V","q",morpheme)
						
						)
						);
		
		assertEquals("The number of forms returned is not correct.",expectedForms.size(),forms.size());
		for (int i =0; i<expectedForms.size(); i++) {
			assertTrue("The element "+expectedForms.get(i).surfaceForm+" is not contained in the returned forms.",forms.contains(expectedForms.get(i)));
		}
	}

	@Test
	public void test_run__Case_infix_ijaq() throws FormGeneratorException, IOException, LinguisticDataException {
		FormGenerator formGenerator = new FormGenerator();
		String morpheme = "ijaq/1nv";
		List<SurfaceFormInContext> forms = formGenerator.run(morpheme);
		List<SurfaceFormInContext> expectedForms = 
				new ArrayList<SurfaceFormInContext>(
						Arrays.asList(
								new SurfaceFormInContext("ijaq","V","V",morpheme),
								new SurfaceFormInContext("ija","V","V",morpheme),
								new SurfaceFormInContext("ijar","V","V",morpheme),
								new SurfaceFormInContext("ijan","V","V",morpheme),
								new SurfaceFormInContext("ijam","V","V",morpheme),
								new SurfaceFormInContext("ijal","V","V",morpheme),
								
								new SurfaceFormInContext("ijaq","V","k",morpheme),
								new SurfaceFormInContext("ija","V","k",morpheme),
								new SurfaceFormInContext("ijar","V","k",morpheme),
								new SurfaceFormInContext("ijan","V","k",morpheme),
								new SurfaceFormInContext("ijam","V","k",morpheme),
								new SurfaceFormInContext("ijal","V","k",morpheme),
								
								new SurfaceFormInContext("ijaq","V","q",morpheme),
								new SurfaceFormInContext("ija","V","q",morpheme),
								new SurfaceFormInContext("ijar","V","q",morpheme),
								new SurfaceFormInContext("ijan","V","q",morpheme),
								new SurfaceFormInContext("ijam","V","q",morpheme),
								new SurfaceFormInContext("ijal","V","q",morpheme),
								
								new SurfaceFormInContext("ngijaq","VV","V",morpheme),
								new SurfaceFormInContext("ngija","VV","V",morpheme),
								new SurfaceFormInContext("ngijar","VV","V",morpheme),
								new SurfaceFormInContext("ngijan","VV","V",morpheme),
								new SurfaceFormInContext("ngijam","VV","V",morpheme),
								new SurfaceFormInContext("ngijal","VV","V",morpheme),
								
								new SurfaceFormInContext("ngijaq","VV","k",morpheme),
								new SurfaceFormInContext("ngija","VV","k",morpheme),
								new SurfaceFormInContext("ngijar","VV","k",morpheme),
								new SurfaceFormInContext("ngijan","VV","k",morpheme),
								new SurfaceFormInContext("ngijam","VV","k",morpheme),
								new SurfaceFormInContext("ngijal","VV","k",morpheme),
								
								new SurfaceFormInContext("ngijaq","VV","q",morpheme),
								new SurfaceFormInContext("ngija","VV","q",morpheme),
								new SurfaceFormInContext("ngijar","VV","q",morpheme),
								new SurfaceFormInContext("ngijan","VV","q",morpheme),
								new SurfaceFormInContext("ngijam","VV","q",morpheme),
								new SurfaceFormInContext("ngijal","VV","q",morpheme),
								
								new SurfaceFormInContext("aijaq","C","t",morpheme),
								new SurfaceFormInContext("aija","C","t",morpheme),
								new SurfaceFormInContext("aijar","C","t",morpheme),
								new SurfaceFormInContext("aijan","C","t",morpheme),
								new SurfaceFormInContext("aijam","C","t",morpheme),
								new SurfaceFormInContext("aijal","C","t",morpheme)

					));
		
		assertEquals("The number of forms returned is not correct.",expectedForms.size(),forms.size());
		for (int i =0; i<expectedForms.size(); i++) {
			assertTrue("The element "+expectedForms.get(i).surfaceForm+" in the context "+
					expectedForms.get(i).contextualConstraintOnStem+" and "+
					expectedForms.get(i).contextualContraintOnReceivingMorpheme+
					" is not contained in the returned forms.",forms.contains(expectedForms.get(i)));
		}
	}
	
	
	@Test
	public void test_formsWithBeginnings__Case_infix_ijaq() throws FormGeneratorException, IOException, LinguisticDataException {
		FormGenerator formGenerator = new FormGenerator();
		String morphemeId = "ijaq/1nv";
		HashSet<SurfaceFormInContext> formsWithBeginnings = formGenerator.formsWithBeginnings(morphemeId);
		List<SurfaceFormInContext> listFormsWithBeginnings = new ArrayList<SurfaceFormInContext>();
		listFormsWithBeginnings.addAll(formsWithBeginnings);
		HashSet<SurfaceFormInContext> expectedFormsWithBeginnings = new HashSet<>(
				Arrays.asList(
						new SurfaceFormInContext("ijaq","V","V",morphemeId),
						new SurfaceFormInContext("ijaq","V","k",morphemeId),
						new SurfaceFormInContext("ijaq","V","q",morphemeId),
						new SurfaceFormInContext("ngijaq","VV","V",morphemeId),
						new SurfaceFormInContext("ngijaq","VV","k",morphemeId),
						new SurfaceFormInContext("ngijaq","VV","q",morphemeId),
						new SurfaceFormInContext("aijaq","C","t",morphemeId)
						)
				);
		List<SurfaceFormInContext> listExpectedFormsWithBeginnings = new ArrayList<SurfaceFormInContext>();
		listExpectedFormsWithBeginnings.addAll(expectedFormsWithBeginnings);
		
		System.out.println("listFormsWithBeginnings= "+PrettyPrinter.print(listFormsWithBeginnings));
		
		assertEquals("",7,listFormsWithBeginnings.size());
		for (int i =0; i<listExpectedFormsWithBeginnings.size(); i++) {
			assertTrue("The element "+listExpectedFormsWithBeginnings.get(i).surfaceForm+" in the context "+listExpectedFormsWithBeginnings.get(i).contextualConstraintOnStem+" is not contained in the returned forms.",listFormsWithBeginnings.contains(listExpectedFormsWithBeginnings.get(i)));
		}
	}
	
	
	
}
