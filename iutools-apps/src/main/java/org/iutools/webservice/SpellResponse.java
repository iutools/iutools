package org.iutools.webservice;

import java.util.List;

import org.iutools.spellchecker.SpellingCorrection;


public class SpellResponse extends ServiceResponse {

	public List<SpellingCorrection> correction;

	public SpellResponse() {
		super();
	}
}
