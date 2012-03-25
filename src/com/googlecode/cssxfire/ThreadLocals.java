/*
 * Copyright 2011 Ronnie Kolehmainen
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

import java.util.ArrayList;
import java.util.List;

/**
 * Re-usable objects such as lists and string buffers. Avoids unnecessary object creation.
 */
public class ThreadLocals {
    public static final ThreadLocal<List<String>> stringList = new ThreadLocal<List<String>>() {
        @Override
        protected List<String> initialValue() {
            return new ArrayList<String>();
        }

        @Override
        public List<String> get() {
            List<String> list = super.get();
            list.clear();
            return list;
        }
    };

    public static final ThreadLocal<StringBuilder> stringBuilder = new ThreadLocal<StringBuilder>() {
        @Override
        protected StringBuilder initialValue() {
            return new StringBuilder();
        }

        @Override
        public StringBuilder get() {
            StringBuilder builder = super.get();
            builder.setLength(0);
            return builder;
        }
    };
}
