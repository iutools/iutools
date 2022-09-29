package org.iutools.concordancer;

import ca.nrc.dtrc.elasticsearch.Document;
import ca.nrc.json.PrettyPrinter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Alignment extends Document {

	public String from_doc = null;
	public Long pair_num = null;
	public String web_domain = null;


	public Alignment() {
		super();
		init__Alignment((String)null, (Long)null);
		return;
	}

	public Alignment(String _fromDoc, long _pairNum) {
		super();
		init__Alignment(_fromDoc, _pairNum);
		return;
	}

	private void init__Alignment(String _fromDoc, Long _pairNum) {
		this.from_doc = _fromDoc;
		this.pair_num = _pairNum;
		return;
	}

	public Alignment setWebDomain(String _domain) {
		this.web_domain = _domain;
		return this;
	}

	public List<String> topics = new ArrayList<String>();
		public Alignment setTopics(List<String> _topics) {
			this.topics = _topics;
			return this;
		}

	public Map<String,String> sentences = new HashMap<String,String>();
		public Alignment setSentence(String lang, String sent) {
			this.sentences.put(lang, sent);
			return this;
		}

	public Set<String> languages() {
		return sentences.keySet();
	}

	public Map<String,WordAlignment> walign4langpair = new HashMap<String,WordAlignment>();

	public Alignment setWordAlignment(String l1, String l1Tokens, String l2,
		String l2Tokens, String l1_l2_wordpairs) {
		String[] l1TokensArr = l1Tokens.split("\\s+");
		String[] l2TokensArr = l2Tokens.split("\\s+");
		String[] matchedTokens = l1_l2_wordpairs.split("\\s");
		WordAlignment walign =
			new WordAlignment(l1, l1TokensArr, l2, l2TokensArr, matchedTokens);
					walign4langpair.put(walign.langPair, walign);
		return this;
	}

	public String sentence4lang(String lang) {
			String sent = null;
			if (sentences.containsKey(lang)) {
				sent = sentences.get(lang);
			}
			return sent;
	}

	public SentencePair sentencePair(String l1, String l2) {
		Logger logger = LogManager.getLogger("org.iutools.concordancer.Alignment_ES.sentencePair");
		SentencePair pair =
			new SentencePair(
				l1, sentence4lang(l1),
				l2, sentence4lang(l2)
			);

		if (logger.isTraceEnabled()) {
			logger.trace("pair="+new PrettyPrinter().pprint(pair));
		}
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