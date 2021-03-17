/*
 * Base class for all Controllers in iutools
 */

class IUToolsController extends WidgetController {

    constructor(config) {
        super(config);
    }

    error(err) {
        this.elementForProp('divError').html(err);
        this.elementForProp('divError').show();
    }

    logOnServer(action, taskData) {
        if (typeof taskData === 'string' || taskData instanceof String) {
            taskData = JSON.parse(taskData);
        }
        var data = {
            action: action,
            taskData: taskData,
            taskID: taskData.taskID
        }
        this.invokeLogService(data,
            this.logSuccessCallback, this.logFailureCallback)
    }

    logSuccessCallback(resp) {
		if (resp.errorMessage != null) {
			this.logFailureCallback(resp);
		} else {
		    // Nothing to do if the log service call succeeded
        }
	}

    logFailureCallback(resp) {
		if (! resp.hasOwnProperty("errorMessage")) {
			// Error condition comes from tomcat itself, not from our servlet
			resp.errorMessage =
				"Server generated a "+resp.status+" error:\n\n" +
				resp.responseText;
		}
		this.error(resp.errorMessage);
	}


    invokeLogService(data, cbkSuccess, cbkFailure) {
        var dataJson = JSON.stringify(data);
        $.ajax({
            method: 'POST',
            url: 'srv2/log_action',
            data: dataJson,
            dataType: 'json',
            async: false,
            success: cbkSuccess,
            error: cbkFailure
        });
        return;
    }
}