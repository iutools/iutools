var SearchControllerMock = function(_config, _mockResp) {  
  SearchController.call(this, _config);
  this.mockResp = _mockResp;
};

SearchControllerMock.prototype = Object.create(SearchController.prototype);  

SearchControllerMock.prototype.constructor = SearchControllerMock;  

SearchControllerMock.prototype.onSearch = function(resp) {  
	if (this.mockResp != null && this.mockResp.errorMessage == null) {
		this.onSearchSuccess(this.mockResp);
	} else {
		this.onSearchFailure(this.mockResp);
	}
};
