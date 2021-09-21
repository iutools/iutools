/*
 * Controller for the search.html page.
 */

class MorphemeDictionaryController extends IUToolsController {
	
	constructor(config) {
		super(config);
		this.wordDictController = new WordEntryController(config);
	} 
	
	// Setup handler methods for different HTML elements specified in the config.
	attachHtmlElements() {
		this.setEventHandler("btnGet", "click", this.onFindExamples);
		this.onReturnKey("inpMorpheme", this.onFindExamples);
	}
	
	onFindExamples() {
		Debug.getTraceLogger("MorphemeDictionaryController.onFindExamples").trace("invoked");
		this.elementForProp("divWordEntry_contents").html('').parent().hide();
		this.elementForProp('inpExampleWord').val('');
		this.elementForProp("divWordEntry").hide();
		var isValid = this.validateQueryInput();
		if (isValid) {
			this.clearResults();
			this.setGetBusy(true);
			var requestData = this.getSearchRequestData();
			if (!this.isDuplicateEvent("onFindExamples", requestData)) {
				this.logOnServer("MORPHEME_SEARCH", requestData)
				this.invokeFindExampleService(requestData,
					this.findExamplesSuccessCallback, this.findExamplesFailureCallback);
            }
		}
	}
	
	onExampleSelect(ev) {
		var element = ev.target;
		var exampleWord = $(element).text();
		occurrenceController.elementForProp("divWordEntry_contents").html('');
		occurrenceController.elementForProp("divWordEntry_word").html('');
        this.wordDictController.dictionaryLookup(exampleWord);
	}

	invokeFindExampleService(jsonRequestData, _successCbk, _failureCbk) {
		this.invokeService(jsonRequestData, _successCbk, _failureCbk,
				'srv2/morpheme_dictionary');
	}
	
	
	invokeService(jsonRequestData, _successCbk, _failureCbk, _url) {
			var tracer = Debug.getTraceLogger('OccurenceController.invokeService');
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
				url: _url,
				data: jsonRequestData,
				dataType: 'json',
				async: true,
		        success: fctSuccess,
		        error: fctFailure
			});
			
	}
	
	
	findExamplesSuccessCallback(resp) {
        Debug.getTraceLogger("MorphemeDictionaryController.findExamplesSuccessCallback").trace("resp="+JSON.stringify(resp));

        if (resp.errorMessage != null) {
			this.findExamplesFailureCallback(resp);
		} else {
			this.setResults(resp);
		}
		this.setGetBusy(false);
	}
	
	findExamplesFailureCallback(resp) {
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
		var wordPattern = this.elementForProp("inpMorpheme").val().trim();
		if (wordPattern=='')
			wordPattern = null;
		var corpusName = this.elementForProp("inpCorpusName").val().trim();
		if (corpusName=='')
			corpusName = null;
		var selectCorpus = this.elementForProp("selCorpusName");
		var selectedCorpusName = selectCorpus.val();
		corpusName = selectedCorpusName;
		var nbExamples = this.elementForProp("inpNbExamples").val().trim();
		if (nbExamples=='')
			nbExamples = "20";

		var request = {
				wordPattern: wordPattern,
				corpusName: corpusName,
				nbExamples: nbExamples
		};
		
		var jsonInputs = JSON.stringify(request);

		return jsonInputs;
	}
	
	enableSearchButton() {
		this.elementForProp('btnGet').attr("disabled", false);
	}
	
	disableSearchButton() {
		this.elementForProp('btnGet').attr("disabled", true);
	}
	
	error(err) {
		err = err.replace("\n", "<br/>\n");
		this.elementForProp('divError').html(err);
		this.elementForProp('divError').show();	 
	}
	

	setResults(results) {
        var tracer = Debug.getTraceLogger("MorphemeDictionaryController.setResults")
        tracer.trace("results="+JSON.stringify(results));
		var divResults = this.elementForProp("divResults");

		divResults.empty();

		var matchingMorphs = results.matchingMorphemes;
		var examplesForMorph = results.examplesForMorpheme;

		// Display number of hits
		var html = matchingMorphs.length+' morpheme'+
			(matchingMorphs.length==1?'':'s')+' found.<br/>';

		// Display table of matching morphekmes
		html += '<div id="list-of-morphemes">\n';
		html += '<table id="tbl-matching-morphs" class="gist">\n';
		for (var imorph=0; imorph < matchingMorphs.length; imorph++) {
		    var morpheme = matchingMorphs[imorph];
			var morphID = morpheme.id;
			var morphExampleWords = examplesForMorph[morphID];
            html +=
                this.htmlMorphemeRow(morpheme, morphExampleWords);
		}
        html += "</table><br/>\n";
		html += '</div><br/>&nbsp;';
		
		divResults.append(html);
		
		this.attachListenersToExampleWords();
		
		new RunWhen().domReady(function() {
				divResults.show();
		});
	}

    htmlMorphemeRow(morpheme, morphWords) {
        var tracer = Debug.getTraceLogger("MorphemeDictionaryController.htmlMorphemeRow")
        tracer.trace("morpheme="+JSON.stringify(morpheme)+", morphWords="+JSON.stringify(morphWords));

        var html =
            '<tr>\n' +
            '  <td>'+morpheme.canonicalForm+'</td>\n'+
            '  <td><i>'+morpheme.grammar+'</i><br/>\n'+
            '      '+morpheme.meaning+'<br/>\n'+
            '      <b>Examples:</b> '
            ;

        for (var ii=0; ii < morphWords.length; ii++) {
            var aWord = morphWords[ii];
            html +=
                '<a class="word-example" id="word-example-'+aWord+'"'
                + '>'+aWord+'</a>&nbsp;&nbsp;&nbsp;'
            ;
        }

        html +=
            '\n' +
            '  </td>\n'+
            '</tr>\n'
        ;

        return html;
    }


	attachListenersToExampleWords() {		
		var anchorsWords = $(document).find('.word-example');
	    for (var ipn=0; ipn<anchorsWords.length; ipn++) {
	    	this.setEventHandler(anchorsWords.eq(ipn), "click", this.onExampleSelect);	   
	    }
	}
	
	validateQueryInput() {
		var isValid = true;
		var query = this.elementForProp("inpMorpheme").val();
		if (query == null || query === "") {
			isValid = false;
			this.error("You need to enter something in the morpheme field");
		}
		return isValid;
	}
	
	clearResults() {
		this.elementForProp('divError').empty();
		this.elementForProp('divResults').empty();
	}
	

	
	// ---------------------- Test Section ------------------------------ //

	onTest() {
		this.invokeTestService({}, 
				this.testSuccessCallback, this.testFailureCallback);
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
