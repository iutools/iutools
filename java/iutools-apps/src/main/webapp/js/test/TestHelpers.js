//function TestHelpers() {
//	
//}
//
//TestHelpers.prototype.typeText = function(fieldID, text) {
//	$("#"+fieldID).val(text);
//}
//
//TestHelpers.prototype.clickOn = function(buttonID) {
//	$("#"+buttonID).click();
//}
//
//TestHelpers.prototype.attachMockAjaxResponse = function
//(controller, mockResp, serviceInvocationName, successCbkName, failureCbkName) {
//	
//	
//	var mockInvokeService = 
//		function() {
//			console.log("-- TestHelpers.mockInvokeService: invoke successCallback with mock response");
//			if (mockResp != null && mockResp.errorMessage == null) {
//				controller[successCbkName](mockResp);
//			} else {
//				controller[failureCbkName](mockResp);
//			}
//			
//			
//		}
//	controller[serviceInvocationName] = mockInvokeService;
//	return;
//}
//
//
//TestHelpers.prototype.attachMockAjaxResponse_DUMMY = function
//	(controller, mockResp, serviceInvocationName, successCbkName, failureCbkName) {
//	var mockInvokeService = 
//		function() {
////			console.log("-- mockInvokeService (attached through helper method): invoke successCallback with mockResp="+JSON.stringify(mockResp)+"\n   txtQuery="+this.txtQuery);
//			controller[successCbkName](mockResp);
//			
//		}
//	controller[serviceInvocationName] = mockInvokeService;
//	return;
//}


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