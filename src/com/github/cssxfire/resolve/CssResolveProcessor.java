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

import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiElement;
import com.intellij.psi.css.CssImport;
import com.intellij.psi.search.PsiElementProcessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public abstract class CssResolveProcessor implements PsiElementProcessor {
    protected final String name;
    protected final Ref<PsiElement> result = new Ref<PsiElement>(null);
    private final Set<CssImport> imports = new HashSet<CssImport>();

    protected CssResolveProcessor(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Nullable
    public PsiElement getResult() {
        return result.get();
    }

    @Nullable
    public synchronized CssImport popImport() {
        if (imports.isEmpty()) {
            return null;
        }
        Iterator<CssImport> iterator = imports.iterator();
        CssImport next = iterator.next();
        iterator.remove();
        return next;
    }

    public boolean executeInScope(@NotNull PsiElement base) {
        return true;
    }
    
    public abstract boolean executeInternal(@NotNull PsiElement element);

    public boolean execute(@NotNull PsiElement element) {
        if (element instanceof CssImport) {
            CssImport cssImport = (CssImport) element;
            imports.add(cssImport);
            return true;
        }
        return executeInternal(element);
    }
}
