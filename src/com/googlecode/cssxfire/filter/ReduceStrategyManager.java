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

import com.googlecode.cssxfire.IncomingChangesComponent;
import com.googlecode.cssxfire.tree.CssDeclarationPath;
import com.intellij.openapi.project.Project;
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
    /**
     * Get a filter for (possibly) reducing a collection of {@link com.googlecode.cssxfire.tree.CssDeclarationPath}
     * candidates. The filter is based on settings from the toolwindow and/or a given filename and media query.
     * @param project the current project
     * @param filename the filename specified (optional)
     * @param media the media query (optional)
     * @return a suitable {@link com.googlecode.cssxfire.filter.ReduceStrategy}
     */
    public static ReduceStrategy<CssDeclarationPath> getStrategy(@NotNull Project project, @NotNull String filename, @NotNull String media)
    {
        final List<ReduceStrategy<CssDeclarationPath>> reduceChain = new ArrayList<ReduceStrategy<CssDeclarationPath>>();

        if (IncomingChangesComponent.getInstance(project).getMediaReduce().get())
        {
            // Reduce for @media is checked
            reduceChain.add(new MediaReduceStrategy(media));
        }
        if (IncomingChangesComponent.getInstance(project).getFileReduce().get())
        {
            // Reduce for file is checked
            reduceChain.add(new FileReduceStrategy(filename));
        }

        return new ReduceStrategy<CssDeclarationPath>()
        {
            public void reduce(@NotNull Collection<CssDeclarationPath> candidates)
            {
                for (ReduceStrategy<CssDeclarationPath> reduceStrategy : reduceChain)
                {
                    reduceStrategy.reduce(candidates);
                }
            }
        };
    }
}
