/*
 * Base class for all Controllers in iutools
 */

class IUToolsController extends WidgetController {

    constructor(config) {
        super(config);
        this.recentlyLogged = [];
    }

    error(err) {
        var tracer = Debug.getTraceLogger("IUController.error");
        var divError = this.elementForProp('divError');
        divError.html(err);
        divError.show();
        this.scrollIntoView(divError)
    }

	logThenInvokeService(actionName, actionURL, actionData,
        cbkActionSuccess, cbkActionFailure) {
        var tracer = Debug.getTraceLogger("UIToolsController.logThenInvokeService");
        tracer.trace("actionName="+actionName+", actionURL="+actionURL+", actionData="+JSON.stringify(actionData));
        var actionDataObj = null;
        var actionDatatStr = null;
        if (typeof actionData === 'string' || actionData instanceof String) {
            actionDataObj = JSON.parse(actionData);
            actionDatatStr = actionData;
        } else {
            actionDataObj = actionData;
            actionDatatStr = JSON.stringify(actionData);
        }

	    var logData = {
	        action: actionName,
            phase: "START",
            taskData: actionDataObj
        }
	    var jsonLogData = JSON.stringify(logData);

	    var controller = this;
	    var cbkLogStartSuccess = function(resp) {
	        controller.invokeService(actionDatatStr,
                cbkActionSuccess, cbkActionFailure, actionURL)
        }
        var cbkLogStartFailure = function(resp) {
	        cbkActionFailure.call(controller, actionData);
        }

		this.invokeService(jsonLogData, cbkLogStartSuccess, cbkLogStartFailure,
            'srv2/log_action');
    }

    logOnServer(action, taskData) {
        var tracer = Debug.getTraceLogger("UIToolsController.logOnServer");
        tracer.trace("action="+action+", taskData="+JSON.stringify(taskData));
        var dataObj = null;
        var dataStr = null;
        if (typeof taskData === 'string' || taskData instanceof String) {
            dataObj = JSON.parse(taskData);
            dataStr = taskData;
        } else {
            dataObj = taskData;
            dataStr = JSON.stringify(taskData);
        }
        var data = {
            action: action,
            taskData: dataObj,
            taskID: dataObj.taskID
        }
        
        this.invokeLogService(data,
            this.logSuccessCallback, this.logFailureCallback)
    }

    logSuccessCallback(resp) {
        var tracer = Debug.getTraceLogger("UIToolsController.logSuccessCallback");
		if (resp.errorMessage != null) {
			this.logFailureCallback(resp);
		} else {
		    // Nothing to do if the log service call succeeded
            tracer.trace("invoked with resp="+JSON.stringify(resp));
        }
	}

    logFailureCallback(resp) {
        var tracer = Debug.getTraceLogger("UIToolsController.logFailureCallback");
        tracer.trace("invoked with resp="+JSON.stringify(resp));
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage =
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}
		this.error(resp.errorMessage);
	}


    invokeLogService(data, cbkSuccess, cbkFailure) {
        var tracer = Debug.getTraceLogger("IUToolsController.invokeLogService");
        var dataJson = JSON.stringify(data);
        var controller = this;
        var fctSuccess =
                function(resp) {
                    cbkSuccess.call(controller, resp);
                };
        var fctFailure =
                function(resp) {
                    cbkFailure.call(controller, resp);
                };
        tracer.trace("Invoking log_action with dataJson="+dataJson);
        $.ajax({
            method: 'POST',
            url: 'srv2/log_action',
            data: dataJson,
            dataType: 'json',
            async: false,
            success: fctSuccess,
            error: fctFailure
        });
        return;
    }
}