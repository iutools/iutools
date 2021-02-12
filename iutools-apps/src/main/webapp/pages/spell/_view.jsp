    <h1>Inuktut Spell Checker</h1>

    <p>Enter some Inuktut text:</p>
    <textarea id="txt-to-check" name="txt-to-check" rows=20 cols=80></textarea>
    <br/>
    <input type="checkbox" id="chk-include-partials" name="chk-include-partials">
    	Include partial corrections (slower)
    <br/>
    <button id="btn-spell" type="button" name="btn-spell" value="Spell">Spell Check</button>
    <button id="btn-cancel-spell" type="button" name="btn-cancel-spell" value="Stop" style="display: none;">Stop</button>
    <p></p>
    <div id="div-message" class="div-message"></div>
    <div id="div-error" class="div-error"></div>
    <div id="div-checked" class="div-checked">
    	<div id="title-and-copy">
    		<h2>Spell-checked content</h2>
        </div>
    	<div id="div-results" class="div-results"></div>
        <button id="btn-copy" type="button" name="btn-copy">Copy to clipboard</button>
    </div>

    <div style="min-height:6em;"></div>
