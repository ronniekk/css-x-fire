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
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssElement;
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

    public static CssNewDeclarationNode forDestination(@NotNull CssDeclaration cssDeclaration, @NotNull PsiElement destinationElement, boolean deleted, boolean important)
    {
        String name = destinationElement.getClass().getSimpleName();
        /*
            Following checks are really ugly but exists only for the reason that the expected classes
            are package protected and may not be referenced (applies to both interfaces and implementations).

            TODO: do a cleaner/better type check, perhaps by inspecting the AST element types instead
         */
        if ("CssBlockImpl".equals(name))
        {
            return new CssNewDeclarationForBlockNode(cssDeclaration, (CssElement) destinationElement, deleted);
        }
        if ("CssMediumListImpl".equals(name))
        {
            return new CssNewDeclarationForMediumNode(cssDeclaration, (CssElement) destinationElement, deleted);
        }
        if ("CssRulesetListImpl".equals(name))
        {
            return new CssNewDeclarationForRulesetListNode(cssDeclaration, (CssElement) destinationElement, deleted);
        }
        throw new IllegalArgumentException("Can not create CssNewDeclarationNode for destination of type " + name);
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
