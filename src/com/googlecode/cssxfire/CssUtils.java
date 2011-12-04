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
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import com.intellij.psi.PsiManager;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.CssRulesetList;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssUtils
{
    public static CssDeclaration createDeclaration(Project project, String selector, String property, String value, boolean important)
    {
        CSSLanguage cssLanguage = Language.findInstance(CSSLanguage.class);
        String text = selector + " {" + property + ":" + value + (important ? " !important" : "") + ";}\n";
        PsiFile dummyFile = PsiFileFactory.getInstance(project).createFileFromText("dummy.css", cssLanguage, text);
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

    public static PsiElement findMediumList(PsiElement element)
    {
        /*
            This is a facade for the helper method in CssUtil. Reflection is used since the signature
            return type has changed and we want the plugin to work regardless which library it is compiled and
            linked against.
         */

        try
        {
            Method method = CssUtil.class.getMethod("getMediumList", PsiElement.class);

            return (PsiElement) method.invoke(null, element);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to get medium list");
        }
    }

    public static PsiSearchHelper getPsiSearchHelper(Project project)
    {
        /*
            Temporary(?) facade for getting PsiSearchHelper before and after v 11
         */
        PsiSearchHelper helper = ServiceManager.getService(project, PsiSearchHelper.class);
        if (helper != null)
        {
            return helper;
        }
        PsiManager psiManager = PsiManager.getInstance(project);
        try
        {
            return (PsiSearchHelper) PsiManager.class.getMethod("getSearchHelper").invoke(psiManager);
        }
        catch (Exception e)
        {
            throw new RuntimeException("Unable to get PsiSearchHelper");
        }
    }

    public static boolean processParents(@NotNull PsiElement element, @NotNull PsiElementProcessor<PsiElement> processor)
    {
        PsiElement parent = element.getParent();
        while (parent != null)
        {
            if (parent instanceof PsiFile)
            {
                break;
            }
            if (!processor.execute(parent))
            {
                return false;
            }
            parent = parent.getParent();
        }
        return true;
    }
}
