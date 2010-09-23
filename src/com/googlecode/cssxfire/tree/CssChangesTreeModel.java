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

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssChangesTreeModel extends DefaultTreeModel
{
    private static final TreeCellRenderer myTreeCellRenderer = new MyTreeCellRenderer();

    public CssChangesTreeModel(Project project)
    {
        super(new CssRootNode(project), true);
    }

    public TreeCellRenderer getTreeCellRenderer()
    {
        return myTreeCellRenderer;
    }

    public void intersect(CssFileNode fileNode, CssSelectorNode selectorNode, CssDeclarationNode declarationNode)
    {
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) getRoot();
        addAbsent(rootNode, new DefaultMutableTreeNode[] {fileNode, selectorNode, declarationNode});
        nodeStructureChanged(rootNode);
    }

    private void addAbsent(DefaultMutableTreeNode parent, DefaultMutableTreeNode[] nodes)
    {
        if (nodes.length == 0)
        {
            return;
        }

        DefaultMutableTreeNode currentNode = nodes[0];

        int numChildren = parent.getChildCount();
        for (int i = 0; i < numChildren; i++)
        {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) parent.getChildAt(i);
            if (child.equals(currentNode))
            {
                if (currentNode instanceof CssDeclarationNode)
                {
                    if (isNewAndDeletedDeclaration(currentNode))
                    {
                        // remove node and all empty parents
                        removeWithEmptyParents(child);
                    }
                    else
                    {
                        // swap nodes
                        parent.remove(child);
                        parent.add(currentNode);
                    }
                    return;
                }
                addAbsent(child, consumeFirst(nodes));
                return;
            }
        }

        if (isNewAndDeletedDeclaration(currentNode))
        {
            if (parent.getChildCount() == 0)
            {
                removeWithEmptyParents(parent);
            }
        }
        else
        {
            parent.add(currentNode);
            addAbsent(currentNode, consumeFirst(nodes));
        }
    }

    private void removeWithEmptyParents(@NotNull DefaultMutableTreeNode child)
    {
        DefaultMutableTreeNode parent = (DefaultMutableTreeNode) child.getParent();
        do
        {
            child.removeFromParent();
            child = parent;
            parent = (DefaultMutableTreeNode) child.getParent();
        }
        while (parent != null && child.getChildCount() == 0);
    }

    private boolean isNewAndDeletedDeclaration(DefaultMutableTreeNode node)
    {
        return node instanceof CssNewDeclarationNode && ((CssNewDeclarationNode) node).isDeleted();
    }

    private DefaultMutableTreeNode[] consumeFirst(DefaultMutableTreeNode[] nodes)
    {
        DefaultMutableTreeNode[] rest = new DefaultMutableTreeNode[nodes.length - 1];
        System.arraycopy(nodes, 1, rest, 0, rest.length);
        return rest;
    }

    private static class MyTreeCellRenderer extends DefaultTreeCellRenderer
    {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus)
        {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (value instanceof CssTreeNode)
            {
                CssTreeNode cssTreeNode = (CssTreeNode) value;
                setIcon(cssTreeNode.getIcon());
                setText(cssTreeNode.getText());
            }

            return this;
        }
    }
}
