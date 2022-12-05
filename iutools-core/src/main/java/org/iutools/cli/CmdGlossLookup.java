package org.iutools.cli;

import ca.nrc.json.PrettyPrinter;
import ca.nrc.ui.commandline.CommandLineException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.iutools.worddict.Glossary;
import org.iutools.worddict.GlossaryEntry;

import java.util.List;

public class CmdGlossLookup extends ConsoleCommand {

	@Override
	public String getUsageOverview() {
		return "Print glossary entries for a word in a language";
	}

	public CmdGlossLookup(String name) throws CommandLineException {
		super(name);
	}

	@Override
	public void execute() throws Exception {
		// DELETE ME
		String json =
			"{\n" +
			"  \"lang2term\":\n" +
			"    {\n" +
			"      \"en\":\n" +
			"        [\"cousins (both male)\"],\n" +
			"      \"iu_roman\":\n" +
			"        [\"illuarjuk\"],\n" +
			"      \"iu_syll\":\n" +
			"       [\"ᐃᓪᓗᐊᕐᔪᒃ\"]\n" +
			"    },\n" +
			"  \"source\": \"tusaalanga\",\n" +
			"  \"reference\": \"https://tusaalanga.ca/glossary\"\n" +
			"}"
			;

		ObjectMapper mapper = new ObjectMapper();
		GlossaryEntry gloss2 = mapper.readValue(json, GlossaryEntry.class);

		Glossary gloss = Glossary.get();
		String lang = getLang();
		String word = getWord();
		lookup(word, lang);
	}

	private void lookup(String word, String lang) throws Exception {
		List<GlossaryEntry> entries = Glossary.get().entries4word(lang, word);
		PrettyPrinter prettyPrinter = new PrettyPrinter();
		user_io.echo("Glossary entries for "+lang+":"+word);
		user_io.echo(1);
		try {
			for (GlossaryEntry anEntry: entries) {
				user_io.echo(prettyPrinter.pprint(anEntry));
				user_io.echo("");
			}
		} finally {
			user_io.echo(-1);
		}
	}
}
