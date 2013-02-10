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

package com.github.cssxfire;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.*;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.search.TextOccurenceProcessor;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssSelectorSearchProcessor implements TextOccurenceProcessor {
    private static final Logger LOG = Logger.getInstance(CssSelectorSearchProcessor.class.getName());
    private static final String DUMMY = "";

    private final List<CssElement> selectors = new ArrayList<CssElement>();
    @NotNull
    private String selector;
    @NotNull
    private String word;

    public CssSelectorSearchProcessor(@NotNull String selector) {
        this.selector = StringUtils.normalizeWhitespace(selector);
        this.word = StringUtils.extractSearchWord(this.selector);
    }

    /**
     * Get the word to use when using {@link com.intellij.psi.search.PsiSearchHelper} to process elements with word
     *
     * @return the word to use in search
     */
    @NotNull
    public String getSearchWord() {
        return word;
    }

    /**
     * Get the entire selector string, with whitespace normalized
     *
     * @return the selector string used for final match
     */
    @NotNull
    public String getSelector() {
        return selector;
    }

    public boolean execute(PsiElement psiElement, int i) {
        if (psiElement instanceof CssSelector || psiElement instanceof CssSelectorList) {
            CssElement cssSelector = (CssElement) psiElement;
            if ((!(cssSelector.getParent() instanceof CssSelectorList)) && canBeReference(cssSelector)) {
                selectors.add(cssSelector);
            }
        }

        return true;
    }

    /**
     * Checks (stringwise) if the given css element could reference the selector to search for. The check is
     * done by expanding the rule of the given css selector by checking all of its parent elements within the
     * same file. This allows for nested rules (Less/Sass).
     *
     * @param cssSelector the candidate element
     * @return true if and only if the rule of the given element matches this selector
     */
    private boolean canBeReference(@NotNull CssElement cssSelector) {
        final List<List<String>> selectorPaths = createSelectorParts(selector);

        boolean complete = CssUtils.processParents(cssSelector, new PsiElementProcessor<PsiElement>() {
            public boolean execute(PsiElement element) {
                if (element instanceof CssRuleset) {
                    CssRuleset cssRuleset = (CssRuleset) element;
                    CssSelectorList selectorList = cssRuleset.getSelectorList();
                    if (selectorList == null) {
                        return false; // abort processing
                    }
                    String selectorText = StringUtils.normalizeWhitespace(selectorList.getText());
                    List<List<String>> comparePaths = createSelectorParts(selectorText);

                    for (List<String> comparePath : comparePaths) {
                        int numToRemove = 0;
                        for (List<String> selectorPath : selectorPaths) {
                            if (endsWith(selectorPath, comparePath)) {
                                numToRemove = comparePath.size();
                                for (int i = 0; i < numToRemove; i++) {
                                    pop(selectorPath);
                                }
                                selectorPath.add(DUMMY); // Add a dummy marker that won't match again
                            }
                        }
                        for (int i = 0; i < numToRemove; i++) {
                            pop(comparePath);
                        }
                    }

                    if (cleanupSelectorParts(comparePaths)) {
                        return false;
                    }

                    for (List<String> selectorPath : selectorPaths) {
                        String stack = pop(selectorPath);// Clear dummy markers
                        if (!"".equals(stack)) {
                            selectorPath.add(DUMMY);
                            return false;
                        }
                    }
                    return true;
                }
                return true;
            }
        });

        // Check for loose ends in given selector and in code.
        if (cleanupSelectorParts(selectorPaths) || !complete) {
            return false;
        }
        return true;
    }

    /**
     * Checks if <tt>match</tt> is equal to the tail of <tt>candidate</tt>
     *
     * @param candidate the list to test
     * @param match     the tail to test for
     * @return <tt>true</tt> if <tt>match</tt> is a tailing sublist of <tt>candidate</tt>,
     *         given the semantics of <tt>comparator</tt>
     */
    private boolean endsWith(List<String> candidate, List<String> match) {
        if (candidate.isEmpty() || match.isEmpty() || match.size() > candidate.size()) {
            return false;
        }
        for (int i = 0; i < match.size(); i++) {
            int cix = candidate.size() - 1 - i;
            int mix = match.size() - 1 - i;

            String s1 = candidate.get(cix);
            String s2 = match.get(mix);

            if (!s1.equals(s2)) {
                if (!(s2.startsWith("&:") && s1.indexOf(':') != -1 && s2.substring(1).equals(s1.substring(s1.indexOf(':'))))) {
                    return false;
                }
                candidate.add(cix, s1.substring(0, s1.indexOf(':')));
            }
        }
        return true;
    }

    /**
     * Removes the last element of the given list of strings
     *
     * @param strings the list to pop
     * @return the removed element, or <tt>null</tt> if the list is empty
     */
    private String pop(List<String> strings) {
        if (!strings.isEmpty()) {
            return strings.remove(strings.size() - 1);
        }
        return null;
    }

    private List<List<String>> createSelectorParts(String s) {
        List<List<String>> parts = new ArrayList<List<String>>();
        String[] selectorParts = s.split(",");
        for (String part : selectorParts) {
            List<String> sub = new ArrayList<String>();
            String[] subParts = part.split(" ");
            for (String subPart : subParts) {
                sub.add(subPart);
            }
            parts.add(sub);
        }
        return parts;
    }

    /**
     * Clean up and free memory
     *
     * @param parts the list to clean
     * @return true if there was any strings left
     */
    private boolean cleanupSelectorParts(List<List<String>> parts) {
        boolean leftovers = false;
        for (List<String> part : parts) {
            if (!part.isEmpty()) {
                leftovers = true;
            }
            part.clear();
        }
        parts.clear();
        return leftovers;
    }

    /**
     * Get the number of hits this processor has collected
     *
     * @return the number of collected elements
     */
    public int size() {
        return selectors.size();
    }

    /**
     * Get the collected elements.
     *
     * @return the elements collected
     * @see #getBlocks()
     */
    @NotNull
    public CssElement[] getResults() {
        return selectors.toArray(new CssElement[selectors.size()]);
    }

    /**
     * Get the corresponding CssBlocks for the collected elements.
     *
     * @return the blocks for the collected elements
     * @see #getResults()
     */
    @NotNull
    public CssBlock[] getBlocks() {
        final Collection<CssBlock> blocks = new ArrayList<CssBlock>();

        for (CssElement result : getResults()) {
            CssRuleset ruleSet = PsiTreeUtil.getParentOfType(result, CssRuleset.class);
            if (ruleSet != null) {
                CssBlock block = ruleSet.getBlock();
                if (block != null) {
                    blocks.add(block);
                }
            }
        }

        return blocks.toArray(new CssBlock[blocks.size()]);
    }
}
