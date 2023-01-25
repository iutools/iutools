/*
 * Base class for all Controllers in iutools
 */

class IUToolsController extends WidgetController {

    static feedbackLinkWasSet = false;

    constructor(config) {
        super(config);
        this.recentlyLogged = [];
        this.setupFeedbackLink();
    }

    error(err) {
        var tracer = Debug.getTraceLogger("IUController.error");
        var divError = this.elementForProp('divError');

        var errMess;
        if (typeof err === 'string') {
            tracer.trace("err is a string")
            errMess = err;
        } else {
            tracer.trace("err is a response object")
            errMess = err.errorMessage;
        }
        tracer.trace("errMess="+errMess);

        if (typeof errMess !== 'undefined') {
            if (errMess.includes("xception")) {
                var errDetails = errMess;
                errDetails = errDetails.replaceAll("\\n", "<br/>\n");
                errDetails = errDetails.replaceAll("\\t", "&nbsp;&nbsp;&nbsp;")
                var errMess = "The server encountered a possibly intermittent error. You MIGHT be able to resolve it by reloading the page and trying again.";
            if (Debug.debugModeIsOn()) {
                errMess += "<br/>\n"+errDetails;
            }
        }
            divError.html(errMess);
            divError.show();
            this.scrollIntoView(divError)
        }
    }

    augmentActionData(actionData, serverResp) {
        var tracer = Debug.getTraceLogger("IUtoolsController.augmentActionData");
        actionData = this.asJsonObject(actionData);
        tracer.trace("actionData="+actionData+", serverResp="+serverResp);
        actionData._taskID = serverResp.taskID;
        return actionData
    }

    userActionStart(actionName, actionURL, actionData,
        cbkActionSuccess, cbkActionFailure) {

        var tracer = Debug.getTraceLogger('UIToolsController.userActionStart');
        tracer.trace(
            "actionName="+actionName+", actionURL="+actionURL+
            ", actionData="+this.asJsonString(actionData));
        // tracer.trace(
        //     "\ncbkActionSuccess="+cbkActionSuccess+
        //     "\ncbkActionFailure="+cbkActionFailure);

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

    userActionEnd(actionName, finalServerResp) {
        var tracer = Debug.getTraceLogger("IUToolsController.userActionEnd");
        tracer.trace("Upon entry, actionName="+actionName+", finalServerResp="+this.asJsonString(finalServerResp));
        finalServerResp = this.asJsonObject(finalServerResp)
        var logData = {
            taskElapsedMsecs: finalServerResp.taskElapsedMsecs
        };
        var logData = this.augmentActionData(logData, finalServerResp)
        var cbkDoNothing = function() {}
        // Just log the end of the user action on the server and do nothing with
        // the server's response to that log request.
        this.logOnServer(actionName, logData, "END",
            cbkDoNothing, cbkDoNothing());
    }

    logOnServer(actionName, taskData, phase, cbkLogSuccess, cbkLogFailure) {
        var tracer = Debug.getTraceLogger("UIToolsController.logOnServer");
        tracer.trace(
            "actionName="+actionName+", phase="+phase+
            // ", cbkLogSuccess="+cbkLogSuccess+", cbkLogFailure="+cbkLogFailure+
            ", taskData="+JSON.stringify(taskData));
        var dataObj = this.asJsonObject(taskData);
        var data = {
            _action: actionName,
            phase: phase,
            taskData: dataObj,
            _taskID: dataObj._taskID
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

    setupFeedbackLink() {
        if (!IUToolsController.feedbackLinkWasSet) {
            var sendTo = this.feedbackSentTo();
            var link = $("#a_feedback_link");
            if (sendTo != null) {
                link.attr("href", sendTo);
            } else {
                link.attr("onclick", "alert('Unable to send feedback (no recipient addresses configured)')");
            }
        }
        IUToolsController.feedbackLinkWasSet = true;
    }

    feedbackSentTo() {
        var mailtoUrl = null;

        if (typeof iutoolsConfig !== 'undefined' && iutoolsConfig != null && iutoolsConfig.feedbackEmails != null && iutoolsConfig.feedbackEmails.length > 0) {
            mailtoUrl = "mailto:";
            for (var ii=0; ii < iutoolsConfig.feedbackEmails.length; ii++) {
                if (ii > 0) {
                    mailtoUrl += ";";
                }
                mailtoUrl += iutoolsConfig.feedbackEmails[ii];
            }
            mailtoUrl += "?subject=Inuktitut Tools Feedback";
        }
        return mailtoUrl;
    }
}