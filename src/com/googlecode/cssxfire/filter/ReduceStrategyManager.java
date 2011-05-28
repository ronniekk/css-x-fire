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

import com.googlecode.cssxfire.FirebugChangesBean;
import com.googlecode.cssxfire.ProjectSettings;
import com.googlecode.cssxfire.tree.CssDeclarationPath;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class ReduceStrategyManager
{
    private static final Logger LOG = Logger.getInstance(ReduceStrategyManager.class.getName());

    /**
     * Get a filter for (possibly) reducing a collection of {@link com.googlecode.cssxfire.tree.CssDeclarationPath}
     * candidates. The filter is based on settings from the toolwindow and/or a given filename and media query.
     * @param project the current project
     * @param bean the container holding path, media and filename information
     * @return a suitable {@link com.googlecode.cssxfire.filter.ReduceStrategy}
     */
    public static ReduceStrategy<CssDeclarationPath> getStrategy(@NotNull Project project, @NotNull FirebugChangesBean bean)
    {
        final List<ReduceStrategy<CssDeclarationPath>> reduceChain = new ArrayList<ReduceStrategy<CssDeclarationPath>>();

        if (ProjectSettings.getInstance(project).isMediaReduce())
        {
            // Reduce for @media is checked
            reduceChain.add(new MediaReduceStrategy(bean.getMedia()));
        }
        if (ProjectSettings.getInstance(project).isFileReduce())
        {
            // Reduce for file is checked
            reduceChain.add(new FileReduceStrategy(bean.getFilename()));
        }
        if (ProjectSettings.getInstance(project).isCurrentDocumentsReduce())
        {
            // Reduce for currently opened files (documents)
            reduceChain.add(new CurrentDocumentsReduceStrategy(project));
        }
        if (ProjectSettings.getInstance(project).isUseRoutes())
        {
            // Use routes is checked
            VirtualFile projectBaseDir = project.getBaseDir();
            if (projectBaseDir != null)
            {
                reduceChain.add(new UrlReduceStrategy(projectBaseDir + bean.getPath()));
            }
        }

        return new ReduceStrategy<CssDeclarationPath>()
        {
            public void reduce(@NotNull Collection<CssDeclarationPath> candidates)
            {
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Filtering " + candidates.size() + " candidates");
                    for (CssDeclarationPath candidate : candidates)
                    {
                        LOG.debug("  Candidate: " + candidate);
                    }
                }
                for (ReduceStrategy<CssDeclarationPath> reduceStrategy : reduceChain)
                {
                    reduceStrategy.reduce(candidates);
                }
                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Filtering done, remaining " + candidates.size() + " candidates");
                }
            }
        };
    }
}
