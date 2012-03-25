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

import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ResolveState;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.css.CssSelectorList;
import com.intellij.psi.scope.PsiScopeProcessor;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssRulesetProcessor implements PsiScopeProcessor {
    /**
     * The name we are looking for, or null if we want all names
     */
    private final String name;

    private final Set<CssRuleset> candidates = new HashSet<CssRuleset>();

    public CssRulesetProcessor(String name) {
        this.name = name;
    }

    public boolean execute(PsiElement element, ResolveState state) {
        if (element instanceof CssRuleset) {
            CssRuleset ruleset = (CssRuleset) element;
            CssSelectorList selectorList = ruleset.getSelectorList();
            if (selectorList.getSelectors().length == 1) {
                String text = selectorList.getText();
                if (text.startsWith(".")) {
                    text = text.substring(1);
                    if (name == null || name.equals(text)) {
                        candidates.add(ruleset);
                        if (name != null) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public CssRuleset[] getCandidates() {
        return candidates.toArray(new CssRuleset[candidates.size()]);
    }

    public <T> T getHint(Key<T> hintKey) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void handleEvent(Event event, @Nullable Object associated) {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
