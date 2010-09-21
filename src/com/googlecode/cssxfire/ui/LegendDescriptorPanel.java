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

import com.intellij.openapi.diff.ex.DiffStatusBar;
import com.intellij.openapi.editor.colors.EditorColorsScheme;

import java.awt.*;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class LegendDescriptorPanel extends DiffStatusBar
{
    private static final Color COLOR_MODIFIED = new Color(188, 207, 249);
    private static final Color COLOR_INVALID = new Color(203, 203, 203);
    private static final Color COLOR_ADDED = new Color(186, 238, 186);

    private static final java.util.List<? extends LegendTypeDescriptor> MY_TYPES =
            Arrays.asList(new MyLegendTypeDescriptor("Modified", COLOR_MODIFIED),
                    new MyLegendTypeDescriptor("Added", COLOR_ADDED),
                    new MyLegendTypeDescriptor("Invalid", COLOR_INVALID));

    public LegendDescriptorPanel()
    {
        super(MY_TYPES);
        setBorder(null);
    }

    private static class MyLegendTypeDescriptor implements DiffStatusBar.LegendTypeDescriptor
    {
        private final String displayName;
        private final Color legendColor;

        private MyLegendTypeDescriptor(String displayName, Color legendColor)
        {
            this.displayName = displayName;
            this.legendColor = legendColor;
        }

        public String getDisplayName()
        {
            return displayName;
        }

        public Color getLegendColor(EditorColorsScheme colorScheme)
        {
            return legendColor;
        }
    }
}
