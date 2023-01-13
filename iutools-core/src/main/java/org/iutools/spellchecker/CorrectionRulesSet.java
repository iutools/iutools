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

				// Normalizing Syllabics Rules	
				// The first set of rules attempt to normalize syllabics. Some users
				// will use two encoded characters, such as "ᕐ+ᑭ", instead of the
				// single character available in unicode "ᕿ". While it may read as "qi"
				// for a human, the computer will however read it as "rki". If the text is
				// not normalized, "qiiq" (white hair) would then become "rkiirk".
				// The rules will attempt to find all malformed composite characters 
				// and re-encode them as single unicode ones. 

				// The syllabic text should be normalized before attempting any
				// transliteration into roman orthography. 

				// Rule N1: normalize ᕐ+ᑭ ("r+ki") as ᕿ ("qi"), and so on.
				// ᕿ = q(V) should be one character
   				// r + k(V) -> q(V)
				new CorrectionRule("ᕐᑭ", "ᕿ"),
				new CorrectionRule("ᕐᑮ", "ᖀ"),
				new CorrectionRule("ᕐᑯ", "ᖁ"),
				new CorrectionRule("ᕐᑰ", "ᖂ"),
				new CorrectionRule("ᕐᑲ", "ᖃ"),
				new CorrectionRule("ᕐᑳ", "ᖄ"),
				new CorrectionRule("ᕐᒃ", "ᖅ"),

				// Rule N2: normalize ᓐ+ᒋ ("n+gi") as ᖏ ("ngi"), and so on.
				// ᖏ = ng(V) should be one character
   				// n + g(V) -> ng(V)
				new CorrectionRule("ᓐᒋ", "ᖏ"),
				new CorrectionRule("ᓐᒌ", "ᖐ"),
				new CorrectionRule("ᓐᒍ", "ᖑ"),
				new CorrectionRule("ᓐᒎ", "ᖒ"),
				new CorrectionRule("ᓐᒐ", "ᖓ"),
				new CorrectionRule("ᓐᒑ", "ᖔ"),
				new CorrectionRule("ᓐᒡ", "ᖕ"),

				// Rule N3: normalize ᖕ+ᒋ ("ng+gi") as ᖏ ("ngi"), and so on.
				// ng + g(V) -> ng(V)
				new CorrectionRule("ᖕᒋ", "ᖏ"),
				new CorrectionRule("ᖕᒌ", "ᖐ"),
				new CorrectionRule("ᖕᒍ", "ᖑ"),
				new CorrectionRule("ᖕᒎ", "ᖒ"),
				new CorrectionRule("ᖕᒐ", "ᖓ"),
				new CorrectionRule("ᖕᒑ", "ᖔ"),
				new CorrectionRule("ᖕᒡ", "ᖕ"),

				// Rule N4: normalize ᓐ+ᖏ ("n+ngi") as ᙱ ("nngi"), and so on.
				// ᙱ = nng(V) should be one character
   				// n + ng(V) -> nng(V)
   				// Note: n'ng is valid, but with an apostrophe, as in avin'ngaq.
				new CorrectionRule("ᓐᖏ", "ᙱ"),
				new CorrectionRule("ᓐᖐ", "ᙲ"),
				new CorrectionRule("ᓐᖑ", "ᙳ"),
				new CorrectionRule("ᓐᖒ", "ᙴ"),
				new CorrectionRule("ᓐᖓ", "ᙵ"),
				new CorrectionRule("ᓐᖔ", "ᙶ"),
				new CorrectionRule("ᓐᖕ", "ᖖ"),

				// Rule N5: normalize ᖕ+ᖏ ("ng+ngi") as ᙱ ("nngi"), and so on.
				// ng + ng(V) -> nng(V)
				new CorrectionRule("ᖕᖏ", "ᙱ"),
				new CorrectionRule("ᖕᖐ", "ᙲ"),
				new CorrectionRule("ᖕᖑ", "ᙳ"),
				new CorrectionRule("ᖕᖒ", "ᙴ"),
				new CorrectionRule("ᖕᖓ", "ᙵ"),
				new CorrectionRule("ᖕᖔ", "ᙶ"),
				new CorrectionRule("ᖕᖕ", "ᖖ"),

				// Rule N6: normalize ᖖ+ᒋ ("nng+gi") as ᙱ ("nngi"), and so on.
				// nng + g(V) -> nng(V)
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
			
				// Rule N7: normalize ᖅ+ᕿ ("q+qi") as ᖅᑭ ("qqi"), and so on.
				// doubled ᕿ = q(V) should be ᖅᑭ = qq(V)
   				// doubled qq = q + q(V) -> qq(V)
				new CorrectionRule("ᖅᕿ", "ᖅᑭ"),
				new CorrectionRule("ᖅᖀ", "ᖅᑮ"),
				new CorrectionRule("ᖅᖁ", "ᖅᑯ"),
				new CorrectionRule("ᖅᖂ", "ᖅᑰ"),
				new CorrectionRule("ᖅᖃ", "ᖅᑲ"),
				new CorrectionRule("ᖅᖄ", "ᖅᑳ"),

				// Rule N8: normalize ᕐ+ᕿ as ᖅᑭ
				// doubled qq = r + q(V) -> qq(V)
				// Rules this group address some rare mistakes in Nunavut.
				// However, then flag some words that are considered valid in Nunavik.
				// Therefore, we comment them out for now.
				// Eventually we may re-activate them if we come up with a way to tell the
				// SpellChecker the dialects that are being processed.
				//
//				 new CorrectionRule("ᕐᕿ", "ᖅᑭ"),
//				 new CorrectionRule("ᕐᖀ", "ᖅᑮ"),
//				 new CorrectionRule("ᕐᖁ", "ᖅᑯ"),
//				 new CorrectionRule("ᕐᖂ", "ᖅᑰ"),
//				 new CorrectionRule("ᕐᖃ", "ᖅᑲ"),
//				 new CorrectionRule("ᕐᖄ", "ᖅᑳ"),

				// Rule N9: normalize H in middle position
				// H is used at the beginning of loaned words, such as Haaki (Hockey)
				// In standardized orthography, ᓯ, ᓱ, ᓴ represent both s and h sounds.
				// The rule will normalize the use of H in middle position, and
				// convert it to ᓯ, ᓱ, ᓴ. The user should review the corrections.

				// Alain: Hum, how come we have H in those syllabic regexps^
				// They are causing problems in the CorrectionRule constructor because it does
				// not allow regexps that are a mix of syllabic and roman.
				// I asked Stéphane about those rules on Jan 10th, 2023. Waiting for his answer.
				// Commenting them out for now.
				new CorrectionRule("(.+)(ᕼᐃ|Hᐃ)(.*$)", "$1ᓯ$3"),
				new CorrectionRule("(.+)(ᕼᐄ|Hᐄ)(.*$)", "$1ᓰ$3"),
				new CorrectionRule("(.+)(ᕼᐅ|Hᐅ)(.*$)", "$1ᓱ$3"),
				new CorrectionRule("(.+)(ᕼᐆ|Hᐆ)(.*$)", "$1ᓲ$3"),
				new CorrectionRule("(.+)(ᕼᐊ|Hᐊ)(.*$)", "$1ᓴ$3"),
				new CorrectionRule("(.+)(ᕼᐋ|Hᐋ)(.*$)", "$1ᓵ$3"),

				// AD-2023-01-11: For some reason, the ᐁ and ᐂ are not recognized as a Syllabic char
				// by the TransCoder. Waiting for an answer from Benoit.
				// Commmenting themn out for now.
				new CorrectionRule("(.+)(ᕼᐁ|Hᐁ)(.*$)", "$1ᓭ$3"),
				new CorrectionRule("(.+)(ᕼᐂ|Hᐂ)(.*$)", "$1ᓮ$3"),

				//
				// Inuktut Standard Spelling (ISS) Rules
				// The next set of rules follow the spelling standards established
				// by the Inuit Uqausinginnik Taiguusiliuqtiit (IUT), based on
				// the Inuit Cultural Institute's standardized writing system,
				// and work done on the Nunavut Utilities' Rules Checker for Inuktitut.
				// 
				// References:
				//
				// From Inuit Uqausinginnik Taiguusiliuqtiit:
				// - Inuktut Spelling Standards: A Basic Guide, 2021
				// - Inuktut Reference Grammar, 2018
				// https://www.taiguusiliuqtiit.ca/en/resources/publications
				//
				// From Inuit Culture Institute:
				// - Writing System Proposal, 1976 (unpublished manuscript)
				//
				// From Department of Culture, Language, Elders and Youth, Government of Nunavut:
				// - Nunavut Utilities Technical Guide, 2005
				//   https://www.gov.nu.ca/sites/default/files/files/utilitiestechnicalguide.pdf
				//

				// Rule 1a: Use only Inuktitut Syllabic Characters
				// Some documents may include non-Inuktitut syllabic characters.
				// The rule will attempt to check for all non-Inuktitut Syllabic Characters, by
				// excluding valid Inuktitut syllabic characters in the search, along with
				// punctuation, white space, digits, a-z letters, apostrophe, X and H.
				//
				// AD-2023-01-10: I don't think this rule is needed. IUTools already has code
				// to ensure that words submitted to the spell checker are NOT of mixed script.
				// The rule as formulated causes the CorrectionRule() constructor to fail because
				// it does not allow regexps of mixed script.
				// Commenting out for now and waiting for clarifications from Stéphane.
				//
//				new CorrectionRule("[^ᐃᐄᐅᐆᐊᐋᐱᐲᐳᐴᐸᐹᑉᑎᑏᑐᑑᑕᑖᑦᑭᑮᑯᑰᑲᑳᒃᒋᒌᒍᒎᒐᒑᒡᒥᒦᒧᒨᒪᒫᒻᓂᓃᓄᓅᓇᓈᓐᓯᓰᓱᓲᓴᓵᔅᓕᓖᓗᓘᓚᓛᓪᔨᔩᔪᔫᔭᔮᔾᕕᕖᕗᕘᕙᕚᕝᕆᕇᕈᕉᕋᕌᕐᕿᖀᖁᖂᖃᖄᖅᙰᖏᖐᖎᖑᖒᖓᖔᖕᙱᙲᙳᙴᙵᙶᖖᖠᖡᖢᖣᖤᖥᖦ᙭ᐟ\\w\\s\\da-zA-Zᕼ_]", null),

				// Rule 2: Finals cannot be initial or alone.
				// No words can start with finals ᑉ ᑦ ᒃ ᒡ ᒻ ᓐ ᔅ ᓪ ᔾ ᕝ ᕐ ᖅ ᖕ ᖖ ᖦ
				// The rule will attempt to check if a final starts a word.
				// It will also find standing alone finals.
				new CorrectionRule("^(ᑉ|ᑦ|ᒃ|ᒡ|ᒻ|ᓐ|ᔅ|ᓪ|ᔾ|ᕝ|ᕐ|ᖅ|ᖕ|ᖖ|ᖦ)", null),

				// Rule 3: Finals cannot be doubled. 
				// Although this may occur in non-Inuktitut words, mostly
				// English names, the rule will check for double finals. The user
				// can then decide if a mistake has been made.  
				// // // IN ROMAN this could be (p|t|k|g|m|n|s|l|j|r|q|ng|nng|ł){3}
				new CorrectionRule("(ᑉ|ᑦ|ᒃ|ᒡ|ᒻ|ᓐ|ᔅ|ᓪ|ᔾ|ᕝ|ᕐ|ᖅ|ᖕ|ᖖ|ᖦ){2}", null),
				
				// Rule 4a: A doubled vowel should be a dotted vowel, or corrected if a typo. 
				// The rule will attempt to check for all double vowels. These should be
				// dotted when appropriate (ᐱᐃ should ᐲ). If the writter made a typo, 
				// such as ᐊᑐᐅᓯᖅ ("atuusiq"), it can then be corrected to ᐊᑕᐅᓯᖅ ("atausiq")
				new CorrectionRule("(ᐃᐃ|ᐱᐃ|ᑎᐃ|ᑭᐃ|ᒋᐃ|ᒥᐃ|ᓂᐃ|ᓯᐃ|ᓕᐃ|ᔨᐃ|ᕕᐃ|ᕆᐃ|ᕿᐃ|ᖏᐃ|ᖠᐃ|ᐊᐊ|ᐸᐊ|ᑕᐊ|ᑲᐊ|ᒐᐊ|ᒪᐊ|ᓇᐊ|ᓴᐊ|ᓚᐊ|ᔭᐊ|ᕙᐊ|ᕋᐊ|ᖃᐊ|ᖓᐊ|ᖤᐊ|ᐅᐅ|ᐳᐅ|ᑐᐅ|ᑯᐅ|ᒍᐅ|ᒧᐅ|ᓄᐅ|ᓱᐅ|ᓗᐅ|ᔪᐅ|ᕗᐅ|ᕈᐅ|ᖁᐅ|ᖑᐅ|ᖢᐅ)", null),

				// Rule 4b: Three vowels or more. 
				// The rule will check if three vowels follow each other. Note that a dotted syllabic
				// character already represents two vowels. 
				new CorrectionRule("[ᐄᑏᐲᑮᒌᒦᓃᓰᓖᔩᕖᕇᖀᖐᖡᐆᑑᐴᑰᒎᒨᓅᓲᓘᔫᕘᕉᖂᖒᖣᐋᑖᐹᑳᒑᒫᓈᓵᓛᔮᕚᕌᖄᖔᖥ][ᐄᐃᐅᐆᐊᐋ]", null),

				// Rule 5: Position in the word
				// The rules will check if characters may be in a wrong position, either at the 
				// beginning or end of a word. 

				// Rule 5a: Position in the word - beginning
				// Some characters should never appears at the beginning of a word
				// such as ᖠᖡᖢᖣᖤᖥᓕᓖᓗᓘᕕᕖᕗᕘᕚᓚᓛᔩᔮᔫᕆᕇᕈᕉᕋᕌᖏᖐᖑᖒᖓᖔᙱᙲᙳᖒᙵᙶ
				new CorrectionRule("^(ᖠ|ᖡ|ᖢ|ᖣ|ᖤ|ᖥ|ᓕ|ᓖ|ᓗ|ᓘ|ᕕ|ᕖ|ᕗ|ᕘ|ᕚ|ᓚ|ᓛ|ᔩ|ᔮ|ᔫ|ᕆ|ᕇ|ᕈ|ᕉ|ᕋ|ᕌ|ᖏ|ᖐ|ᖑ|ᖒ|ᖓ|ᖔ|ᙱ|ᙲ|ᙳ|ᖒ|ᙵ|ᙶ)", null),

				// Rule 5b: Position in the word - ending
				// Characters at the end of word can only be ᑉ ᑦ ᒃ ᖅ or a vowel.
				// These ᒡ ᒻ ᓐ ᔅ ᔾ ᓪ ᕐ ᕝ ᖦ ᖕ ᖖ ᕼ can never be at the end of a word,
				// although they may occur in some loaned words or names.
				new CorrectionRule("(ᒡ|ᒻ|ᓐ|ᔅ|ᔾ|ᓪ|ᕐ|ᕝ|ᖦ|ᖕ|ᖖ|ᕼ|H)$", null),

				// Rule 6: Fix opening and closing quotation marks
				new CorrectionRule("‘’", "“"),
				new CorrectionRule("’’", "”"),

				// Rule 7: Glottal Stop and Apostrophe Separator
				// The glottal stop and the apostrophe can only be used inside an Inuktitut word.
				// There are various encodings in syllabic text for the apostrophe.  
				// The rule attempts to normalize it to a single character (U+0027).
				new CorrectionRule("(.)[`ʼ'’´](.)",
					"$1ʼ$2"),

				// In the standardized orthography, there are 14 consonants. 
				// They are grouped as voiceless, voiced and nasal (voiced). 
				// Voiceless consonants: p, t, s (h), ł, k, q
				// Voiced consonants: v, l, j, g, r
				// Nasal (voiced) consonants: m, n, ng
				// Consonant pairs can only be formed respectively within the voiceless group,
				// and voiced groups. 

				// Valid Consonant Clusters available in Inuktitut
				// VOICELESS
				// pp, pt, ps (ph), pk, pq
				// tp, tt, ts, tk, tq
				// ss (hh)
				// łł, łr
				// kp, kt, ks (kh), kł, kk
				// qp, qt, qs (qh), qł, qq

				// VOICED
				// vv, vl (pl/bl), vj, vg, vr	
				// lg, ll, lr, lv
				// jj, jv, jg, jr
				// gv, gl, gj, gg
				// rv, rl, rj, rr
				
				// NASAL (VOICED)
				// mm, mn, mng
				// nm, nn, n’ng
				// ngm, ngn, nng
				
				// VOICED + NASAL (VOICED)
				// rm, rn, rng

				// Rule 8a: Fix final r to q 
				// The rule will attempt to find ᕐ "r" (voiced) appearing before [ptsł] (voiceless) 
				// and replace it by ᖅ "q" (voiceless), in accordance with the standardized orthography.
				// The replacement will be automatic for all instances, but the user should
				// nevertheless review them for acuracy. 
				// In roman othorgraphy, this rule would be ("r([ptshł])", "q$1")
				new CorrectionRule("ᕐ(ᐱ|ᐳ|ᐸ|ᑎ|ᑐ|ᑕ|ᓯ|ᓱ|ᓴ|ᖠ|ᖢ|ᖤ|ᐲ|ᐴ|ᐹ|ᑏ|ᑑ|ᑖ|ᓰ|ᓲ|ᓵ|ᖡ|ᖣ|ᖥ)",
					"ᖅ$1"),

				// Rule 8b: Fix final q to r
				// The rule will attempt to find ᖅ "q" (voiceless) appearing before (v|l|j|r|m|n|ng) (voiced)
				// and replace it by ᕐ "r" (voiced), in accordance with the standardized orthography.
				// The replacement will be automatic for all instances, but the user should
				// nevertheless review them for acuracy. 
				// In roman orthography, this rule would simply be ("q([vljrmn])", "r$1")
				new CorrectionRule("ᖅ(ᒥ|ᒧ|ᒪ|ᓂ|ᓄ|ᓇ|ᕕ|ᕗ|ᕙ|ᓕ|ᓗ|ᓚ|ᔨ|ᔪ|ᔭ|ᕆ|ᕈ|ᕋ|ᖏ|ᖑ|ᖓ|ᒦ|ᒨ|ᒫ|ᓃ|ᓅ|ᓈ|ᕖ|ᕘ|ᕚ|ᓖ|ᓘ|ᓛ|ᔩ|ᔫ|ᔮ|ᕇ|ᕉ|ᕌ|ᖐ|ᖒ|ᖔ)",
					"ᕐ$1"),

				// Rule 9: Impossible consonant pairs:
				// The next set of rules will check for impossible consonant pairs
				// in Inuktitut. The user should review all occurances and make corrections 
				// as needed. 

				// Note: rules 9a and 9b automatically correct r and q finals,
				// and are therefore omitted in the following set of rules. 

				// These rules are encompassing of all dialects, including Inuinnaqtun,
				// Nattilik, Kivalliq, North and South Baffin and Sanikiluaq (Nunavik).

				// Rule 9p: Check s, g, j, l, v, n, m, ł, ng, nng before pi|pu|pa
				new CorrectionRule("(ᔅ|ᒡ|ᔾ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᐱ|ᐳ|ᐸ|ᐲ|ᐴ|ᐹ)", null),
				// Rule 9t: Check s, g, j, l, v, n, m, ł, ng, nng before ti|tu|ta
				new CorrectionRule("(ᔅ|ᒡ|ᔾ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᑎ|ᑐ|ᑕ|ᑏ|ᑑ|ᑖ)", null),
				// Rule 9s: Check g, j, l, v, n, m, ł, ng, nng before si|su|sa
				new CorrectionRule("(ᒡ|ᔾ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᓯ|ᓱ|ᓴ|ᓰ|ᓲ|ᓵ)", null),
				// Rule 9ł: Check t, p, s, g, j, l, v, n, m, ng, nng before łi|łu|ła
				new CorrectionRule("(ᑦ|ᑉ|ᔅ|ᒡ|ᔾ|ᓪ|ᕝ|ᓐ|ᒻ|ᖕ|ᖖ)(ᖠ|ᖢ|ᖤ|ᖡ|ᖣ|ᖥ)", null),
				// Rule 9k: Check s, g, j, l, v, n, m, ng, nng before ki|ku|ka
				new CorrectionRule("(ᔅ|ᒡ|ᔾ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᑭ|ᑮ|ᑯ|ᑰ|ᑲ|ᑳ)", null),
				// Rule 9q: Check s, g, j, l, v, n, m, ł, ng, nng before qi|qu|qa
				new CorrectionRule("(ᔅ|ᒡ|ᔾ|ᒃ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᕿ|ᖀ|ᖁ|ᖂ|ᖃ|ᖄ)", null),

				// Rules for Voiced Consonant Clusters
				// Rule 9v: Check t, p, s, j, k, l, n, m, ł, ng, nng before vi|vu|va
				new CorrectionRule("(ᑦ|ᑉ|ᔅ|ᔾ|ᒃ|ᓪ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᕕ|ᕗ|ᕙ|ᕖ|ᕘ|ᕚ)", null),
				// Rule 9l: Check t, s, j, k, n, m, ł, ng, nng before li|lu|la
				new CorrectionRule("(ᑦ|ᔅ|ᔾ|ᒃ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᓕ|ᓗ|ᓚ|ᓖ|ᓘ|ᓛ)", null),
				// Rule 9j: Check p, s, k, l, n, m, ł, ng, nng  before ji|ju|ja
				new CorrectionRule("(ᑉ|ᔅ|ᒃ|ᓪ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᔨ|ᔪ|ᔭ|ᔩ|ᔫ|ᔮ)", null),
				// Rule 9g: Check q, r, t, p, s, j, k, l, n, m, ł, ng, nng before gi|gu|ga
				new CorrectionRule("(ᖅ|ᕐ|ᑦ|ᑉ|ᔅ|ᔾ|ᒃ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᒋ|ᒌ|ᒍ|ᒎ|ᒐ|ᒑ)", null),
				// Rule 9r: Check t, p, s, g, j, k, l, n, m, ł, ng, nng  before ri|ru|ra
				new CorrectionRule("(ᑦ|ᑉ|ᔅ|ᒡ|ᒃ|ᓪ|ᔾ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᕆ|ᕈ|ᕋ|ᕇ|ᕉ|ᕌ)", null),

				// Rules for Nasal (Voiced) Consonant Clusters
				// Rule 9m: Check t, p, s, g, j, k, l, v, n, ł, nng before mi|mu|ma
				new CorrectionRule("(ᑦ|ᑉ|ᔅ|ᒡ|ᔾ|ᒃ|ᓪ|ᕝ|ᓐ|ᖦ|ᖖ)(ᒥ|ᒧ|ᒪ|ᒦ|ᒨ|ᒫ)", null),
				// Rule 9n: Check t, p, s, g, j, k, l, v, m, ł, nng before ni|nu|na
				new CorrectionRule("(ᑦ|ᑉ|ᔅ|ᒡ|ᔾ|ᒃ|ᓪ|ᕝ|ᒻ|ᖦ|ᖖ)(ᓂ|ᓄ|ᓇ|ᓃ|ᓅ|ᓈ)", null),
				// Rule 9ng: Check t, p, s, g, j, k, l, v, n, m, ł, ng, nng before ngi|ngu|nga
				new CorrectionRule("(ᑦ|ᑉ|ᔅ|ᒡ|ᔾ|ᒃ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᖏ|ᖑ|ᖓ|ᖐ|ᖒ|ᖔ)", null),
				// Rule 9nng: Check q, r, t, p, s, g, j, k, l, v, n, m, ł, ng, nng before nngi|nngu|nnga
				new CorrectionRule("(ᖅ|ᕐ|ᑦ|ᑉ|ᔅ|ᒡ|ᔾ|ᒃ|ᓪ|ᕝ|ᓐ|ᒻ|ᖦ|ᖕ|ᖖ)(ᙱ|ᙲ|ᙳ|ᙴ|ᙵ|ᙶ)", null),

				// Rule 10: Missing endings
				// Certain words may have a missing final, either q, t, k or p.
				// TODO: create an exclusion list
				new CorrectionRule("(ᕗ|ᕘ|ᐳ|ᐴ|ᔪ|ᔫ|ᑐ|ᑑ|ᒧ|ᒨ|ᓄ|ᓅ|ᕕ|ᕖ)$", null),

				// Geminate k as either ng or g (North Baffin).
				// SC: I commented these lines out as it would be different in South Baffin. 
				// With the first set of checks above, the user can make the appropriate corrections.  
				// If South Baffin, you would need other regex to assimilate k to the next consonant ex: mm, nn, vv, ll, jj
				 new CorrectionRule("ᒃ(ᒥ|ᒧ|ᒪ|ᓂ|ᓄ|ᓇ|ᒦ|ᒨ|ᒫ|ᓃ|ᓅ|ᓈ)",
					"ᖕ$1"),
				 new CorrectionRule("ᒃ(ᕕ|ᕗ|ᕙ|ᓕ|ᓗ|ᓚ|ᔨ|ᔪ|ᔭ|ᕖ|ᕘ|ᕚ|ᓖ|ᓘ|ᓛ|ᔩ|ᔫ|ᔮ|ᕇ)",
					"ᒡ$1"),

				// Rule XX - Nunavummi
				// Nunavut's Inuit Uqausinginnik Taiguusiliuqtiit recommends the standard spelling
				// of Nunavummi and to avoid Nunavutmi or Nunavuumi. 
				 new CorrectionRule("(ᓄᓇᕗᑦ|ᓄᓇᕗ|ᓄᓇᕘ)(ᒥ|ᒧ)", "ᓄᓇᕗᒻ$2"),

				// Replace rngn/rngm by rn/rm such as in irniq
				 new CorrectionRule("ᕐᖕ(ᓂ|ᓃ|ᓄ|ᓅ|ᓇ|ᓈ|ᒥ|ᒦ|ᒧ|ᒨ|ᒪ|ᒫ)",
					"ᕐ$1"),

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
