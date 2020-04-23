$(document).ready(function() {
	
	var hitsPerPage = 2;
	var currentPage = null;
	var hitsForQuery = null;
	var nbPages = null;
	
	$('button#submit-request').on('click',function() {
		var request = $('input#request').val();
		console.log('request: '+request);
		$.ajax({
//			url: 'service-get-expansions.php',
			url: 'search',
			dataType: 'json',
			method: 'post',
			data: { request: request },
			success: function(json) {
				$('div#page-hits').html('');
				$('div#page-numbers').html('');
				var expansions = json.join(' ');
				var expandedRequest = request+' '+expansions;
				console.log('expansions: '+expansions);
				console.log('expanded request: '+expandedRequest);
				$('input#request').val(expandedRequest);
				
				$.ajax({
					url: 'service-get-hits.php',
					dataType: 'json',
					method: 'post',
					data: { query: expandedRequest },
					success: function(hits) {
						hitsForQuery = hits;
						currentPage = 1;
						$('div#links-to-pages').css('display','block');
						var nbHits = hits.length;
						if (nbHits>1)
							$('div#nb-hits span#plural').css('display','inline');
						nbPages = Math.ceil(nbHits / hitsPerPage);
						console.log('nbHits = '+nbHits);
						console.log('hitsPerPage = '+hitsPerPage);
						console.log('nbPages = '+nbPages);
						for (var ip=0; ip<nbPages; ip++) {
							var pageLink = '<input class="page-number"' +
								'type="button" '+
								'name="'+'page-number'+(ip+1)+'" '+
								'value="'+(ip+1)+'"/>'
							$('div#page-numbers').append(pageLink);
							if (ip != nbPages-1)
								$('div#page-numbers').append('&nbsp;&nbsp;');
						}
						$('div#nb-hits span#nb-hits').text(nbHits);
						$('div#hits').css('display','block');
						console.log('hits:'+JSON.stringify(hits));
						showPageResults(1);
					}
				});
			}
		});
	});
	
	/*$('button#next-page, button#previous-page').on('click',function(){
		var page = $(this).val();
		showPageResults(page);
	});
	$(document).on('click','input.page-number',function(){
		var page = $(this).val();
		showPageResults(page);
	});*/
	
	
	function showPageResults(page) {
	    var index = (page-1)*hitsPerPage;
	    var hitsDisplayed = hitsForQuery.slice(index,index+hitsPerPage);
	    console.log('display hits from '+index+' to '+(index+hitsPerPage));
	    $('div#hits div#page-hits').html('');

		for (var ih=0; ih<hitsDisplayed.length; ih++) {
			var title = $('<div class="hit-disp title">'+
					'<a class="hit-title" href="'+
					hitsDisplayed[ih].url+'">'+
					hitsDisplayed[ih].title+'</a></div>');
			var url = $('<div class="hit-disp">'+
					'<a class="hit-url" href="'+
					hitsDisplayed[ih].url+'">'+
					hitsDisplayed[ih].url+'</a></div>');
			var snippet = $('<div class="hit-disp">'+
					'<span class="hit-snippet">'+
					hitsDisplayed[ih].snippet+'</span></div>');
			var hitDiv = $('<div></div>');
			hitDiv.append(title).append(url).append(snippet);
			$('div#hits div#page-hits').append(hitDiv).append('<br>');
		}
		
		var previousPage = null;
		if (page==1) {
			previousPage = page;
			$('button#previous-page').prop('disabled',true);
		} else {
			previousPage = page-1;
			$('button#previous-page').prop('disabled',false);
		}
		$('button#previous-page').val(previousPage);
		var nextPage = null;
		if (page < nbPages) {
			nextPage = page+1;
			$('button#next-page').prop('disabled',false);
		} else {
			nextPage = page;
			$('button#next-page').prop('disabled',true);
		}
		$('button#next-page').val(nextPage);
		
		$('input.page-number').removeClass('current-page');
		$('input.page-number[name="page-number'+page+'"]').addClass('current-page');
	}
	
});