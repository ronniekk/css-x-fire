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
import com.googlecode.cssxfire.resolve.CssXFireReferenceProvider;
import com.googlecode.cssxfire.tree.CssDeclarationPath;
import com.googlecode.cssxfire.tree.TreeViewModel;
import com.googlecode.cssxfire.ui.CssToolWindow;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.lang.css.CSSLanguage;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.patterns.PlatformPatterns;
import com.intellij.psi.*;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.impl.CssTreeElementFactory;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class IncomingChangesComponent implements ProjectComponent
{
    public static final String TOOLWINDOW_ID = "CSS-X-Fire";

    private final Project project;
    private final CssToolWindow cssToolWindow;

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

        CssXFireReferenceProvider provider = new CssXFireReferenceProvider();
        ReferenceProvidersRegistry.getInstance().getRegistrar(CSSLanguage.INSTANCE).registerReferenceProvider(PlatformPatterns.instanceOf(CssTreeElementFactory.CssTokenImpl.class), provider);
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

    public void processRule(final FirebugChangesBean changesBean)
    {
        DumbService.getInstance(project).smartInvokeLater(new Runnable()
        {
            public void run()
            {
                if (!project.isInitialized())
                {
                    return;
                }

                // Apply routes
                FirebugChangesBean routedChangesBean = changesBean.applyRoutes(project);

                // Get all possible candidates from the style info provided by Firebug
                final Collection<CssDeclarationPath> candidates = IncomingChangesProcessor.getProjectCandidates(project, routedChangesBean);

                // Reduce results if any of the filter options are checked
                ReduceStrategyManager.getStrategy(project, routedChangesBean).reduce(candidates);

                // Render remaining candidates in the "Incoming changes" tree view
                for (CssDeclarationPath candidate : candidates)
                {
                    cssToolWindow.getTreeModel().intersect(candidate);
                }

                if (ProjectSettings.getInstance(project).isAutoExpand())
                {
                    cssToolWindow.expandAll();
                }
            }
        });
    }

    @NotNull
    public TreeViewModel getTreeViewModel()
    {
        return cssToolWindow;
    }

    public void handleEvent(final FirebugEvent event)
    {
        DumbService.getInstance(project).smartInvokeLater(new Runnable()
        {
            public void run()
            {
                if (!project.isInitialized())
                {
                    return;
                }

                if ("refresh".equals(event.getName()) && ProjectSettings.getInstance(project).isAutoClear())
                {
                    cssToolWindow.clearTree();
                }
            }
        });
    }
}
