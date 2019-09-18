package ca.inuktitutcomputing.data;

import org.junit.Test;

import ca.inuktitutcomputing.dataCSV.LinguisticDataCSV;
import org.junit.*;

public class LinguisticDataCSVTest {
	
	@Test
	public void test_getBases() throws Exception {
		LinguisticDataCSV data = new LinguisticDataCSV("r");
		Base gotBase = data.getBase("iglu/1n");
		Assert.assertEquals("Morpheme for base iglu/1n was not as expected.", "iglu", gotBase.morpheme);
	}

}
