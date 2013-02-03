/*
 * Copyright 2012 Ronnie Kolehmainen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
define([
    "firebug/lib/lib",
],
function(FBL) {

// ********************************************************************************************* //
// Custom Module Implementation

Firebug.MyModule = FBL.extend(Firebug.Module,
{
    // * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * //
    // Initialization

    initialize: function(owner)
    {
        Firebug.Module.initialize.apply(this, arguments);
        Firebug.CSSModule.addListener(this);
    },

    shutdown: function()
    {
        Firebug.Module.shutdown.apply(this, arguments);
    },

    /**
     * Called from Firebug when a page is opened or refreshed, or when Firebug is activated
     * @param context the page context
     * @param state the state
     */
    initContext: function(context, state) {
        this.sendEvent("refresh");
    },

    /**
     * The registered listener function for property change. Receives callbacks from Firebug CSS editor
     * @param style
     * @param propName
     * @param propValue
     * @param propPriority
     * @param prevValue
     * @param prevPriority
     * @param rule
     * @param baseText
     */
    onCSSSetProperty: function(style, propName, propValue, propPriority, prevValue, prevPriority, rule, baseText) {
        if (propValue != prevValue || propPriority != prevPriority) {
            // if value has changed, send change to the IDE
            this.send(this.getMediaText(rule), this.getHref(rule), rule.selectorText, propName, propValue, propPriority, false);
        }
    },

    /**
     * The registered listener function for deletion of properties. Receives callbacks from Firebug CSS editor
     * @param style
     * @param propName
     * @param prevValue
     * @param prevPriority
     * @param rule
     * @param baseText
     */
    onCSSRemoveProperty: function(style, propName, prevValue, prevPriority, rule, baseText) {
        this.send(this.getMediaText(rule), this.getHref(rule), rule.selectorText, propName, prevValue, prevPriority, true);
    },

    /**
     * Extract the media query text.
     * <b>Note:</b> the text returned from Firebug might differ in whitespace from the original document.
     * @param rule the css rule
     * @return the media query text, or null if no media query specified
     */
    getMediaText: function(rule) {
        if (rule && rule.parentRule && rule.parentRule.media) {
            return rule.parentRule.media.mediaText;
        }
        return null;
    },

    /**
     * Get the filename for the rule that is displayed in Firebug.
     * Note that the filename might have been modified by FireSass.
     * @param rule the css rule
     * @return the href of the file containing the rule.
     */
    getHref: function(rule) {
        // Check if FireSass has done its work
        if (rule.sassDebugInfo && rule.sassDebugInfo["filename"]) {
            return rule.sassDebugInfo["filename"];
        }
        // Default Firebug info
        return rule.parentStyleSheet.href;
    },

    timerId: null,

    /**
     * Alerts a message, like the regular "alert" function
     * @param msg the text to alert
     */
    alert: function(msg) {
        Components.classes["@mozilla.org/embedcomp/prompt-service;1"].getService(Components.interfaces.nsIPromptService).alert(window, "CSS-X-Fire", msg);
    },

    /**
     * Sends a change to the local web server
     * @param media media query text (null means not specified)
     * @param href css file href (null means inline)
     * @param selector the selector name
     * @param property the property name
     * @param value the value
     * @param important the priority
     * @param deleted if the property was deleted or not
     */
    send: function(media, href, selector, property, value, important, deleted) {
        var querystring = "http://" + this.getIdeAddress() + "/?selector=" + this.encode(selector) + "&property="
                + this.encode(property) + "&value=" + this.encode(value) + "&important=" + (important ? "true" : "false")
                + "&deleted=" + deleted + "&href=" + this.encode(href || window.content.location.href)
                + "&media=" + this.encode(media || "");
        var httpRequest = new XMLHttpRequest();
        httpRequest.open("GET", querystring, true);
        // send event in 0.5 seconds from now
        this.sendDelayed(httpRequest);
    },

    /**
     * Simple DOS protection: schedules a send(null) on the httpRequest in 500 ms and aborts any already scheduled.
     * @param httpRequest the XMLHttpRequest
     */
    sendDelayed: function(httpRequest) {
        window.clearTimeout(this.timerId);
        this.timerId = window.setTimeout( function() {
            httpRequest.send(null);
        }, 500);
    },

    /**
     * Sends a signal to the local web server
     * @param eventName the name of event
     */
    sendEvent: function(eventName) {
        var querystring = "http://" + this.getIdeAddress() + "/?event=" + eventName;
        var httpRequest = new XMLHttpRequest();
        httpRequest.open("GET", querystring, true);
        httpRequest.send(null);
    },

    /**
     * Get the address of the CSS-X-Fire server (IDE).
     */
    getIdeAddress: function() {
        var prefManager = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefBranch);
        return prefManager.getCharPref("extensions.cssxfire@cssxfire.host") + ":" + prefManager.getIntPref("extensions.cssxfire@cssxfire.port");
    },

    /**
     * URLEncoder
     * @param str the string to encode
     * @return the encoded string
     */
    encode: function(str) {
        return encodeURIComponent(str); // .replace(/#/g, '%23');
    }

});

// ********************************************************************************************* //
// Registration

Firebug.registerModule(Firebug.MyModule);

return Firebug.MyModule;

// ********************************************************************************************* //
});

