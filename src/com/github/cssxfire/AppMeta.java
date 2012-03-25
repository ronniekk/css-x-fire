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

/**
 * <p>Store for various plugin settings that will be persisted for the application. Used for default values and version checking.
 * <p><p>Created by IntelliJ IDEA.
 * User: Ronnie
 *
 * @see CssXFireConnector#getState()
 * @see CssXFireConnector#loadState(AppMeta)
 */
public class AppMeta {
    /**
     * Last known version of plugin
     */
    private String version = null;
    /**
     * This actually 'fileReduce' but changing the name now affects previous versions since this is persisted.
     */
    private boolean smartReduce = false;
    /**
     * Reduce for @media flag
     */
    private boolean mediaReduce = false;

    public boolean isMediaReduce() {
        return mediaReduce;
    }

    public void setMediaReduce(boolean mediaReduce) {
        this.mediaReduce = mediaReduce;
    }

    public boolean isSmartReduce() {
        return smartReduce;
    }

    public void setSmartReduce(boolean smartReduce) {
        this.smartReduce = smartReduce;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
