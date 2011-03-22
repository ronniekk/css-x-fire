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

import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.NonDefaultProjectConfigurable;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.ui.tree.AbstractFileTreeTable;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.plaf.ScrollPaneUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class ProjectSettingsConfigurable implements SearchableConfigurable, NonDefaultProjectConfigurable
{
    public static ProjectSettingsConfigurable getInstance(final Project project)
    {
        return ShowSettingsUtil.getInstance().findProjectConfigurable(project, ProjectSettingsConfigurable.class);
    }

    private Project myProject;
    private JCheckBox checkBoxAutoClear;
    private JCheckBox checkBoxMediaReduce;
    private JCheckBox checkBoxFileReduce;
    private JCheckBox checkBoxUseRoutes;
    private FileTreeTable routesTable;
    private JScrollPane routesScrollPane;
    private JPanel myPanel;
    private JButton buttonSetRoot;

    public ProjectSettingsConfigurable(Project project)
    {
        myProject = project;
    }

    public String getId()
    {
        return getClass().getName();
    }

    public Runnable enableSearch(String option)
    {
        return null;
    }

    public JComponent createComponent()
    {
        routesTable = new FileTreeTable();
        routesTable.getColumnModel().getColumn(0).setPreferredWidth(260);
        routesTable.getColumnModel().getColumn(1).setPreferredWidth(240);

        routesScrollPane.setViewportView(routesTable);

        routesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener()
        {
            public void valueChanged(ListSelectionEvent e)
            {
                updateWebRootButton();
            }
        });
        buttonSetRoot.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent e)
            {
                updateWebRoot();
            }
        });

        reset();
        updateWebRootButton();
        
        return myPanel;
    }

    private void updateWebRoot()
    {
        int selectedRow = routesTable.getSelectedRow();
        if (selectedRow != -1)
        {
            Map<VirtualFile,String> currentValues = routesTable.getValues();
            Object file = routesTable.getValueAt(selectedRow, 0);
            if (file instanceof VirtualFile)
            {
                for (VirtualFile key : new HashSet<VirtualFile>(currentValues.keySet()))
                {
                    if ("/".equals(currentValues.get(key)))
                    {
                        currentValues.remove(key);
                    }
                }
                currentValues.put((VirtualFile) file, "/");
            }
            routesTable.reset(currentValues);
            updateWebRootButton();
        }
    }

    private void updateWebRootButton()
    {
        int selectedRow = routesTable.getSelectedRow();
        if (selectedRow == -1)
        {
            buttonSetRoot.setEnabled(false);
            return;
        }
        Object file = routesTable.getValueAt(selectedRow, 0);
        Object route = routesTable.getValueAt(selectedRow, 1);
        buttonSetRoot.setEnabled(!"/".equals(route) && file instanceof VirtualFile && ((VirtualFile) file).isDirectory());
    }

    public boolean isModified()
    {
        ProjectSettings state = ProjectSettings.getInstance(myProject);
        return !state.getRoutes().getMappings().equals(routesTable.getValues())
                || state.isAutoClear() != checkBoxAutoClear.isSelected()
                || state.isMediaReduce() != checkBoxMediaReduce.isSelected()
                || state.isFileReduce() != checkBoxFileReduce.isSelected()
                || state.isUseRoutes() != checkBoxUseRoutes.isSelected();
    }

    public void apply() throws ConfigurationException
    {
        ProjectSettings state = ProjectSettings.getInstance(myProject);
        state.getRoutes().setMappings(routesTable.getValues());
        state.setAutoClear(checkBoxAutoClear.isSelected());
        state.setMediaReduce(checkBoxMediaReduce.isSelected());
        state.setFileReduce(checkBoxFileReduce.isSelected());
        state.setUseRoutes(checkBoxUseRoutes.isSelected());

        // set defalut values for new projects (legacy)
        CssXFireConnector.getInstance().getState().setMediaReduce(checkBoxMediaReduce.isSelected());
        CssXFireConnector.getInstance().getState().setSmartReduce(checkBoxFileReduce.isSelected());
    }

    public void reset()
    {
        ProjectSettings state = ProjectSettings.getInstance(myProject);
        routesTable.reset(state.getRoutes().getMappings());
        checkBoxAutoClear.setSelected(state.isAutoClear());
        checkBoxFileReduce.setSelected(state.isFileReduce());
        checkBoxMediaReduce.setSelected(state.isMediaReduce());
        checkBoxUseRoutes.setSelected(state.isUseRoutes());
    }

    public void disposeUIResources()
    {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Nls
    public String getDisplayName()
    {
        return "CSS-X-Fire";
    }

    public Icon getIcon()
    {
        return null;
    }

    public String getHelpTopic()
    {
        return null;
    }

    private void createUIComponents()
    {
        routesScrollPane = new MyScrollPane(new JTable());
    }

    private class FileTreeTable extends AbstractFileTreeTable<String>
    {
        private FileTreeTable()
        {
            super(myProject, String.class, "Route");
        }

        @Override
        protected boolean isValueEditableForFile(VirtualFile virtualFile)
        {
            if (virtualFile == null)
            {
                return false;
            }
            return !virtualFile.getUrl().startsWith(getSettingsUrl());
        }

        @Override
        protected boolean isNullObject(String value)
        {
            if (value == null)
            {
                return true;
            }
            String trimmed = value.trim();
            return !trimmed.equals(value) || !trimmed.startsWith("/");
        }

        @NotNull
        private String getSettingsUrl()
        {
            VirtualFile settingsDir = myProject.getBaseDir();
            return (settingsDir != null ? settingsDir.getUrl() : "") + "/.idea";
        }
    }

    private static class MyScrollPane extends JScrollPane
    {
        MyScrollPane(JComponent view)
        {
            super(view);
        }

        /**
         * Scrollpane's background should be always in sync with view's background
         */
        public void setUI(ScrollPaneUI ui)
        {
            super.setUI(ui);
            SwingUtilities.invokeLater(new Runnable()
            {
                public void run()
                {
                    Component component = getViewport().getView();
                    if (component != null)
                    {
                        getViewport().setBackground(component.getBackground());
                    }
                }
            });
        }
    }
}
