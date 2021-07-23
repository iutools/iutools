/*
 * Controller for IU Word Dictionary dialog.
 */

class WordDictController extends IUToolsController {
    constructor(gistConfig) {
        var tracer = Debug.getTraceLogger('WordDictController.constructor');
        tracer.trace("gistConfig="+JSON.stringify(gistConfig));
        var wdConfig = {};

        wdConfig['divWordEntry'] = gistConfig['divGist'];
        wdConfig['divWordEntry_message'] = gistConfig['divGist_message'];
        wdConfig['divWordEntry_word'] = gistConfig['divGist_word'];
        wdConfig['divWordEntry_contents'] = gistConfig['divGist_contents'];
        wdConfig['divWordEntry_iconized'] = gistConfig['divGist_iconized'];
        super(wdConfig);

        tracer.trace("upon exit, this="+JSON.stringify(this));
    }

    dictionaryLookup(word) {
        var tracer = Debug.getTraceLogger('WordDictController.dictionaryLookup');
        tracer.trace("word="+word)
		this.elementForProp("divWordEntry").show();
		this.clearWordEntry();
		this.displayWordBeingLookedUp(word);
		this.showSpinningWheel("divWordEntry_message","Gisting word");

		var data = this.getWordDictRequestData(word);
		this.logOnServer("GIST_WORD", data);
		this.invokeWordDictService(
            data,
            this.successWordDictCallback,
            this.failureWordDictCallback);
	}

	displayWordBeingLookedUp(word) {
		var divWord = this.elementForProp("divWordEntry_word");
		divWord.html("<h2>"+word+"</h2>\n");
	}
	
	invokeWordDictService(jsonRequestData, _successCbk, _failureCbk) {
		this.invokeWebService('srv2/worddict', jsonRequestData,
				_successCbk, _failureCbk);
	}

	successWordDictCallback(resp) {
		var tracer = Debug.getTraceLogger('WordDictController.successWordDictCallback');
		tracer.trace("resp="+JSON.stringify(resp));
		if (resp.errorMessage != null) {
			this.failureWordDictCallback(resp);
		} else {
			this.displayWordEntry(resp);
		}
		this.setWordDictBusy(false);
	}
	
	failureWordDictCallback(resp) {
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage = 
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}				
		this.error(resp.errorMessage);
		this.hideSpinningWheel("divWordEntry_message");
	}
	
	setWordDictBusy(flag) {
		this.busy = flag;		
		if (flag) {
			this.showSpinningWheel('divWordEntry_message','Searching');
		} else {
			this.hideSpinningWheel('divWordEntry_message');
		}
	}	
	
	getWordDictRequestData(_word) {
		var request = { 
			word: _word
			};
		var jsonInputs = JSON.stringify(request);;
		return jsonInputs;
	}
	
	clearWordEntry() {
		this.elementForProp("divWordEntry_word").html("");
		this.elementForProp("divWordEntry_contents").html("");
	}
	
	displayWordEntry(results) {
		var tracer = Debug.getTraceLogger('WordGistController.displayWordGist');
		var divWordEntry = this.elementForProp("divWordEntry");
		this.hideSpinningWheel("divWordEntry_message");
		
		var html = this.htmlMorphologicalAnalyses(results, html);
		html = this.htmlAlignments(results, html);
		this.elementForProp("divWordEntry_contents").html(html);
	}
	
	htmlMorphologicalAnalyses(results, html) {
        var tracer = Debug.getTraceLogger('WordGistController.htmlMorphologicalAnalyses');
        tracer.trace("results="+JSON.stringify(results));
		var html = "<h3>Morphological decomposition<h3>\n";
		var wordComponents = results.entry.morphDecomp;
		if (wordComponents != null) {
			html += '<table id="tbl-gist" class="gist"><tr><th>Morpheme</th><th>Meaning</th></tr>';
			for (var iwc=0; iwc<wordComponents.length; iwc++) {
				var component = wordComponents[iwc];
				html +=
                    '<tr><td>'+component.canonicalForm+'</td>'+
                    '<td><em>'+component.grammar+'</em><br/><b>'+component.meaning+'</b></td></tr>'
			}
			html += '</table>';
		} else {
			html += "<h4>Word could not be decomposed into morphemes</h4>"
		}
	
		return html;
	}
	
	
	htmlAlignments(results, html) {
		var gist = results.wordGist
		var alignments = results.entry.examplesForTranslation['ALL'];

		html += "<h3>Examples</h3>\n";
		if (alignments != null && alignments.length > 0) {
			html += '<table id="tbl-alignments" class="alignments"><th>Inuktitut</th><th>English</th></tr>';
            for (var ii=0; ii < alignments.length; ii++) {
                var anAlignment = alignments[ii];
				html += '<tr><td>'+anAlignment[0]+'</td><td>'+anAlignment[1]+'</td></tr>';
			}
			html += '</table>';
		} else {
			html += "<h4>Could not find examples of use for this word.</h4>"
		}
		
		return html;
	}
	
	iconizeDivExampleWord() {
		var divWordEntry = this.elementForProp("divWordEntry");
		divWordEntry.hide();
		var divIconizedWordExample = this.elementForProp("divWordEntry_iconized");
		divIconizedWordExample.show();
	}
	
	deiconizeDivExampleWord() {
		var divWordEntry = this.elementForProp("divWordEntry");
		divWordEntry.show();
		var divIconizedWordExample = this.elementForProp("divWordEntry_iconized");
		divIconizedWordExample.hide();
	}
}