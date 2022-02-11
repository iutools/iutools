package org.iutools.concordancer;

import ca.nrc.dtrc.elasticsearch.Document;

import java.util.*;

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

	public Map<String,WordAlignment> walign4langpair = new HashMap<String,WordAlignment>();

	public Alignment_ES setWordAlignment(String l1, String l1Tokens, String l2,
		String l2Tokens, String l1_l2_wordpairs) {
		String[] l1TokensArr = l1Tokens.split("\\s+");
		String[] l2TokensArr = l2Tokens.split("\\s+");
		String[] matchedTokens = l1_l2_wordpairs.split("\\s");
		WordAlignment walign =
			new WordAlignment(l1, l1TokensArr, l2, l2TokensArr, matchedTokens);
		walign4langpair.put(walign.langPair, walign);
		return this;
	}

	public Alignment_ES() {
		init__Alignment_ES((String)null, (Long)null);
	}

	public Alignment_ES(String _fromDoc, long _pairNum) {
		init__Alignment_ES(_fromDoc, _pairNum);
	}

	private void init__Alignment_ES(String _fromDoc, Long _pairNum) {
		this.from_doc = _fromDoc;
		this.pair_num = _pairNum;
		this.type = "Alignment";
		return;
	}

	@Override
	public String getIdWithoutType() {
		return from_doc+"-p"+pair_num;
	}

	public Set<String> languages() {
		return sentences.keySet();
	}

	public String sentence4lang(String lang) {
			String sent = null;
			if (sentences.containsKey(lang)) {
				sent = sentences.get(lang);
			}
			return sent;
	}

	public SentencePair sentencePair(String l1, String l2) {
		SentencePair pair =
			new SentencePair(
				l1, sentence4lang(l1),
				l2, sentence4lang(l2)
			);

		WordAlignment walign = null;
		String langPair = l1+"-"+l2;
		if (walign4langpair.containsKey(langPair)) {
			walign = walign4langpair.get(langPair);
		} else {
			langPair = l2+"-"+l1;
			if (walign4langpair.containsKey(langPair)) {
				try {
					walign = walign4langpair.get(langPair);
					walign = walign.reverseDirection();
				} catch (WordAlignmentException e) {
					// If there is something wrong with the word alignments, just
					// don't set it.
				}
			}
		}

		if (walign != null) {
			pair.setTokenAlignments(walign);
		}

		return pair;
	}

	public boolean hasWordAlignmentForLangPair(String l1, String l2) {
		return (
			this.walign4langpair.containsKey(l1+"-"+l2) ||
			this.walign4langpair.containsKey(l2+"-"+l1));
	}
}
