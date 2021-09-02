/*
 * Controller for Dictionary Entry dialog.
 */

class WordEntryController extends IUToolsController {
    constructor(wdConfig) {
        var tracer = Debug.getTraceLogger('WordEntryController.constructor');
        tracer.trace("wdConfig="+JSON.stringify(wdConfig));
        super(wdConfig);
        this.hideIconisationControls();

        // this.elementForProp("divWordEntry").draggable();
        $("#div-gist").draggable()
        tracer.trace("upon exit, this="+JSON.stringify(this));
    }

    // Setup handler methods for different HTML elements specified in the config.
    attachHtmlElements() {
        this.setEventHandler("divWordEntry_iconizer", "click", this.iconizeDivExampleWord);
        this.setEventHandler("divWordEntry_iconized", "click", this.deiconizeDivExampleWord);
    }

    onDictionaryLookupEvent(ev) {
        var element = ev.target;
        var exampleWord = $(element).text();
        this.dictionaryLookup(exampleWord);
    }

    dictionaryLookup(word) {
        var tracer = Debug.getTraceLogger('WordEntryController.dictionaryLookup');
        tracer.trace("word="+word)
		this.elementForProp("divWordEntry").show();
		this.clearWordEntry();
		this.displayWordBeingLookedUp(word);
		this.showSpinningWheel("divWordEntry_message","Looking up word");

		var data = this.getWordDictRequestData(word);
		this.logOnServer("GIST_WORD", data);
		this.invokeWordDictService(
            data,
            this.successWordDictCallback,
            this.failureWordDictCallback);
	}

	displayWordBeingLookedUp(word, wordInOtherScript) {
        this.maximize()
		var divWord = this.elementForProp("divWordEntry_word");
        var wordText = word;
        if (wordInOtherScript) {
            wordText += "/"+wordInOtherScript
        }
		divWord.html("<h2>"+wordText+"</h2>\n");
	}
	
	invokeWordDictService(jsonRequestData, _successCbk, _failureCbk) {
		this.invokeWebService('srv2/worddict', jsonRequestData,
				_successCbk, _failureCbk);
	}

	successWordDictCallback(resp) {
		var tracer = Debug.getTraceLogger('WordEntryController.successWordDictCallback');
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
		var tracer = Debug.getTraceLogger('WordEntryController.displayWordEntry');
		var divWordEntry = this.elementForProp("divWordEntry");
		this.hideSpinningWheel("divWordEntry_message");

		// Change the word being looked up in order to add its
        // transcoding in the other script
        // this.displayWordBeingLookedUp()
        var wordEntry = results.queryWordEntry;
		this.displayWordBeingLookedUp(
            wordEntry.word, wordEntry.wordInOtherScript);

        var html = "";
		html += this.htmlTranslations(wordEntry);
		html += this.htmlRelatedWords(wordEntry);
        html = this.htmlMorphologicalAnalyses(wordEntry, html);
		html = this.htmlAlignments(wordEntry, html);
		this.elementForProp("divWordEntry_contents").html(html);
		this.attachWordLookupListeners();
    }

    htmlTranslations(wordEntry) {
        var tracer = Debug.getTraceLogger('WordEntryController.htmlTranslations');
        tracer.trace("wordEntry="+JSON.stringify(wordEntry));
        var heading = "English Translations"

        var info = this.translationsInfo(wordEntry);
        var disclaimer = null;
        if (info.areRelatedTranslations) {
            heading = heading+" (Related words only)"
        }
        var html = "<h3>"+heading+"</h3>\n";

        var examples = info.examples;
        var translations = Object.keys(examples);

        var totalDisplayed = 0;
        for (var ii=0; ii < translations.length; ii++) {
            var word = translations[ii];
            if (word === "ALL" || word === "MISC") {
                continue;
            }
            if (totalDisplayed > 0) {
                html += ", ";
            }
            html += word;
            totalDisplayed++;
        }
        if (totalDisplayed == 0) {
            html += "none";
        }
        html += "<br/>\n";

        return html;
    }

    translationsInfo(wordEntry) {
        var tracer = Debug.getTraceLogger('WordEntryController.translationsInfo');
        tracer.trace("wordEntry="+JSON.stringify(wordEntry));
        var translations = wordEntry.origWordTranslations;
        var examples = wordEntry.examplesForOrigWordTranslation;
        var areRelatedTranslations = false;
        if (examples == null || Object.keys(examples).length == 0) {
            translations = wordEntry.relatedWordTranslations;
            examples = wordEntry.examplesForRelWordsTranslation;
            areRelatedTranslations = true;
        }

        var info =
            {
                'translations': translations,
                'examples': examples,
                'areRelatedTranslations': areRelatedTranslations
            };
        return info;
    }

    htmlRelatedWords(wordEntry) {
        var tracer = Debug.getTraceLogger('WordEntryController.htmlRelatedWords');
        tracer.trace("wordEntry="+JSON.stringify(wordEntry));
        var html = "<h3>Related Words</h3>\n";
        var relatedWords = wordEntry.relatedWords;
        for (var ii=0; ii < relatedWords.length; ii++) {
            var word = relatedWords[ii];
            if (ii > 0) {
                html += ", ";
            }
            // html += word;
            html += this.htmlClickableWordLookup(word);
        }
        html += "<br/>\n";

        return html;
    }

	htmlMorphologicalAnalyses(wordEntry, html) {
        var tracer = Debug.getTraceLogger('WordEntryController.htmlMorphologicalAnalyses');
        tracer.trace("wordEntry="+JSON.stringify(wordEntry));
		html += "<h3>Morphological decomposition<h3>\n";
		var wordComponents = wordEntry.morphDecomp;
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
	
	htmlAlignments(wordEntry, html) {
        var trInfo = this.translationsInfo(wordEntry);
		var translations = trInfo.translations;
        var alignments = trInfo.examples;

        var heading = "Examples";
        if (trInfo.areRelatedTranslations) {
            heading += " (for related words)";
        }
		html += "<h3>"+heading+"</h3>\n";
		if (translations != null && translations.length > 0) {
			html += '<table id="tbl-alignments" class="alignments"><th>Inuktitut</th><th>English</th></tr>';
            for (var ii=0; ii < translations.length; ii++) {
                var aTranslation = translations[ii];
                var aTransAlignments = alignments[aTranslation];
                if (aTransAlignments != null && aTransAlignments.length > 0) {
                    for (var jj=0; jj < aTransAlignments.length; jj++) {
                        var anAlignment = aTransAlignments[jj];
                        html += '<tr><td>'+anAlignment[0]+'</td><td>'+anAlignment[1]+'</td></tr>';
                    }
                }
			}
			html += '</table>';
		} else {
			html += "<h4>Could not find examples of use for this word.</h4>"
		}
		
		return html;
	}

    attachWordLookupListeners() {
        var anchorsWords = $(document).find('.clickable-word-lookup');
        for (var ipn=0; ipn<anchorsWords.length; ipn++) {
            this.setEventHandler(
                anchorsWords.eq(ipn), "click",
                this.onDictionaryLookupEvent);
        }
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

    htmlClickableWordLookup(word) {
        var html = '<a class="clickable-word-lookup">'+word+'</a>';
        return html;
    }

    hideIconisationControls() {
        this.elementForProp("divWordEntry_iconizer").hide();
        this.elementForProp("divWordEntry_iconized").hide();
    }

    maximize() {
        this.elementForProp("divWordEntry_iconized").hide();
        this.elementForProp("divWordEntry_iconizer").show();
    }
    minimize() {
        this.elementForProp("divWordEntry_iconized").show();
        this.elementForProp("divWordEntry_iconizer").hide();
    }
}