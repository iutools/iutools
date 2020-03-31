/*
 * Controller for the gisttext.html page.
 */

class GistTextController extends WidgetController {

	constructor(config) {
		super(config);
		this.busy = false;
	} 
	
	// Setup handler methods for different HTML elements specified in the config.
	attachHtmlElements() {
		this.setEventHandler("btnGist", "click", this.onGistSubmit)
		this.setEventHandler("divGistIconizer", "click", this.iconizeDivGist);
		this.setEventHandler("divGistIconized", "click", this.deiconizeDivGist);
	}
	
	onGistSubmit() {
		var textOrURL = this.elementForProp('txtUrlOrText').val();
		gistTextOrUrl(textOrUrl);
    }
	
//	gistActualText(text) {
//		var tokens = this.tokenizeText(text);
//	}
//	
//	gistURL(url) {
//		console.log("** GistTextNEWController.gistURL: invoked with url="+
//			JSON.stringify(url));
//	}
//	
	gistTextOrUrl(textOrUrl) {
		var inputs = {
			textOrUrl: textOrUrl
		};
		var json_inputs = JSON.stringify(inputs);
		this.invokeWebService('srv/gist/preparecontent/', json_inputs, 
				this.prepareContentSuccessCallback, 
				this.prepareContentFailureCallback);
	}
	
	prepareContentSuccessCallback(resp) {
		console.log('resp= '+JSON.stringify(resp));
		if (resp.errorMessage != null) {
			this.tokenizeFailureCallback(resp);
		} else {
			console.log("** GistTextNEWController.prepareContentSuccessCallback: successfully prepared text to: "+
				JSON.stringify(resp));
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

	
	
	isURL(str) {
		var answer = false;
		try {
			new URL(string);
			answer = true;
		} catch (_) {
			answer = false;
		}
		return answer;  
	}
	
	prepareTextForGisting(text) {
		var divText = this.elementForProp('divText');
		var textWithLinks = this.addLinksToText(text);
		divText.html(textWithLinks);
		this.attachListenersToIUWords(this);
	}
	
	attachListenersToIUWords(controller) {
		var anchorsWords = document.querySelectorAll('.iuword');
	    for (var ipn=0; ipn<anchorsWords.length; ipn++) {
	    	anchorsWords[ipn].addEventListener(
		    		  'click', 
		    		  controller.onCLickIUWord
		    		  );
	    }
	}

	
	onCLickIUWord() {
		var clickedWord = $(this).text();
		var jsonRequestData = "{\"word\":\""+clickedWord+"\"}"
		var controller = gistTextController;
		var fctSuccess = 
				function(resp) {
					controller.successGetGistCallback.call(controller, resp);
				};
		var fctFailure = 
				function(resp) {
					controller.failureGetGistCallback.call(controller, resp);
				};
		$.ajax({
			method: 'POST',
			url: 'srv/gisttext',
			data: jsonRequestData,
			dataType: 'json',
			async: true,
			success: fctSuccess,
			error: fctFailure
		});
	}
	
	addLinksToText(text) {
		var output = "";
		var tokens = text.split(" ");
		// TODO: envoyer un appel AJAX au code JAVA IUTokenizer
		for (var itok=0; itok<tokens.length; itok++) {
			if (this.allInuktitutCharacters(tokens[itok]))
				output += "<span class='iuword'>"+tokens[itok]+"</span>";
			else
				output += tokens[itok];
			output += " ";
		}
		return output;
	}
	
	allInuktitutCharacters(text) {
		var res = true;
		var iuchars = ["a","i","u","g","j","k","l","m","n","p","q","r","s","t","v","&"];
		for (var ich=0; ich<text.length; ich++)
			if ( !iuchars.includes(text.charAt(ich)) ) {
				res = false;
				break;
			}
		return res;
	}

	iconizeDivGist() {
		var divGist = this.elementForProp("divGist");
		divGist.hide();
		var divGistIconized = this.elementForProp("divGistIconized");
		divGistIconized.show();
	}
	
	deiconizeDivGist() {
		var divGist = this.elementForProp("divGist");
		divGist.show();
		var divGistIconized = this.elementForProp("divGistIconized");
		divGistIconized.hide();
	}
	
	successGetGistCallback(resp) {
		var tracer = new Tracer('GistTextController.successGetCallback', true);
		tracer.trace("resp="+JSON.stringify(resp));
		
		if (resp.errorMessage != null) {
			this.failureGetCallback(resp);
		} else {
			this.setGetResults(resp);	
		}
		tracer.trace("exited");
	}
	
	failureGetGistCallback(resp) {
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage = 
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}				
		gistTextController.error(resp.errorMessage);
	}

	error(err) {
		this.elementForProp('divError').html(err);
		this.elementForProp('divError').show();	 
	}

	setGetResults(results) {
		var tracer = new Tracer('GistTextController.setGetResults', true);
		var jsonResults = JSON.stringify(results);
		var divGist = gistTextController.elementForProp("divGist");
		var divGistContents = gistTextController.elementForProp("divGistContents");
		divGistContents.empty();
		var divGistWordHolder = gistTextController.elementForProp("divGistWordHolder");
		divGistWordHolder.text("word");

		var decompositions = results.decompositions;
		tracer.trace("nb. decompositions="+decompositions.length);
		var html = 'The word has the following decompositions: ';
		html += '<div id="gist-decompositions">';
		for (var idec=0; idec<decompositions.length; idec++) {
			html += this.generateTableForDecomposition(decompositions[idec]);
			var decomposition = decompositions[idec];
		}
		html += '</div>';
		
		var alignments = results.sentencePairs;
		if (alignments.length != 0) {
			html += "<div id='gist-alignments'>"
				+'<table id="tbl-alignments" class="alignments"><th>Inuktitut</th><th>English</th></tr>';
			for (var ial=0; ial<Math.min(30,alignments.length); ial++) {
				var inuktitutSentence = alignments[ial].sentences.iu;
				var englishSentence = alignments[ial].sentences.en;
				html += '<tr><td>'+inuktitutSentence+'</td><td>'+englishSentence+'</td></tr>';
			}
			html += '</table></div>';
		} else {
			html += "This word was not found in the Hansard 1999-2002.";
		}
		
		divGistContents.html(html);

		new RunWhen().domReady(function() {
				divGist.show();
		});
	}
	
	generateTableForDecomposition(decomposition) {
		var parts = decomposition.parts;
		var meanings = decomposition.meanings;
		var html = "<table>";
		for (var ipart=0; ipart<parts.length; ipart++) {
			html += "<tr"+(ipart==0?" class='root'":"")+">"
				+"<td>"+parts[ipart].surface+"</td>"
				+"<td>"+meanings[ipart]+"</td>"
				+"</tr>";
		}
		html += "</table>";
		return html;
	}
	
	setBusy(flag) {
		this.busy = flag;
		if (flag) {
			this.disableGistButton();	
			this.showSpinningWheel('divMessage', "Gisting text...");
			this.error("");
		} else {
			this.enableGistButton();		
			this.hideSpinningWheel('divMessage');
		}
	}	
	
	disableGistButton() {
		this.elementForProp('btnGist').attr("disabled", true);
	}
	
	enableGistButton() {
		this.elementForProp('btnGist').attr("disabled", false);
	}
	
}
