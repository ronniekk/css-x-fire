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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.CharArrayWriter;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class StringUtils {
    private static final Logger LOG = Logger.getInstance(StringUtils.class.getName());

    @NotNull
    public static String extractPath(@NotNull String url) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractPath() in = " + url);
        }
        String path;
        try {
            path = new URL(url).getPath();
            return path;
        } catch (MalformedURLException e) {
            LOG.warn("WARN: " + e);
            path = trimEnd(trimEnd(url, "?"), "#");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("extractPath() out = " + path);
        }
        return path;
    }

    @NotNull
    public static String extractFilename(@NotNull String path) {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    public static boolean equalsNormalizeWhitespace(@Nullable String s1, @Nullable String s2) {
        if (s1 == null && s2 == null) {
            return true;
        }
        if (s1 == null || s2 == null) {
            return false;
        }
        return normalizeWhitespace(s1).equals(normalizeWhitespace(s2));
    }

    @NotNull
    public static String extractSearchWord(@NotNull String s) {
        s = s.trim();
        for (int i = s.length() - 1; i > 0; i--) {
            char c = s.charAt(i);
            if (c == ':' || c == ',' || Character.isWhitespace(c)) {
                return s.substring(i + 1);
            }
        }
        return s;
    }

    @NotNull
    public static String normalizeWhitespace(@NotNull String s) {
        CharArrayWriter writer = new CharArrayWriter(s.length());
        char lastWritten = 'a';
        for (char c : s.trim().toCharArray()) {
            if (Character.isWhitespace(c)) {
                if (Character.isWhitespace(lastWritten) || lastWritten == ',') {
                    continue;
                }
                lastWritten = ' ';
                writer.append(lastWritten);
            } else {
                lastWritten = c;
                writer.append(lastWritten);
            }
        }
        return writer.toString();
    }

    public static String trimEnd(String input, String delim) {
        int ix = input.indexOf(delim);
        if (ix == -1) {
            return input;
        }
        return input.substring(0, ix);
    }

    public static void main(String[] args) {
        System.out.println(normalizeWhitespace(" "));
        System.out.println(normalizeWhitespace("a,a:link"));
        System.out.println(normalizeWhitespace("  a,a:link"));
        System.out.println(normalizeWhitespace("  a,a:link  "));
        System.out.println(normalizeWhitespace("a, a:link"));
        System.out.println(normalizeWhitespace("a,  a:link"));
        System.out.println(normalizeWhitespace(" a,  a:link  "));
        System.out.println(normalizeWhitespace(" a,\na:link  "));
        System.out.println(normalizeWhitespace(" a,\n  a:link  "));
        System.out.println(normalizeWhitespace(".cool a,a:link"));
        System.out.println(normalizeWhitespace(".cool   a,a:link"));
        System.out.println(normalizeWhitespace(".cool   a,a:link  "));
        System.out.println(normalizeWhitespace(".cool a, a:link"));
        System.out.println(normalizeWhitespace(".cool a,  a:link"));
        System.out.println(normalizeWhitespace(" .cool a,  a:link  "));
        System.out.println(normalizeWhitespace(" .cool a,\na:link  "));
        System.out.println(normalizeWhitespace(" .cool a,\n  a:link  "));

        System.out.println(extractFilename("https://assets3.yammer.com/stylesheets/message_feed_packaged.css?1287337838"));
        System.out.println(extractFilename("message_feed_packaged.css?1287337838"));
        System.out.println(extractFilename("https://assets3.yammer.com/stylesheets/message_feed_packaged.css"));
        System.out.println(extractFilename("https://assets3.yammer.com/stylesheets/message_feed_packaged.css/"));
    }
}
