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

package com.googlecode.cssxfire.tree;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class TreeUtils
{
    public static int countLeafs(CssTreeNode root)
    {
        int numLeafs = 0;
        for (CssTreeNode leaf : iterateLeafs(root))
        {
            numLeafs++;
        }
        return numLeafs;
    }

    public static Iterable<CssTreeNode> iterateLeafs(CssTreeNode root)
    {
        return new CssTreeLeafIterable(root);
    }
    
    private static class CssTreeLeafIterable implements Iterable<CssTreeNode>
    {
        private final LeafIterator leafIterator;

        private CssTreeLeafIterable(CssTreeNode root)
        {
            this.leafIterator = new LeafIterator(root);
        }

        public Iterator<CssTreeNode> iterator()
        {
            return leafIterator;
        }

        private class LeafIterator implements Iterator<CssTreeNode>
        {
            private Enumeration enumeration;
            private CssTreeNode next;

            public LeafIterator(CssTreeNode root)
            {
                enumeration = root.depthFirstEnumeration();
                seek();
            }

            private void seek()
            {
                next = null;
                CssTreeNode node;
                while (enumeration.hasMoreElements())
                {
                    node = (CssTreeNode) enumeration.nextElement();
                    if (node.isLeaf())
                    {
                        if (!node.isRoot())
                        {
                            next = node;
                        }
                        break;
                    }
                }
            }

            public boolean hasNext()
            {
                return next != null;
            }

            public CssTreeNode next()
            {
                if (next == null)
                {
                    throw new NoSuchElementException("No more leafs");
                }
                CssTreeNode ret = next;
                seek();
                return ret;
            }

            public void remove()
            {
                throw new UnsupportedOperationException("Not supported");
            }
        }
    }
}
