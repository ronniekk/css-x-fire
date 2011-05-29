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

package com.googlecode.cssxfire.filter;

import com.googlecode.cssxfire.tree.CssDeclarationPath;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>Reduces the candidates down to the elements which are in files currently opened in editor(s).
 * If the collection is empty this reducer does nothing.
 * <p><p>
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CurrentDocumentsReduceStrategy implements ReduceStrategy<CssDeclarationPath>
{
    private static final Logger LOG = Logger.getInstance(CurrentDocumentsReduceStrategy.class.getName());

    @NotNull
    private final Project project;

    public CurrentDocumentsReduceStrategy(@NotNull Project project)
    {
        this.project = project;
    }

    public void reduce(@NotNull Collection<CssDeclarationPath> candidates)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Reducing " + candidates.size() + " candidates for currently opened documents");
        }
        if (candidates.isEmpty())
        {
            // nothing to do here
            return;
        }

        VirtualFile[] openFiles = FileEditorManager.getInstance(project).getOpenFiles();
        List<CssDeclarationPath> matches = new ArrayList<CssDeclarationPath>();
        for (CssDeclarationPath candidate : candidates)
        {
            VirtualFile candidateFile = candidate.getFileNode().getVirtualFile();
            for (VirtualFile openFile : openFiles)
            {
                if (candidateFile == openFile)
                {
                    // this candidate is is a currently opened file
                    matches.add(candidate);
                }
            }
        }

        candidates.retainAll(matches);
    }
}
