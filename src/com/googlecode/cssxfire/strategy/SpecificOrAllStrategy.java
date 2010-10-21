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

package com.googlecode.cssxfire.strategy;

import com.googlecode.cssxfire.tree.CssDeclarationPath;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * <p>Reduces the candidates down to one element, if and only if there is one single candidate which
 * matches the given filename. If the collection contains only one element, or if it is empty, this reducer does nothing.
 * <p><p>
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class SpecificOrAllStrategy implements ReduceStrategy<CssDeclarationPath>
{
    @NotNull
    private String filename;

    public SpecificOrAllStrategy(@NotNull String filename)
    {
        this.filename = filename;
    }

    public void reduce(@NotNull Collection<CssDeclarationPath> candidates)
    {
        if (candidates.size() <= 1)
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

        if (matches.size() == 1)
        {
            candidates.retainAll(matches);
        }
    }
}
