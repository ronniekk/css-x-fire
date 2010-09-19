/*
 * Copyright 2010 Ronnie Kolehmainen
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

var cssPropertyListener = {

    /**
     * The registered listener function. Receives callbacks from Firebug CSS editor
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
        if (propValue != prevValue) {
            // if value has changed, send change to the IDE
            cssxfire.send(rule.selectorText, propName, propValue);
        }
    }
};

var cssxfire = {

    /**
     * Initialization
     */
    onLoad: function() {
        try {
            Firebug.CSSModule.addListener(cssPropertyListener);
            // this.alert("CSS-X-Fire initialized");
        } catch (err) {
            this.alert(err.toString() + "\n\nMake sure you have Firebug installed properly: http://www.getfirebug.com/");
        }
    },

    /**
     * Alerts a message, like the regular "alert" function
     * @param msg the text to alert
     */
    alert: function(msg) {
        Components.classes["@mozilla.org/embedcomp/prompt-service;1"].getService(Components.interfaces.nsIPromptService).alert(window, "CSS-X-Fire", msg);
    },

    /**
     * Sends a change to the local web server
     * @param selector the selector name
     * @param property the property name
     * @param value the value
     */
    send: function(selector, property, value) {
        var querystring = "http://localhost:6776/?selector=" + this.encode(selector) + "&property=" + this.encode(property) + "&value=" + this.encode(value);
        var httpRequest = new XMLHttpRequest();
        httpRequest.open("GET", querystring, true);
        httpRequest.send(null);
    },

    /**
     * URLEncoder
     * @param str the string to encode
     * @return the encoded string
     */
    encode: function(str) {
        return encodeURIComponent(str); // .replace(/#/g, '%23');
    }
};


// load plugin
window.addEventListener("load", function(e) { cssxfire.onLoad(e); }, false);

