/*
 * Controller for the gist.html page.
 */

class GistController extends WidgetController {

	constructor(config) {
		super(config);
		this.totalHits = 0;
		this.alreadyShownHits = [];
	} 
	
	// Setup handler methods for different HTML elements specified in the config.
	attachHtmlElements() {
		this.setEventHandler("btnGet", "click", this.onGet);
		this.onReturnKey("inpWord", this.onGet);
	}
	
	onGet() {
		var isValid = this.validateQueryInput();
		if (isValid) {
			this.clearResults();
			this.setGetBusy(true);
			var requestData = this.getSearchRequestData();
			console.log('requestData= '+JSON.stringify(requestData));
			this.invokeGetService(requestData, 
					this.successGetCallback, this.failureGetCallback);
		}
	}
	
	clearResults() {
		this.elementForProp('divError').empty();
		this.elementForProp('divResults').empty();
	}
	
	invokeGetService(jsonRequestData, _successCbk, _failureCbk) {
		this.invokeSearchService(jsonRequestData, _successCbk, _failureCbk);
	}
	
	invokeSearchService(jsonRequestData, _successCbk, _failureCbk) {
			var tracer = new Tracer('GistController.invokeSearchService', true);
			tracer.trace("_successCbk="+_successCbk+", jsonRequestData="+JSON.stringify(jsonRequestData));
			this.busy = true;
			var controller = this;
			var fctSuccess = 
					function(resp) {
						_successCbk.call(controller, resp);
					};
			var fctFailure = 
					function(resp) {
						_failureCbk.call(controller, resp);
					};
					
			// this line is for development only, allowing to present results without calling Bing.
			//var jsonResp = this.mockSrvSearch();fctSuccess(jsonResp);
		
			$.ajax({
				method: 'POST',
				url: 'srv/gist',
				data: jsonRequestData,
				dataType: 'json',
				async: true,
		        success: fctSuccess,
		        error: fctFailure
			});
			
			tracer.trace('GistController.invokeSearchService', "exited");
	}
	
	validateQueryInput() {
		var isValid = true;
		var query = this.elementForProp("inpWord").val();
		if (query == null || query === "") {
			isValid = false;
			this.error("You need to enter something in the word field");
		}
		return isValid;
	}

	successGetCallback(resp) {
		var tracer = new Tracer('GistController.successGetCallback', true);
		tracer.trace("resp="+JSON.stringify(resp));
		
		if (resp.errorMessage != null) {
			this.failureGetCallback(resp);
		} else {
			this.setGetResults(resp);	
		}
		this.setGetBusy(false);
		tracer.trace("exited");
	}
	
	failureGetCallback(resp) {
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage = 
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}				
		this.error(resp.errorMessage);
		this.setGetBusy(false);
	}
	
	setGetBusy(flag) {
		this.busy = flag;
		if (flag) {
			this.disableSearchButton();	
			this.showSpinningWheel('divMessage','Searching');
			this.error("");
		} else {
			this.enableSearchButton();		
			this.hideSpinningWheel('divMessage');
		}
	}
	
	getSearchRequestData() {
		var tracer = new Tracer('GistController.getSearchRequestData');
		var word = this.elementForProp("inpWord").val().trim();
		if (word=='')
			word = null;

		var request = {
				word: word,
		};
		
		var jsonInputs = JSON.stringify(request);;
		
		tracer.trace("returning jsonInputs="+jsonInputs);
		return jsonInputs;
	}
	
	enableSearchButton() {
		this.elementForProp('btnGet').attr("disabled", false);
	}
	
	disableSearchButton() {
		this.elementForProp('btnGet').attr("disabled", true);
	}
	
	error(err) {
		this.elementForProp('divError').html(err);
		this.elementForProp('divError').show();	 
	}
	

	setGetResults(results) {
		var jsonResults = JSON.stringify(results);
		var divResults = this.elementForProp("divResults");
		
		divResults.empty();
		
		var word = this.elementForProp("inpWord").val();
		var decompositions = results.decompositions;
		var html = 'The word has the following decompositions: ';
		html += '<div id="gist-decompositions">';
		for (var idec=0; idec<decompositions.length; idec++) {
			html += this.generateTableForDecomposition(decompositions[idec]);
			var decomposition = decompositions[idec];
		}
		html += '</div>';
		
		console.log("results: "+JSON.stringify(results));
		var alignments = results.sentencePairs;
		if (alignments.length != 0) {
			html += "<div id='gist-alignments'>"
				+'<table id="tbl-alignments" class="alignments"><th>Inuktitut</th><th>English</th></tr>';
			for (var ial=0; ial<Math.min(30,alignments.length); ial++) {
				console.log(alignments[ial]);
				var inuktitutSentence = alignments[ial].sentences.in;
				var englishSentence = alignments[ial].sentences.en;
				html += '<tr><td>'+inuktitutSentence+'</td><td>'+englishSentence+'</td></tr>';
			}
			html += '</table></div>';
		} else {
			html += "This word was not found in the Hansard 1999-2002.";
		}

		divResults.append(html);
				
		var thisController = this;
		
		new RunWhen().domReady(function() {
				divResults.show();
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
		
	
	// ---------------------- Test Section ------------------------------ //

	onTest() {
		this.invokeTestService({}, 
				this.testSuccessCallback, this.testFailureCallback);
	}

	invokeTestService(jsonRequestData, _successCbk, _failureCbk) {
			var controller = this;
			var fctSuccess = 
					function(resp) {
						_successCbk.call(controller, resp);
					};
			var fctFailure = 
					function(resp) {
						_failureCbk.call(controller, resp);
					};
		
			$.ajax({
				method: 'POST',
				url: 'srv/hello',
				data: jsonRequestData,
				dataType: 'json',
				async: true,
		        success: fctSuccess,
		        error: fctFailure
			});
	}
	
	testSuccessCallback(resp) {
		var element = this.elementForProp("divTestResponse");
		element.empty();
		element.html(resp.message);
	}
	    
	testFailureCallback(resp) {
		var element = this.elementForProp("divTestResponse");
		element.empty();
		element.html("Server returned error, resp="+JSON.stringify(resp));
	}
	

}
