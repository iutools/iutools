class WidgetController {   

	constructor(_config) {
		this.config = _config;
		this.busy = false;
		this.isReady = false;
		
		this.attachHtmlElements();
		{
			var controller = this;
			var attachFct = function() {
				controller.attachHtmlElements()
			}
			new RunWhen().domReady(attachFct, 10 * 1000, 1000);
		}
	}
			
	attachHtmlElements() {
		// This method should be overridden by subclasses if 
		// you want to actually attach elements to the controller.
	}
	
	elementForProp(property) {
		if (property == null) {
			throw new Error("Config property name cannot be null");
		}
		var eltID = this.config[property];
		if (eltID == null) {
			throw new Error("Controller has no config property called '"+property+"'");
		}
		
		var elt = $('#'+eltID);
		if (elt == null || elt.length == 0 || elt.val() == null) {
			elt = null;
		}
		if (elt == null) {
			throw new Error("Element with ID "+eltID+" was not defined. Maybe you need to execute this method after the DOM was loaded?");
		}
		return elt;
	}
	
	
	activeElement() {
		var elt = $(':focus').context.activeElement();
		return elt;
	}
	
	setEventHandler(propName, evtName, handler) {
		var elt = this.elementForProp(propName);
		var controller = this;
		var fct_handler =
				function() {
					handler.call(controller);
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
					console.log("-- onReturnKey.keypressHandler: event="+JSON.stringify(event));
					var keycode = (event.keyCode ? event.keyCode : event.which);
					if(keycode == '13'){
						method.call(controller);
					}
				};		
		element.keypress(keypressHandler);

		return;
	}	
	
	showSpinningWheel(divMessProp, message) {
		if (message == null) message = "Processing request";
		var divMessage = this.elementForProp(divMessProp);
		divMessage.html("<img src=\"ajax-loader.gif\">"+message+" ...");
		divMessage.show();
	}

	hideSpinningWheel(divMessProp) {
		var divMessage = this.elementForProp(divMessProp);
		divMessage.empty();
		divMessage.hide();
	}
}
