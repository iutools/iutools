function TestHelpers() {
	
}

TestHelpers.prototype.typeText = function(fieldID, text) {
	$("#"+fieldID).val(text);
}

TestHelpers.prototype.clickOn = function(buttonID) {
	console.log("-- TestHelpers.clickOn: buttonID="+buttonID);
	console.log("-- TestHelpers.clickOn: button object="+$("#"+buttonID));
	console.log("-- TestHelpers.clickOn: button object attrs="+JSON.stringify($("#"+buttonID)));
	$("#"+buttonID).click();
}

//
// TODO: Why does this not work?
//
//TestHelpers.prototype.attachMockResponse = function(controller, mockResp, handlerName) {
//    console.log("-- TestHelpers.attachMockResponse: invoked");
//    var handlerMain = "on"+handlerName;
//    var className = controller.constructor.name;
//    var handlerSuccess = className+"."+handlerMain+"Success";
//    var handlerFailure = className+"."+handlerMain+"Failure";
//    console.log("-- TestHelpers.attachMockResponse: className="+className+", handlerSuccess="+handlerSuccess+", handlerFailure="+handlerFailure);
//
//    if (mockResp != null) {
//        controller[handlerMain] =
//            function() {
//                console.log("-- TestHelpers.attachMockResponse."+handlerMain+": invoked");
//                console.log("-- TestHelpers.attachMockResponse."+handlerMain+": Properties of object are: "+Object.keys(this));
//
//                if (mockResp.errorMessage == null) {
//                    console.log("-- TestHelpers.attachMockResponse."+handlerMain+":  invoking success handler: "+handlerSuccess);
////                    controller[handlerSuccess](mockResp);
////                    window[handlerSuccess](controller, mockResp);
//                    controller["onSearchSuccess"](mockResp);
//                } else {
////                    controller[handlerFailure](mockResp);
////                    window[handlerFailure](controller, mockResp);
//                }
//            };
//    }
//}

