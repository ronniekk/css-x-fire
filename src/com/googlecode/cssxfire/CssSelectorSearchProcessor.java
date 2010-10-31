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

import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssElement;
import com.intellij.psi.css.CssSelector;
import com.intellij.psi.css.CssSelectorList;
import com.intellij.psi.search.TextOccurenceProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssSelectorSearchProcessor implements TextOccurenceProcessor
{
    private final List<CssElement> selectors = new ArrayList<CssElement>();
    private @NotNull String selector;
    private @NotNull String word;

    public CssSelectorSearchProcessor(@NotNull String selector)
    {
        this.selector = StringUtils.normalizeWhitespace(selector);
        this.word = StringUtils.extractSearchWord(this.selector);
    }

    public String getSearchWord()
    {
        return word;
    }

    public boolean execute(PsiElement psiElement, int i)
    {
        if (psiElement instanceof CssSelector || psiElement instanceof CssSelectorList)
        {
            CssElement cssSelector = (CssElement) psiElement;
            if ((!(cssSelector.getParent() instanceof CssSelectorList)) && selector.equals(StringUtils.normalizeWhitespace(cssSelector.getText())))
            {
                selectors.add(cssSelector);
            }
        }

        return true;
    }

    public List<CssElement> getResults()
    {
        return selectors;
    }
}
