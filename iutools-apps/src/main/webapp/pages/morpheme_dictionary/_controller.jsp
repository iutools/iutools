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

    		// Button for starting the search
    		btnGet: "btn-occ",

    		// Areas where to display results, progress status and error message.
    		divResults:  "div-results",
    		divMessage: "div-message",
    		divError: "div-error",

    		inpExampleWord: "example-word",
    		inpNbExamples: "nb-examples",
        };
    var occurrenceController = new MorphemeDictionaryController(config);
</script>
