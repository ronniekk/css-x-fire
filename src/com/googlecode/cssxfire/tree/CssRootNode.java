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

package com.googlecode.cssxfire.tree;

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiManager;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssRootNode extends CssTreeNode
{
    private final Project project;

    public CssRootNode(Project project)
    {
        this.project = project;
    }

    @Override
    public Icon getIcon()
    {
        VirtualFile baseDir = project.getBaseDir();
        if (baseDir != null)
        {
            if (!project.isInitialized())
            {
                return baseDir.getIcon();
            }
            PsiDirectory directory = PsiManager.getInstance(project).findDirectory(baseDir);
            return directory != null ? directory.getIcon(1) : baseDir.getIcon();
        }
        return null;
    }

    @Override
    public String getText()
    {
        VirtualFile baseDir = project.getBaseDir();
        return baseDir != null ?  project.getName() + " (" + baseDir.getPresentableUrl() + ")" : project.getName();
    }

    @Override
    public ActionGroup getActionGroup()
    {
        return null;
    }

    @Override
    public boolean isRoot()
    {
        return true;
    }
}
