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

import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.util.IncorrectOperationException;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssDeclarationNode extends CssTreeNode
{
    protected final CssDeclaration cssDeclaration;
    protected final String value;

    public CssDeclarationNode(CssDeclaration cssDeclaration, String value)
    {
        this.cssDeclaration = cssDeclaration;
        this.value = value;
    }

    @Override
    public Icon getIcon()
    {
        return isValid() ? cssDeclaration.getIcon(1) : null;
    }

    @Override
    public String getText()
    {
        return wrapWithHtml("<font color=green>" + cssDeclaration.getPropertyName() + ": <b>" + value + "</b></font>" + (isValid() ? "" : " <b><INVALID></b>"));
    }

    public boolean isValid()
    {
        return cssDeclaration.isValid();
    }

    public void applyToCode()
    {
        try
        {
            if (isValid())
            {
                cssDeclaration.setValue(value);
            }
        }
        catch (IncorrectOperationException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public ActionGroup getActionGroup()
    {
        return (ActionGroup) ActionManager.getInstance().getAction("IncomingChanges.DeclarationNodePopup.Single");
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

        CssDeclarationNode that = (CssDeclarationNode) o;

        if (cssDeclaration != null ? !cssDeclaration.equals(that.cssDeclaration) : that.cssDeclaration != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = cssDeclaration != null ? cssDeclaration.hashCode() : 0;
        result = 31 * result + (value != null ? value.hashCode() : 0);
        return result;
    }
}
