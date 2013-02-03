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

import com.intellij.psi.PsiElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.plugins.scss.psi.SCSSMixinDeclaration;

/**
 * This class will only be loaded if the SASS plugin is active. See {@link CssPluginsFacade#getMixinProcessor(com.intellij.psi.PsiElement, String)}
 * <br><br>
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class ScssMixinProcessor extends CssResolveProcessor {
    public ScssMixinProcessor(String name) {
        super(name);
    }

    @Override
    public boolean executeInScope(@NotNull PsiElement base) {
        return super.executeInScope(base);
    }

    @Override
    public boolean executeInternal(@NotNull PsiElement element) {
        if (element.getParent() instanceof SCSSMixinDeclaration) {
            SCSSMixinDeclaration scssMixinDeclaration = (SCSSMixinDeclaration) element.getParent();
            String scssMixinDeclarationName = scssMixinDeclaration.getName();
            if (scssMixinDeclarationName.equals(name)) {
                if (CssResolveUtils.containsErrors(scssMixinDeclaration)) {
                    // Error in PSI tree - skip
                    return true;
                }
                result.set(scssMixinDeclaration);
                return false;
            }
        }
        return true;
    }
}
