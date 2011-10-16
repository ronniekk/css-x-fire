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

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.*;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.search.TextOccurenceProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssSelectorSearchProcessor implements TextOccurenceProcessor
{
    private static final Logger LOG = Logger.getInstance(CssSelectorSearchProcessor.class.getName());

    private final List<CssElement> selectors = new ArrayList<CssElement>();
    private @NotNull String selector;
    private @NotNull String word;

    public CssSelectorSearchProcessor(@NotNull String selector)
    {
        this.selector = StringUtils.normalizeWhitespace(selector);
        this.word = StringUtils.extractSearchWord(this.selector);
    }

    /**
     * Get the word to use when using {@link com.intellij.psi.search.PsiSearchHelper} to process elements with word
     * @return the word to use in search
     */
    @NotNull
    public String getSearchWord()
    {
        return word;
    }

    /**
     * Get the entire selector string, with whitespace normalized
     * @return the selector string used for final match
     */
    @NotNull
    public String getSelector()
    {
        return selector;
    }

    public boolean execute(PsiElement psiElement, int i)
    {
        if (psiElement instanceof CssSelector || psiElement instanceof CssSelectorList)
        {
            CssElement cssSelector = (CssElement) psiElement;
            if ((!(cssSelector.getParent() instanceof CssSelectorList)) && canBeReference(cssSelector))
            {
                selectors.add(cssSelector);
            }
        }

        return true;
    }

    /**
     * Checks (stringwise) if the given css element could reference the selector to search for. The check is
     * done by expanding the rule of the given css selector by checking all of its parent elements within the
     * same file. This allows for nested rules (Less/Sass).
     * @param cssSelector the candidate element
     * @return true if and only if the rule of the given element matches this selector
     */
    private boolean canBeReference(@NotNull CssElement cssSelector)
    {
        final List<String> candidatePartList = ThreadLocals.stringList.get();

        CssUtils.processParents(cssSelector, new PsiElementProcessor<PsiElement>()
        {
            public boolean execute(PsiElement element)
            {
                if (element instanceof CssRuleset)
                {
                    CssRuleset cssRuleset = (CssRuleset) element;
                    CssSelectorList selectorList = cssRuleset.getSelectorList();
                    candidatePartList.add(0, selectorList.getText());
                }
                return true;
            }
        });

        String candidatePath = createComparablePath(candidatePartList);
        if (LOG.isDebugEnabled())
        {
            LOG.debug("comparable path = " + candidatePath);
        }
        return selector.equals(candidatePath);
    }

    private String createComparablePath(List<String> pathParts)
    {
        StringBuilder sb = ThreadLocals.stringBuilder.get();
        for (String part : pathParts)
        {
            if (part.startsWith("&") && sb.length() > 0)
            {
                sb.replace(sb.length() - 1, sb.length(), part.substring(1));
            }
            else
            {
                sb.append(part);
            }
            sb.append(" ");
        }
        return StringUtils.normalizeWhitespace(sb.toString());
    }

    /**
     * Get the number of hits this processor has collected
     * @return the number of collected elements
     */
    public int size()
    {
        return selectors.size();
    }

    /**
     * Get the collected elements.
     * @return the elements collected
     * @see #getBlocks()
     */
    @NotNull
    public CssElement[] getResults()
    {
        return selectors.toArray(new CssElement[selectors.size()]);
    }

    /**
     * Get the corresponding CssBlocks for the collected elements.
     * @return the blocks for the collected elements
     * @see #getResults()
     */
    @NotNull
    public CssBlock[] getBlocks()
    {
        final Collection<CssBlock> blocks = new ArrayList<CssBlock>();

        for (CssElement result : getResults())
        {
            CssRuleset ruleSet = PsiTreeUtil.getParentOfType(result, CssRuleset.class);
            if (ruleSet != null)
            {
                CssBlock block = ruleSet.getBlock();
                if (block != null)
                {
                    blocks.add(block);
                }
            }
        }

        return blocks.toArray(new CssBlock[blocks.size()]);
    }
}
