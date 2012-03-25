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
public class CssChangesTreeModel extends DefaultTreeModel {
    private static final TreeCellRenderer myTreeCellRenderer = new MyTreeCellRenderer();

    public CssChangesTreeModel(Project project) {
        super(new CssRootNode(project), true);
    }

    public TreeCellRenderer getTreeCellRenderer() {
        return myTreeCellRenderer;
    }

    public void intersect(CssDeclarationPath declarationPath) {
        CssTreeNode rootNode = (CssTreeNode) getRoot();
        addAbsent(rootNode, declarationPath.getPathFromRoot());
    }

    private void addAbsent(CssTreeNode parent, CssTreeNode[] nodes) {
        if (nodes.length == 0) {
            return;
        }

        CssTreeNode currentNode = nodes[0];

        int numChildren = parent.getChildCount();
        for (int i = 0; i < numChildren; i++) {
            CssTreeNode child = (CssTreeNode) parent.getChildAt(i);
            if (child.equals(currentNode)) {
                if (currentNode instanceof CssDeclarationNode) {
                    if (isNewAndDeletedDeclaration(currentNode)) {
                        // remove node and all empty parents
                        removeWithEmptyParents(child);
                    } else {
                        // swap nodes
                        removeChildAndFireEvent(parent, child);
                        addChildAndFireEvent(parent, currentNode);
                    }
                    return;
                }
                addAbsent(child, consumeFirst(nodes));
                return;
            }
        }

        if (isNewAndDeletedDeclaration(currentNode)) {
            if (parent.getChildCount() == 0) {
                removeWithEmptyParents(parent);
            }
        } else {
            addChildAndFireEvent(parent, currentNode);
            addAbsent(currentNode, consumeFirst(nodes));
        }
    }

    private void removeWithEmptyParents(@NotNull CssTreeNode child) {
        CssTreeNode parent = (CssTreeNode) child.getParent();
        do {
            removeChildAndFireEvent(parent, child);
            child = parent;
            parent = (CssTreeNode) child.getParent();
        }
        while (parent != null && child.getChildCount() == 0);
    }

    /**
     * Removes <tt>child</tt> from <tt>parent</tt> and notifies listeners.
     *
     * @param parent the parent
     * @param child  the existing child to remove
     */
    private void removeChildAndFireEvent(CssTreeNode parent, CssTreeNode child) {
        int index = parent.getIndex(child);
        parent.remove(child);
        nodesWereRemoved(parent, new int[]{index}, new CssTreeNode[]{child});
        if (child instanceof CssDeclarationNode) {
            // notify that file node is changed (update the number of changes in file)
            nodeChanged(parent.getParent());
        }
    }

    /**
     * Adds <tt>child</tt> to <tt>parent</tt> and notifies listeners.
     *
     * @param parent the parent
     * @param child  the new child
     */
    private void addChildAndFireEvent(CssTreeNode parent, CssTreeNode child) {
        parent.add(child);
        nodesWereInserted(parent, new int[]{parent.getIndex(child)});
        if (child instanceof CssDeclarationNode) {
            // notify that file node is changed (update the number of changes in file)
            nodeChanged(parent.getParent());
        }
    }

    private boolean isNewAndDeletedDeclaration(DefaultMutableTreeNode node) {
        return node instanceof CssNewDeclarationNode && ((CssNewDeclarationNode) node).isDeleted();
    }

    private CssTreeNode[] consumeFirst(CssTreeNode[] nodes) {
        CssTreeNode[] rest = new CssTreeNode[nodes.length - 1];
        System.arraycopy(nodes, 1, rest, 0, rest.length);
        return rest;
    }

    private static class MyTreeCellRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

            if (value instanceof CssTreeNode) {
                CssTreeNode cssTreeNode = (CssTreeNode) value;
                setIcon(cssTreeNode.getIcon());
                setText(cssTreeNode.getText());
            }

            return this;
        }
    }
}
