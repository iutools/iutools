package org.iutools.cli;

import ca.nrc.json.PrettyPrinter;
import org.iutools.concordancer.DocAlignment;
import org.iutools.concordancer.WebConcordancer;
import static org.iutools.concordancer.WebConcordancer.AlignOptions;

import org.iutools.concordancer.WebConcordancerException;
import org.iutools.concordancer.WebConcordancer_HtmlCleaner;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class CmdAlignPages extends ConsoleCommand {

	String[] langs = null;
	protected Mode mode = Mode.INTERACTIVE;
	AlignOptions[] alignOptions = null;

	protected WebConcordancer concordancer = null;
	protected ObjectMapper mapper = new ObjectMapper();
	Scanner stdinScanner = new Scanner(System.in);
	boolean singleInputAlreadyProcessed = false;

	public CmdAlignPages(String name) {
		super(name);
	}

	@Override
	public String getUsageOverview() {
		return "Align content of two or more documents that are translations of each other.";
	}

	@Override
	public void execute() throws Exception {
		mode = getMode(OPT_URL);
		langs = getLangs(true);
		alignOptions = getAlignOptions();

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
		concordancer = new WebConcordancer_HtmlCleaner(alignOptions);
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
				Set<String> ignoreFields = new HashSet<String>();
				ignoreFields.add("_pageMainSentences");
				ignoreFields.add("_pageSentences");
				json = PrettyPrinter.print(alignment, ignoreFields);
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
