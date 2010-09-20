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
import com.googlecode.cssxfire.tree.*;
import com.googlecode.cssxfire.ui.CssToolWindow;
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
        String currentVersion = PluginManager.getPlugin(PluginId.getId("CSS-X-Fire")).getVersion();
        AppMeta appMeta = CssXFireConnector.getInstance().getState();
        if (!currentVersion.equals(appMeta.getVersion()))
        {
            appMeta.setVersion(currentVersion);
            ApplicationManager.getApplication().invokeLater(new Runnable()
            {
                public void run()
                {
                    int res = Messages.showYesNoDialog(project, "This is the first run after installation or upgrade of CSS-X-Fire.\n\nWould you like to view the help page?", "CSS-X-Fire", null);
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
    }

    public void projectClosed()
    {
        CssXFireConnector.getInstance().removeProjectComponent(this);

        ToolWindowManager.getInstance(project).unregisterToolWindow(TOOLWINDOW_ID);
    }

    public void processRule(final String selector, final String property, final String value)
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
