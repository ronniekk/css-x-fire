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

import com.github.cssxfire.CssUtils;
import com.intellij.codeInsight.navigation.actions.GotoDeclarationHandler;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.xml.XmlToken;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class GotoDeclarationResolver implements GotoDeclarationHandler {
    public static final GotoDeclarationResolver INSTANCE = new GotoDeclarationResolver();
    public static final PsiElement[] EMPTY_TARGETS = new PsiElement[0];

    public PsiElement[] getGotoDeclarationTargets(PsiElement element, Editor editor) {
        return getGotoDeclarationTargets(element, 0, editor);
    }

    public PsiElement[] getGotoDeclarationTargets(PsiElement element, int i, Editor editor) {
        if (CssUtils.isDynamicCssLanguage(element)) {
            if (element instanceof XmlToken) {
                PsiElement parent = element.getParent();
                if (parent != null) {
                    String text = element.getText();
                    if (text.startsWith("$") || text.startsWith("@")) {
                        // Scss & Less variables
                        PsiReference[] references = parent.getReferences();
                        if (references.length == 1){// && references[0].resolve() == null) {
                            PsiElement resolved = CssResolveUtils.resolveVariable(element, text);
                            System.out.println("resolved = " + resolved);
                            if (resolved != null) {
                                return new PsiElement[] {resolved};
                            }
                        }
                    } else {
                        if (parent.getText().startsWith("@include")) {
                            // Scss mixin
                            PsiElement resolved = CssResolveUtils.resolveMixin(element, text);
                            System.out.println("resolved = " + resolved);
                            if (resolved != null) {
                                return new PsiElement[]{resolved};
                            }
                        } else {
                            PsiElement prevSibling = element.getPrevSibling();
                            if (prevSibling != null) {
                                text = prevSibling.getText() + text;
                                if (text.startsWith(".") || text.startsWith("#")) {
                                    // Less mixin
                                    PsiElement resolved = CssResolveUtils.resolveMixin(element, text);
                                    System.out.println("resolved = " + resolved);
                                    if (resolved != null) {
                                        return new PsiElement[]{resolved};
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return EMPTY_TARGETS;
    }

    public String getActionText(DataContext dataContext) {
        return "My action text";
    }
}
