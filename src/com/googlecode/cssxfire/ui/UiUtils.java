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

package com.googlecode.cssxfire.ui;

import com.googlecode.cssxfire.IncomingChangesComponent;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.openapi.wm.WindowManager;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class UiUtils
{
    public static void setStatusMessage(final Project project, final String message)
    {
        final WindowManager windowManager = WindowManager.getInstance();
        final StatusBar statusBar = windowManager.getStatusBar(project);
        Runnable r = new Runnable()
        {
            public void run()
            {
                statusBar.setInfo(message);
            }
        };

        ApplicationManager.getApplication().invokeLater(r);
    }

    public static void showInfoBallon(final Project project, final String text)
    {
        Runnable r = new Runnable()
        {
            public void run()
            {
                ToolWindowManager.getInstance(project).notifyByBalloon(IncomingChangesComponent.TOOLWINDOW_ID, MessageType.INFO, text);
            }
        };
        ApplicationManager.getApplication().invokeLater(r);
    }

    public static void showErrorBallon(final Project project, final String text)
    {
        Runnable r = new Runnable()
        {
            public void run()
            {
                ToolWindowManager.getInstance(project).notifyByBalloon(IncomingChangesComponent.TOOLWINDOW_ID, MessageType.ERROR, text);
            }
        };
        ApplicationManager.getApplication().invokeLater(r);
    }

}
