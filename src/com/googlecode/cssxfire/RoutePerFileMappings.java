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

import com.intellij.lang.PerFileMappings;
import com.intellij.openapi.vfs.VirtualFile;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class RoutePerFileMappings implements PerFileMappings<String>
{
    private Map<VirtualFile, String> myMappings = new HashMap<VirtualFile, String>();

    public Map<VirtualFile, String> getMappings()
    {
        return new HashMap<VirtualFile, String>(myMappings);
    }

    public void setMappings(Map<VirtualFile, String> mappings)
    {
        myMappings.clear();
        myMappings.putAll(mappings);
    }

    public Collection<String> getAvailableValues(VirtualFile file)
    {
        return Collections.emptyList();
    }

    public String getMapping(VirtualFile file)
    {
        return myMappings.get(file);
    }
}
