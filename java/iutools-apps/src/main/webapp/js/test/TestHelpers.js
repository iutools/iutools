class TestHelpers {
	

	typeText(fieldID, text) {
		$("#"+fieldID).val(text);
	}
	
	clickOn(buttonID) {
		$("#"+buttonID).click();
	}
	
	pressEnter(eltID) {
		var press = jQuery.Event("keypress");
		press.ctrlKey = false;
		press.which = 13;
		$("#"+eltID).trigger(press);
	}
	
	attachMockAjaxResponse(controller, mockResp, 
			serviceInvocationName, successCbkName, failureCbkName) {
		
		
		var mockInvokeService = 
			function() {
				console.log("-- TestHelpers.mockInvokeService: invoke successCallback with mock response");
				if (mockResp != null && mockResp.errorMessage == null) {
					controller[successCbkName](mockResp);
				} else {
					controller[failureCbkName](mockResp);
				}
				
				
			}
		controller[serviceInvocationName] = mockInvokeService;
		return;
	}
}