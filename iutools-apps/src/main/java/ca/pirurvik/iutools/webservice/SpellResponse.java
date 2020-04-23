package ca.pirurvik.iutools.webservice;

import java.util.List;

import ca.pirurvik.iutools.search.SearchHit;
import ca.pirurvik.iutools.spellchecker.SpellingCorrection;


public class SpellResponse extends ServiceResponse {

	public List<SpellingCorrection> correction;

}
