package org.iutools.morph;

import org.iutools.linguisticdata.LinguisticData;
import org.iutools.linguisticdata.Morpheme;
import org.iutools.morph.StateGraphForward;
import org.junit.Test;

import static org.junit.Assert.*;

public class StateGraphForwardTest {

	@Test
	public void test_nextState() {
		Morpheme morpheme = LinguisticData.getInstance().getMorpheme("inuk/1n");
		StateGraphForward.State stateFrom = StateGraphForward.initialState;
		StateGraphForward.State stateTo = stateFrom.nextState(morpheme);
		String expectedStateId = "nounstem";
		assertEquals("The noun root "+morpheme.id+" does not bring the state 'nounstem' from the initial state.",expectedStateId,stateTo.id);

		morpheme = LinguisticData.getInstance().getMorpheme("pisuk/1v");
		stateTo = stateFrom.nextState(morpheme);
		expectedStateId = "verbstem";
		assertEquals("The verb root "+morpheme.id+" does not bring the state 'verbstem' from the initial state.",expectedStateId,stateTo.id);

		morpheme = LinguisticData.getInstance().getMorpheme("juq/tv-ger-3s");
		stateFrom = StateGraphForward.getState("verbstem");
		stateTo = stateFrom.nextState(morpheme);
		expectedStateId = "verb";
		assertEquals("The verb root "+morpheme.id+" does not bring the state 'word' from the state 'verbstem'.",expectedStateId,stateTo.id);
	}

	@Test
	public void test_canBeFinal() {
		StateGraphForward.State state = StateGraphForward.getState("verbstem");
		boolean canBeFinal = state.canBeFinal();
		assertFalse("The state 'verbstem' cannot be final.",canBeFinal);

		state = StateGraphForward.getState("verb");
		canBeFinal = state.canBeFinal();
		assertTrue("The state 'verb' can be final.",canBeFinal);
	}

}
