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

package com.github.cssxfire.ui;

import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.vcs.FileStatus;

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class Colors {

    public static Color getModified() {
        return getColor(FileStatus.MODIFIED.getColorKey());
    }

    public static Color getAdded() {
        return getColor(FileStatus.ADDED.getColorKey());
    }

    public static Color getInvalid() {
        return getColor(FileStatus.DELETED.getColorKey());
    }

    public static Color getLegend(Color color) {
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), 64);
    }

    public static Color getDefaultBackground() {
        return getEditorColorsScheme().getDefaultBackground();
    }

    private static Color getColor(ColorKey key) {
        return getEditorColorsScheme().getColor(key);
    }

    private static EditorColorsScheme getEditorColorsScheme() {
        return EditorColorsManager.getInstance().getGlobalScheme();
    }
}
