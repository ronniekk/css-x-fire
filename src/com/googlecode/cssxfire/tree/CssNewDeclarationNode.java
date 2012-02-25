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

import com.googlecode.cssxfire.ui.Colors;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.*;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreeNode;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public abstract class CssNewDeclarationNode extends CssDeclarationNode
{
    @NotNull protected final CssElement destinationBlock;
    @NotNull protected final String property;

    public static CssNewDeclarationNode forDestination(@NotNull CssDeclaration cssDeclaration, @NotNull CssElement destinationElement, boolean deleted)
    {
        if (destinationElement instanceof CssBlock)
        {
            return new CssNewDeclarationForBlockNode(cssDeclaration, (CssBlock) destinationElement, deleted);
        }
        if (destinationElement instanceof CssMediumList)
        {
            return new CssNewDeclarationForMediumNode(cssDeclaration, (CssMediumList) destinationElement, deleted);
        }
        if (destinationElement instanceof CssRulesetList)
        {
            return new CssNewDeclarationForRulesetListNode(cssDeclaration, (CssRulesetList) destinationElement, deleted);
        }
        throw new IllegalArgumentException("Can not create CssNewDeclarationNode for destination of type " + destinationElement.getClass().getName());
    }

    protected CssNewDeclarationNode(@NotNull CssDeclaration cssDeclaration, @NotNull CssElement destinationElement, boolean deleted)
    {
        super(cssDeclaration, cssDeclaration.getValue().getText(), deleted, cssDeclaration.isImportant());
        this.destinationBlock = destinationElement;
        this.property = cssDeclaration.getPropertyName();
    }

    /**
     * Inserts this declaration (and possible parents) into the target destination.
     * <br><br>Must be called in a write-action
     */
    @Override
    public abstract void applyToCode();

    @Override
    public final boolean isValid()
    {
        return destinationBlock.isValid();
    }

    @Override
    public final String getText()
    {
        String text = cssDeclaration.getText();
        return deleted
                ? wrapWithHtmlColor("<strike>" + text + "</strike>", isValid() ? Colors.ADDED : Colors.INVALID)
                : wrapWithHtmlColor(text, isValid() ? Colors.ADDED : Colors.INVALID);
    }

    @Override
    protected final CssElement getNavigationElement()
    {
        return isValid() ? destinationBlock : null;
    }

    @NotNull
    protected CssSelectorNode getCssSelectorNode()
    {
        TreeNode parentNode = getParent();
        if (parentNode instanceof CssSelectorNode)
        {
            return (CssSelectorNode) parentNode;
        }
        throw new IllegalArgumentException("A " + getClass().getName() + " must have a parent node of type CssSelectorNode. Found: " + parentNode);
    }

    @Override
    public final boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CssNewDeclarationNode that = (CssNewDeclarationNode) o;

        return property.equals(that.property) && destinationBlock.equals(that.destinationBlock);
    }

    @Override
    public final int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + destinationBlock.hashCode();
        result = 31 * result + property.hashCode();
        return result;
    }
}
