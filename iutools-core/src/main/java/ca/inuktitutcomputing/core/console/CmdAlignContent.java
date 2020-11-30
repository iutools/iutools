package ca.inuktitutcomputing.core.console;

import ca.nrc.json.PrettyPrinter;
import ca.pirurvik.iutools.concordancer.DocAlignment;
import ca.pirurvik.iutools.concordancer.WebConcordancer;
import static ca.pirurvik.iutools.concordancer.WebConcordancer.AlignOptions;

import ca.pirurvik.iutools.concordancer.WebConcordancerException;
import ca.pirurvik.iutools.concordancer.WebConcordancer_HtmlCleaner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class CmdAlignContent extends ConsoleCommand {

	String[] langs = null;
	protected Mode mode = Mode.INTERACTIVE;
	boolean alignSentences = true;

	protected WebConcordancer concordancer = null;
	protected ObjectMapper mapper = new ObjectMapper();
	Scanner stdinScanner = new Scanner(System.in);
	boolean singleInputAlreadyProcessed = false;

	public CmdAlignContent(String name) {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Align content of two or more documents that are translations of each other.";
	}

	@Override
	public void execute() throws Exception {
		mode = getMode(ConsoleCommand.OPT_URL);
		langs = getLangs(true);
		alignSentences = getAlignSentences();

		URL url = nextInputURL();
		while (url != null) {
			if (mode != Mode.PIPELINE) {
				echo("Aligning\n" + inputDetails(url, langs));
			}

			try {
				DocAlignment alignment = align(url, langs);
				boolean multilinePrint = true;
				if (mode == Mode.PIPELINE) {
					multilinePrint = false;
				} else {
					multilinePrint = true;
				}
				echoAlignment(alignment, multilinePrint);
			} catch (Exception e) {
				if (mode == Mode.PIPELINE) {
					// In pipeline mode, we print exceptions on single line.
					echo(e.getMessage().replaceAll("\n", "\\n"));
				}
			}
			url = nextInputURL();
		}
	}

	private DocAlignment align(URL url, String[] langs) throws ConsoleException {
		AlignOptions[] options = new AlignOptions[0];
		if (alignSentences) {
			options = new AlignOptions[] {AlignOptions.ALIGNED_SENTENCES};
		}
		concordancer = new WebConcordancer_HtmlCleaner(options);
		DocAlignment alignment = null;
		try {
			alignment = concordancer.alignPage(url, langs);
		} catch (WebConcordancerException e) {
			throw new ConsoleException("Could align\n"+inputDetails(url, langs));
		}
		return alignment;
	}

	private URL nextInputURL() throws ConsoleException {
		String urlStr = null;
		if (mode == Mode.SINGLE_INPUT &&
			!singleInputAlreadyProcessed) {
			urlStr = getURL().toString();
			singleInputAlreadyProcessed = true;
		} else if (mode == Mode.INTERACTIVE) {
			urlStr = prompt("Enter URL");
		} else if (mode == Mode.PIPELINE) {
			if (stdinScanner.hasNext()) {
				urlStr = stdinScanner.nextLine();
			}
		}

		URL url = null;
		if (urlStr != null) {
			try {
				url = new URL(urlStr);
			} catch (MalformedURLException e) {
				throw new ConsoleException("Malformed input URL" + url, e);
			}
		}
		return url;
	}

	private void echoAlignment(DocAlignment alignment, Boolean prettyPrint) throws ConsoleException {
		try {
			if (prettyPrint == null) {
				prettyPrint = true;
			}
			String json = null;
			if (prettyPrint) {
				json = PrettyPrinter.print(alignment);
			} else {
				json = mapper.writeValueAsString(alignment);
			}
			echo(json);
		} catch (JsonProcessingException e) {
			throw new ConsoleException(e);
		}
	}

	private String inputDetails(URL url, String[] langs) {
		String details =
			"   URL  : "+url+"\n   langs : "+String.join(",", langs);
		return details;
	}
}
