class WidgetController {   

	constructor(_config) {
		this.config = _config;
        this.validateConfig();
		this.busy = false;
		this.isReady = false;
		this.recentEvents = [];
		this.recentEventTimes = {};

		// this.attachHtmlElements();
		{
			var controller = this;
			var attachFct = function() {
				controller.attachHtmlElements()
			}
			new RunWhen().domReady(attachFct, 10 * 1000, 1000);
		}
	}

    validateConfig() {
        // This method should be overridden by subclasses if
        // you want to validate the config received by the controller
    }

	attachHtmlElements() {
		// This method should be overridden by subclasses if 
		// you want to actually attach elements to the controller.
	}

    validateProps(propDefs) {
	    var tracer = Debug.getTraceLogger("WidgetController.validateProps");
	    tracer.trace("propDefs=\n"+jsonStringifySafe(propDefs,undefined,2));
        tracer.trace("this.config=\n"+jsonStringifySafe(this.config,undefined,2));

        for (var ii=0; ii > propDefs.length; ii++) {
            var aPropDef = propDefs[ii];
            tracer.trace("ii=" + ii + ", aPropDef=" + jsonStringifySafe(aPropDef));
            if (!Array.isArray(aPropDef)) {
                aPropDef = [aPropDef];
            }
            var propName = aPropDef[0];
            var expEltName = null;
            if (aPropDef.length > 1) {
                expEltName = aPropDef[1];
            }
            if (!this.config.hasOwnProperty(propName)) {
                throw new Error("Undefined config property '"+propName+"'");
            }
            if (expEltName) {
                try {
                    var elt = this.elementForProp(propName);
                } catch (error) {
                    throw new Error("No DOM element defined for property '"+propName+"'");
                }
                var gotEltName = elt.attr('name');
                if (gotEltName !== expEltName) {
                    throw new Error(
                        "Wrong DOM element type for property '"+propName+".\n"+
                        "Should have been '"+expEltName+"' but was '"+gotEltName
                    );
                }
            }
        }
    }

	elementForProp(property) {
	    var tracer = Debug.getTraceLogger("WidgetController.elementForProp");
	    tracer.trace("property="+property);
		if (property == null) {
			throw new Error("Config property name cannot be null");
		}
		var eltID = this.config[property];
		if (eltID == null) {
			throw new Error("Controller for widget '"+this.constructor.name+"' has no config property called '"+property+"'");
		}
		
		var elt = $('#'+eltID);
		if (elt == null || elt.length == 0 || elt.val() == null) {
			elt = null;
		}
		if (elt == null) {
			throw new Error("Element with ID "+eltID+" was not defined. Maybe you need to execute this method after the DOM was loaded?");
		}
        tracer.trace("for property="+property+", returning elt="+elt);
		return elt;
	}
	
	
	activeElement() {
		var elt = $(':focus').context.activeElement();
		return elt;
	}
	
	setEventHandler(propNameOrElt, evtName, handler) {
	    var tracer = Debug.getTraceLogger("WidgetController.setEventHandler")
        tracer.trace("propNameOrElt="+propNameOrElt+", evtName="+evtName+", handler="+handler);
		var elt = propNameOrElt;
		if (typeof(propNameOrElt) == "string") {
			elt = this.elementForProp(propNameOrElt);
		}

        tracer.trace("elt="+elt+"; type="+elt.constructor.name);
		var controller = this;
		var fct_handler =
				function(evt) {
					handler.call(controller, evt);
				};

		if (evtName == "click") {
			elt.off('click').on("click", fct_handler);
		}
	}	
	
	onReturnKey(id, method) {
		var element = this.elementForProp(id);
		var controller = this;
		
		var keypressHandler = 
			function(event) {
				// event.preventDefault();
				var keycode = (event.keyCode ? event.keyCode : event.which);
				if(keycode == '13'){
					method.call(controller);
				}
			};
		element.keypress(keypressHandler);

		return;
	}	
	
	showSpinningWheel(divMessLocator, message) {
		if (message == null) message = "Processing request";
		var divMessage = null;
		if (divMessLocator.search(/^(#|\.)/) >= 0) {
		    divMessage = $(divMessLocator);
        } else {
            divMessage = this.elementForProp(divMessLocator);
        }
		divMessage.html("<img src=\"ajax-loader.gif\">"+message+" ...");
		divMessage.show();
	}

	hideSpinningWheel(divMessLocator) {
        var divMessage = null;
        if (divMessLocator.search(/^(#|\.)/) >= 0) {
            divMessage = $(divMessLocator);
        } else {
            divMessage = this.elementForProp(divMessLocator);
        }
		divMessage.empty();
		divMessage.hide();
	}

    asJsonObject(jsonStringOrObj) {
        var jsonObj = null;
        if (typeof jsonStringOrObj === 'string' || jsonStringOrObj instanceof String) {
            jsonObj = JSON.parse(jsonStringOrObj);
        } else {
            jsonObj = jsonStringOrObj;
        }
        return jsonObj;
    }

    asJsonString(jsonStringOrObj) {
        var jsonStr = null;
        if (typeof jsonStringOrObj === 'string' || jsonStringOrObj instanceof String) {
            jsonStr = jsonStringOrObj;
        } else {
            jsonStr = jsonStringifySafe(jsonStringOrObj);
        }
        return jsonStr;
    }

	invokeWebService(url, jsonRequestData, _successCbk, _failureCbk) {
        jsonRequestData = this.asJsonString(jsonRequestData);
	    var tracer = Debug.getTraceLogger("WidgetController.invokeWebService");
	    tracer.trace("url="+url+", jsonRequestData="+jsonRequestData);
        tracer.trace(
            "\n_successCbk="+_successCbk+
            "\n_failureCbk="+_failureCbk);
		var controller = this;
		var fctSuccess = 
				function(resp) {
					_successCbk.call(controller, resp);
				};
		var fctFailure = 
				function(resp) {
					_failureCbk.call(controller, resp);
				};

		$.ajax({
			type: 'POST',
			url: url,
			data: jsonRequestData,
			dataType: 'json',
			async: true,
	        success: fctSuccess,
	        error: fctFailure
		});
	}

	error(err) {
		var errMess;
		if (typeof err === 'string' || err instanceof String) {
			errMess = err;
		} else {
			errMess = jsonStringifySafe(err, null, 2);
		}
		this.displayError(errMess);
	}

	// Override this method if you want a controller to dislay errors
	// in a different way.
	//
	displayError(errMess) {
		console.log(errMess);
	}

	// Sometimes, two "equivalent" events are generated for a single user action
	// For example, when user types Enter in a text field, it may end up
	// generating two keypress 13 events. Not sure why.
	//
	// To prevent this from happening, invoke isDuplicateEvent().
	// This method will check if the event is the same as another event that
	// was recently processed.
	//
	isDuplicateEvent(method, data, maxMsecs) {
		var isDup = false;
		var tracer = Debug.getTraceLogger("WidgetController.isDuplicateEvent");
		if (!maxMsecs) {
			maxMsecs = 1000;
		}
        var nowMsecs = new Date().getTime();
		var eventDescr = this.evenDescription(method, data);
		tracer.trace("nowMsecs="+nowMsecs+", eventDescr="+eventDescr+", this.recentEvents="+jsonStringifySafe(this.recentEvents));
		var ind = this.recentEvents.indexOf(eventDescr);
		var elapsed = -1;
		if (ind >= 0) {
			var lastIssued = this.recentEventTimes[eventDescr];
			elapsed = nowMsecs - lastIssued;
            tracer.trace("lastIssued="+lastIssued+", elapsed="+elapsed);
			if (elapsed <= maxMsecs) {
				isDup = true;
			}
		}
		tracer.trace("isDup="+isDup+", ind="+ind+", elapsed="+elapsed+", maxMsecs="+maxMsecs);
		this.recentEventTimes[eventDescr] = nowMsecs;
		this.recentEvents.push(eventDescr);
		if (this.recentEvents.length > 10) {
			this.recentEvents.pop();
		}

		return isDup;
	}

	evenDescription(method, data) {
		if (typeof data !== 'string' && ! (data instanceof String)) {
			data = jsonStringifySafe(data);
		}
		var descr = method+"("+data+")";
		return descr;
	}

	// Scrolls window so that jquery DOM element jqElt is visible
	scrollIntoView(jqElt) {
	    const bufferSpace = 200;
        const y = jqElt[0].getBoundingClientRect().top
            + window.pageYOffset
            - bufferSpace;
        window.scrollTo({top: y, behavior: 'smooth'});

    }
}
