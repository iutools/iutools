package org.iutools.concordancer.tm.sql;

import org.iutools.sql.SQLPersistent;
import org.json.JSONObject;

/**
 * This class captures one language of a sentence alignment
 */
public class SentenceInLang extends SQLPersistent {
	public String lang = null;
	public String from_doc = null;
	public String text = null;
	public Long pair_num = null;
	public String sentence_id = null;


	public SentenceInLang(String _lang, String _text, String _from_doc, Long _pair_num) {
		super(new SentenceInLangSchema());
		init__SentenceInLang(_lang, _text, _from_doc, _pair_num);
	}

	public SentenceInLang(JSONObject json) {
		super(new SentenceInLangSchema());
		init__SentenceInLang(
			json.getString("lang"), json.getString("text"),
			json.getString("from_doc"), json.getLong("pair_num"));
	}

	private void init__SentenceInLang(String lang, String text, String from_doc, Long pair_num) {
		this.lang = lang;
		this.text = text;
		this.from_doc = from_doc;
		this.pair_num = pair_num;
		this.sentence_id = pair_num+":"+from_doc;
	}
}
