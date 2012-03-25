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

package com.googlecode.cssxfire.tree;

import com.googlecode.cssxfire.CssUtils;
import com.googlecode.cssxfire.StringUtils;
import com.googlecode.cssxfire.ui.Icons;
import com.intellij.ide.SelectInEditorManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssMediumList;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssSelectorNode extends CssTreeNode implements Navigatable {
    @NotNull
    private final String selector;
    @NotNull
    protected final PsiElement cssBlock;
    private static final String EMPTY_STRING = "";

    public CssSelectorNode(@NotNull String selector, @NotNull PsiElement cssBlock) {
        this.selector = selector;
        this.cssBlock = cssBlock;
    }

    @NotNull
    String getSelector() {
        return selector;
    }

    @Override
    public Icon getIcon() {
        if (selector.startsWith(".")) {
            return Icons.CSS_CLASS;
        }
        if (selector.startsWith("#")) {
            return Icons.CSS_ID;
        }
        return Icons.CSS_HTML_TAG;
    }

    @Override
    public String getText() {
        return wrapWithHtmlColor("<b>" + selector + "</b>", Color.BLACK);
    }

    @Override
    public ActionGroup getActionGroup() {
        return (ActionGroup) ActionManager.getInstance().getAction("IncomingChanges.DeclarationNodePopup.Selector");
    }

    public void navigate() {
        if (cssBlock.isValid()) {
            SelectInEditorManager selectInEditorManager = SelectInEditorManager.getInstance(cssBlock.getProject());
            VirtualFile virtualFile = cssBlock.getContainingFile().getVirtualFile();
            if (virtualFile != null) {
                TextRange textRange = cssBlock.getTextRange();
                selectInEditorManager.selectInEditor(virtualFile, textRange.getStartOffset(), textRange.getEndOffset(), false, false);
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CssSelectorNode that = (CssSelectorNode) o;

        return StringUtils.normalizeWhitespace(selector).equals(StringUtils.normalizeWhitespace(that.selector));
    }

    @Override
    public int hashCode() {
        return selector.hashCode();
    }

    @NotNull
    public String getMedia() {
        CssMediumList mediumList = CssUtils.findMediumList(cssBlock);

        return mediumList == null ? EMPTY_STRING : mediumList.getText();
    }
}
