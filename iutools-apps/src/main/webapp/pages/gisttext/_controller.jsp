<% String IUTOOLS_JS_VERSION=(new java.util.Date()).toLocaleString(); %>

<script src="js/utils/IUUtils.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="js/utils/HtmlUtils.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="js/controllers/gist/GistTextController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>
<script src="js/controllers/concordancer/Alignment.js?version=<%= IUTOOLS_JS_VERSION %>"></script>

<script>
	// Setup and configure the controller for this page
    // TODO: replace corpus-name by corpus to get the value of the select, etc.
    var config = {
    		btnGist: "btn-gisttext",
    		divResults:  "div-results",
    		txtUrlOrText: "txt-url-or-text",
    		divMessage: "div-message",
    		divError: "div-error",

    		// Area where to put the clickable gist of the whole text
    		divGistTextResults: "div-gist-text-results",

       		// Floating div where we display the "gist" of a word
       		// (after the user clicks on the example word)
       		//
       		divWordEntry_contents: "div-wordentry-contents",
            divWordEntry: "div-gist",
       		divWordEntry_iconizer: "div-wordentry-iconizer",
       		divWordEntry_iconized: "div-gist-iconized",
       		divWordEntry_message: "div-wordentry-message",
       		divWordEntry_word: "div-wordentry-word",
        };

    var gistTextController = new GistTextController(config);
</script>
