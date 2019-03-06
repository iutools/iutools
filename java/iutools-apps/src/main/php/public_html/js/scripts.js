$(document).ready(function() {
	
	/*$('button#submit-request').on('click',function() {
		var request = $('input#request').val();
		console.log('request: '+request);
		$.ajax({
			url: '_ajax-get-expansions.php',
			dataType: 'json',
			method: 'post',
			data: { request: request },
			success: function(json) {
				$('div#hits').html('');
				console.log(JSON.stringify(json));
				console.log('expanded request: '+json.expandedQuery);
				$('input#request').val(json.expandedQuery);
				console.log('hits:'+JSON.stringify(json.hits));
				for (var i=0; i<json.hits.length; i++) {
					var title = $('<div class="hit-disp title"><a class="hit-title" href="'+json.hits[i].url+'">'+json.hits[i].title+'</a></div>');
					var url = $('<div class="hit-disp"><a class="hit-url" href="'+json.hits[i].url+'">'+json.hits[i].url+'</a></div>');
					var snippet = $('<div class="hit-disp"><span class="hit-snippet">'+json.hits[i].snippet+'</span></div>');
					$('div#hits').append(title);
					$('div#hits').append(url);
					$('div#hits').append(snippet);
					$('div#hits').append('<br>');
				}
			}
		});
	});*/
});