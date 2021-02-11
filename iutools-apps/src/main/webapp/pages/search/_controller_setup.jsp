<% String IUTOOLS_JS_VERSION=(new java.util.Date()).toLocaleString(); %>

<script src="js/OccurenceController.js?version=<%= IUTOOLS_JS_VERSION %>"></script>

<script>
	function onTest() {
		$.ajax({
			method: 'POST',
			url: 'srv/hello',
			data: {},
			dataType: 'json',
			async: true,
	        success: testSuccessCallback,
	        error: testFailureCallback
		});
	}

	function testSuccessCallback(resp) {
		$("#div-resp").empty();
		$("#div-resp").html(resp.message);
	}

	function testFailureCallback(resp) {
		$("#div-resp").empty();
		$("#div-resp").html("Server returned error, resp="+JSON.stringify(resp));
	}


	// Setup and configure the controller for this page
    var config = {
    		btnSearch: "btn-search",
    		txtQuery: "txt-query-words",
    		divMessage: "div-message",
    		divTotalHits: "div-total-hits",
    		divResults:  "div-search-results",
    		divError: "div-error",
    		divPageNumbers: "page-numbers",
    		prevPage: "previous-page",
    		nextPage: "next-page"
        };
    var srchController;
    $(document).ready(function() {srchController = new SearchController(config);});
</script>
