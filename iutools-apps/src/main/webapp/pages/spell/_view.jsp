    <p>Enter some Inuktut text:</p>
    <textarea id="txt-to-check" name="txt-to-check" rows=20 cols=80></textarea>
    <br/>
    <!--
    <input type="checkbox" id="chk-include-partials" name="chk-include-partials">
    	Include partial corrections (slower)
    <br/>
    -->
    <div>
        <label for="sel-check-level">Check level</label>
        <select name="sel-check-level" id="sel-check-level">
            <option value="1" selected>1</option>
            <option value="2">2</option>
            <option value="3">3</option>
        </select>
        <div id="div-info" class="div-info inline">
            <a href="help.jsp?topic=spell-checker" target="#iutools_help"></a>
         </div>
     </div>

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

<%--    <!----%>
<%--    Floating div where we display the dialog for choosing a correction--%>
<%--    Note: This div may disappear after it has been 'mounted' by Winbox--%>
<%--    -->--%>
<%--    <div class="div-floating-dlg" id="div-choose-correction-dlg">--%>
<%--        <div class="div-floating-dlg-titlebar" id="div-choose-correction-title"></div>--%>
<%--        <div class="div-floating-dlg-contents">--%>
<%--            <div id="div-choose-correction-message"></div>--%>
<%--            <div id="div-choose-correction-main">--%>
<%--                <input id="txt-finalized-correction" type="text" value="" />--%>
<%--                <button id="btn-choose-correction-apply">Apply</button>--%>
<%--                <button id="btn-choose-correction-cancel">Cancel</button>--%>
<%--                <div id="div-choose-correction-suggestions"></div>--%>
<%--                <div id="div-choose-correction-error"></div>--%>
<%--            </div>--%>
<%--        </div>--%>
<%--    </div>--%>
