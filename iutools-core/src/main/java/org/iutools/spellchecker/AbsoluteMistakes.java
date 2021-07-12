package org.iutools.spellchecker;


import org.junit.Ignore;

@Ignore
public class AbsoluteMistakes {
	private static AbsoluteMistake_Regex[] _allMistakes = null;

	public static AbsoluteMistake_Regex[] mistakes() {
		if (_allMistakes == null) {
			_allMistakes = new AbsoluteMistake_Regex[] {
				new AbsoluteMistake_Regex("q([jmnv])", "r$1"),
				new AbsoluteMistake_Regex("qk", "qq")
			};
		}
		return _allMistakes;
	}

	public String fixWord(String origWord) {
		String fixed = origWord;
		for (AbsoluteMistake_Regex mistake: mistakes()) {
			fixed = mistake.fixWord(fixed);
		}
		return fixed;
	}
}
