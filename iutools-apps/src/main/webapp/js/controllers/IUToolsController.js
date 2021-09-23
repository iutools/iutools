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

    augmentActionData(actionData, serverResp) {
        var tracer = Debug.getTraceLogger("IUtoolsController.augmentActionData");
        actionData = this.asJsonObject(actionData);
        tracer.trace("actionData="+actionData+", serverResp="+serverResp);
        actionData.taskID = serverResp.taskID;
        actionData.startedAt = serverResp.startedAt;
        return actionData
    }

    userActionStart(actionName, actionURL, actionData,
        cbkActionSuccess, cbkActionFailure) {

        var tracer = Debug.getTraceLogger('UIToolsController.userActionStart');
        tracer.trace(
            "actionName="+actionName+", actionURL="+actionURL+
            ", actionData="+JSON.stringify(actionData));
        tracer.trace(
            "\ncbkActionSuccess="+cbkActionSuccess+
            "\ncbkActionFailure="+cbkActionFailure);

        var controller = this;

        // If the log service succeeded, proceed with the actual action
        var cbkLogSuccess = function(resp) {
            var tracer = Debug.getTraceLogger("IUToolsController.userActionStart.cbkLogSuccess");
            tracer.trace("invoked");
            tracer.trace(
                "resp="+JSON.stringify(resp)
                // + ", cbkActionSuccess="+cbkActionSuccess+", cbkActionFailure="+cbkActionFailure
                );
            actionData = this.augmentActionData(actionData, resp);
            this.invokeWebService(actionURL, actionData,
                cbkActionSuccess, cbkActionFailure);
        };

        // If the log service fails, abort the actual action
        var cbkLogFailure = function(resp) {
            cbkActionFailure.call(controller, resp);
        };

        this.logOnServer(actionName, actionData, "START",
            cbkLogSuccess, cbkLogFailure);
    }

    logOnServer(actionName, taskData, phase, cbkLogSuccess, cbkLogFailure) {
        var tracer = Debug.getTraceLogger("UIToolsController.logOnServer");
        tracer.trace(
            "actionName="+actionName+", phase="+phase+
            // ", cbkLogSuccess="+cbkLogSuccess+", cbkLogFailure="+cbkLogFailure+
            ", taskData="+JSON.stringify(taskData));
        var dataObj = this.asJsonObject(taskData);
        var data = {
            action: actionName,
            phase: phase,
            taskData: dataObj,
            taskID: dataObj.taskID
        }
        if (cbkLogSuccess == null) {
            cbkLogSuccess = this.logSuccessCallback;
        }
        if (cbkLogFailure == null) {
            cbkLogFailure = this.logFailureCallback;
        }
        this.invokeLogService(data, cbkLogSuccess, cbkLogFailure)
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
        // tracer.trace("cbkSuccess="+cbkSuccess);
        var dataJson = JSON.stringify(data);
        tracer.trace("dataJson="+dataJson);

        var controller = this;
        var fctSuccess =function(resp) {
            var tracer = Debug.getTraceLogger("IUToolsController.invokeLogService.fctSuccess");
            cbkSuccess.call(controller, resp);
        };
        var fctFailure =
                function(resp) {
                    cbkFailure.call(controller, resp);
                };
        $.ajax({
            method: 'POST',
            url: 'srv2/log_action',
            data: dataJson,
            dataType: 'json',
            async: true,
            success: fctSuccess,
            error: fctFailure
        });
        return;
    }
}