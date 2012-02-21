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

package com.googlecode.cssxfire.resolve;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.impl.CssTreeElementFactory;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssXFireReferenceProvider extends PsiReferenceProvider
{
    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement psiElement, @NotNull ProcessingContext processingContext)
    {
        if (psiElement instanceof CssTreeElementFactory.CssTokenImpl)
        {
            if (PsiTreeUtil.getParentOfType(psiElement, CssRuleset.class) == null)
            {
                return PsiReference.EMPTY_ARRAY;
            }
            PsiElement prevSibling = psiElement.getPrevSibling();
            if (prevSibling != null && ".".equals(prevSibling.getText()))
            {
                return new PsiReference[] {new CssRulesetReference((CssTreeElementFactory.CssTokenImpl) psiElement)};
            }
        }
        return PsiReference.EMPTY_ARRAY;
    }
}
