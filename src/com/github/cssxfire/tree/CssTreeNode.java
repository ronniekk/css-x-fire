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

package com.github.cssxfire.tree;

import com.intellij.openapi.actionSystem.ActionGroup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public abstract class CssTreeNode extends DefaultMutableTreeNode {
    @Nullable
    public abstract Icon getIcon();

    public abstract String getText();

    @Override
    public boolean getAllowsChildren() {
        return true;
    }

    @Nullable
    public abstract ActionGroup getActionGroup();

    @NotNull
    private String wrapWithHtml(String text) {
        return "<html>" + text + "</html>";
    }

    @NotNull
    protected String wrapWithHtmlColor(String text, Color color) {
        return wrapWithHtml("<font color=\"#" + toHtml(color) + "\">" + text + "</font>");
    }

    private static String toHtml(Color color) {
        StringBuilder sb = new StringBuilder();
        sb.append(padWithZeros(Integer.toHexString(color.getRed())));
        sb.append(padWithZeros(Integer.toHexString(color.getGreen())));
        sb.append(padWithZeros(Integer.toHexString(color.getBlue())));
        return sb.toString();
    }

    private static String padWithZeros(String s) {
        while (s.length() < 2) {
            s = "0" + s;
        }
        return s;
    }
}
