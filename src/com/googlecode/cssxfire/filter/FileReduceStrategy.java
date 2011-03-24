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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>Reduces the candidates down to the elements which matches the given filename. If the collection is
 * empty this reducer does nothing.
 * <p><p>
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class FileReduceStrategy implements ReduceStrategy<CssDeclarationPath>
{
    private static final Logger LOG = Logger.getInstance(FileReduceStrategy.class.getName());

    @NotNull
    private String filename;

    public FileReduceStrategy(@NotNull String filename)
    {
        this.filename = filename;
    }

    public void reduce(@NotNull Collection<CssDeclarationPath> candidates)
    {
        if (LOG.isDebugEnabled())
        {
            LOG.debug("Reducing " + candidates.size() + " candidates for filename: " + filename);
        }
        if (candidates.isEmpty())
        {
            // nothing to do here
            return;
        }

        List<CssDeclarationPath> matches = new ArrayList<CssDeclarationPath>();
        for (CssDeclarationPath candidate : candidates)
        {
            if (filename.equals(candidate.getFileNode().getFilename()))
            {
                // filename matches candidate file
                matches.add(candidate);
            }
        }

        candidates.retainAll(matches);
    }
}
