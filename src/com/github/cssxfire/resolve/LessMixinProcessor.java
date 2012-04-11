/*
 * Copyright 2012 Ronnie Kolehmainen
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

package com.github.cssxfire.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiErrorElement;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.CssSelectorList;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class LessMixinProcessor extends CssResolveProcessor {
    public LessMixinProcessor(String name) {
        super(name);
    }

    @Override
    public boolean executeInScope(@NotNull PsiElement base) {
        return super.executeInScope(base);
    }

    @Override
    public boolean executeInternal(@NotNull PsiElement element) {
        if (element instanceof CssRuleset) {
            CssRuleset ruleset = (CssRuleset) element;
            CssSelectorList selectorList = ruleset.getSelectorList();
            if (selectorList.getSelectors().length == 1) {
                String text = selectorList.getText();
                if (text != null && text.equals(name)) {
                    if (PsiTreeUtil.findChildOfType(ruleset, PsiErrorElement.class) != null) {
                        // Error in PSI tree - skip
                        return true;
                    }
                    result.set(ruleset);
                    return false;
                }
            }
        }
        return true;
    }
}
