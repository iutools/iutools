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
		var tracer = Debug.getTraceLogger('MorphemeDictionaryController.onFindExamples');
		tracer.trace("invoked");
        var isValid = this.validateQueryInput();
        if (isValid) {
            this.clearResults();
            this.setGetBusy(true);
            var requestData = this.getSearchRequestData();
            if (!this.isDuplicateEvent("onFindExamples", requestData)) {
                tracer.trace("Invoking service with"+
                    ": this.findExamplesSuccessCallback="+this.findExamplesSuccessCallback+
                    ", this.findExamplesFailureCallback="+this.findExamplesFailureCallback)
                this.invokeFindExampleService(requestData,
                    this.findExamplesSuccessCallback, this.findExamplesFailureCallback);
            }
        }
	}
	
	onExampleSelect(ev) {
		var element = ev.target;
		var exampleWord = $(element).text();
        this.wordDictController.dictionaryLookup(exampleWord);
	}

	invokeFindExampleService(actionData, cbkActionSuccess, cbkActionFailure) {
        this.userActionStart(
            'MORPHEME_SEARCH', 'srv2/morpheme_dictionary',
            actionData, cbkActionSuccess, cbkActionFailure)
	}

	findExamplesSuccessCallback(resp) {
        var tracer = Debug.getTraceLogger('MorphemeDictionaryController.findExamplesSuccessCallback');
        tracer.trace("resp="+JSON.stringify(resp));

        if (resp.errorMessage != null) {
			this.findExamplesFailureCallback(resp);
		} else {
			this.setResults(resp);
		}
        this.scrollIntoView(this.elementForProp("divResults"));
		this.setGetBusy(false);
        this.userActionEnd('MORPHEME_SEARCH', resp);
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
        var corpusName = null;
		var nbExamples = this.elementForProp("inpNbExamples").val().trim();
		if (nbExamples=='')
			nbExamples = "20";

		var request = {
				wordPattern: wordPattern,
				corpusName: corpusName,
				nbExamples: nbExamples
		};
		

		return request;
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

		// Display table of matching morphemes
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
            '      '+morpheme.meaning+'<br/>\n      ';

        if (morphWords.length == 0) {
            html += "<b>No examples found for this morpheme</b>";
        } else {
            html += "<b>Examples:</b> ";
        }

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
	    this.wordDictController.hide();
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
