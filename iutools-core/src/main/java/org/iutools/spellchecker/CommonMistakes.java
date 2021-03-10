package org.iutools.spellchecker;


import org.junit.Ignore;

@Ignore
public class CommonMistakes {
	private static CommonMistake_Regex[] _allMistakes = null;

	public static CommonMistake_Regex[] mistakes() {
		if (_allMistakes == null) {
			_allMistakes = new CommonMistake_Regex[] {
				new CommonMistake_Regex("q([jkmv])", "r$1"),
				new CommonMistake_Regex("qk", "qq")
			};
		}
		return _allMistakes;
	}
}
