package org.iutools.webservice.spell;

import org.iutools.spellchecker.SpellingCorrection;
import org.iutools.webservice.EndpointResult;

import java.util.List;

public class Spell2Result extends EndpointResult {

	public List<SpellingCorrection> correction;

	public Spell2Result() {
		super();
	}
}