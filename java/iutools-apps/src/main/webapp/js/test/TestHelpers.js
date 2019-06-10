class TestHelpers {
	
	constructor() {
		this.mockResponsesAllServices = {};
	}
	

	typeText(fieldID, text) {
		$("#"+fieldID).val(text);
	}
	
	clickOn(buttonID) {
		var promise = $("#"+buttonID).click();
//		console.log("-- TestHelpers.clickOn: buttonID="+buttonID+", promise="+promise)
//		new RunWhen().sleep(2*1000);
		
		return;
	}
	
	pressEnter(eltID) {
		var press = jQuery.Event("keypress");
		press.ctrlKey = false;
		press.which = 13;
		$("#"+eltID).trigger(press);
	}
	
	attachMockAjaxResponse(controller, mockResp, 
			serviceInvocationName, successCbkName, failureCbkName) {
		
		var responsesAllServices = this.mockResponsesAllServices;
		var serviceResponses = responsesAllServices[serviceInvocationName];
		if (serviceResponses == null) {
			responsesAllServices[serviceInvocationName] = [];
			serviceResponses = responsesAllServices[serviceInvocationName];
		}
		serviceResponses.push(mockResp);
		
		
		var mockInvokeService = 
			function() {
				console.log("-- TestHelpers.mockInvokeService: invoke successCallback with mock response, this="+JSON.stringify(this));
				var mockResp = responsesAllServices[serviceInvocationName].shift();
				if (mockResp == null) throw "Ran out of mock responses for service invocation method: "+serviceInvocationName;
				if (mockResp != null && mockResp.errorMessage == null) {
					controller[successCbkName](mockResp);
				} else {
					controller[failureCbkName](mockResp);
				}
				
				
			}
		controller[serviceInvocationName] = mockInvokeService;
		return;
	}
	
	assertStringEquals(assert, message, gotText, expText, ignoreSpaces) {
		if (ignoreSpaces != null && ignoreSpaces) {
			gotText = gotText.replace(/[\t\n\s]+/g, " "); 
			expText = expText.replace(/[\t\n\s]+/g, " "); 
		}
		if (message != null) message += "\nThe two strings differened";
		assert.equal(gotText, expText, message);
	}
	
	consoleLogTime(who, message) {
		var currentdate = new Date();
		var datetime = currentdate.getDay() + "/"+currentdate.getMonth() 
		+ "/" + currentdate.getFullYear() + " @ " 
		+ currentdate.getHours() + ":" 
		+ currentdate.getMinutes() + ":" + currentdate.getSeconds();
	}
}