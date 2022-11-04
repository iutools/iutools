package org.iutools.concordancer.tm.sql;

import org.iutools.concordancer.Alignment;
import org.iutools.concordancer.WordAlignment;
import org.iutools.sql.Row2Pojo;
import org.iutools.sql.Row2PojoTest;

public class Row2AlignmentTest extends Row2PojoTest<Alignment> {

	@Override
	protected Row2Pojo<Alignment> makeRow2Pojo() {
		return new Row2Alignment();
	}

	@Override
	protected Alignment makeTestPojo() {
		Alignment align = new Alignment();
		align.setSentence("en", "hello world");
		align.setSentence("iu", "Haluu silarjuarmi");

		String[] enTokens = new String[] {"Hello", "world"};
		String[] iuTokens = new String[] {"Haluu", "silarjuarmi"};
		String[] tokenMatches = new String[] {"0-0", "1-1"};
		WordAlignment wordAlignment =
			new WordAlignment("en", enTokens, "iu", iuTokens, tokenMatches);
		align.setWordAlignment(wordAlignment);
		return align;
	}
}
