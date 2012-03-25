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

import com.googlecode.cssxfire.ProjectSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class AutoExpand extends ToggleAction {
    @Nullable
    protected ProjectSettings getProjectSettings(AnActionEvent event) {
        Project project = LangDataKeys.PROJECT.getData(event.getDataContext());
        if (project == null) {
            return null;
        }
        return ProjectSettings.getInstance(project);
    }

    @Override
    public boolean isSelected(AnActionEvent e) {
        ProjectSettings projectSettings = getProjectSettings(e);
        return projectSettings != null && projectSettings.isAutoExpand();
    }

    @Override
    public void setSelected(AnActionEvent e, boolean state) {
        ProjectSettings projectSettings = getProjectSettings(e);
        if (projectSettings != null) {
            projectSettings.setAutoExpand(state);
        }
    }
}
