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

import com.googlecode.cssxfire.ui.Icons;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssSelectorNode extends CssTreeNode
{
    private final String selector;

    public CssSelectorNode(String selector)
    {
        this.selector = selector;
    }

    @Override
    public Icon getIcon()
    {
        if (selector.startsWith("."))
        {
            return Icons.CSS_CLASS;
        }
        if (selector.startsWith("#"))
        {
            return Icons.CSS_ID;
        }
        return Icons.CSS_HTML_TAG;
    }

    @Override
    public String getText()
    {
        return wrapWithHtml("<b>" + selector + "</b>");
    }

    @Override
    public ActionGroup getActionGroup()
    {
        return (ActionGroup) ActionManager.getInstance().getAction("IncomingChanges.DeclarationNodePopup.Selector");
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        CssSelectorNode that = (CssSelectorNode) o;

        if (selector != null ? !selector.equals(that.selector) : that.selector != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return selector != null ? selector.hashCode() : 0;
    }
}
