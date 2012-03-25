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

import java.awt.*;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public interface Colors {
    public static final Color MODIFIED = new Color(0, 0, 255);
    public static final Color INVALID = new Color(128, 128, 128);
    public static final Color ADDED = new Color(0, 128, 0);

    public static final Color MODIFIED_LEGEND = new Color(MODIFIED.getRed(), MODIFIED.getGreen(), MODIFIED.getBlue(), 64);
    public static final Color INVALID_LEGEND = new Color(INVALID.getRed(), INVALID.getGreen(), INVALID.getBlue(), 64);
    public static final Color ADDED_LEGEND = new Color(ADDED.getRed(), ADDED.getGreen(), ADDED.getBlue(), 64);
}
