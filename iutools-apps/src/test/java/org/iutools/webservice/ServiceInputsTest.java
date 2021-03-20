package org.iutools.webservice;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public abstract class ServiceInputsTest {

	protected abstract ServiceInputs makeInputs() throws Exception;

	ServiceInputs inputs = null;

	@BeforeEach
	public void setUp() throws Exception {
		inputs = makeInputs();
	}

	@Test
	public void test__summarizeForLogging() throws Exception {
		String mess =
			"Test class "+this.getClass().getSimpleName()+
			" should override this test method.\n"+
			"The test should check that the method "+
			inputs.getClass().getSimpleName()+".summarizeForLogging() returns the "+
			"appropriate values for different inputs."
			;
		Assertions.fail(mess);
	}
}
