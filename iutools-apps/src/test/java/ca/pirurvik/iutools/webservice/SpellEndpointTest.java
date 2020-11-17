package ca.pirurvik.iutools.webservice;

import ca.inuktitutcomputing.utilities.StopWatch;
import ca.nrc.testing.AssertNumber;
import org.junit.Before;
import org.junit.Test;

import ca.nrc.ui.web.testing.MockHttpServletResponse;

public class SpellEndpointTest {

	SpellEndpoint endPoint = null;
	
	@Before
	public void setUp() throws Exception {
		endPoint = new SpellEndpoint();
	}

	
	/***********************
	 * VERIFICATION TESTS
	 ***********************/	
	
	@Test
	public void test__SpellEndpoint__Syllabic__HappyPath() throws Exception {
		
		SpellInputs spellInputs = new SpellInputs("ᐃᓄᑦᒧᑦ ᑕᑯᔪᖅ");
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.SPELL,
					spellInputs
				);
		
		SpellCheckerAssertion.assertThat(response, "")
			.raisedNoError()
			.nthCorrectionIs(0, true,
				new String[] {
					"ᐃᓄᒻᒧᑦ",
					"ᐃᓄᑐᐊᒧᑦ",
					"ᐃᓄᑐᐊᑦ",
					"ᐃᓄᒃᑐᑦ",
					"ᐃᓄᕗᑦ",
					"ᐃᓄᒻᒧᑦ",
					"ᐃᓄᑐᐊᒧᑦ",
					"ᐃᓄᑐᐊᑦ",
					"ᐃᓄᒃᑐᑦ",
					"ᐃᓄᕗᑦ"
				}
			)
			.nthCorrectionIs(1, false)
			;
		
		return;	
	}

	@Test
	public void test__SpellEndpoint__WordWithSyllCharsThatAreExpressedAsTwoRomanChars() 
			throws Exception {
		
		SpellInputs spellInputs = new SpellInputs("ᒐᕙᒪᒃᑯᑎᒍᑦ");
				
		MockHttpServletResponse response = 
				IUTServiceTestHelpers.postEndpointDirectly(
					IUTServiceTestHelpers.EndpointNames.SPELL,
					spellInputs
				);
		
		SpellCheckerAssertion.assertThat(response, "")
			.raisedNoError()
			.nthCorrectionIs(0, true,
				new String[]{
					"ᒐᕙᒪᒃᑎᒍᑦ",
					"ᒐᕙᒪᒃᑯᑎᑐᑦ",
					"ᒐᕙᒪᒃᑯᑎᓐᓄᑦ",
					"ᒐᕙᒪᒃᑯᖏᑎᒍᑦ",
					"ᒐᕙᒪᑎᒍᑦ",
					"ᒐᕙᒪᒃᑎᒍᑦ",
					"ᒐᕙᒪᒃᑯᑎᑐᑦ",
					"ᒐᕙᒪᒃᑯᑎᓐᓄᑦ",
					"ᒐᕙᒪᒃᑯᖏᑎᒍᑦ",
					"ᒐᕙᒪᑎᒍᑦ"
				});
		
		return;	
	}

    @Test
    public void test__SpellEndpoint__SpeedTest() throws Exception {

		SpellInputs spellInputs = new SpellInputs(
				"ᒪᓕᒐᓕᐅᖅᑎ ᔫ ᓴᕕᑲᑖᖅ ᓂᕈᐊᖅᑕᐅᒃᑲᓐᓂᓚᐅᖅᐳᖅ ᓂᕈᐊᕕᒡᔪᐊᕐᓇᐅᑎᓪᓗᒍ ᐅᑐᐱᕆ 30, " +
						"2017-ᒥ, ᑭᒡᒐᖅᑐᖅᑎᐅᓂᐊᖅᖢᓂ ᐊᕐᕕᐊᑦ ᓂᒋᐊᓄᑦ ᑕᓪᓕᒪᖓᓐᓂ ᒪᓕᒐᓕᕐᕕᖕᒥᑦ ᓄᓇᕗᒻᒥ. " +
						"ᔫ ᓴᕕᑲᑖᖅ ᓂᕈᐊᖅᑕᐅᓚᐅᖅᐳᖅ ᐱᔨᑦᑎᕋᖁᔭᐅᓪᓗᓂᑦ ᓯᕗᓕᖅᑏᑦ ᑲᑎᒪᔨᖏᓐᓄᑦ ᓄᕕᐱᕆ 17, " +
						"2017−ᒥᑦ, ᑲᑎᒪᑎᓪᓗᒋᑦ ᓄᓇᕗᒻᒥ ᓯᕗᓕᖅᑎᑦ. ᒥᓂᔅᑕ ᓴᕕᑲᑖᖅ ᐊᖏᖅᑎᑕᐅᓚᐅᖅᑐᖅ ᒪᓕᒐᓕᐅᕐᕕᖕᒧᑦ " +
						"ᓄᕕᐱᕆ 21, 2017-ᒥᑦ. ᓯᕗᓕᖅᑎᒧᑦ ᑐᒡᓕᕆᔭᐅᔪᖅ, ᒥᓂᔅᑕ ᐱᕙᓪᓕᐊᔪᓕᕆᔨᒃᑯᓐᓄᑦ " +
						"ᐃᖏᕐᕋᔪᓕᕆᔨᒃᑯᓐᓄᓪᓗ ᐊᒻᒪ ᒥᓂᔅᑕᐅᓪᓗᓂ ᐊᕙᑎᓕᕆᔨᒃᑯᓐᓄᑦ. ᒥᓂᔅᑕᐅᖕᒥᔪᖅ ᓄᓇᕗᒻᒥ " +
						"ᓇᖕᒥᓂᖃᖅᑐᓄᑦ ᐊᑭᓕᒃᓴᓂᒡᕕᖕᒥ, ᓄᓇᕗᒻᒥ ᐱᕙᓪᓕᐊᔪᓕᕆᔨᒃᑯᑦ ᑯᐊᐳᕇᓴᒃᑯᓐᓄᑦ, " +
						"ᐆᒻᒪᖅᑯᑎᓕᕆᔨᒃᑯᓐᓄᑦ, ᐊᒻᒪ ᐅᔭᕋᖕᓂᐊᖅᑐᓕᕆᔨᒃᑯᓐᓄᑦ.");

		long startMSecs = StopWatch.nowMSecs();
		MockHttpServletResponse response =
				IUTServiceTestHelpers.postEndpointDirectly(
						IUTServiceTestHelpers.EndpointNames.SPELL,
						spellInputs
				);
		double elapsedSecs = StopWatch.elapsedMsecsSince(startMSecs) / 1000.0;
		AssertNumber.performanceHasNotChanged(
			"Spell checking time",
			elapsedSecs, 50.0, 10.0, false);
	}
}
