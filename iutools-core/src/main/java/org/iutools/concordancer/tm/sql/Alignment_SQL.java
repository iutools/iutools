package org.iutools.concordancer.tm.sql;

import org.apache.commons.collections.map.HashedMap;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.WordAlignment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Alignment_SQL extends Alignment {

	public String from_doc = null;
	public long pair_num = -1;
	public List<String> topics = new ArrayList<String>();
	public Map<String, String> sentences = new HashMap<String,String>();
	public Map<String, WordAlignment> walign4langpair = new HashMap<String, WordAlignment>();
	public String web_domain = null;

	public Alignment_SQL(Alignment anAlignment) {
		super(anAlignment.from_doc, anAlignment.pair_num);
		setTopics(anAlignment.topics);
		setWebDomain(anAlignment.web_domain);
		for (String lang: anAlignment.languages()) {
			setSentence(lang, anAlignment.sentence4lang(lang));
		}
	}
}
