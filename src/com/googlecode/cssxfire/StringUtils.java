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
import org.jetbrains.annotations.Nullable;

import java.io.CharArrayWriter;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class StringUtils
{
    @NotNull
    public static String extractFilename(@NotNull String path)
    {
        if (path.length() == 0)
        {
            return path;
        }
        int endIndex = path.indexOf('?');
        if (endIndex != -1)
        {
            path = path.substring(0, endIndex);
        }
        int startIndex = path.lastIndexOf('/');
        return path.substring(startIndex + 1);
    }

    public static boolean equalsNormalizeWhitespace(@Nullable String s1, @Nullable String s2)
    {
        if (s1 == null && s2 == null)
        {
            return true;
        }
        if (s1 == null || s2 == null)
        {
            return false;
        }
        return normalizeWhitespace(s1).equals(normalizeWhitespace(s2));
    }

    @NotNull
    public static String extractSearchWord(@NotNull String s)
    {
        CharArrayWriter writer = new CharArrayWriter(s.length());
        for (char c : s.trim().toCharArray())
        {
            if (Character.isWhitespace(c) || c == ',')
            {
                break;
            }
            writer.append(c);
        }
        return writer.toString();
    }

    @NotNull
    public static String normalizeWhitespace(@NotNull String s)
    {
        CharArrayWriter writer = new CharArrayWriter(s.length());
        char lastWritten = 'a';
        for (char c : s.trim().toCharArray())
        {
            if (Character.isWhitespace(c))
            {
                if (Character.isWhitespace(lastWritten) || lastWritten == ',')
                {
                    continue;
                }
                lastWritten = ' ';
                writer.append(lastWritten);
            }
            else
            {
                lastWritten = c;
                writer.append(lastWritten);
            }
        }
        return writer.toString();
    }

    public static void main(String[] args)
    {
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
