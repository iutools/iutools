package org.iutools.webservice.spell;

import org.iutools.webservice.AssertServiceInputs;
import org.iutools.webservice.ServiceInputs;
import org.iutools.webservice.ServiceInputsTest;
import org.junit.jupiter.api.Test;

public class SpellInputsTest extends ServiceInputsTest {

	@Override
	protected ServiceInputs makeInputs() throws Exception {
		return new ServiceInputs();
	}

	@Test
	public void test__summarizeForLogging() throws Exception {
		ServiceInputs inputs =
			new SpellInputs("inukkksuk");
		new AssertServiceInputs(inputs)
			.logSummaryIs("{\"includePartiallyCorrect\":false,\"taskID\":null,\"text\":\"inukkksuk\"}");
			;
	}
}