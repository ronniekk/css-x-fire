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

package com.github.cssxfire;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class RouteUtils {
    private static final Logger LOG = Logger.getInstance(RouteUtils.class.getName());
    private static final String[] ROOT_MAPPING = new String[]{""};

    /**
     * Finds the local file for a given route, if any. This method does not perform any I/O
     * operations but works only with the paths of the files.
     *
     * @param project  the project
     * @param filePath the remote file path, e.g. the string returnd from {@link java.net.URL#getPath()}
     * @return the mapped local file in given project, or <tt>null</tt> if no mapping can be detected
     */
    @Nullable
    public static VirtualFile detectLocalFile(@NotNull final Project project, @NotNull final String filePath) {
        final RoutePerFileMappings routes = ProjectSettings.getInstance(project).getRoutes();
        String[] parts = filePath.split("/");
        Map<VirtualFile, String> mappings = routes.getMappings();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Detect local file, path: " + filePath + " routes: " + mappings);
        }

        String[] longestMatch = new String[0];
        VirtualFile bestMatch = null;
        String bestRoute = null;

        for (Map.Entry<VirtualFile, String> entry : mappings.entrySet()) {
            VirtualFile file = entry.getKey();
            String route = entry.getValue();
            if (filePath.equals(route)) {
                // full match
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Full match, route: " + route + ", file: " + file);
                }
                return file;
            }
            if (!file.isDirectory()) {
                continue;
            }
            String[] routeParts = "/".equals(route) ? ROOT_MAPPING : route.split("/"); // fix for String.split() inconsistency
            if (routeParts.length <= parts.length && routeParts.length > longestMatch.length) {
                if (startsWith(parts, routeParts)) {
                    longestMatch = routeParts;
                    bestMatch = file;
                    bestRoute = route;
                }
            }
        }

        if (longestMatch.length == 0) {
            // no route matched
            LOG.debug("No match");
            return null;
        }

        //noinspection ConstantConditions
        VirtualFile virtualFile = bestMatch.findFileByRelativePath(filePath.substring(bestRoute.length()));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Partial match, route: " + bestRoute + ", best match: " + bestMatch + ", local file: " + virtualFile);
        }
        return virtualFile;
    }

    private static boolean startsWith(@NotNull String[] array, @NotNull String[] start) {
        if (start.length > array.length) {
            return false;
        }
        for (int i = 0; i < start.length; i++) {
            if (!array[i].equals(start[i])) {
                return false;
            }
        }
        return true;
    }
}
