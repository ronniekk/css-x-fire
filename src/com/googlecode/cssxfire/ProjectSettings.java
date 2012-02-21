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

import com.intellij.openapi.components.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.util.containers.ContainerUtil;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
@State(
    name = "CssXFireSettings",
    storages = {
            @Storage(id = "default", file = "$PROJECT_FILE$"),
            @Storage(id = "CSS-X-Fire", file = "$PROJECT_CONFIG_DIR$/cssxfire.xml", scheme = StorageScheme.DIRECTORY_BASED)
    }
)
public class ProjectSettings implements ProjectComponent, PersistentStateComponent<Element>
{
    private RoutePerFileMappings routes = new RoutePerFileMappings();
    private boolean autoClear;
    private boolean useRoutes;
    private boolean mediaReduce;
    private boolean fileReduce;
    private boolean currentDocumentsReduce;
    private boolean autoExpand;
    private boolean resolveVariables;
    private boolean resolveMixins;

    private static final Comparator<VirtualFile> FILE_COMPARATOR = new Comparator<VirtualFile>()
    {
        public int compare(final VirtualFile o1, final VirtualFile o2)
        {
            if (o1 == null || o2 == null)
            {
                return o1 == null ? o2 == null ? 0 : 1 : -1;
            }
            return o1.getPath().compareTo(o2.getPath());
        }
    };

    public static ProjectSettings getInstance(final Project project)
    {
        return project.getComponent(ProjectSettings.class);
    }

    public RoutePerFileMappings getRoutes()
    {
        return routes;
    }

    public boolean isAutoExpand()
    {
        return autoExpand;
    }

    public void setAutoExpand(boolean autoExpand)
    {
        this.autoExpand = autoExpand;
    }

    public boolean isAutoClear()
    {
        return autoClear;
    }

    public void setAutoClear(boolean autoClear)
    {
        this.autoClear = autoClear;
    }

    public boolean isUseRoutes()
    {
        return useRoutes;
    }

    public void setUseRoutes(boolean useRoutes)
    {
        this.useRoutes = useRoutes;
    }

    public boolean isMediaReduce()
    {
        return mediaReduce;
    }

    public void setMediaReduce(boolean mediaReduce)
    {
        this.mediaReduce = mediaReduce;
    }

    public boolean isFileReduce()
    {
        return fileReduce;
    }

    public void setFileReduce(boolean fileReduce)
    {
        this.fileReduce = fileReduce;
    }

    public boolean isCurrentDocumentsReduce()
    {
        return currentDocumentsReduce;
    }

    public void setCurrentDocumentsReduce(boolean currentDocumentsReduce)
    {
        this.currentDocumentsReduce = currentDocumentsReduce;
    }

    public boolean isResolveVariables()
    {
        return resolveVariables;
    }

    public void setResolveVariables(boolean resolveVariables) 
    {
        this.resolveVariables = resolveVariables;
    }

    public boolean isResolveMixins()
    {
        return resolveMixins;
    }

    public void setResolveMixins(boolean resolveMixins)
    {
        this.resolveMixins = resolveMixins;
    }

    @NotNull
    public String getComponentName()
    {
        return getClass().getName();
    }

    public void initComponent()
    {
    }

    public void disposeComponent()
    {
    }

    public Element getState()
    {
        Element root = new Element("root");
        Element general = new Element("general");
        Element strategy = new Element("strategy");
        Element routes = new Element("routes");
        List<VirtualFile> files = new ArrayList<VirtualFile>(this.routes.getMappings().keySet());
        ContainerUtil.quickSort(files, FILE_COMPARATOR);
        for (VirtualFile file : files)
        {
            String route = this.routes.getMappings().get(file);
            if (!StringUtil.isEmptyOrSpaces(route) && file != null)
            {
                Element child = new Element("file");
                child.setAttribute("url", file.getUrl());
                child.setAttribute("route", route.trim());
                routes.addContent(child);
            }
        }
        general.setAttribute("autoClear", Boolean.toString(this.autoClear));
        general.setAttribute("autoExpand", Boolean.toString(this.autoExpand));
        strategy.setAttribute("useRoutes", Boolean.toString(this.useRoutes));
        strategy.setAttribute("mediaReduce", Boolean.toString(this.mediaReduce));
        strategy.setAttribute("fileReduce", Boolean.toString(this.fileReduce));
        strategy.setAttribute("currentDocumentsReduce", Boolean.toString(this.currentDocumentsReduce));
        strategy.setAttribute("resolveVariables", Boolean.toString(this.resolveVariables));
        strategy.setAttribute("resolveMixins", Boolean.toString(this.resolveMixins));
        root.addContent(general);
        root.addContent(strategy);
        root.addContent(routes);
        return root;
    }

    public void loadState(Element root)
    {
        HashMap<VirtualFile, String> routeMappings = new HashMap<VirtualFile, String>();
        Element routes = root.getChild("routes");
        if (routes != null)
        {
            List<Element> files = routes.getChildren("file");
            for (Element fileElement : files)
            {
                String url = fileElement.getAttributeValue("url");
                String route = fileElement.getAttributeValue("route");
                if (route == null)
                {
                    continue;
                }
                VirtualFile file = VirtualFileManager.getInstance().findFileByUrl(url);
                if (file != null)
                {
                    routeMappings.put(file, route);
                }
            }
        }
        this.routes.setMappings(routeMappings);

        Element general = root.getChild("general");
        this.autoClear = general != null && Boolean.parseBoolean(general.getAttributeValue("autoClear"));
        this.autoExpand = general == null || general.getAttributeValue("autoExpand") == null || Boolean.parseBoolean(general.getAttributeValue("autoExpand"));
        Element strategy = root.getChild("strategy");
        this.fileReduce = strategy != null && Boolean.parseBoolean(strategy.getAttributeValue("fileReduce"));
        this.mediaReduce = strategy != null && Boolean.parseBoolean(strategy.getAttributeValue("mediaReduce"));
        this.currentDocumentsReduce = strategy != null && Boolean.parseBoolean(strategy.getAttributeValue("currentDocumentsReduce"));
        this.resolveVariables = strategy != null && Boolean.parseBoolean(strategy.getAttributeValue("resolveVariables"));
        this.resolveMixins = strategy != null && Boolean.parseBoolean(strategy.getAttributeValue("resolveMixins"));
        this.useRoutes = strategy != null && Boolean.parseBoolean(strategy.getAttributeValue("useRoutes"));
    }

    public void projectOpened()
    {
    }

    public void projectClosed()
    {
    }
}
