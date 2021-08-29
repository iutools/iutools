package org.iutools.morph.l2r;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.*;

import org.junit.Test;
import org.junit.Ignore;

import org.iutools.linguisticdata.LinguisticDataException;
import org.iutools.linguisticdata.SurfaceFormInContext;

public class FormGeneratorTest {

	@Test
	public void test_run__Case_root_in_k_no_group_of_consonants() throws FormGeneratorException, IOException, LinguisticDataException {
		FormGenerator formGenerator = new FormGenerator();
		String morpheme = "malik/1v";
		List<SurfaceFormInContext> forms = formGenerator.run(morpheme);
		List<String> expectedForms = 
				new ArrayList<String>(
						Arrays.asList(
						new SurfaceFormInContext("mali",null,null,morpheme).toString(),
						new SurfaceFormInContext("malik",null,null,morpheme).toString(),
						new SurfaceFormInContext("malip",null,null,morpheme).toString(),
						new SurfaceFormInContext("malit",null,null,morpheme).toString(),
						new SurfaceFormInContext("malis",null,null,morpheme).toString(),
						new SurfaceFormInContext("malig",null,null,morpheme).toString(),
						new SurfaceFormInContext("maliv",null,null,morpheme).toString(),
						new SurfaceFormInContext("malil",null,null,morpheme).toString(),
						new SurfaceFormInContext("malij",null,null,morpheme).toString(),
						new SurfaceFormInContext("maling",null,null,morpheme).toString(),
						new SurfaceFormInContext("malim",null,null,morpheme).toString(),
						new SurfaceFormInContext("malin",null,null,morpheme).toString()
						));
		
		assertEquals("The number of forms returned is incorrect.",forms.size(),expectedForms.size());
		for (int i =0; i<expectedForms.size(); i++) {
			String form = forms.get(i).toString();
			assertTrue("The surface form object "+expectedForms.get(i)+" is not in the returned objects.",expectedForms.contains(form));
		}
	}

	@Test
	public void test_run__Case_root_in_k_with_group_of_consonants() throws FormGeneratorException, IOException, LinguisticDataException {
		FormGenerator formGenerator = new FormGenerator();
		String morpheme = "iksik/1v";
		List<SurfaceFormInContext> forms = formGenerator.run(morpheme);
		List<String> expectedForms = 
				new ArrayList<String>(
						Arrays.asList(
								new SurfaceFormInContext("iksi",null,null,morpheme).toString(),
								new SurfaceFormInContext("iksik",null,null,morpheme).toString(),
								new SurfaceFormInContext("iksip",null,null,morpheme).toString(),
								new SurfaceFormInContext("iksit",null,null,morpheme).toString(),
								new SurfaceFormInContext("iksis",null,null,morpheme).toString(),
								new SurfaceFormInContext("iksig",null,null,morpheme).toString(),
								new SurfaceFormInContext("iksiv",null,null,morpheme).toString(),
								new SurfaceFormInContext("iksil",null,null,morpheme).toString(),
								new SurfaceFormInContext("iksij",null,null,morpheme).toString(),
								new SurfaceFormInContext("iksing",null,null,morpheme).toString(),
								new SurfaceFormInContext("iksim",null,null,morpheme).toString(),
								new SurfaceFormInContext("iksin",null,null,morpheme).toString(),
								
								new SurfaceFormInContext("issi",null,null,morpheme).toString(),
								new SurfaceFormInContext("issik",null,null,morpheme).toString(),
								new SurfaceFormInContext("issip",null,null,morpheme).toString(),
								new SurfaceFormInContext("issit",null,null,morpheme).toString(),
								new SurfaceFormInContext("issis",null,null,morpheme).toString(),
								new SurfaceFormInContext("issig",null,null,morpheme).toString(),
								new SurfaceFormInContext("issiv",null,null,morpheme).toString(),
								new SurfaceFormInContext("issil",null,null,morpheme).toString(),
								new SurfaceFormInContext("issij",null,null,morpheme).toString(),
								new SurfaceFormInContext("issing",null,null,morpheme).toString(),
								new SurfaceFormInContext("issim",null,null,morpheme).toString(),
								new SurfaceFormInContext("issin",null,null,morpheme).toString(),
								
								new SurfaceFormInContext("itsi",null,null,morpheme).toString(),
								new SurfaceFormInContext("itsik",null,null,morpheme).toString(),
								new SurfaceFormInContext("itsip",null,null,morpheme).toString(),
								new SurfaceFormInContext("itsit",null,null,morpheme).toString(),
								new SurfaceFormInContext("itsis",null,null,morpheme).toString(),
								new SurfaceFormInContext("itsig",null,null,morpheme).toString(),
								new SurfaceFormInContext("itsiv",null,null,morpheme).toString(),
								new SurfaceFormInContext("itsil",null,null,morpheme).toString(),
								new SurfaceFormInContext("itsij",null,null,morpheme).toString(),
								new SurfaceFormInContext("itsing",null,null,morpheme).toString(),
								new SurfaceFormInContext("itsim",null,null,morpheme).toString(),
								new SurfaceFormInContext("itsin",null,null,morpheme).toString()
						));
		
		assertEquals("The number of forms returned is incorrect.",forms.size(),expectedForms.size());
		for (int i =0; i<expectedForms.size(); i++) {
			String form = forms.get(i).toString();
			assertTrue("The surface form object "+expectedForms.get(i)+" is not in the returned objects.",expectedForms.contains(form));
		}
	}

	@Test
	public void test_run__Case_infix_gaq() throws FormGeneratorException, IOException, LinguisticDataException {
		FormGenerator formGenerator = new FormGenerator();
		String morphemeId = "gaq/1vn";
		List<SurfaceFormInContext> forms = formGenerator.run(morphemeId);
		List<SurfaceFormInContext> expectedForms = 
				new ArrayList<SurfaceFormInContext>(
						Arrays.asList(
						new SurfaceFormInContext("gaq","V",'V',morphemeId),
						new SurfaceFormInContext("ga","V",'V',morphemeId),
						new SurfaceFormInContext("gar","V",'V',morphemeId),
						
						new SurfaceFormInContext("gaq","V",'C',morphemeId),
						new SurfaceFormInContext("ga","V",'C',morphemeId),
						new SurfaceFormInContext("gar","V",'C',morphemeId)
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
		String morphemeId = "juq/1vn";
		List<SurfaceFormInContext> forms = formGenerator.run(morphemeId);
		List<SurfaceFormInContext> expectedForms = 
				new ArrayList<SurfaceFormInContext>(
						Arrays.asList(
						new SurfaceFormInContext("juq","V",'V',morphemeId),
						new SurfaceFormInContext("ju","V",'V',morphemeId),
						new SurfaceFormInContext("jur","V",'V',morphemeId),

						new SurfaceFormInContext("tuq","C",'C',morphemeId),
						new SurfaceFormInContext("tu","C",'C',morphemeId),
						new SurfaceFormInContext("tur","C",'C',morphemeId)
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
		String morphemeId = "liuq/1nv";
		List<SurfaceFormInContext> forms = formGenerator.run(morphemeId);
		List<SurfaceFormInContext> expectedForms = 
				new ArrayList<SurfaceFormInContext>(
						Arrays.asList(
						new SurfaceFormInContext("liuq","V",'V',morphemeId),
						new SurfaceFormInContext("liu","V",'V',morphemeId),
						new SurfaceFormInContext("liur","V",'V',morphemeId),

						new SurfaceFormInContext("liuq","V",'C',morphemeId),
						new SurfaceFormInContext("liu","V",'C',morphemeId),
						new SurfaceFormInContext("liur","V",'C',morphemeId),

						new SurfaceFormInContext("tiuq","C",'t',morphemeId),
						new SurfaceFormInContext("tiu","C",'t',morphemeId),
						new SurfaceFormInContext("tiur","C",'t',morphemeId),

						new SurfaceFormInContext("siuq","C",'t',morphemeId),
						new SurfaceFormInContext("siu","C",'t',morphemeId),
						new SurfaceFormInContext("siur","C",'t',morphemeId),

						new SurfaceFormInContext("jjiuq","V",'q',morphemeId),
						new SurfaceFormInContext("jjiu","V",'q',morphemeId),
						new SurfaceFormInContext("jjiur","V",'q',morphemeId)

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
		String morphemeId = "ijaq/1nv";
		List<SurfaceFormInContext> forms = formGenerator.run(morphemeId);
		List<SurfaceFormInContext> expectedForms = 
				new ArrayList<SurfaceFormInContext>(
						Arrays.asList(
								new SurfaceFormInContext("ijaq","V",'V',morphemeId),
								new SurfaceFormInContext("ija","V",'V',morphemeId),
								new SurfaceFormInContext("ijar","V",'V',morphemeId),

								new SurfaceFormInContext("ijaq","V",'k',morphemeId),
								new SurfaceFormInContext("ija","V",'k',morphemeId),
								new SurfaceFormInContext("ijar","V",'k',morphemeId),

								new SurfaceFormInContext("ijaq","V",'q',morphemeId),
								new SurfaceFormInContext("ija","V",'q',morphemeId),
								new SurfaceFormInContext("ijar","V",'q',morphemeId),

								new SurfaceFormInContext("ngijaq","2V",'V',morphemeId),
								new SurfaceFormInContext("ngija","2V",'V',morphemeId),
								new SurfaceFormInContext("ngijar","2V",'V',morphemeId),

								new SurfaceFormInContext("ngijaq","2V",'k',morphemeId),
								new SurfaceFormInContext("ngija","2V",'k',morphemeId),
								new SurfaceFormInContext("ngijar","2V",'k',morphemeId),

								new SurfaceFormInContext("ngijaq","2V",'q',morphemeId),
								new SurfaceFormInContext("ngija","2V",'q',morphemeId),
								new SurfaceFormInContext("ngijar","2V",'q',morphemeId),

								new SurfaceFormInContext("aijaq","t",'t',morphemeId),
								new SurfaceFormInContext("aija","t",'t',morphemeId),
								new SurfaceFormInContext("aijar","t",'t',morphemeId)

					));
		
		assertEquals("The number of forms returned is not correct.",expectedForms.size(),forms.size());
		for (int i =0; i<expectedForms.size(); i++) {
			assertTrue("The element "+expectedForms.get(i).surfaceForm+" in the context "+
					expectedForms.get(i).endOfStem+" and "+
					expectedForms.get(i).context+
					" is not contained in the returned forms.",forms.contains(expectedForms.get(i)));
		}
	}


	@Test @Ignore
	public void test_run__Case_infix_it_tn_nom_p() throws FormGeneratorException, IOException, LinguisticDataException {
		FormGenerator formGenerator = new FormGenerator();
		String morphemeId = "it/tn-nom-p";
		List<SurfaceFormInContext> forms = formGenerator.run(morphemeId);
		List<SurfaceFormInContext> expectedForms =
				new ArrayList<SurfaceFormInContext>(
						Arrays.asList(
								new SurfaceFormInContext("it","1V",'V',morphemeId),
								new SurfaceFormInContext("t","2V",'V',morphemeId),

								new SurfaceFormInContext("it","t",'t',morphemeId),

								new SurfaceFormInContext("it","1V",'k',morphemeId),
								new SurfaceFormInContext("t","2V",'k',morphemeId),

								new SurfaceFormInContext("it","1V",'q',morphemeId),
								new SurfaceFormInContext("siu","2V",'1',morphemeId)

						)
				);

		assertEquals("The number of forms returned is not correct.",expectedForms.size(),forms.size());
		for (int i =0; i<expectedForms.size(); i++) {
			assertTrue("The element "+expectedForms.get(i).surfaceForm+" is not contained in the returned forms.",forms.contains(expectedForms.get(i)));
		}
	}

	
	
}
