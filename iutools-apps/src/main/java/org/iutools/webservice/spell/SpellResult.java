package org.iutools.webservice.spell;

import org.iutools.spellchecker.SpellingCorrection;
import org.iutools.webservice.EndpointResult;

import java.util.List;

public class SpellResult extends EndpointResult {

	public List<SpellingCorrection> correction;

	public SpellResult() {
		super();
	}
}