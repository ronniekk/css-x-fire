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
import com.googlecode.cssxfire.tree.CssDeclarationNode;
import com.googlecode.cssxfire.tree.CssFileNode;
import com.googlecode.cssxfire.tree.CssNewDeclarationNode;
import com.googlecode.cssxfire.tree.CssSelectorNode;
import com.googlecode.cssxfire.tree.TreeModificator;
import com.googlecode.cssxfire.ui.CssToolWindow;
import com.intellij.ide.plugins.IdeaPluginDescriptor;
import com.intellij.ide.plugins.PluginManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowAnchor;
import com.intellij.openapi.wm.ToolWindowManager;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiTreeChangeAdapter;
import com.intellij.psi.PsiTreeChangeEvent;
import com.intellij.psi.PsiTreeChangeListener;
import com.intellij.psi.css.CssBlock;
import com.intellij.psi.css.CssDeclaration;
import com.intellij.psi.css.CssElement;
import com.intellij.psi.css.CssRuleset;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiSearchHelper;
import com.intellij.psi.search.UsageSearchContext;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class IncomingChangesComponent implements ProjectComponent
{
    public static final String TOOLWINDOW_ID = "CSS-X-Fire";

    private final Project project;
    private CssToolWindow cssToolWindow;
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
                    ? "CSS-X-Fire has been installed.\n\nPress OK to install the browser plugin."
                    : "CSS-X-Fire has been upgraded from " + previousVersion + " to " + currentVersion + ".\n\nPress OK to update the browser plugin.";
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
        final ToolWindow toolWindow = ToolWindowManager.getInstance(project).registerToolWindow(TOOLWINDOW_ID, true, ToolWindowAnchor.BOTTOM);
        cssToolWindow = new CssToolWindow(project);

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
        PsiManager.getInstance(project).removePsiTreeChangeListener(myListener);

        getTreeModificator().clearTree();

        CssXFireConnector.getInstance().removeProjectComponent(this);

        ToolWindowManager.getInstance(project).unregisterToolWindow(TOOLWINDOW_ID);
    }

    public void processRule(final String selector, final String property, final String value, final boolean deleted)
    {
        ApplicationManager.getApplication().invokeLater(new Runnable()
        {
            public void run()
            {
                PsiSearchHelper helper = PsiManager.getInstance(project).getSearchHelper();
                CssSelectorSearchProcessor processor = new CssSelectorSearchProcessor(selector);

                helper.processElementsWithWord(processor,
                        GlobalSearchScope.projectScope(project),
                        selector,
                        UsageSearchContext.ANY,
                        true);

                for (CssElement result : processor.getResults())
                {
                    CssRuleset ruleSet = PsiTreeUtil.getParentOfType(result, CssRuleset.class);
                    if (ruleSet != null)
                    {
                        CssBlock block = ruleSet.getBlock();
                        if (block != null)
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

                                        CssDeclarationNode declarationNode = new CssDeclarationNode(declaration, value);
                                        if (deleted)
                                        {
                                            declarationNode.markDeleted();
                                        }
                                        CssSelectorNode selectorNode = new CssSelectorNode(selector);
                                        CssFileNode fileNode = new CssFileNode(declaration.getContainingFile().getOriginalFile());

                                        cssToolWindow.getTreeModel().intersect(fileNode, selectorNode, declarationNode);
                                    }
                                }
                            }
                            if (!hasDeclaration)
                            {
                                // non-existing - create new
                                CssDeclaration declaration = CssUtils.createDeclaration(project, selector, property, value);
                                CssDeclarationNode declarationNode = new CssNewDeclarationNode(declaration, block);
                                if (deleted)
                                {
                                    declarationNode.markDeleted();
                                }
                                CssSelectorNode selectorNode = new CssSelectorNode(selector);
                                CssFileNode fileNode = new CssFileNode(block.getContainingFile().getOriginalFile());

                                cssToolWindow.getTreeModel().intersect(fileNode, selectorNode, declarationNode);
                            }
                        }
                    }

                }
            }
        });
    }

    @NotNull
    public TreeModificator getTreeModificator()
    {
        return cssToolWindow;
    }

}
