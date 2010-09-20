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

package com.googlecode.cssxfire.ui;

import com.googlecode.cssxfire.IncomingChangesComponent;
import com.googlecode.cssxfire.tree.*;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssToolWindow extends JPanel implements TreeModelListener, TreeModificator
{
    private final CssChangesTreeModel treeModel;
    private final JTree tree;
    private JButton clearButton, applyButton;
    private final Project project;

    public CssToolWindow(final Project project)
    {
        this.project = project;
        this.treeModel = new CssChangesTreeModel(project);
        
        setLayout(new BorderLayout());

        ActionGroup toolbarGroup = (ActionGroup) ActionManager.getInstance().getAction("IncomingChanges.ToolBar");
        ActionToolbar actionToolbar = ActionManager.getInstance().createActionToolbar(IncomingChangesComponent.TOOLWINDOW_ID, toolbarGroup, false);

        JToolBar toolBar = new JToolBar(SwingConstants.VERTICAL);
        toolBar.add(actionToolbar.getComponent());

        JPanel incomingChangesPanel = new JPanel(new BorderLayout(5, 5));

        tree = new JTree(treeModel);
        tree.setBorder(new EmptyBorder(3, 3, 3, 3));
        // tree.setRootVisible(false);
        tree.setCellRenderer(treeModel.getTreeCellRenderer());
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        tree.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3)
                {
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());

                    if (selPath != null)
                    {
                        tree.setSelectionPath(selPath);
                        Object source = selPath.getLastPathComponent();
                        ActionGroup actionGroup = source instanceof CssTreeNode ? ((CssTreeNode) source).getActionGroup() : null;

                        if (actionGroup != null)
                        {
                            DataContext dataContext = DataManager.getInstance().getDataContext(tree);
                            ListPopup listPopup = JBPopupFactory.getInstance().createActionGroupPopup(null,
                                    actionGroup,
                                    dataContext,
                                    JBPopupFactory.ActionSelectionAid.MNEMONICS,
                                    true);

                            Point point = new Point(e.getXOnScreen(), e.getYOnScreen());
                            listPopup.showInScreenCoordinates(tree, point);
                        }
                    }
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(tree);

        incomingChangesPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        clearButton = new JButton("Clear list", Icons.TRASHCAN);
        clearButton.setEnabled(false);
        clearButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                clearTree();
            }
        });
        applyButton = new JButton("Apply all changes", Icons.COMMIT);
        applyButton.setEnabled(false);
        applyButton.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                applyPending();
            }
        });
        southPanel.add(clearButton);
        southPanel.add(applyButton);

        add(toolBar, BorderLayout.WEST);
        add(incomingChangesPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        treeModel.addTreeModelListener(this);
    }

    public CssChangesTreeModel getTreeModel()
    {
        return treeModel;
    }

    private void updateButtons()
    {
        boolean hasChildren = ((CssTreeNode) treeModel.getRoot()).getChildCount() > 0;
        clearButton.setEnabled(hasChildren);
        applyButton.setEnabled(hasChildren);
    }

    private void clearTree()
    {
        CssTreeNode root = (CssTreeNode) treeModel.getRoot();
        root.removeAllChildren();
        treeModel.nodeStructureChanged(root);
    }

    private void deleteNode(CssTreeNode node)
    {
        CssTreeNode parent = (CssTreeNode) node.getParent();
        if (parent != null)
        {
            node.removeFromParent();
            treeModel.nodeStructureChanged(parent);
            if (parent.getChildCount() == 0)
            {
                deleteNode(parent);
            }
        }
    }

    /**
     * Executes a runnable in a write action. The command may be undo'ed.
     * @param command the command
     */
    private void executeCommand(final Runnable command)
    {
        CommandProcessor.getInstance().executeCommand(project, new Runnable()
        {
            public void run()
            {
                ApplicationManager.getApplication().runWriteAction(command);
            }
        }, "Apply CSS", "CSS");
    }

    //
    // TreeModificator
    //

    public void applyPending()
    {
        executeCommand(new Runnable()
        {
            public void run()
            {
                CssTreeNode root = (CssTreeNode) treeModel.getRoot();
                CssTreeNode leaf;

                while ((leaf = (CssTreeNode) root.getFirstLeaf()) != null)
                {
                    if (leaf.isRoot())
                    {
                        // eventually getFirstLeaf() will return the root itself
                        break;
                    }
                    if (leaf instanceof CssDeclarationNode)
                    {
                        CssDeclarationNode declarationNode = (CssDeclarationNode) leaf;
                        declarationNode.applyToCode();
                    }
                    treeModel.removeNodeFromParent(leaf);
                }

                treeModel.nodeStructureChanged(root);
            }
        });
    }

    public void applySelectedNode()
    {
        Object source = tree.getSelectionPath().getLastPathComponent();
        if (source instanceof CssFileNode || source instanceof CssSelectorNode)
        {
            final Collection<CssDeclarationNode> declarations = new ArrayList<CssDeclarationNode>();
            for (CssTreeNode leaf : TreeUtils.iterateLeafs((CssTreeNode) source))
            {
                if (leaf instanceof CssDeclarationNode)
                {
                    declarations.add((CssDeclarationNode) leaf);
                }
            }
            executeCommand(new Runnable()
            {
                public void run()
                {
                    for (CssDeclarationNode declarationNode : declarations)
                    {
                        declarationNode.applyToCode();
                        deleteNode(declarationNode);
                    }
                }
            });
        }
        else if (source instanceof CssDeclarationNode)
        {
            final CssDeclarationNode declarationNode = (CssDeclarationNode) source;
            executeCommand(new Runnable()
            {
                public void run()
                {
                    declarationNode.applyToCode();
                    deleteSelectedNode();
                }
            });
        }
    }

    public void deleteSelectedNode()
    {
        Object source = tree.getSelectionPath().getLastPathComponent();
        if (source instanceof CssFileNode || source instanceof CssSelectorNode)
        {
            final Collection<CssDeclarationNode> declarations = new ArrayList<CssDeclarationNode>();
            for (CssTreeNode leaf : TreeUtils.iterateLeafs((CssTreeNode) source))
            {
                if (leaf instanceof CssDeclarationNode)
                {
                    declarations.add((CssDeclarationNode) leaf);
                }
            }
            for (CssDeclarationNode declarationNode : declarations)
            {
                deleteNode(declarationNode);
            }
        }
        else if (source instanceof CssDeclarationNode)
        {
            deleteNode((CssDeclarationNode) source);
        }
    }

    public void collapseAll()
    {
        for (CssTreeNode node : TreeUtils.iterateLeafs((CssTreeNode) treeModel.getRoot()))
        {
            CssTreeNode parent = node;
            while ((parent = (CssTreeNode) parent.getParent()) != null)
            {
                if (parent.isRoot())
                {
                    break;
                }
                tree.collapsePath(new TreePath(parent.getPath()));
            }
        }
    }

    public void expandAll()
    {
        for (CssTreeNode node : TreeUtils.iterateLeafs((CssTreeNode) treeModel.getRoot()))
        {
            tree.expandPath(new TreePath(node.getPath()));
        }
    }

    //
    // TreeModelListener
    //

    public void treeNodesChanged(TreeModelEvent e)
    {
        updateButtons();
    }

    public void treeNodesInserted(TreeModelEvent e)
    {
        updateButtons();
    }

    public void treeNodesRemoved(TreeModelEvent e)
    {
        updateButtons();
    }

    public void treeStructureChanged(TreeModelEvent e)
    {
        updateButtons();
    }
}
