package org.iutools;

import static org.junit.Assert.*;

import org.junit.Assert;
import org.junit.Test;

public class NumericExpressionTest {


	@Test
	public void test__tokenIsNumberWithSuffix() {
		String token;
		NumericExpression numericTermParts;
	token = "34-mi";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "34", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", "-", numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts.morphemicEndPart);
	token = "$34,000-mi";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "$34,000", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", "-", numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts.morphemicEndPart);
	token = "4:30-mi";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "4:30", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", "-", numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts.morphemicEndPart);
	token = "5.5-mi";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "5.5", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", "-", numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts.morphemicEndPart);
	token = "5,500.33-mi";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "5,500.33", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", "-", numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts.morphemicEndPart);
	token = "bla";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts == null);
	token = "34\u2212mi"; // –
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "34", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", "\u2212", numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts.morphemicEndPart);
	token = "40\u2013mi";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "40", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", "\u2013", numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", "mi", numericTermParts.morphemicEndPart);
	token = "0.08%-mit";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "0.08%", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", "-", numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", "mit", numericTermParts.morphemicEndPart);
	token = "0.08%−mit";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "0.08%", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", "−", numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", "mit", numericTermParts.morphemicEndPart);
	token = "33mit";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "33", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", null, numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", "mit", numericTermParts.morphemicEndPart);
	token = "33-ti&ugu";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "33", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", "-", numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", "ti&ugu", numericTermParts.morphemicEndPart);
	token = "33-&ugu";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "33", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", "-", numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", "&ugu", numericTermParts.morphemicEndPart);
	token = "33";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "33", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", null, numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", null, numericTermParts.morphemicEndPart);
	token = "33.45";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "33.45", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", null, numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", null, numericTermParts.morphemicEndPart);
	token = "$33.45";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "$33.45", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", null, numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", null, numericTermParts.morphemicEndPart);
	token = "3.2%";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "3.2%", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", null, numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", null, numericTermParts.morphemicEndPart);
	token = "-3.2%";
	numericTermParts = NumericExpression.tokenIsNumberWithSuffix(token);
	Assert.assertTrue("Word "+token+" should have been acknowledged as a number-based word", numericTermParts != null);
	assertEquals("The 'number' part is not as expected.", "-3.2%", numericTermParts.numericFrontPart);
	assertEquals("The 'separator' part is not as expected.", null, numericTermParts.separator);
	assertEquals("The 'ending' part is not as expected.", null, numericTermParts.morphemicEndPart);
	}
	
}
