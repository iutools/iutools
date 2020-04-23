package ca.inuktitutcomputing.morph.exp;

import static org.junit.Assert.*;

import java.util.Set;

import org.junit.Test;

import ca.nrc.json.PrettyPrinter;

public class DialectalGeminateTest {

	@Test
	public void test_formsWithGeminate() {
		String morpheme = "ikpaksaq";
		Set<String> variants = DialectalGeminate.formsWithGeminate(morpheme);
		System.out.println(PrettyPrinter.print(variants));
	}

}
