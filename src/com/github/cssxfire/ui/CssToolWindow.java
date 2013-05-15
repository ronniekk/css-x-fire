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

package com.github.cssxfire.ui;

import com.github.cssxfire.IncomingChangesComponent;
import com.github.cssxfire.action.ApplyAll;
import com.github.cssxfire.action.ClearAll;
import com.github.cssxfire.tree.*;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.ListPopup;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class CssToolWindow extends JPanel implements TreeModelListener, TreeViewModel {
    private final CssChangesTreeModel treeModel;
    private final JTree tree;
    private JButton clearButton, applyButton;
    private final Project project;

    public CssToolWindow(final Project project) {
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

        tree.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if ((e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
                        || (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON2)) {
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                    navigateTo(selPath);
                } else if (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3) {
                    TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                    Point point = new Point(e.getXOnScreen(), e.getYOnScreen());
                    showMenu(selPath, point);
                }
            }
        });
        tree.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    TreePath path = tree.getSelectionPath();
                    navigateTo(path);
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tree);

        incomingChangesPanel.add(scrollPane, BorderLayout.CENTER);

        Presentation clearAllPresentation = ActionManager.getInstance().getAction(ClearAll.ID).getTemplatePresentation();
        clearButton = new JButton(clearAllPresentation.getText(), clearAllPresentation.getIcon());
        clearButton.setEnabled(false);
        clearButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ActionManager.getInstance().getAction(ClearAll.ID).actionPerformed(createAnActionEvent(ClearAll.ID));
            }
        });
        Presentation applyAllPresentation = ActionManager.getInstance().getAction(ApplyAll.ID).getTemplatePresentation();
        applyButton = new JButton(applyAllPresentation.getText(), applyAllPresentation.getIcon());
        applyButton.setEnabled(false);
        applyButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ActionManager.getInstance().getAction(ApplyAll.ID).actionPerformed(createAnActionEvent(ApplyAll.ID));
            }
        });

        JPanel southPanel = new JPanel(new BorderLayout());

        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
        buttonsPanel.add(clearButton);
        buttonsPanel.add(applyButton);

        southPanel.add(buttonsPanel, BorderLayout.WEST);
        southPanel.add(new LegendDescriptorPanel(), BorderLayout.CENTER);

        add(toolBar, BorderLayout.WEST);
        add(incomingChangesPanel, BorderLayout.CENTER);
        add(southPanel, BorderLayout.SOUTH);

        treeModel.addTreeModelListener(this);
    }

    public CssChangesTreeModel getTreeModel() {
        return treeModel;
    }

    private void navigateTo(@Nullable TreePath path) {
        if (path != null) {
            Object source = path.getLastPathComponent();
            if (source instanceof Navigatable) {
                ((Navigatable) source).navigate();
            }
        }
    }

    private void showMenu(@Nullable TreePath path, @NotNull Point point) {
        if (path != null) {
            tree.setSelectionPath(path);
            Object source = path.getLastPathComponent();
            ActionGroup actionGroup = source instanceof CssTreeNode ? ((CssTreeNode) source).getActionGroup() : null;

            if (actionGroup != null) {
                ListPopup listPopup = JBPopupFactory.getInstance().createActionGroupPopup(null,
                        actionGroup,
                        createDataContext(),
                        JBPopupFactory.ActionSelectionAid.MNEMONICS,
                        true,
                        IncomingChangesComponent.TOOLWINDOW_ID);

                listPopup.showInScreenCoordinates(tree, point);
            }
        }
    }

    private AnActionEvent createAnActionEvent(String actionId) {
        return new AnActionEvent(null,
                createDataContext(),
                IncomingChangesComponent.TOOLWINDOW_ID,
                ActionManager.getInstance().getAction(actionId).getTemplatePresentation(),
                ActionManager.getInstance(),
                0);
    }

    private DataContext createDataContext() {
        return DataManager.getInstance().getDataContext(tree);
    }

    private void updateButtons() {
        boolean hasChildren = ((CssTreeNode) treeModel.getRoot()).getChildCount() > 0;
        clearButton.setEnabled(hasChildren);
        applyButton.setEnabled(hasChildren);
    }

    public void clearTree() {
        CssTreeNode root = (CssTreeNode) treeModel.getRoot();
        root.removeAllChildren();
        treeModel.nodeStructureChanged(root);
    }

    private void deleteNode(CssTreeNode node) {
        CssTreeNode parent = (CssTreeNode) node.getParent();
        if (parent != null) {
            int index = parent.getIndex(node);
            parent.remove(node);
            treeModel.nodesWereRemoved(parent, new int[]{index}, new CssTreeNode[]{node});
            if (node instanceof CssDeclarationNode) {
                // notify that file node is changed (update the number of changes in file)
                treeModel.nodeChanged(parent.getParent());
            }
            if (parent.getChildCount() == 0) {
                deleteNode(parent);
            }
        }
    }

    /**
     * Executes a runnable in a write action. The command may be undo'ed. After
     * the command has been executed all documents in project will be saved, which will
     * trigger other actions which listen for file changes such as "Transfer files"
     *
     * @param command the command
     */
    private void executeCommand(final Runnable command) {
        CommandProcessor.getInstance().executeCommand(project, new Runnable() {
            public void run() {
                ApplicationManager.getApplication().runWriteAction(command);
            }
        }, "Apply CSS", "CSS");

        FileDocumentManager.getInstance().saveAllDocuments();
    }

    //
    // TreeViewModel
    //

    public void applyPending() {
        executeCommand(new Runnable() {
            public void run() {
                CssTreeNode root = (CssTreeNode) treeModel.getRoot();
                CssTreeNode leaf;

                while ((leaf = (CssTreeNode) root.getFirstLeaf()) != null) {
                    if (leaf.isRoot()) {
                        // eventually getFirstLeaf() will return the root itself
                        break;
                    }
                    if (leaf instanceof CssDeclarationNode) {
                        CssDeclarationNode declarationNode = (CssDeclarationNode) leaf;
                        declarationNode.applyToCode();
                    }
                    treeModel.removeNodeFromParent(leaf);
                }

                treeModel.nodeStructureChanged(root);
            }
        });
    }

    public void applySelectedNode() {
        TreePath selectedPath = tree.getSelectionPath();
        if (selectedPath == null) {
            return;
        }
        Object source = selectedPath.getLastPathComponent();
        if (source instanceof CssDirectoryNode || source instanceof CssFileNode || source instanceof CssSelectorNode) {
            final Collection<CssDeclarationNode> declarations = new ArrayList<CssDeclarationNode>();
            for (CssTreeNode leaf : TreeUtils.iterateLeafs((CssTreeNode) source)) {
                if (leaf instanceof CssDeclarationNode) {
                    declarations.add((CssDeclarationNode) leaf);
                }
            }
            executeCommand(new Runnable() {
                public void run() {
                    for (CssDeclarationNode declarationNode : declarations) {
                        declarationNode.applyToCode();
                        deleteNode(declarationNode);
                    }
                }
            });
        } else if (source instanceof CssDeclarationNode) {
            final CssDeclarationNode declarationNode = (CssDeclarationNode) source;
            executeCommand(new Runnable() {
                public void run() {
                    declarationNode.applyToCode();
                    deleteSelectedNode();
                }
            });
        }
    }

    public void deleteSelectedNode() {
        TreePath selectedPath = tree.getSelectionPath();
        if (selectedPath == null) {
            return;
        }
        Object source = selectedPath.getLastPathComponent();
        if (source instanceof CssDirectoryNode || source instanceof CssFileNode || source instanceof CssSelectorNode) {
            final Collection<CssDeclarationNode> declarations = new ArrayList<CssDeclarationNode>();
            for (CssTreeNode leaf : TreeUtils.iterateLeafs((CssTreeNode) source)) {
                if (leaf instanceof CssDeclarationNode) {
                    declarations.add((CssDeclarationNode) leaf);
                }
            }
            for (CssDeclarationNode declarationNode : declarations) {
                deleteNode(declarationNode);
            }
        } else if (source instanceof CssDeclarationNode) {
            deleteNode((CssDeclarationNode) source);
        }
    }

    public void collapseAll() {
        for (CssTreeNode node : TreeUtils.iterateLeafs((CssTreeNode) treeModel.getRoot())) {
            CssTreeNode parent = node;
            while ((parent = (CssTreeNode) parent.getParent()) != null) {
                if (parent.isRoot()) {
                    break;
                }
                tree.collapsePath(new TreePath(parent.getPath()));
            }
        }
    }

    public void expandAll() {
        for (CssTreeNode node : TreeUtils.iterateLeafs((CssTreeNode) treeModel.getRoot())) {
            tree.expandPath(new TreePath(((CssTreeNode) node.getParent()).getPath()));
        }
    }

    public void refreshLeafs() {
        for (CssTreeNode node : TreeUtils.iterateLeafs((CssTreeNode) treeModel.getRoot())) {
            treeModel.nodeChanged(node);
        }
    }

    public boolean canSelect(int direction) {
        int leafCount = TreeUtils.countLeafs((CssTreeNode) treeModel.getRoot());
        switch (leafCount) {
            case 0:
                return false;
            case 1:
                TreePath selectionPath = tree.getSelectionPath();
                if (direction < 0) {
                    // When there's only one declaration node, only forward navigation is possible 
                    return false;
                }
                return selectionPath == null || !(selectionPath.getLastPathComponent() instanceof CssDeclarationNode);
            default:
                return true;
        }
    }

    public void select(int direction) {
        CssTreeNode root = (CssTreeNode) treeModel.getRoot();
        TreePath selectionPath = tree.getSelectionPath();
        CssTreeNode anchor = selectionPath == null ? null : (CssTreeNode) selectionPath.getLastPathComponent();
        CssDeclarationNode declarationNode = TreeUtils.seek(root, anchor, direction);

        if (declarationNode != null) {
            TreePath path = new TreePath(declarationNode.getPath());
            tree.getSelectionModel().setSelectionPath(path);
            navigateTo(path);
        }
    }

    //
    // TreeModelListener
    //

    public void treeNodesChanged(TreeModelEvent e) {
        updateButtons();
    }

    public void treeNodesInserted(TreeModelEvent e) {
        updateButtons();
    }

    public void treeNodesRemoved(TreeModelEvent e) {
        updateButtons();
    }

    public void treeStructureChanged(TreeModelEvent e) {
        updateButtons();
    }
}
