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

import com.intellij.psi.css.CssBlock;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssNewDeclarationForBlockNode extends CssNewDeclarationNode {
    public CssNewDeclarationForBlockNode(@NotNull CssDeclaration cssDeclaration, @NotNull CssBlock destinationBlock, boolean deleted) {
        super(cssDeclaration, destinationBlock, deleted);
    }

    @Override
    public void applyToCode() {
        try {
            if (isValid() && !deleted) {
                CssDeclaration[] declarations = ((CssBlock) destinationBlock).getDeclarations();
                CssDeclaration relativeTo = declarations != null && declarations.length > 0
                        ? declarations[declarations.length - 1]
                        : null;
                ((CssBlock) destinationBlock).addDeclaration(property, value + (important ? " !important" : ""), relativeTo);
            }
        } catch (IncorrectOperationException e) {
            e.printStackTrace();
        }
    }
}
