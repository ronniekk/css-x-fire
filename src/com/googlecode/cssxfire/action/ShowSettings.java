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

package com.googlecode.cssxfire.action;

import com.googlecode.cssxfire.ProjectSettingsConfigurable;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class ShowSettings extends AnAction
{
    @Override
    public void actionPerformed(AnActionEvent event)
    {
        Project project = LangDataKeys.PROJECT.getData(event.getDataContext());
        if (project == null)
        {
            return;
        }

        ShowSettingsUtil.getInstance().editConfigurable(project, ProjectSettingsConfigurable.getInstance(project));
    }
}
