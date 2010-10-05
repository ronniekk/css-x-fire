(function(){

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
        if (propValue != prevValue) {
            // if value has changed, send change to the IDE
            cssxfire.send(rule.selectorText, propName, propValue, false);
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
        cssxfire.send(rule.selectorText, propName, prevValue, true);
    }
};

var cssxfire = {

    /**
     * Sends a change to the local web server
     * @param selector the selector name
     * @param property the property name
     * @param value the value
     * @param deleted if the property was deleted or not
     */
    send: function(selector, property, value, deleted) {
        var querystring = "http://localhost:6776/?selector=" + this.encode(selector) + "&property=" + this.encode(property) + "&value=" + this.encode(value) + "&deleted=" + deleted;
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

// Initialization
try {
    Firebug.CSSModule.addListener(cssPropertyListener);
    // Place a marker in the DOM so that we don't inject more than one Firebug listener
    var e = document.createElement("script");
    e.setAttribute("type","text/javascript");
    e.setAttribute("id","css-x-fire");
    document.body.appendChild(e);
    alert("CSS-X-Fire initialized!");
} catch (err) {
    alert("Firebug Lite not active");
}

})();