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

package com.googlecode.cssxfire;

import com.intellij.lang.Language;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.CssRulesetList;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssUtils
{
    public static CssDeclaration createDeclaration(Project project, String selector, String property, String value)
    {
        CSSLanguage cssLanguage = Language.findInstance(CSSLanguage.class);
        PsiFile dummyFile = PsiFileFactory.getInstance(project).createFileFromText("dummy.css", cssLanguage, selector + " {" + property + ":" + value + ";}\n");
        return findFirstChildOfType(dummyFile, CssDeclaration.class);
    }

    public static CssRuleset createRuleset(Project project, String selector)
    {
        CSSLanguage cssLanguage = Language.findInstance(CSSLanguage.class);
        PsiFile dummyFile = PsiFileFactory.getInstance(project).createFileFromText("dummy.css", cssLanguage, selector + " {\n\n}\n");
        return findFirstChildOfType(dummyFile, CssRuleset.class);
    }

    private static <T extends PsiElement> T findFirstChildOfType(@NotNull PsiElement element, Class<T> type)
    {
        PsiElement[] children = element.getChildren();
        for (PsiElement child : children)
        {
            if (type.isAssignableFrom(child.getClass()))
            {
                return (T) child;
            }
            T t = findFirstChildOfType(child, type);
            if (t != null)
            {
                return (T) t;
            }
        }
        return null;
    }

    @Nullable
    public static CssRulesetList findFirstCssRulesetList(@NotNull PsiFile file)
    {
        final Ref<CssRulesetList> ref = new Ref<CssRulesetList>();
        PsiTreeUtil.processElements(file, new PsiElementProcessor()
        {
            public boolean execute(PsiElement element)
            {
                if (element instanceof CssRulesetList)
                {
                    ref.set((CssRulesetList) element);
                    return false;
                }
                return true;
            }
        });
        return ref.get();
    }
}
