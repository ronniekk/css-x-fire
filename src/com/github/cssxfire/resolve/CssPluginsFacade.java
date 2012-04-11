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

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssPluginsFacade {
    private static final FileType LESS_FILE_TYPE = FileTypeManager.getInstance().getStdFileType("LESS");
    private static final FileType SASS_FILE_TYPE = FileTypeManager.getInstance().getStdFileType("SASS");
    private static final FileType SCSS_FILE_TYPE = FileTypeManager.getInstance().getStdFileType("SCSS");
    private static final CssResolveProcessor NOP_PROCESSOR = new CssResolveProcessor(null) {
        @Override
        public boolean executeInternal(@NotNull PsiElement element) {
            return false;
        }
    };

    @NotNull
    public static CssResolveProcessor getVariableProcessor(@NotNull PsiElement element, String name) {
        return createResolveProcessor(element, name, "Variable");
    }
    
    @NotNull
    public static CssResolveProcessor getMixinProcessor(@NotNull PsiElement element, String name) {
        return createResolveProcessor(element, name, "Mixin");
    }
    
    @NotNull
    private static CssResolveProcessor createResolveProcessor(@NotNull PsiElement element, String name, String type) {
        PsiFile file = element.getContainingFile();
        if (file == null) {
            return NOP_PROCESSOR;
        }
        
        FileType fileType = file.getFileType();
        if (fileType == PlainTextFileType.INSTANCE) {
            return NOP_PROCESSOR;
        }

        String prefix = null;
        if (fileType == LESS_FILE_TYPE) {
            prefix = "Less";
        } else if (fileType == SCSS_FILE_TYPE) {
            prefix = "Scss";
        }

        if (prefix != null) {
            try {
                Class<? extends CssResolveProcessor> clazz = (Class<? extends CssResolveProcessor>) Class.forName(CssPluginsFacade.class.getPackage().getName() + "." + prefix + type + "Processor");
                Constructor<? extends CssResolveProcessor> constructor = clazz.getConstructor(String.class);
                return constructor.newInstance(name);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return NOP_PROCESSOR;
    }
}
