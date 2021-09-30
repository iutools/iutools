package org.iutools.text.segmentation;

public class Token {
	public String text = null;
	public Boolean isWord = true;

	public Token(){
		init_Token((String)null, (Boolean)null);
	}

	public Token(String _word, Boolean _isWord) {
		init_Token(_word, _isWord);
	}

	private void init_Token(String _text, Boolean _isWord) {
		if (_isWord != null) {
			this.isWord = _isWord;
		}
		this.text = _text;
	}

}
