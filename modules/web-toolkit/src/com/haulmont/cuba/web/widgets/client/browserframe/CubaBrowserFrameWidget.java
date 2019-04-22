/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.cuba.web.widgets.client.browserframe;

import com.google.gwt.dom.client.Document;
import com.vaadin.client.ui.VBrowserFrame;

public class CubaBrowserFrameWidget extends VBrowserFrame {

    protected void setAttribute(String name, String value, String connectorId) {
        if (value == null) {
            if (iframe != null && iframe.hasAttribute(name)) {
                iframe.removeAttribute(name);
            }
        } else {
            if ("srcdoc".equals(name) && iframe == null) {
                createIFrame(connectorId);
            }
            iframe.setAttribute(name, value);
        }
    }

    protected void createIFrame(String connectorId) {
        if (altElement != null) {
            getElement().removeChild(altElement);
            altElement = null;
        }

        iframe = Document.get().createIFrameElement();
        iframe.setFrameBorder(0);
        iframe.setAttribute("width", "100%");
        iframe.setAttribute("height", "100%");
        iframe.setAttribute("allowTransparency", "true");

        setName(connectorId);

        getElement().appendChild(iframe);
    }
}
