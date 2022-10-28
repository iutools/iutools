package org.iutools.concordancer.tm.sql;

import org.apache.commons.collections.map.HashedMap;
import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.Alignment_ES;
import org.iutools.concordancer.WordAlignment;
import org.iutools.sql.SQLPersistent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Alignment_SQL extends Alignment_ES {

	public String from_doc = null;
	public long pair_num = -1;
	public List<String> topics = new ArrayList<String>();
	public Map<String, String> sentences = new HashMap<String,String>();
	public Map<String, WordAlignment> walign4langpair = new HashMap<String, WordAlignment>();
	public String web_domain = null;

	public Alignment_SQL(Alignment anAlignment) {
		super(anAlignment.from_doc, anAlignment.pair_num);
		init__Alignment_SQL(
			anAlignment.from_doc, anAlignment.web_domain, anAlignment.topics, anAlignment.pair_num, anAlignment.sentences);
	}

	public Alignment_SQL(String _from_doc, String _web_domain,
		List<String> _topics, Long _pair_num) {
		super(_from_doc, _pair_num);
		init__Alignment_SQL(
			_from_doc, _web_domain, _topics, _pair_num, (Map)null);
	}

	private void init__Alignment_SQL(String _from_doc, String _web_domain,
		List<String> _topics, Long _pair_num, Map<String, String> _sentences) {
		if (_sentences == null) {
			_sentences = new HashMap<String,String>();
		}
		if (_topics == null) {
			_topics = new ArrayList<String>();
		}
		setTopics(_topics);
		setWebDomain(_web_domain);
		for (String lang: _sentences.keySet()) {
			setSentence(lang, _sentences.get(lang));
		}
	}
}
