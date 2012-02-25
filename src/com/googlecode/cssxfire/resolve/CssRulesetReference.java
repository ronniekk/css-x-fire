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

import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.CssRulesetList;
import com.intellij.psi.css.CssStylesheet;
import com.intellij.psi.impl.source.resolve.reference.impl.PsiPolyVariantCachingReference;
import com.intellij.psi.scope.PsiScopeProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssRulesetReference extends PsiPolyVariantCachingReference
{
    private static final ResolveResult[] EMPTY_RESOLVE_RESULTS = new ResolveResult[0];
    /** Avoid completion on mixins for now */
    private static final boolean COMPLETION_ENABLED = false;

    private final PsiElement place;
    private static final LookupElement[] EMPTY_VARIANTS = new LookupElement[0];

    public CssRulesetReference(PsiElement place)
    {
        this.place = place;
    }

    protected String getName()
    {
        return getElement().getText();
    }

    @NotNull
    @Override
    protected ResolveResult[] resolveInner(boolean incompleteCode)
    {
        CssRulesetProcessor processor = new CssRulesetProcessor(getName());
        processTopLevelRulesets(processor);
        CssRuleset[] rulesets = processor.getCandidates();
        if (rulesets.length == 0)
        {
            return EMPTY_RESOLVE_RESULTS;
        }
        List<ResolveResult> results = new ArrayList<ResolveResult>();
        for (CssRuleset ruleset : rulesets)
        {
            results.add(new PsiElementResolveResult(ruleset));
        }
        return results.toArray(new ResolveResult[results.size()]);
    }

    public PsiElement getElement()
    {
        return place;
    }

    public TextRange getRangeInElement()
    {
        return new TextRange(0, getName().length());
    }

    @NotNull
    public String getCanonicalText()
    {
        return getElement().getText();
    }

    public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException
    {
        return null;
    }

    public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException
    {
        return null;
    }

    @NotNull
    public Object[] getVariants()
    {
        if (COMPLETION_ENABLED)
        {
            CssRulesetProcessor processor = new CssRulesetProcessor(null);
            processTopLevelRulesets(processor);
            return toLookupElements(processor.getCandidates());
        }
        return EMPTY_VARIANTS;
    }

    private LookupElement[] toLookupElements(CssRuleset[] candidates)
    {
        LookupElement[] results = new LookupElement[candidates.length];
        for (int i = 0; i < candidates.length; i++)
        {
            String name = candidates[i].getSelectorList().getText();
            results[i] = LookupElementBuilder.create(candidates[i], name.substring(1)).setPresentableText(name);
        }
        return results;
    }

    /**
     * Process all top-level rulesets with given processor
     * @param processor the processor
     */
    private void processTopLevelRulesets(PsiScopeProcessor processor)
    {
        PsiFile file = getElement().getContainingFile();
        CssStylesheet[] styleSheets = PsiTreeUtil.getChildrenOfType(file, CssStylesheet.class);
        if (styleSheets != null)
        {
            for (CssStylesheet styleSheet : styleSheets)
            {
                CssRulesetList rulesetList = PsiTreeUtil.getChildOfType(styleSheet, CssRulesetList.class);
                if (rulesetList != null)
                {
                    CssRuleset[] rulesets = PsiTreeUtil.getChildrenOfType(rulesetList, CssRuleset.class);
                    if (rulesets != null)
                    {
                        for (CssRuleset ruleset : rulesets)
                        {
                            if (!processor.execute(ruleset, ResolveState.initial()))
                            {
                                return;
                            }
                        }
                    }
                }
            }
        }
    }
}
