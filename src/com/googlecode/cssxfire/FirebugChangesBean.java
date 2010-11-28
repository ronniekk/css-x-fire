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

import org.jetbrains.annotations.NotNull;

/**
 * <p>A simple bean which holds the properties reported by Firebug extension when editing a CSS rule.
 * <p><p>Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class FirebugChangesBean
{
    @NotNull
    private final String media;
    @NotNull
    private final String filename;
    @NotNull
    private final String selector;
    @NotNull
    private final String property;
    @NotNull
    private final String value;
    private final boolean deleted;

    public FirebugChangesBean(@NotNull String media, @NotNull String filename, @NotNull String selector,
                              @NotNull String property, @NotNull String value, boolean deleted)
    {
        this.media = media;
        this.filename = filename;
        this.selector = selector;
        this.property = property;
        this.value = value;
        this.deleted = deleted;
    }

    @NotNull
    public String getMedia()
    {
        return media;
    }

    @NotNull
    public String getFilename()
    {
        return filename;
    }

    @NotNull
    public String getSelector()
    {
        return selector;
    }

    @NotNull
    public String getProperty()
    {
        return property;
    }

    @NotNull
    public String getValue()
    {
        return value;
    }

    public boolean isDeleted()
    {
        return deleted;
    }
}
