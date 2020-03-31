/*
 * Controller for the gisttext.html page.
 */

class GistTextController extends WidgetController {

	constructor(config) {
		super(config);
		this.wordGistController = new WordGistController(config);
	} 
	
	// Setup handler methods for different HTML elements specified in the config.
	attachHtmlElements() {
		this.setEventHandler("btnPaste", "click", this.onButtonPaste)
		this.setEventHandler("divGistIconizer", "click", this.iconizeDivGist);
		this.setEventHandler("divGistIconized", "click", this.deiconizeDivGist);
	}
	
	onButtonPaste() {
        navigator.clipboard.readText().then(
        	clipText => this.prepareTextForGisting(clipText));
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
	
//	clearResults() {
//		this.elementForProp('divError').empty();
//		this.elementForProp('divResults').empty();
//	}
//	
//	invokeGetService(jsonRequestData, _successCbk, _failureCbk) {
//		this.invokeSearchService(jsonRequestData, _successCbk, _failureCbk);
//	}
//	
//	invokeSearchService(jsonRequestData, _successCbk, _failureCbk) {
//			var tracer = new Tracer('GistTextController.invokeSearchService', true);
//			tracer.trace("_successCbk="+_successCbk+", jsonRequestData="+JSON.stringify(jsonRequestData));
//			this.busy = true;
//			var controller = this;
//			var fctSuccess = 
//					function(resp) {
//						_successCbk.call(controller, resp);
//					};
//			var fctFailure = 
//					function(resp) {
//						_failureCbk.call(controller, resp);
//					};
//					
//			// this line is for development only, allowing to present results without calling Bing.
//			//var jsonResp = this.mockSrvSearch();fctSuccess(jsonResp);
//		
//			$.ajax({
//				method: 'POST',
//				url: 'srv/gisttext',
//				data: jsonRequestData,
//				dataType: 'json',
//				async: true,
//		        success: fctSuccess,
//		        error: fctFailure
//			});
//			
//			tracer.trace('GistTextController.invokeSearchService', "exited");
//	}
//	
//	validateQueryInput() {
//		var isValid = true;
//		var query = this.elementForProp("inpWord").val();
//		if (query == null || query === "") {
//			isValid = false;
//			this.error("You need to enter something in the word field");
//		}
//		return isValid;
//	}
//
	successGetGistCallback(resp) {
		var tracer = new Tracer('GistTextController.successGetCallback', true);
		tracer.trace("resp="+JSON.stringify(resp));
		
		if (resp.errorMessage != null) {
			this.failureGetCallback(resp);
		} else {
			this.setGetResults(resp);	
		}
//		this.setGetBusy(false);
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
//		
//	
//	// ---------------------- Test Section ------------------------------ //
//
//	onTest() {
//		this.invokeTestService({}, 
//				this.testSuccessCallback, this.testFailureCallback);
//	}
//
//	invokeTestService(jsonRequestData, _successCbk, _failureCbk) {
//			var controller = this;
//			var fctSuccess = 
//					function(resp) {
//						_successCbk.call(controller, resp);
//					};
//			var fctFailure = 
//					function(resp) {
//						_failureCbk.call(controller, resp);
//					};
//		
//			$.ajax({
//				method: 'POST',
//				url: 'srv/hello',
//				data: jsonRequestData,
//				dataType: 'json',
//				async: true,
//		        success: fctSuccess,
//		        error: fctFailure
//			});
//	}
//	
//	testSuccessCallback(resp) {
//		var element = this.elementForProp("divTestResponse");
//		element.empty();
//		element.html(resp.message);
//	}
//	    
//	testFailureCallback(resp) {
//		var element = this.elementForProp("divTestResponse");
//		element.empty();
//		element.html("Server returned error, resp="+JSON.stringify(resp));
//	}
	

}
