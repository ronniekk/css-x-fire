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

import com.intellij.psi.css.CssBlock;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssNewDeclarationNode extends CssDeclarationNode
{
    private final CssBlock destinationBlock;
    private final String property;

    public CssNewDeclarationNode(CssDeclaration cssDeclaration, CssBlock destinationBlock)
    {
        super(cssDeclaration, cssDeclaration.getValue().getText());
        this.destinationBlock = destinationBlock;
        this.property = cssDeclaration.getPropertyName();
    }

    @Override
    public boolean isValid()
    {
        return destinationBlock.isValid();
    }

    @Override
    public String getText()
    {
        return wrapWithHtml("<font color=blue>" + cssDeclaration.getText() + "</font>" + (isValid() ? "" : " <INVALID>"));
    }

    @Override
    public void applyToCode()
    {
        try
        {
            if (isValid())
            {
                CssDeclaration[] declarations = PsiTreeUtil.getChildrenOfType(destinationBlock, CssDeclaration.class);
                CssDeclaration anchor = declarations != null && declarations.length > 0
                        ? declarations[declarations.length - 1]
                        : null;
                destinationBlock.addDeclaration(property, value, anchor);
            }
        }
        catch (IncorrectOperationException e)
        {
            e.printStackTrace();
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CssNewDeclarationNode that = (CssNewDeclarationNode) o;

        if (destinationBlock != null ? !destinationBlock.equals(that.destinationBlock) : that.destinationBlock != null)
            return false;
        if (property != null ? !property.equals(that.property) : that.property != null) return false;

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + (destinationBlock != null ? destinationBlock.hashCode() : 0);
        result = 31 * result + (property != null ? property.hashCode() : 0);
        return result;
    }
}
