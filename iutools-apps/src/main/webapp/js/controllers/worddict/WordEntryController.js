/*
 * Controller for Dictionary Entry dialog.
 */

class WordEntryController extends IUToolsController {
    constructor(wdConfig) {
        var tracer = Debug.getTraceLogger('WordEntryController.constructor');
        tracer.trace("wdConfig="+JSON.stringify(wdConfig));
        super(wdConfig);
        this.hide();
        this.hideIconisationControls();

        // this.elementForProp("divWordEntry").draggable();
        $("#div-wordentry")
            .draggable(
                {
                    handle: ".div-floating-dlg-titlebar",
                    containment: "window"
                }
            )
        tracer.trace("upon exit, this="+JSON.stringify(this));
    }

    // Setup handler methods for different HTML elements specified in the config.
    attachHtmlElements() {
        // this.setEventHandler("divWordEntry_iconizer", "click", this.iconizeDivExampleWord);
        // this.setEventHandler("divWordEntry_iconized", "click", this.deiconizeDivExampleWord);

        var minButton = $('#div-wordentry-iconizer');
        var maxButton = $('#div-wordentry-iconized');

        minButton.click(function() {
            var dlgBox = $("#div-wordentry");
            var dlgOffset = dlgBox.offset();
            var winHeight = $(window).height();
            var winWidth = $(window).width();
            var margTop = winHeight - dlgOffset.top;
            var margLeft = winWidth - dlgOffset.left;
            console.log("Animating with winHeight="+winHeight+", winWidth="+winWidth+
                "margTop="+margTop+", margLeft="+margLeft);

            dlgBox.css({"left": dlgOffset.left, "top": dlgOffset.top}).animate({height: 0, width: 0, marginTop: margTop, marginLeft: margLeft, opacity: 0}, 250);
            maxButton.show(250);
        });

        maxButton.click(function() {
            $(".div-floating-dlg").animate({height: "600px", width: "600px", marginTop: 0, marginLeft: 0, opacity: 1}, 250);
            $(this).hide(250);
        });


    }

    onDictionaryLookupEvent(ev) {
        var element = ev.target;
        var exampleWord = $(element).text();
        this.dictionaryLookup(exampleWord);
    }

    dictionaryLookup(word, lang) {
        var tracer = Debug.getTraceLogger('WordEntryController.dictionaryLookup');
        tracer.trace("word="+word)
        this.show();
		this.initWordEntry(word);
		this.displayWordBeingLookedUp(word, null);
		this.showSpinningWheel("divWordEntry_message","Looking up word");

		var data = this.getWordDictRequestData(word, lang);
		tracer.trace(
            "Invoking word lookup service with:"+
            "\nthis.successWordDictCallback="+this.successWordDictCallback+
            "\nthis.failureWordDictCallback="+this.failureWordDictCallback);
		this.invokeWordDictService(
            data,  this.successWordDictCallback, this.failureWordDictCallback);
	}

	displayWordBeingLookedUp(word, wordInOtherScript) {
        var tracer = Debug.getTraceLogger('WordEntryController.displayWordBeingLookedUp');
        tracer.trace("word="+word)
        if (word != null) {
            var divWord = this.elementForProp("divWordEntry_word");
            var wordText = word;
            if (wordInOtherScript) {
                wordText += "/" + wordInOtherScript
            }
            divWord.html("<h2>" + wordText + "</h2>\n");
        }
	}
	
	invokeWordDictService(jsonRequestData, _successCbk, _failureCbk) {
        var tracer = Debug.getTraceLogger("WordEntryController.invokeWordDictService");
        tracer.trace("jsonRequestData="+this.asJsonString(jsonRequestData));
        tracer.trace(
            "\n_successCbk="+_successCbk+"\n_failureCbk="+_failureCbk
        );
        this.userActionStart("WORD_LOOKUP", 'srv2/worddict', jsonRequestData,
            _successCbk, _failureCbk)
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
		this.userActionEnd("WORD_LOOKUP", resp);
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
	
	getWordDictRequestData(_word, _lang) {
		var request = { 
			word: _word,
            lang: _lang
        };
		var jsonInputs = JSON.stringify(request);;
		return jsonInputs;
	}
	
	initWordEntry() {
        this.maximize()
        this.elementForProp("divWordEntry_word").html();
		this.elementForProp("divWordEntry_contents").html("");
	}
	
	displayWordEntry(results) {
		var tracer = Debug.getTraceLogger('WordEntryController.displayWordEntry');
		var lang = results.lang;
		var otherLang = results.otherLang;
		var divWordEntry = this.elementForProp("divWordEntry");
		this.hideSpinningWheel("divWordEntry_message");

		// Change the word being looked up in order to add its
        // transcoding in the other script
        // this.displayWordBeingLookedUp()
        var wordEntry = results.queryWordEntry;
        var word = null; var wordInOtherScript = null;
        if (wordEntry != null) {
            word = wordEntry.word; wordInOtherScript = wordEntry.wordInOtherScript;
        }
		this.displayWordBeingLookedUp(word, wordInOtherScript);
		if (wordEntry != null) {
            var html = "";
            html += this.htmlTranslations(wordEntry, otherLang);
            html += this.htmlRelatedWords(wordEntry, lang);
            html = this.htmlMorphologicalAnalyses(wordEntry, lang, html);
            html +=  this.htmlAlignmentsByTranslation(wordEntry, lang, otherLang);
            this.elementForProp("divWordEntry_contents").html(html);
            this.attachWordLookupListeners();
            this.enableAccordions();
        }
    }

    htmlTranslations(wordEntry, otherLang) {
        var tracer = Debug.getTraceLogger('WordEntryController.htmlTranslations');
        tracer.trace("wordEntry="+JSON.stringify(wordEntry));
        var heading = this.langName(otherLang)+" Translations"

        var info = this.translationsInfo(wordEntry);
        var translations = info.translations;

        var disclaimer = null;
        if (info.areRelatedTranslations) {
            heading = heading+" (Related words only)"
        }
        var html = "<h3>"+heading+"</h3>\n";

        var examples = info.examples;

        var totalDisplayed = 0;
        for (var ii=0; ii < translations.length; ii++) {
            var word = translations[ii];
            if (word === "ALL" || word === "MISC") {
                continue;
            }
            if (totalDisplayed > 0) {
                html += "; ";
            }
            html += this.htmlTranslationWord(word);
            totalDisplayed++;
        }
        if (totalDisplayed == 0) {
            html += "none";
        }
        html += "<br/>\n";

        return html;
    }

    htmlTranslationWord(word) {
        var html = "<a href=\"#examples4_"+word+"\">"+word+"</a>";
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

    langName(langCode) {
        var name = langCode;
        if (langCode === "iu") {
            name = "Inuktitut";
        } else if (langCode === "en") {
            name = "English";
        }
        return name;
    }

    htmlRelatedWords(wordEntry, lang) {
        var tracer = Debug.getTraceLogger('WordEntryController.htmlRelatedWords');
        tracer.trace("wordEntry="+JSON.stringify(wordEntry));
        var html = "";
        if (lang === "iu") {
            // We don't display related words for an English word
            html = "<h3>Related Words</h3>\n";
            var relatedWords = wordEntry.relatedWords;
            if (relatedWords.length == 0) {
                html += "none\n";
            } else {
                for (var ii = 0; ii < relatedWords.length; ii++) {
                    var word = relatedWords[ii];
                    if (ii > 0) {
                        html += ", ";
                    }
                    // html += word;
                    html += this.htmlClickableWordLookup(word);
                }
            }
            html += "<br/>\n";
        }

        return html;
    }

	htmlMorphologicalAnalyses(wordEntry, lang, html) {
        var tracer = Debug.getTraceLogger('WordEntryController.htmlMorphologicalAnalyses');
        tracer.trace("wordEntry="+JSON.stringify(wordEntry));
        if (lang === "iu") {
            // We only display decompositions for an inuktitut word
            html += "<h3>Morphological decomposition</h3>\n";
            var wordComponents = wordEntry.morphDecomp;
            if (wordComponents == null || wordComponents.length == 0) {
                html += "Word could not be decomposed";
            } else if (wordComponents != null) {
                html += '<table id="tbl-gist" class="gist"><tr><th>Morpheme</th><th>Meaning</th></tr>';
                for (var iwc = 0; iwc < wordComponents.length; iwc++) {
                    var component = wordComponents[iwc];
                    html +=
                        '<tr><td>' + component.canonicalForm + '</td>' +
                        '<td><em>' + component.grammar + '</em><br/><b>' + component.meaning + '</b></td></tr>'
                }
                html += '</table>';
            } else {
                html += "<h4>Word could not be decomposed into morphemes</h4>"
            }
        }
	
		return html;
	}

    htmlAlignmentsByTranslation(wordEntry, lang, otherLang) {
        var tracer = Debug.getTraceLogger('WordEntryController.htmlAlignmentsByTranslation');
        tracer.trace("wordEntry="+JSON.stringify(wordEntry));
        var html = "";
        var trInfo = this.translationsInfo(wordEntry);
        tracer.trace("trInfo="+JSON.stringify(trInfo));
		var translations = trInfo.translations;
        var translation2alignments = trInfo.examples;

        var heading = "Examples of use";
        if (trInfo.areRelatedTranslations) {
            heading += " (for related words)";
        }
		html += "<h3>"+heading+"</h3>\n";

        if (translations != null && translations.length > 0) {
            html += "<div class=\"accordion\" id=\"accordion\">\n";
		    for (var ii=0; ii < translations.length; ii++) {
		        var aTranslation = translations[ii];
		        var translAlignments = translation2alignments[aTranslation];
		        html +=
                    this.htmlAlignments4Translation(
		                aTranslation, translAlignments, lang, otherLang);
            }
            html += "</div>\n";
		} else {
			html += "<h4>Could not find examples of use for this word.</h4>"
		}

		return html;
	}

    htmlAlignments4Translation(aTranslation, aTransAlignments, lang, otherLang) {
        var html = ""
        // html += "<a name='examples4_"+aTranslation+"'/>\n";
        html += "<h4><a name=\"examples4_"+aTranslation+"\">as <i>\""+aTranslation+"\"</i>...</a></h4>\n";
        html +=
            "<div>\n"+
            "<p>\n"
            ;
        html += '<table id="tbl-alignments" class="alignments"><th>'+this.langName(lang)+'</th><th>'+this.langName(otherLang)+'</th></tr>';
        if (aTransAlignments != null && aTransAlignments.length > 0) {
            for (var jj=0; jj < aTransAlignments.length; jj++) {
                var anAlignment = aTransAlignments[jj];
                html += '<tr><td>'+anAlignment[0]+'</td><td>'+anAlignment[1]+'</td></tr>';
            }
        }
        html +=
            '' +
            "</table>\n"+
            "</p>\n"+
            "</div>\n";

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

    hide() {
        this.elementForProp("divWordEntry").hide();
        this.hideIconisationControls();
    }

    show() {
        var divWordEntry = this.elementForProp("divWordEntry");
        divWordEntry.css('display', 'flex');
        divWordEntry.show();
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

    enableAccordions() {
        // Note: The heightStyle ensures that there will not be empty space
        // at the bottom of entries when expanded.
        // $(".accordion" ).accordion();
        // $(".accordion" ).accordion({heightStyle: "fill"});
        $(".accordion" ).accordion({heightStyle: "content"});
    }
}