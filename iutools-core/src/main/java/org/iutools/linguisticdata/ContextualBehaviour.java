package org.iutools.linguisticdata;

import java.util.ArrayList;
import java.util.List;

public class ContextualBehaviour {
	
	/*
	 * context: last character of stem: V (any vowel), t, k, q
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

	/**
	 * The forms of an affix depend on the context and on the action(s) in that context.
	 * An affix may have a different form in different contexts.
	 * In the case of a consonantal context where the first action is deletion of
	 * the stem's final consonant resulting in a stem ending with 2 vowels, and
	 * in the case of a vowel context where the stem's ends with 2 vowels,
	 * the second action may have an effect on the surface form of the affix,
	 * for example by inserting one or two characters.
	 * @return A Set of SurfaceFormOfAffix
	 */
	public List<String[]> formsInContext() {
		// eg. allak/1vv in 'k' context 1) deletes 'k' and 2) inserts 'ra' if VV
		//   1. get the canonical of the affix in this context; this will normally be the surface form in that context : allak
		//   2. apply action 1 (may modify the default surface form, like insertion): allak
		//   3. apply action 2 (may modify the form resulting of action1 for cases of VV stems)
		List<String[]> formAndEndOfStemInContext = new ArrayList<String[]>();
		String[] formAndEndOfStemInContextAfterAction1 = this.action1.formAndEndOfStemInContext(this.form,this.context,1);
		if (formAndEndOfStemInContextAfterAction1!=null)
			formAndEndOfStemInContext.add(formAndEndOfStemInContextAfterAction1);
		if (this.action2 != null && this.action2.type != Action.NULLACTION) {
			String[] formAndEndOfStemInContextAfterAction2 =
					this.action2.formAndEndOfStemInContext(formAndEndOfStemInContextAfterAction1[0],this.context,2);
			if (formAndEndOfStemInContextAfterAction2!=null)
				formAndEndOfStemInContext.add(formAndEndOfStemInContextAfterAction2);
		}
		return formAndEndOfStemInContext;
	}

}
