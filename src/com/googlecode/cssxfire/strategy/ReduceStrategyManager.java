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
import com.googlecode.cssxfire.ui.CssToolWindow;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class ReduceStrategyManager
{
    /**
     * Get a strategy for (possibly) reducing a collection of {@link com.googlecode.cssxfire.tree.CssDeclarationPath}
     * candidates. The strategy is based on settings from the toolwindow and/or a given filename.
     * @param cssToolWindow the toolwindow
     * @param filename the filename specified
     * @return a suitable {@link com.googlecode.cssxfire.strategy.ReduceStrategy}
     */
    public static ReduceStrategy<CssDeclarationPath> getStrategy(@NotNull CssToolWindow cssToolWindow, @Nullable String filename)
    {
        if (filename != null && cssToolWindow.smartReduce())
        {
            return new SpecificOrAllStrategy(filename);
        }
        return new KeepAllStrategy();
    }
}
