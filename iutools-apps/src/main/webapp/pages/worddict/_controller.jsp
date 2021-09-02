<% String IUTOOLS_JS_VERSION=(new java.util.Date()).toLocaleString(); %>

<script src="js/controllers/worddict/WordDictController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>

<script>
	// Setup and configure the controller for this page
    var config = {
        divMessage: "div-message",
        divError: "div-error",

        // Search form
        txtQuery: "txt-word-query",
        btnSearch: "btn-search-word",

        // Div where search results will be displayed
        divSearchResults: "div-results",

        // Floating div where we display the dictionary entry of a word
        // (after the user clicks on that word)
        //
        divWordEntry: "div-gist",
        divWordEntry_contents: "div-wordentry-contents",
        divWordEntry_iconizer: "div-gist-iconizer",
        divWordEntry_iconized: "div-gist-iconized",
        divWordEntry_message: "div-gist-message",
        divWordEntry_word: "div-wordentry-word",
    };

    var wordEntryController = new WordEntryController(config);
    var wordDictController = new WordDictController(config);
</script>


