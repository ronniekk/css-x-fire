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

import com.googlecode.cssxfire.action.Help;
import com.googlecode.cssxfire.filter.ReduceStrategyManager;
import com.googlecode.cssxfire.tree.*;
import com.googlecode.cssxfire.ui.CssToolWindow;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.*;
import com.intellij.psi.css.CssBlock;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssRulesetList;
import com.intellij.psi.css.impl.util.CssUtil;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class IncomingChangesComponent implements ProjectComponent
{
    public static final String TOOLWINDOW_ID = "CSS-X-Fire";

    private final Project project;
    private final CssToolWindow cssToolWindow;
    private final AtomicBoolean fileReduce = new AtomicBoolean(CssXFireConnector.getInstance().getState().isSmartReduce());
    private final AtomicBoolean mediaReduce = new AtomicBoolean(CssXFireConnector.getInstance().getState().isMediaReduce());

    private final PsiTreeChangeListener myListener = new PsiTreeChangeAdapter()
    {
        @Override
        public void childReplaced(PsiTreeChangeEvent event)
        {
            IncomingChangesComponent.this.onPsiChange(event);
        }

        @Override
        public void childRemoved(PsiTreeChangeEvent event)
        {
            IncomingChangesComponent.this.onPsiChange(event);
        }
    };

    private void onPsiChange(PsiTreeChangeEvent event)
    {
        if (event.getOldChild() instanceof CssDeclaration || event.getParent() instanceof CssDeclaration)
        {
            cssToolWindow.refreshLeafs();
        }
    }

    public IncomingChangesComponent(Project project)
    {
        this.project = project;
        this.cssToolWindow = new CssToolWindow(project);
    }

    /**
     * Helper
     * @param project the project
     * @return the IncomingChangesComponent instance
     */
    public static IncomingChangesComponent getInstance(Project project)
    {
        return project.getComponent(IncomingChangesComponent.class);
    }

    @NotNull
    public AtomicBoolean getFileReduce()
    {
        return fileReduce;
    }

    @NotNull
    public AtomicBoolean getMediaReduce()
    {
        return mediaReduce;
    }

    public void initComponent()
    {
        if (!CssXFireConnector.getInstance().isInitialized())
        {
            return;
        }
        IdeaPluginDescriptor pluginDescriptor = PluginManager.getPlugin(PluginId.getId("CSS-X-Fire"));
        if (pluginDescriptor == null)
        {
            return;
        }
        String currentVersion = pluginDescriptor.getVersion();
        AppMeta appMeta = CssXFireConnector.getInstance().getState();
        String previousVersion = appMeta.getVersion();
        if (!currentVersion.equals(previousVersion))
        {
            appMeta.setVersion(currentVersion);
            final String message = previousVersion == null
                    ? "CSS-X-Fire has been installed.\n\nPress Yes to install the browser plugin."
                    : "CSS-X-Fire has been upgraded from " + previousVersion + " to " + currentVersion + ".\n\nPress Yes to update the browser plugin.";
            ApplicationManager.getApplication().invokeLater(new Runnable()
            {
                public void run()
                {
                    int res = Messages.showYesNoDialog(project, message, "CSS-X-Fire", null);
                    if (res == 0)
                    {
                        new Help().actionPerformed(null);
                    }
                }
            });
        }
    }

    public void disposeComponent()
    {
        // TODO: insert component disposal logic here
    }

    @NotNull
    public String getComponentName()
    {
        return "IncomingChangesComponent";
    }

    public void projectOpened()
    {
        if (!CssXFireConnector.getInstance().isInitialized())
        {
            return;
        }
        final ToolWindow toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(TOOLWINDOW_ID, true, ToolWindowAnchor.BOTTOM);

        final ContentFactory contentFactory = toolWindow.getContentManager().getFactory();
        final Content content = contentFactory.createContent(cssToolWindow, "Incoming changes", true);

        toolWindow.getContentManager().addContent(content);
        toolWindow.setAutoHide(false);
        toolWindow.setAvailable(true, null);

        CssXFireConnector.getInstance().addProjectComponent(this);

        PsiManager.getInstance(project).addPsiTreeChangeListener(myListener);
    }

    public void projectClosed()
    {
        if (!CssXFireConnector.getInstance().isInitialized())
        {
            return;
        }
        PsiManager.getInstance(project).removePsiTreeChangeListener(myListener);

        getTreeViewModel().clearTree();

        CssXFireConnector.getInstance().removeProjectComponent(this);

        ToolWindowManager.getInstance(project).unregisterToolWindow(TOOLWINDOW_ID);
    }

    public void processRule(final String media, final String href, final String selector, final String property, final String value, final boolean deleted)
    {
        final String filename = StringUtils.extractFilename(href);
        
        DumbService.getInstance(project).smartInvokeLater(new Runnable()
        {
            public void run()
            {
                if (!project.isInitialized())
                {
                    return;
                }

                final GlobalSearchScope searchScope = GlobalSearchScope.projectScope(project);
                final short searchContext = UsageSearchContext.ANY;

                // find possible media query targets with its own processor
                Set<PsiElement> mediaCandidates = findCandidateMediaLists(project, media, searchScope, searchContext);

                // find possible file targets with its own search
                Set<PsiFile> fileCandidates = findCandidateFiles(project, filename, searchScope);

                // search for existing selectors
                CssSelectorSearchProcessor selectorProcessor = new CssSelectorSearchProcessor(selector);
                PsiManager.getInstance(project).getSearchHelper().processElementsWithWord(selectorProcessor,searchScope, selectorProcessor.getSearchWord(), searchContext, true);

                final List<CssDeclarationPath> candidates = new ArrayList<CssDeclarationPath>();
                for (CssBlock block : selectorProcessor.getBlocks())
                {
                    boolean hasDeclaration = false;
                    CssDeclaration[] declarations = PsiTreeUtil.getChildrenOfType(block, CssDeclaration.class);
                    if (declarations != null)
                    {
                        for (CssDeclaration declaration : declarations)
                        {
                            if (property.equals(declaration.getPropertyName()))
                            {
                                hasDeclaration = true;

                                CssDeclarationNode declarationNode = new CssDeclarationNode(declaration, value, deleted);
                                CssSelectorNode selectorNode = new CssSelectorNode(selector, block);
                                CssFileNode fileNode = new CssFileNode(declaration.getContainingFile().getOriginalFile());

                                candidates.add(new CssDeclarationPath(fileNode, selectorNode, declarationNode));

                            }
                        }
                    }
                    if (!hasDeclaration)
                    {
                        // non-existing - create new
                        CssDeclaration declaration = CssUtils.createDeclaration(project, selector, property, value);
                        CssDeclarationNode declarationNode = CssNewDeclarationNode.forDestination(declaration, block, deleted);
                        CssSelectorNode selectorNode = new CssSelectorNode(selector, block);
                        CssFileNode fileNode = new CssFileNode(block.getContainingFile().getOriginalFile());

                        candidates.add(new CssDeclarationPath(fileNode, selectorNode, declarationNode));
                    }

                    deleteCandidate(fileCandidates, block.getContainingFile().getOriginalFile());
                    deleteCandidate(mediaCandidates, CssUtil.getMediumList(block));
                }

                // add candidates from remaining media candidates
                for (PsiElement mediaCandidate : mediaCandidates)
                {
                    // remove from collected files
                    deleteCandidate(fileCandidates, mediaCandidate.getContainingFile().getOriginalFile());

                    CssDeclaration declaration = CssUtils.createDeclaration(project, selector, property, value);
                    CssDeclarationNode declarationNode = CssNewDeclarationNode.forDestination(declaration, mediaCandidate, deleted);
                    CssSelectorNode selectorNode = new CssSelectorNode(selector, mediaCandidate);
                    CssFileNode fileNode = new CssFileNode(mediaCandidate.getContainingFile().getOriginalFile());

                    candidates.add(new CssDeclarationPath(fileNode, selectorNode, declarationNode));
                }

                // add candidate paths for remaining files
                for (PsiFile fileCandidate : fileCandidates)
                {
                    CssRulesetList rulesetList = CssUtils.findFirstCssRulesetList(fileCandidate);
                    if (rulesetList != null)
                    {
                        CssDeclaration declaration = CssUtils.createDeclaration(project, selector, property, value);
                        CssDeclarationNode declarationNode = CssNewDeclarationNode.forDestination(declaration, rulesetList, deleted);
                        CssSelectorNode selectorNode = new CssSelectorNode(selector, rulesetList);
                        CssFileNode fileNode = new CssFileNode(fileCandidate);

                        candidates.add(new CssDeclarationPath(fileNode, selectorNode, declarationNode));
                    }
                }

                // Reduce results if any of the filter options are checked
                ReduceStrategyManager.getStrategy(project, filename, media).reduce(candidates);

                for (CssDeclarationPath candidate : candidates)
                {
                    cssToolWindow.getTreeModel().intersect(candidate);
                }
            }
        });
    }

    @NotNull
    public TreeViewModel getTreeViewModel()
    {
        return cssToolWindow;
    }

    @NotNull
    private Set<PsiElement> findCandidateMediaLists(@NotNull Project project, @Nullable String media, @NotNull GlobalSearchScope searchScope, short searchContext)
    {
        final Set<PsiElement> elements = new HashSet<PsiElement>();
        if (media != null && media.length() > 0)
        {
            CssMediaSearchProcessor mediaProcessor = new CssMediaSearchProcessor(media);
            PsiSearchHelper helper = PsiManager.getInstance(project).getSearchHelper();
            helper.processElementsWithWord(mediaProcessor, searchScope, mediaProcessor.getSearchWord(), searchContext, true);
            elements.addAll(mediaProcessor.getMediaLists());
        }
        return elements;
    }

    @NotNull
    private Set<PsiFile> findCandidateFiles(@NotNull Project project, @Nullable String filename, @NotNull GlobalSearchScope searchScope)
    {
        final Set<PsiFile> files = new HashSet<PsiFile>();
        if (filename != null && filename.length() > 0)
        {
            files.addAll(Arrays.asList(FilenameIndex.getFilesByName(project, filename, searchScope)));
        }
        return files;
    }

    private <T> void deleteCandidate(@NotNull Collection<T> collection, @Nullable T object)
    {
        if (object != null)
        {
            collection.remove(object);
        }
    }
}
