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

import com.github.cssxfire.CssUtils;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.*;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssNewDeclarationForRulesetListNode extends CssNewDeclarationNode {
    protected CssNewDeclarationForRulesetListNode(@NotNull CssDeclaration cssDeclaration, @NotNull CssRulesetList destinationElement, boolean deleted) {
        super(cssDeclaration, destinationElement, deleted);
    }

    private CssBlock ensureSelectorTargetExists() {
        CssSelectorNode selectorNode = getCssSelectorNode();
        String selector = selectorNode.getSelector();

        CssRulesetList rulesetList = (CssRulesetList) destinationBlock;
        CssRuleset[] rulesets = rulesetList.getRulesets();
        if (rulesets != null) {
            for (CssRuleset ruleset : rulesets) {
                CssSelectorList selectorList = ruleset.getSelectorList();
                if (selectorList != null && selector.equals(selectorList.getText())) {
                    return ruleset.getBlock();
                }
            }
        }

        // not found, which is also expected... we have to create a new one

        CssRuleset ruleset = CssUtils.createRuleset(destinationBlock.getProject(), selector);
        PsiElement psiElement = rulesetList.add(ruleset);
        if (psiElement instanceof CssRuleset) {
            return ((CssRuleset) psiElement).getBlock();
        }

        // we really shouldn't get here TODO: raise some error?
        return ruleset.getBlock();
    }

    @Override
    public void applyToCode() {
        try {
            if (isValid() && !deleted) {
                CssBlock cssBlock = ensureSelectorTargetExists();

                CssDeclaration[] declarations = cssBlock.getDeclarations();
                CssDeclaration anchor = declarations != null && declarations.length > 0
                        ? declarations[declarations.length - 1]
                        : null;
                cssBlock.addDeclaration(property, value + (important ? " !important" : ""), anchor);
            }
        } catch (IncorrectOperationException e) {
            e.printStackTrace();
        }
    }
}
