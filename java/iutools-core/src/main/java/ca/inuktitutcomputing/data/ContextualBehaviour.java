package ca.inuktitutcomputing.data;

public class ContextualBehaviour {
	
	/*
	 * context: last character of stem: V (vowel), t, k, q
	 * form: surface of the affix for in this context
	 * action1: action of the affix on (last character of) stem in this context;
	 *          in the vowel context, it must be only one vowel, not two
	 * action2: action of the affix in this context in case the stem (after
	 *          applying the action1) ends with 2 vowels
	 */
	
	Character context;
	String form;
	Action action1;
	Action action2;

	public ContextualBehaviour(Character _context, String _form, Action _action1, Action _action2) {
		this.context = _context;
		this.form = _form;
		this.action1 = _action1;
		this.action2 = _action2;
	}

}
