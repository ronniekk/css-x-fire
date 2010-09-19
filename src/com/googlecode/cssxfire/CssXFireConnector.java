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

package com.googlecode.cssxfire;

import com.googlecode.cssxfire.webserver.SimpleWebServer;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.io.IOException;
import java.net.BindException;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssXFireConnector implements ApplicationComponent
{
    private SimpleWebServer webServer;
    private Collection<IncomingChangesComponent> incomingChangesComponents = new ArrayList<IncomingChangesComponent>();

    public static CssXFireConnector getInstance()
    {
        return ApplicationManager.getApplication().getComponent(CssXFireConnector.class);
    }

    public CssXFireConnector()
    {
    }

    public void initComponent()
    {
        // start web server
         try
         {
             webServer = new SimpleWebServer();
             new Thread(webServer).start();
         }
         catch (BindException e)
         {
             System.out.println("CssXFireConnector: unable to start SimpleWebServer - address in use");
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
    }

    public void disposeComponent()
    {
        // tear down web server
        if (webServer != null)
        {
            try
            {
                webServer.stop();
            }
            catch (IOException e)
            {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        webServer = null;
    }

    @NotNull
    public String getComponentName()
    {
        return getClass().getSimpleName();
    }

    public void addProjectComponent(@NotNull IncomingChangesComponent incomingChangesComponent)
    {
        incomingChangesComponents.add(incomingChangesComponent);
    }

    public void removeProjectComponent(@NotNull IncomingChangesComponent incomingChangesComponent)
    {
        incomingChangesComponents.remove(incomingChangesComponent);
    }

    public void processCss(final String selector, final String property, final String value)
    {
        for (final IncomingChangesComponent incomingChangesComponent : incomingChangesComponents)
        {
            ApplicationManager.getApplication().invokeLater(new Runnable()
            {
                public void run()
                {
                    incomingChangesComponent.processRule(selector, property, value);
                }
            });
        }
    }
}
