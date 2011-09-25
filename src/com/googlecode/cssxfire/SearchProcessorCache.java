/*
 * Copyright 2011 Ronnie Kolehmainen
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

import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.PsiTreeChangeListener;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Project search cache.
 */
public class SearchProcessorCache implements ProjectComponent
{
    private final Map<String, CssSelectorSearchProcessor> selectorProcessorCache = new ConcurrentHashMap<String, CssSelectorSearchProcessor>();
    private final Map<String, CssMediaSearchProcessor> mediaProcessorCache = new ConcurrentHashMap<String, CssMediaSearchProcessor>();

    private final GlobalSearchScope searchScope;
    private final short searchContext = UsageSearchContext.ANY;
    private final Project project;

    public SearchProcessorCache(Project project)
    {
        this.project = project;
        this.searchScope = GlobalSearchScope.projectScope(project);
    }

    /**
     * Helper
     * @param project the project
     * @return the {@link com.googlecode.cssxfire.SearchProcessorCache} instance tied to the project
     */
    public static SearchProcessorCache getInstance(Project project)
    {
        return project.getComponent(SearchProcessorCache.class);
    }

    private void clearCaches()
    {
        selectorProcessorCache.clear();
        mediaProcessorCache.clear();
    }

    /**
     * Gets a new or cached search processor for given selector. In either case the returned processor has
     * been processed with candidates in the project.
     * @param selector the selector to search for
     * @return a {@link com.googlecode.cssxfire.CssSelectorSearchProcessor} instance
     */
    @NotNull
    public CssSelectorSearchProcessor getSelectorSearchProcessor(@NotNull String selector)
    {
        CssSelectorSearchProcessor selectorProcessor = selectorProcessorCache.get(selector);
        if (selectorProcessor != null)
        {
            return selectorProcessor;
        }

        selectorProcessor = new CssSelectorSearchProcessor(selector);
        PsiSearchHelper helper = CssUtils.getPsiSearchHelper(project);
        helper.processElementsWithWord(selectorProcessor, searchScope, selectorProcessor.getSearchWord(), searchContext, true);

        selectorProcessorCache.put(selector, selectorProcessor);

        return selectorProcessor;
    }

    /**
     * Gets a new or cached search processor for given media query. In either case the returned processor has
     * been processed with candidates in the project.
     * @param media the media query to search for
     * @return a {@link com.googlecode.cssxfire.CssMediaSearchProcessor} instance
     */
    @NotNull
    public CssMediaSearchProcessor getMediaSearchProcessor(@NotNull String media)
    {
        CssMediaSearchProcessor mediaProcessor = mediaProcessorCache.get(media);
        if (mediaProcessor != null)
        {
            return mediaProcessor;
        }

        mediaProcessor = new CssMediaSearchProcessor(media);
        PsiSearchHelper helper = CssUtils.getPsiSearchHelper(project);
        helper.processElementsWithWord(mediaProcessor, searchScope, mediaProcessor.getSearchWord(), searchContext, true);

        mediaProcessorCache.put(media, mediaProcessor);

        return mediaProcessor;
    }

    public void projectOpened()
    {
        // Attach cache invalidator
        PsiManager.getInstance(project).addPsiTreeChangeListener(myCacheInvalidator);
    }

    public void projectClosed()
    {
        // Detach cache invalidator
        PsiManager.getInstance(project).removePsiTreeChangeListener(myCacheInvalidator);
        clearCaches();
    }

    public void initComponent()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void disposeComponent()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @NotNull
    public String getComponentName()
    {
        return getClass().getSimpleName();
    }

    /**
     * Clears caches on any PSI change
     */
    private PsiTreeChangeListener myCacheInvalidator = new PsiTreeChangeAdapter()
    {
        @Override
        public void childAdded(PsiTreeChangeEvent event)
        {
            SearchProcessorCache.this.clearCaches();
        }

        @Override
        public void childRemoved(PsiTreeChangeEvent event)
        {
            SearchProcessorCache.this.clearCaches();
        }

        @Override
        public void childReplaced(PsiTreeChangeEvent event)
        {
            SearchProcessorCache.this.clearCaches();
        }

        @Override
        public void childMoved(PsiTreeChangeEvent event)
        {
            SearchProcessorCache.this.clearCaches();
        }

        @Override
        public void childrenChanged(PsiTreeChangeEvent event)
        {
            SearchProcessorCache.this.clearCaches();
        }

        @Override
        public void propertyChanged(PsiTreeChangeEvent event)
        {
            SearchProcessorCache.this.clearCaches();
        }
    };
}
