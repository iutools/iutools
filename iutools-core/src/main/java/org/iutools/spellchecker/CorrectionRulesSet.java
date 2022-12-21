package org.iutools.spellchecker;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.iutools.text.IUWord;
import org.iutools.text.WordException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CorrectionRulesSet {
	private static CorrectionRule[] _allRules = null;
	public static Pattern _pattAnyBadSequence = null;

	public static CorrectionRule[] rules() throws SpellCheckerException {
		if (_allRules == null) {
			_allRules = new CorrectionRule[] {

				// Simple, "shallow" rules that identify and optionally correct
				// sequences of characters that can never appear in valid Inuktitut
				// words.
				//
				// Each rule is specified in the following format:
				//
				// 	new CorrectionRule(badSequence, fix)
				//
				// Where:
				//
				//   badSequence: A regular expression that catches a bad sequence
				//      of characters.
				//
				//   fix: A regular expression that fixes the bad sequence. If null,
				//      it means the rule can only identify the mistake without
				//      fixing it.
				//
				// A rule may be in written in either SYLLABIC or ROMAN, but both
				// regex must use the same script.
				//
				// When applying a rule, we need to convert the input word to the
				// script in which the rule was written. Therefore, we try to
				// put rules in a given script together to avoid constant transcoding.
				//
				// Note: When writing a rule in syllabic and that rule applies also
				// to roman, we need to specify alternatives as "(a|b|c|...)"
				// instead of "[abc...]". The reason being that a single syllabic
				// character will usually be transcoded as two, and as a result, if
				// we transcode the rule to try and read and undertand it in ROMAN,
				// we won't get the correct rule.
				//
				// For example, if we were to write a syllabic regex as "[ᐱᐳᐸ]", it
				// would be transcoded as "[pipupa]" which is wrong. So instead we
				// write the regexp as "(ᐱ|ᐳ|ᐸ)" which transcodes to a regexp that
				// will have the same effect in roman, namely "(pi|pu|pa)".
				//

				// Replace ᕐ+X with a single "composite" character that looks the same
				new CorrectionRule("ᕐᑭ", "ᕿ"),
				new CorrectionRule("ᕐᑮ", "ᖀ"),
				new CorrectionRule("ᕐᑯ", "ᖁ"),
				new CorrectionRule("ᕐᑰ", "ᖂ"),
				new CorrectionRule("ᕐᑲ", "ᖃ"),
				new CorrectionRule("ᕐᑳ", "ᖄ"),
				new CorrectionRule("ᕐᒃ", "ᖅ"),

				// Replace ᓐ+X with a single "composite" character that looks the same
				new CorrectionRule("ᓐᒋ", "ᖏ"),
				new CorrectionRule("ᓐᒌ", "ᖐ"),
				new CorrectionRule("ᓐᒍ", "ᖑ"),
				new CorrectionRule("ᓐᒎ", "ᖒ"),
				new CorrectionRule("ᓐᒐ", "ᖓ"),
				new CorrectionRule("ᓐᒑ", "ᖔ"),
				new CorrectionRule("ᓐᒡ", "ᖕ"),

				// Replace ᖕ+X with a single "composite" character that looks the same
				new CorrectionRule("ᖕᒋ", "ᖏ"),
				new CorrectionRule("ᖕᒌ", "ᖐ"),
				new CorrectionRule("ᖕᒍ", "ᖑ"),
				new CorrectionRule("ᖕᒎ", "ᖒ"),
				new CorrectionRule("ᖕᒐ", "ᖓ"),
				new CorrectionRule("ᖕᒑ", "ᖔ"),
				new CorrectionRule("ᖕᒡ", "ᖕ"),

				// Replace ᓐ+X by a single composite character that looks the same,
				//   where X is already a composite character starting with ᖕ
				new CorrectionRule("ᓐᖏ", "ᙱ"),
				new CorrectionRule("ᓐᖐ", "ᙲ"),
				new CorrectionRule("ᓐᖑ", "ᙳ"),
				new CorrectionRule("ᓐᖒ", "ᙴ"),
				new CorrectionRule("ᓐᖓ", "ᙵ"),
				new CorrectionRule("ᓐᖔ", "ᙶ"),
				new CorrectionRule("ᓐᖕ", "ᖖ"),

				// Replace ᖕ+X by a single composite character,
				//   where X is already a composite character starting with ᖕ
				new CorrectionRule("ᖕᖏ", "ᙱ"),
				new CorrectionRule("ᖕᖐ", "ᙲ"),
				new CorrectionRule("ᖕᖑ", "ᙳ"),
				new CorrectionRule("ᖕᖒ", "ᙴ"),
				new CorrectionRule("ᖕᖓ", "ᙵ"),
				new CorrectionRule("ᖕᖔ", "ᙶ"),
				new CorrectionRule("ᖕᖕ", "ᖖ"),

				// Replace ᖖ+X by a single composite character that looks the same
				new CorrectionRule("ᖖᒋ", "ᙱ"),
				new CorrectionRule("ᖖᒌ", "ᙲ"),
				new CorrectionRule("ᖖᒍ", "ᙳ"),
				new CorrectionRule("ᖖᒎ", "ᙴ"),
				new CorrectionRule("ᖖᒐ", "ᙵ"),
				new CorrectionRule("ᖖᒑ", "ᙶ"),
				new CorrectionRule("ᖖᒡ", "ᖖ"),

				// Note: the rules above are carefully ordered so that n+n+g(V), 
				// n+ng+g(V), ng+n+g(V) and ng+ng+g(V) also all correctly get 
				// mapped to nng(V).
			
				// Replace ᖅ+X by single composite character,
				//   where X is already a composite character that starts with ᕐ
				new CorrectionRule("ᖅᕿ", "ᖅᑭ"),
				new CorrectionRule("ᖅᖀ", "ᖅᑮ"),
				new CorrectionRule("ᖅᖁ", "ᖅᑯ"),
				new CorrectionRule("ᖅᖂ", "ᖅᑰ"),
				new CorrectionRule("ᖅᖃ", "ᖅᑲ"),
				new CorrectionRule("ᖅᖄ", "ᖅᑳ"),

				// Find final r before voiceless consonants and replace by q
				new CorrectionRule("ᕐ(ᐱ|ᐳ|ᐸ|ᑎ|ᑐ|ᑕ|ᓯ|ᓱ|ᓴ|ᖠ|ᖢ|ᖤ|ᐲ|ᐴ|ᐹ|ᑏ|ᑑ|ᑖ|ᓰ|ᓲ|ᓵ|ᖡ|ᖣ|ᖥ)",
					"ᖅ$1"),

				// Find final q before voiceless consonants and replace by r
				new CorrectionRule("ᖅ(ᒥ|ᒧ|ᒪ|ᓂ|ᓄ|ᓇ|ᕕ|ᕗ|ᕙ|ᓕ|ᓗ|ᓚ|ᔨ|ᔪ|ᔭ|ᕆ|ᕈ|ᕋ|ᖏ|ᖑ|ᖓ|ᒦ|ᒨ|ᒫ|ᓃ|ᓅ|ᓈ|ᕖ|ᕘ|ᕚ|ᓖ|ᓘ|ᓛ|ᔩ|ᔫ|ᔮ|ᕇ|ᕉ|ᕌ|ᖐ|ᖒ|ᖔ)",
					"ᕐ$1"),

				// Geminate k as either ng or g (North Baffin).
				// If South Baffin, you would need other regex to assimilate k to the next consonant ex: mm, nn, vv, ll, jj
				new CorrectionRule("ᒃ(ᒥ|ᒧ|ᒪ|ᓂ|ᓄ|ᓇ|ᒦ|ᒨ|ᒫ|ᓃ|ᓅ|ᓈ)",
					"ᖕ$1"),
				new CorrectionRule("ᒃ(ᕕ|ᕗ|ᕙ|ᓕ|ᓗ|ᓚ|ᔨ|ᔪ|ᔭ|ᕖ|ᕘ|ᕚ|ᓖ|ᓘ|ᓛ|ᔩ|ᔫ|ᔮ|ᕇ)",
					"ᒡ$1"),

				// Replace rngn/rngm by rn/rm such as in irniq
				new CorrectionRule("ᕐᖕ(ᓂ|ᓃ|ᓄ|ᓅ|ᓇ|ᓈ|ᒥ|ᒦ|ᒧ|ᒨ|ᒪ|ᒫ)",
					"ᕐ$1"),

				// Ensure correct spelling of Nunavummi Nunavummit Nunavummut Nunavummiut etc
				new CorrectionRule("(ᓄᓇᕗᑦ|ᓄᓇᕗ|ᓄᓇᕘ)(ᒥ|ᒧ)",
					"ᓄᓇᕗᒻ$2"),

				// Two finals are not allowed, although they often appear in NonInuit names
				// Q POUR BENOIT: Is there a way to specify this rule differently for ROMAN?
				//
				new CorrectionRule("(ᖅ|ᕐ|ᑦ|ᑉ|ᔅ|ᒡ|ᔾ|ᒃ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ){2}", null),

				// Rules for invalid consonant clusters in North and South Baffin
				// Q POUR STÉPHANE:
				//   Tu as écrit:
				//      Add a dialect switch (ex: north baffin, kivalliq, south baffin)
				//   Je ne comprends pas trop de quoi il s'agit.
				//
				// SOME RULES DISABLED FOR NOW BECAUSE BENOIT DOES NOT UNDERSTAND THEM
				//
//////				new CorrectionRule("(ᖅ|ᕐ|ᑦ|ᑉ|ᔅ|ᒡ|ᔾ|ᒃ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᕿ|ᖀ|ᖁ|ᖂ|ᖃ|ᖄ)", null, false),
				new CorrectionRule("(ᖅ|ᕐ|ᑦ|ᑉ|ᔅ|ᔾ|ᒃ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᒋ|ᒌ|ᒍ|ᒎ|ᒐ|ᒑ)", null),
				new CorrectionRule("(ᕐ|ᑦ|ᑉ|ᔅ|ᒡ|ᔾ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᑭ|ᑮ|ᑯ|ᑰ|ᑲ|ᑳ)", null),
				new CorrectionRule("(ᕐ|ᑦ|ᔅ|ᒡ|ᔾ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᐱ|ᐳ|ᐸ|ᐲ|ᐴ|ᐹ)", null),
				new CorrectionRule("(ᕐ|ᑉ|ᔅ|ᒡ|ᔾ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᑎ|ᑐ|ᑕ|ᑏ|ᑑ|ᑖ)", null),
//////				new CorrectionRule("(ᕐ|ᑉ|ᔅ|ᒡ|ᔾ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᓯ|ᓱ|ᓴ|ᓰ|ᓲ|ᓵ)", null),
				new CorrectionRule("(ᕐ|ᑦ|ᑉ|ᔅ|ᒡ|ᔾ|ᓪ|ᕝ|ᓐ|ᒻ|ᖕ|ᖖ)(ᖠ|ᖢ|ᖤ|ᖡ|ᖣ|ᖥ)", null),
				new CorrectionRule("(ᖅ|ᑦ|ᑉ|ᔅ|ᒡ|ᔾ|ᒃ|ᓪ|ᕝ|ᓐ|ᖦ|ᖖ)(ᒥ|ᒧ|ᒪ|ᒦ|ᒨ|ᒫ)", null),
				new CorrectionRule("(ᖅ|ᑦ|ᑉ|ᔅ|ᒡ|ᔾ|ᒃ|ᓪ|ᕝ|ᒻ|ᖦ|ᖖ)(ᓂ|ᓄ|ᓇ|ᓃ|ᓅ|ᓈ)", null),
				new CorrectionRule("(ᖅ|ᑦ|ᑉ|ᔅ|ᒡ|ᔾ|ᒃ|ᓪ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᕕ|ᕗ|ᕙ|ᕖ|ᕘ|ᕚ)", null),
				new CorrectionRule("(ᖅ|ᑦ|ᑉ|ᔅ|ᔾ|ᒃ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᓕ|ᓗ|ᓚ|ᓖ|ᓘ|ᓛ)", null),
				new CorrectionRule("(ᖅ|ᑦ|ᑉ|ᔅ|ᒃ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᔨ|ᔪ|ᔭ|ᔩ|ᔫ|ᔮ)", null),
				new CorrectionRule("(ᖅ|ᑦ|ᑉ|ᔅ|ᒡ|ᔾ|ᒃ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᕆ|ᕈ|ᕋ|ᕇ|ᕉ|ᕌ)", null),
				new CorrectionRule("(ᖅ|ᑦ|ᑉ|ᔅ|ᒡ|ᔾ|ᒃ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᖏ|ᖑ|ᖓ|ᖐ|ᖒ|ᖔ)", null),
				new CorrectionRule("(ᖅ|ᕐ|ᑦ|ᑉ|ᔅ|ᒡ|ᔾ|ᒃ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᙱ|ᙲ|ᙳ|ᙴ|ᙵ|ᙶ)", null),

				// Certain endings can't end with a vowel ex vu should be -vuq vut.
				// May need to expand this list.
				new CorrectionRule("(ᕗ|ᕘ|ᕕ|ᕖ|ᐳ|ᐴ|ᒧ|ᒨ|ᓄ|ᓅ|ᑐ|ᑑ)$", null),

				// Cannot have three vowels in a row
				new CorrectionRule("(ᐃ|ᐅ|ᐊ){3}", null),

				// QUESTION POUR STÉPHANE: Est-ce qu'on devrait appliquer cette règle?
				//   Si oui, comment on l'encoderait?
				//
				// Il y a aussi la regle du H. Celui-ci peut etre utilise a la position initiale
				// d'un mot (souvent emprunte: Haaki (hockey) ou Hamlaat (Hamlet). Par contre,
				// certains utilisent H pour hi hu ha, alors que les symboles ᓯ ᓱ ᓴ peuvent ausi
				// representer ces sons.
				//
				// Cette règle n'est pas encodée pour le moment

				// Deux règles proposées par Benoit Farley
				new CorrectionRule("q([jmnv])", "r$1"),
				new CorrectionRule("qk", "qq"),
			};
		}
		return _allRules;
	}

	public Pattern pattAnyBadSequence() throws SpellCheckerException {
		if (_pattAnyBadSequence == null) {
			String regex = "(";
			boolean first = true;
			for (CorrectionRule rule: rules()) {
				if (!first) {
					regex += "|";
				}
				first = false;
				regex += rule.regexBad;
			}
			regex += ")";
			_pattAnyBadSequence = Pattern.compile(regex);
		}
		return _pattAnyBadSequence;
	}

	public boolean someRulesApply(String word) throws SpellCheckerException {
		Logger logger = LogManager.getLogger("org.iutools.spellchecker.CorrectionRulesSet.someRulesApply");
		boolean answer = false;
		String traceMess = "word='"+word+"': ";
		CorrectionRule applicableRule = null;
		for (CorrectionRule rule: rules()) {
			Matcher matcher = rule.pattBad().matcher(word);
			if (matcher.find()) {
				applicableRule = rule;
				logger.trace(traceMess+"Rule "+rule.toString()+" matched. Matched '"+matcher.group(0)+"'");
				break;
			}
		}
		if (applicableRule != null) {
			traceMess += " "+applicableRule.toString()+" APPLIES";
		} else {
			traceMess += " NO rules apply";
		}
		logger.trace(traceMess);

		return (applicableRule != null);
	}

	public String fixWord(String origWordStr) throws SpellCheckerException {
		Logger logger = LogManager.getLogger("org.iutools.spellchecker.CorrectionRulesSet.fixWord");
		logger.trace("Invoked with origWordStr="+origWordStr);
		try {
			IUWord origWord = new IUWord(origWordStr);
			IUWord fixedWord = fixWord(origWord);

			String fixedWordStr = fixedWord.inScript(origWord.origScript());
			if (logger.isTraceEnabled()) {
				logger.trace("Returning fixedWordStr="+fixedWordStr);
			}
			return fixedWordStr;
		} catch (WordException e) {
			throw new SpellCheckerException(e);
		}
	}

	public IUWord fixWord(IUWord origWord) throws SpellCheckerException {
		Logger logger = LogManager.getLogger("org.iutools.spellchecker.CorrectionRulesSet.fixWord");

		IUWord fixed = origWord;
		for (CorrectionRule rule: rules()) {
			fixed = rule.fixWord(fixed);
		}
		logger.trace("for origWord='"+origWord+"', returning '"+fixed+"'");

		return fixed;
	}
}
