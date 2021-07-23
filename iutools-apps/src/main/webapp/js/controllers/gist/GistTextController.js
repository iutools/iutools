/*
 * Controller for the gisttext.html page.
 */

class GistTextController extends IUToolsController {

	constructor(config) {
        var tracer = Debug.getTraceLogger('GistTextController.dictionaryLookup');
		super(config);
		this.busy = false;
		this.wordDictController = new WordDictController(config);
        tracer.trace("upon exit, this="+JSON.stringify(this));
	} 
	
	// Setup handler methods for different HTML elements specified in the config.
	attachHtmlElements() {
		this.setEventHandler("btnGist", "click", this.onGistText)
	}
	
	onGistText() {
		var textOrUrl = this.elementForProp('txtUrlOrText').val();
		this.prepareContent(textOrUrl);
    }
	
	prepareContent(textOrUrl) {
		var inputs = {
			textOrUrl: textOrUrl
		};
		var json_inputs = JSON.stringify(inputs);
		this.logOnServer("GIST_TEXT", inputs);
		
		this.clearResults();
		this.setBusy(true);
		this.invokeWebService('srv2/gist/preparecontent/', json_inputs,
			this.prepareContentSuccessCallback,
			this.prepareContentFailureCallback);
	}
	
	prepareContentSuccessCallback(resp) {
		var tracer = Debug.getTraceLogger("GistTextController.prepareContentSuccessCallback")
		tracer.trace('resp= '+JSON.stringify(resp));
		if (resp.errorMessage != null) {
			this.prepareContentFailureCallback(resp);
		} else {
			tracer.trace("GistTextNEWController.prepareContentSuccessCallback: successfully prepared text to: "+
				JSON.stringify(resp));
			this.setBusy(false);
			this.displayTextGist(resp);
		}
	}

	prepareContentFailureCallback(resp) {
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage = 
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}				
		this.error(resp.errorMessage);
		this.setBusy(false);
	}
	
	displayTextGist(response) {
		var iuSentences = response.iuSentences;
		var enSentences = response.enSentences;
		if (response.alignmentsAvailable) {
			this.displayBilingualGist(iuSentences, enSentences);
		} else {
			this.displayUnilingualGist(iuSentences);
		}
	}
	
	displayUnilingualGist(iuSentences) {
		var html = "";
		for (var sentNum=0; sentNum < iuSentences.length; sentNum++) {
			var sent = iuSentences[sentNum];
			for (var tokenNum=0; tokenNum < sent.length; tokenNum++) {
				var rawToken = sent[tokenNum];
				var token = HtmlUtils.escapeHtmlEntities(rawToken);

				// Reformat the token so as to preserve the original spacing
				// and line wrapping.
				//

				// Firstly, replace multiple spaces with &nbsp;.
				//
				// Note: We leave the initial space as is because otherwise the
				// browser cannot do automatic line wrapping (&nbsp; entities
				// prevent automatic line wrapping).
				//
				token = token.replace(/  /g, " &nbsp;");
				token = token.replace(/&nbsp; /g, "&nbsp;&nbsp;");

				// Secondly, replace newlines by <br/> tags;
				token = token.replace(/\n/g, "<br/>\n");

				if (IUUtils.isInuktut(token)) {
					html += "<a class=\"iu-word\">"+token+"</a>";
				} else {
					html += token;
				}
			}
		}
		this.displayTextWithClickableWords(html);
	}

	displayBilingualGist(iuSentences, enSentences) {
		var html = "";
		var maxSents = Math.max(iuSentences.length, enSentences.length);
		if (maxSents > 0) {
			html +=
				"<table id=\"tbl-alignments\" class=\"alignments\" style=\"table-layout: fixed; width: 100%\">\n"+
				"<tr><th>Inuktitut</th><th>English</th></tr>\n";

			for (var sentNum=0; sentNum < maxSents; sentNum++) {
				// Create new row for an alignment
				html += "<tr>\n"
				html = this.htmlAddAlignmentOneSide(html, sentNum, iuSentences);
				html = this.htmlAddAlignmentOneSide(html, sentNum, enSentences);
				// Close the current alignment row
				html += "<tr>\n"
			}
			html += "</table>";
			this.displayTextWithClickableWords(html);
		}
	}

	htmlAddAlignmentOneSide(html, sentNum, sentences) {
		var tracer = Debug.getTraceLogger("GistTextController.htmlAddAlignmentOneSide");
		html += "<td>"
		var sent = [];
		if (sentences.length > sentNum) {
			sent = sentences[sentNum];
		}
		tracer.trace("sent="+JSON.stringify(sent));

		// Not sure why, but we sometimes get null sentences
		//
		if (sent != null) {
			for (var ii = 0; ii < sent.length; ii++) {
				var token = sent[ii];
				if (IUUtils.isInuktut(token)) {
					html += '<a class="iu-word">' + token + "</a>";
				} else {
					html += token;
				}
			}
		}
		html += "</td>"

		return html;
	}
	
	displayTextWithClickableWords(html) {
		html = "<h2>Click on words to see their gist</h2>\n"+html;
		var div_results = this.elementForProp("divGistTextResults");
		div_results.html(html);
		this.attachListenersToIUWords(this)
        // const y = div_results[0].getBoundingClientRect().top + window.pageYOffset - 200;
        // window.scrollTo({top: y, behavior: 'smooth'});
        this.scrollIntoView(div_results)
	}

	attachListenersToIUWords(controller) {
		var anchorWords = $(document).find('.iu-word');
	    for (var ipn=0; ipn<anchorWords.length; ipn++) {
	    	this.setEventHandler(anchorWords.eq(ipn), "click", this.onCLickIUWord);	   
	    }		
	}
	
	clearResults() {
		var div_results = this.elementForProp("divGistTextResults");
		div_results.html("");		
	}
	
	onCLickIUWord(evt) {
		var element = evt.target;
		var iuWord = $(element).text();
		// this.wordGistController.gistWord(iuWord);
        this.wordDictController.dictionaryLookup(iuWord);
	}

	setBusy(flag) {
		this.busy = flag;
		if (flag) {
			this.disableGistTextButton();	
			this.showSpinningWheel('divMessage', "Gisting text...");
			this.error("");
		} else {
			this.enableGistTextButton();		
			this.hideSpinningWheel('divMessage');
		}
	}	
	
	disableGistTextButton() {
		this.elementForProp('btnGist').attr("disabled", true);
	}
	
	enableGistTextButton() {
		this.elementForProp('btnGist').attr("disabled", false);
	}	
}
