/*
 * Controller for Dictionary Entry dialog.
 */

class WordEntryController extends IUToolsController {
    constructor(weConfig) {
        var tracer = Debug.getTraceLogger('WordEntryController.constructor');
        tracer.trace("wdConfig="+jsonStringifySafe(weConfig));
        super(weConfig);
        this.windowController =
            new FloatingWindowController(weConfig);

        this.hide();
        tracer.trace("upon exit, this="+jsonStringifySafe(this));
    }

    // Setup handler methods for different HTML elements specified in the config.
    attachHtmlElements() {
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
		this.displayWordBeingLookedUp(word, null);
		this.showSpinningWheel("Looking up word");

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
            var wordText = word;
            if (wordInOtherScript) {
                wordText += "/" + wordInOtherScript
            }
            this.windowController.setTitle(wordText);
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
		tracer.trace("resp="+jsonStringifySafe(resp));
		if (resp.errorMessage != null) {
			this.failureWordDictCallback(resp);
		} else {
			this.displayWordEntry(resp);
		}
		this.setWordDictBusy(false);
		this.userActionEnd("WORD_LOOKUP", resp);
	}
	
	failureWordDictCallback(resp) {
		var tracer = Debug.getTraceLogger('WordEntryController.failureWordDictCallback');
		tracer.trace("resp="+jsonStringifySafe(resp));
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage = 
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}				
		this.error(resp.errorMessage);
	}
	
	setWordDictBusy(flag) {
		this.busy = flag;		
		if (flag) {
            this.showSpinningWheel('Searching');
		}
	}	
	
	getWordDictRequestData(_word, _lang) {
        var tracer = Debug.getTraceLogger("WordEntryController.getWordDictRequestData");
        var iuAlphabet = new SettingsController().iuAlphabet();
		var request = { 
			word: _word,
            lang: _lang,
            iuAlphabet: iuAlphabet,
        };
		var jsonInputs = jsonStringifySafe(request);;
		tracer.trace("Returning jsonInputs="+jsonInputs);
		return jsonInputs;
	}

	displayWordEntry(result) {
		var tracer = Debug.getTraceLogger('WordEntryController.displayWordEntry');
		tracer.trace("results="+jsonStringifySafe(result));
		var lang = result.lang;
		var otherLang = result.otherLang;

		// Change the word being looked up in order to add its
        // transcoding in the other script
        var wordEntryData = (result.queryWordEntry == null ? null: new WordEntryData(result.queryWordEntry));
        var word = null; var wordInOtherScript = null;
        if (wordEntryData != null) {
            word = wordEntryData.word; wordInOtherScript = wordEntryData.wordInOtherScript;
        }
		this.displayWordBeingLookedUp(word, wordInOtherScript);
        var html = null;
        if (wordEntryData == null) {
            html = "No entry found for this word";
        } else {
            html =
                "<div id='div-info' class='div-info' align='right'>\n" +
                "  <a href='help.jsp?topic=about_dictionary' target='#iutools_help'></a>\n" +
                "</div>";

            html += this.htmlStandardizedSpelling(wordEntryData);
            html += this.htmlTranslations(wordEntryData, otherLang);
            html += this.htmlRelatedWords(wordEntryData, lang);
            html = this.htmlMorphologicalAnalyses(wordEntryData, lang, html);
            html += this.htmlExamplesByTranslation(wordEntryData, lang, otherLang);
        }
        this.windowController.setBody(html);
        this.attachWordLookupListeners();
        this.enableAccordions();
    }

    htmlStandardizedSpelling(wordEntry) {
        // public String wordStandardizedSpelling = null;
        // public String otherScriptStandardizedSpelling = null;
        var html = "";

        if (wordEntry.wordStandardizedSpelling) {
            html +=
                "\n"+
                "<h3>Standardized Spelling</h3>\n"+
                wordEntry.wordStandardizedSpelling + "/" +
                    wordEntry.otherScriptStandardizedSpelling
                "\n";
        }

        return html;
    }


    htmlTranslations(wordEntry, otherLang) {
        var tracer = Debug.getTraceLogger('WordEntryController.htmlTranslations');
        tracer.trace("wordEntry="+jsonStringifySafe(wordEntry));
        var heading = this.langName(otherLang)+" Translations"


        if (wordEntry.translationsAreForRelatedWords()) {
            heading = heading+" (Related words only)"
        }
        var html = "<h3>"+heading+"</h3>\n";

        var examples = wordEntry.examples4Translation;

        var totalTranslationsDisplayed = 0;
        var totalL1WordsDisplayed = 0;
        if (wordEntry.translationsAreForRelatedWords()) {}
        var l1Words = wordEntry.l1Words();
        for (var ii=0; ii < l1Words.length; ii++) {
            var l1Word = l1Words[ii];
            var wordTranslations = wordEntry.getTranslations4l1Word(l1Word);
            if (wordTranslations == null || typeof wordTranslations == 'undefined') {
                continue;
            }

            if (wordEntry.translationsAreForRelatedWords()) {
                if (ii > 0) {
                    html += "<br/>\n";
                }
                html += this.highlightCommonLead(l1Word, wordEntry.word)+": ";
            }
            totalL1WordsDisplayed++;
            tracer.trace("ii="+ii+", l1Word="+l1Word+", wordTranslations="+wordTranslations);

            var isFirst = true;
            for (var jj=0; jj < wordTranslations.length; jj++) {
                var aTranslation = wordTranslations[jj];
                if (!wordEntry.bestTranslations.includes(aTranslation)) {
                    continue;
                }
                if (!isFirst) {
                    html += "; ";
                }
                html += this.htmlTranslationWord(wordTranslations[jj], wordEntry);
                isFirst = false;
                totalTranslationsDisplayed++;
            }
        }
        if (totalTranslationsDisplayed == 0) {
            html += "none";
        }
        html += "<br/>\n";

        return html;
    }

    htmlTranslationWord(word, wordEntry) {
        if (wordEntry.isHumanTranslation(word)) {
            word = "<b>" + word + "</b>";
        }
        var html = "<a href=\"#examples4_"+word+"\">"+word+"</a>";
        return html;
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
        tracer.trace("wordEntry="+jsonStringifySafe(wordEntry));
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
        tracer.trace("wordEntry="+jsonStringifySafe(wordEntry));
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

    htmlExamplesByTranslation(wordEntry, lang, otherLang) {
        var tracer = Debug.getTraceLogger('WordEntryController.htmlAlignmentsByTranslation');
        tracer.trace("wordEntry="+jsonStringifySafe(wordEntry));
        var html = "";
        tracer.trace("wordEntry.bestTranslations="+jsonStringifySafe(wordEntry.bestTranslations));

        var heading = "Examples of use";
        if (wordEntry.translationsAreForRelatedWords()) {
            heading += " (for related words)";
        }
		html += "<h3>"+heading+"</h3>\n";

        if (wordEntry.bestTranslations != null && wordEntry.bestTranslations.length > 0) {
            html += "<div class=\"accordion\" id=\"accordion\">\n";
		    for (var ii=0; ii < wordEntry.bestTranslations.length; ii++) {
		        var aTranslation = wordEntry.bestTranslations[ii];
		        var translAlignments = wordEntry.translationExamplesIndex[aTranslation];
		        html +=
                    this.htmlExamples4Translation(
		                aTranslation, translAlignments, lang, otherLang, wordEntry);
            }
            html += "</div>\n";
		} else {
			html += "<h4>Could not find examples of use for this word.</h4>"
		}

		return html;
	}

    htmlExamples4Translation(aTranslation, aTransAlignments, lang, otherLang, wordEntry) {
        var tracer = Debug.getTraceLogger('WordEntryController.htmlExamples4Translation');
        tracer.trace("aTranslation="+aTranslation+", aTransAlignments="+jsonStringifySafe(aTransAlignments));
        var html = ""
        // html += "<a name='examples4_"+aTranslation+"'/>\n";
        html += "<h4><a name=\"examples4_"+aTranslation+"\">as <i>\""+aTranslation+"\"</i>...</a></h4>\n";
        html +=
            "<div>\n"+
            "<p>\n"
            ;

        // First, show the glossary sources if any available
        html += this.htmlGlossaryReference(aTranslation, wordEntry);

        if (aTransAlignments != null && aTransAlignments.length > 0) {
            html += '<table id="tbl-alignments" class="alignments"><th>'+this.langName(lang)+'</th><th>'+this.langName(otherLang)+'</th></tr>';
            for (var jj=0; jj < aTransAlignments.length; jj++) {
                var anAlignment = aTransAlignments[jj];
                html += '<tr><td>'+anAlignment[0]+'</td><td>'+anAlignment[1]+'</td></tr>';
            }
            html += "</table>\n";
        } else {
            html += "No examples found for this translation"
        }
        html +=
            "</p>\n"+
            "</div>\n";

        return html;
    }

    htmlGlossaryReference(aTranslation, wordEntry) {
        var html = "";
        if (wordEntry.isHumanTranslation(aTranslation)) {
            html +=
                "<table id=\"tbl-glossary-ref\" class=\"alignments\">\n"+
                "<tbody>\n"+
                "<tr><th>Found in glossaries</th></tr>\n";
            html += "<tr><td>c.f. ";
            var isFirst = true;
            var glossaries = wordEntry.glossarySources4word(aTranslation, wordEntry.otherLang())
            for (const glossaryName of glossaries) {
                if (!isFirst) {
                    html += ", "
                }
                isFirst = false;
                html += glossaryName;
            }
            html += "</td></tr>\n<p/>\n";
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

    htmlClickableWordLookup(word) {
        var html = '<a class="clickable-word-lookup">'+word+'</a>';
        return html;
    }

    hide() {
        this.windowController.hide();
    }

    show() {
        this.windowController.show();
    }

    maximize() {
        this.windowController.maximize();
    }
    minimize() {
        this.windowController.minimize();
    }

    enableAccordions() {
        // Note: The heightStyle ensures that there will not be empty space
        // at the bottom of entries when expanded.
        // $(".accordion" ).accordion();
        // $(".accordion" ).accordion({heightStyle: "fill"});
        $(".accordion" ).accordion({heightStyle: "content"});
    }

    showSpinningWheel(message) {
        this.windowController.showSpinningWheel(".wb-body", message);
    }

    highlightCommonLead(l1Word, word) {
        var highlighted = "";
        // var highlighted = "<strong>";
        var foundDifferences = false;
        for (var ii=0; ii < l1Word.length; ii++) {
            if (ii >= word.length || l1Word.charAt(ii) !== word.charAt(ii)) {
                if (!foundDifferences) {
                    // highlighted += "</strong>";
                    highlighted += "<strong>"
                    foundDifferences = true;
                }
            }
            highlighted += l1Word.charAt(ii);
        }
        if (foundDifferences) {
            highlighted += "</strong>";
        }

        return highlighted;
    }
}