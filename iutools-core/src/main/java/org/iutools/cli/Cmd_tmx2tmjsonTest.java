package org.iutools.cli;

import ca.nrc.testing.AssertString;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URL;

public class Cmd_tmx2tmjsonTest {

	private Cmd_tmx2tmjson cmd = null;

	@BeforeEach
	public void setUp() throws Exception {
		cmd = new Cmd_tmx2tmjson("name");

		// These two URLs have the same file name = 'forms'
		{
			cmd.onNewURL("https://gov.nu.ca/health/information/forms");
			cmd.onNewURL("https://gov.nu.ca/publications-resources/forms");
		}

		// These two URLs have the same file name with same extension ('index.html')
		{
			cmd.onNewURL("https://gov.nu.ca/index.html");
			cmd.onNewURL("https://gov.nu.ca/environment/index.html");
		}
	}

	@Test
	public void test__url4tmxfile__SeveralCases() throws Exception {
		String focusOnCase = null;
//		focusOnCase = "index.html_en_ENG-IUK_BT.tmx";

		Pair<String,String>[] cases = new Pair[] {
			Pair.of("/some/path/forms_en_ENG-IUK_BT.tmx",
				"https://gov.nu.ca/health/information/forms"),
			Pair.of("/some/path/forms.1_en_ENG-IUK_BT.tmx",
				"https://gov.nu.ca/publications-resources/forms"),
			Pair.of("/some/path/index.html_en_ENG-IUK_BT.tmx",
				"https://gov.nu.ca/index.html"),
			Pair.of("/some/path/index.html.1_en_ENG-IUK_BT.tmx",
				"https://gov.nu.ca/environment/index.html")
		};

		for (Pair<String,String> aCase: cases) {
			String fpath = aCase.getLeft();
			if (focusOnCase != null && !fpath.contains(focusOnCase)) {
				continue;
			}

			URL gotUrl = cmd.url4tmxfile(new File(fpath));
			AssertString.assertStringEquals(
				"Wrong URL for file path: "+fpath,
				aCase.getRight(), gotUrl.toString());
		}

		if (focusOnCase != null) {
			Assertions.fail("Case run on a single case. Set focusOnCase=false to run all cases");
		}
	}

	@Test
	public void test__tmxFileKey__SeveralCases() throws Exception {
		String focusOnCase = null;
//		focusOnCase = "index.html_en_ENG-IUK_BT.tmx";

		Pair<String,String>[] cases = new Pair[] {
			Pair.of("/some/path/forms_en_ENG-IUK_BT.tmx",
				"forms"),
			Pair.of("/some/path/forms.1_en_ENG-IUK_BT.tmx",
				"forms_1"),
			Pair.of("/some/path/index.html_en_ENG-IUK_BT.tmx",
				"index.html"),
			Pair.of("/some/path/index.html.1_en_ENG-IUK_BT.tmx",
				"index.html_1")
		};

		for (Pair<String,String> aCase: cases) {
			String fpath = aCase.getLeft();
			if (focusOnCase != null && !fpath.contains(focusOnCase)) {
				continue;
			}

			String gotKey = cmd.tmxFileKey(new File(fpath));
			AssertString.assertStringEquals(
				"Wrong key for tmx file path: "+fpath,
				aCase.getRight(), gotKey);
		}

		if (focusOnCase != null) {
			Assertions.fail("Case run on a single case. Set focusOnCase=false to run all cases");
		}
	}

	@Test
	public void test__urlFileName__SeveralCases() throws Exception {
		String focusOnCase = null;
//		focusOnCase = "index.html";

		Pair<String,String>[] cases = new Pair[] {
			// This URL does not have an extension
			Pair.of("https://gov.nu.ca/health/information/forms",
				"forms"),
			// This URL has an extension .html
			Pair.of("https://gov.nu.ca/node/1705/index.html",
				"index.html"),
		};

		for (Pair<String,String> aCase: cases) {
			String url = aCase.getLeft();
			if (focusOnCase != null && !url.contains(focusOnCase)) {
				continue;
			}

			String gotFname = cmd.urlFileName(url);
			AssertString.assertStringEquals(
				"Wrong file name for url: "+url,
				aCase.getRight(), gotFname);
		}

		if (focusOnCase != null) {
			Assertions.fail("Case run on a single case. Set focusOnCase=false to run all cases");
		}
	}

}