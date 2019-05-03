class SearchControllerMock extends SearchController {
	
	constructor(config, _mockResp) {
		super(config);
		this.mockResp = _mockResp;
	} 

	invokeSearchService(jsonRequestData) { 
		if (this.mockResp != null && this.mockResp.errorMessage == null) {
			this.successCallback(this.mockResp);
		} else {
			this.failureCallback(this.mockResp);
		}
	}
}
