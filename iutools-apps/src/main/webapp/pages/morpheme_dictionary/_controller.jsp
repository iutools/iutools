<% String IUTOOLS_JS_VERSION=(new java.util.Date()).toLocaleString(); %>

<script src="js/controllers/morpheme_dictionary/MorphemeDictionaryController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="js/controllers/concordancer/Alignment.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="js/utils/Morpheme.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="js/utils/IUUtils.js?version=<%= IUTOOLS_JS_VERSION %>"></script>

<script>
	// Setup and configure the controller for this page
    // TODO: replace corpus-name by corpus to get the value of the select, etc.
    var config = {
    		// Morpheme for which we are searching examples
    		inpMorpheme: "morpheme",

    		// Widgets for specifying the corpus in which to search examples
    		inpCorpusName: "corpus-name",
    		selCorpusName: "corpus",

    		// Button for starting the search
    		btnGet: "btn-occ",

    		// Areas where to display results, progress status and error message.
    		divResults:  "div-results",
    		divMessage: "div-message",
    		divError: "div-error",

    		inpExampleWord: "example-word",
    		inpNbExamples: "nb-examples",


    		// Floating div where we display the "gist" of an example word
    		// (after the user clicks on the example word)
    		//
            divWordEntry_contents: "div-gist-contents",
            divWordEntry: "div-gist",
       		divWordEntry_iconizer: "div-gist-iconizer",
       		divWordEntry_iconized: "div-gist-iconized",
       		divWordEntry_message: "div-gist-message",
       		divWordEntry_word: "div-gist-word",
        };
    var occurrenceController = new MorphemeDictionaryController(config);
</script>
