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
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.search.TextOccurenceProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssMediaSearchProcessor implements TextOccurenceProcessor
{
    private final Set<PsiElement> mediaLists = new HashSet<PsiElement>();
    private @NotNull String media;
    private @NotNull String word;

    public CssMediaSearchProcessor(@NotNull String media)
    {
        this.media = StringUtils.normalizeWhitespace(media);
        this.word = StringUtils.extractSearchWord(this.media);
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


    public boolean execute(PsiElement element, int offsetInElement)
    {
        // CssMediumListImpl mediumList = element.getParent()
        PsiElement mediumList = CssUtil.getMediumList(element);
        if (mediumList != null)
        {
            // CssMediaImpl mediaImpl = mediumList.getParent()
            String text = mediumList.getText();
            if (media.equals(StringUtils.normalizeWhitespace(text)))
            {
                mediaLists.add(mediumList);
            }
        }
        return false;
    }

    public Set<PsiElement> getMediaLists()
    {
        return new HashSet<PsiElement>(mediaLists);
    }
}
