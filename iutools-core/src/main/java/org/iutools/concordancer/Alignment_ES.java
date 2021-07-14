package org.iutools.concordancer;

import ca.nrc.dtrc.elasticsearch.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Alignment_ES extends Document {
	public String from_doc = null;
	public Long pair_num = null;

	public String web_domain = null;
			public Alignment_ES setWebDomain(String _domain) {
				this.web_domain = _domain;
				return this;
			}

	public List<String> topics = new ArrayList<String>();
		public Alignment_ES setTopics(List<String> _topics) {
			this.topics = _topics;
			return this;
		}

	public Map<String,String> sentences = new HashMap<String,String>();
		public Alignment_ES setSentence(String lang, String sent) {
			this.sentences.put(lang, sent);
			return this;
		}

	public Alignment_ES() {
	}

	public Alignment_ES(String fromDoc, long _pairNum) {
		this.from_doc = fromDoc;
		this.pair_num = _pairNum;
	}

	@Override
	public String getId() {
		return from_doc+": p"+pair_num;
	}

	public String sentence4lang(String lang) {
			String sent = null;
			if (sentences.containsKey(lang)) {
				sent = sentences.get(lang);
			}
			return sent;
	}
}
