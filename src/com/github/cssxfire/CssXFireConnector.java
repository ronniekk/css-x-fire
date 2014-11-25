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

package com.github.cssxfire;

import com.github.cssxfire.webserver.SimpleWebServer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ApplicationNamesInfo;
import com.intellij.openapi.components.*;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.Messages;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
@State(
        name = "CssXFireConnector",
        storages = {@Storage(id = "CSS-X-Fire", file = StoragePathMacros.APP_CONFIG + "/CSS-X-Fire.xml")}
)
public class CssXFireConnector implements ApplicationComponent, PersistentStateComponent<AppMeta> {
    private static final Logger LOG = Logger.getInstance(CssXFireConnector.class.getName());

    private AppMeta appMeta = new AppMeta();
    private SimpleWebServer webServer;
    private Collection<IncomingChangesComponent> incomingChangesComponents = new ArrayList<IncomingChangesComponent>();
    private boolean initialized = false;

    public static CssXFireConnector getInstance() {
        return ApplicationManager.getApplication().getComponent(CssXFireConnector.class);
    }

    public boolean isInitialized() {
        return initialized;
    }

    public CssXFireConnector() {
    }

    public void initComponent() {
        // start web server
        try {
            webServer = new SimpleWebServer();
            new Thread(webServer).start();
            initialized = true;
        } catch (BindException e) {
            LOG.error("Unable to start web server - address in use: ", e);
            Messages.showErrorDialog("Unable to start SimpleWebServer on localhost:6776 - address is in use.\n\nCSS-X-Fire will be disabled until restart of " + ApplicationNamesInfo.getInstance().getFullProductName(), "CSS-X-Fire error");
        } catch (IOException e) {
            LOG.error("Unable to start web server: ", e);
            Messages.showErrorDialog("Unable to start SimpleWebServer on localhost:6776 - " + e.getMessage() + "\n\nCSS-X-Fire will be disabled until restart of " + ApplicationNamesInfo.getInstance().getFullProductName(), "CSS-X-Fire error");
        }
    }

    public void disposeComponent() {
        // tear down web server
        if (webServer != null) {
            try {
                webServer.stop();
                LOG.debug("Web server stopped");
            } catch (IOException e) {
                LOG.error(e);
            }
        }
        webServer = null;
    }

    public AppMeta getState() {
        return appMeta;
    }

    public void loadState(AppMeta appMeta) {
        this.appMeta = appMeta;
    }

    @NotNull
    public String getComponentName() {
        return getClass().getSimpleName();
    }

    public void addProjectComponent(@NotNull IncomingChangesComponent incomingChangesComponent) {
        incomingChangesComponents.add(incomingChangesComponent);
    }

    public void removeProjectComponent(@NotNull IncomingChangesComponent incomingChangesComponent) {
        incomingChangesComponents.remove(incomingChangesComponent);
    }

    public void processEvent(final FirebugEvent event) {
        // Dispatch the incoming event to every open project
        for (final IncomingChangesComponent incomingChangesComponent : incomingChangesComponents) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                    incomingChangesComponent.handleEvent(event);
                }
            });
        }
    }

    public void processCss(final FirebugChangesBean changesBean) {
        // Dispatch the incoming change to every open project
        for (final IncomingChangesComponent incomingChangesComponent : incomingChangesComponents) {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                public void run() {
                    incomingChangesComponent.processRule(changesBean);
                }
            });
        }
    }
}
