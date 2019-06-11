class TestHelpers {
	
	constructor() {
//		this.mockResponsesAllServices = {};
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

	
	getMockAjaxResponses(controller) {
		if (!controller.hasOwnProperty('mockResponsesAllServices')) {
			controller.mockResponsesAllServices = {};
		}		
		return controller.mockResponsesAllServices;
	}
	
	getMockAjaxResponsesFor(controller, serviceInvocationName) {
		var mockResponsesAllServices = this.getMockAjaxResponses(controller);
		var mockResponses = mockResponsesAllServices[serviceInvocationName];
		if (mockResponses == null) {
			mockResponses = [] 
			mockResponsesAllServices[serviceInvocationName] = mockResponses;
		}
		return mockResponses;
	}
	
	clearMockAjaxResponsesFor(controller, serviceInvocationName) {
		var mockResponses = this.getMockAjaxResponses(controller);
		mockResponses[serviceInvocationName] = [];
		return;
	}
	
	attachMockAjaxResponse(controller, mockResp, 
			serviceInvocationName, successCbkName, failureCbkName) {
		
//		ensureControlerHasMockResponsesRegistry();
		console.log("-- TestHelpers.attachMockAjaxResponse: serviceInvocationName="+serviceInvocationName+", successCbkName="+successCbkName+", failureCbkName="+failureCbkName);
		console.log("-- TestHelpers.attachMockAjaxResponse: mockResp="+JSON.stringify(mockResp));
		console.log("-- TestHelpers.attachMockAjaxResponse: upon entry, controller.mockResponsesAllServices="+JSON.stringify(controller.mockResponsesAllServices));
//		var serviceResponses = controller.mockResponsesAllServices[serviceInvocationName];
//		if (serviceResponses == null) {
//			controller.mockResponsesAllServices[serviceInvocationName] = [];
//			serviceResponses = controller.mockResponsesAllServices[serviceInvocationName];
//		}
		var serviceResponses = this.getMockAjaxResponsesFor(controller, serviceInvocationName);
		serviceResponses.push([mockResp, successCbkName, failureCbkName]);
		
		
		var mockInvokeService = 
			function() {
				console.log("-- TestHelpers.mockInvokeService: invoke successCallback with mock response, this="+JSON.stringify(this));
				var mockRespInfo = controller.mockResponsesAllServices[serviceInvocationName].shift();
				if (mockRespInfo == null) throw "Ran out of mock responses for service invocation method: "+serviceInvocationName;
				
				var mockResp = mockRespInfo[0];
				var successCbkName = mockRespInfo[1];
				var failureCbkName = mockRespInfo[2];
				if (mockResp != null && mockResp.errorMessage == null) {
					controller[successCbkName](mockResp);
				} else {
					controller[failureCbkName](mockResp);
				}
				
				
			}
		controller[serviceInvocationName] = mockInvokeService;
		
		console.log("-- TestHelpers.attachMockAjaxResponse: upon EXIT, controller.mockResponsesAllServices="+JSON.stringify(controller.mockResponsesAllServices));
		console.log("-- TestHelpers.attachMockAjaxResponse: upon EXIT, controller.__proto__="+JSON.stringify(controller.__proto__));
		
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