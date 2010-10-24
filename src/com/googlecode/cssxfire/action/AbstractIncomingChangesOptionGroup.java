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

package com.googlecode.cssxfire.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public abstract class AbstractIncomingChangesOptionGroup extends DefaultActionGroup
{
    @NotNull
    protected BitSet getCurrentOptions(AnActionEvent event)
    {
        AnAction[] children = getOptionClasses(event);
        BitSet options = new BitSet(children.length);
        for (int i = 0; i < children.length; i++)
        {
            AtomicBoolean option = ((BooleanOption) children[i]).getOptionValue(event);
            boolean value = option != null && option.get();
            options.set(i, value);
        }
        return options;
    }

    @NotNull
    protected BooleanOption[] getOptionClasses(AnActionEvent event)
    {
        AnAction[] children = getChildren(event);
        List<BooleanOption> optionClasses = new ArrayList<BooleanOption>();
        for (AnAction child : children)
        {
            if (child instanceof BooleanOption)
            {
                optionClasses.add((BooleanOption) child);
            }
        }
        return optionClasses.toArray(new BooleanOption[optionClasses.size()]);
    }
}
