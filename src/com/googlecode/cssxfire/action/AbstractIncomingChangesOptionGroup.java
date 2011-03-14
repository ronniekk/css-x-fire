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

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public abstract class AbstractIncomingChangesOptionGroup extends DefaultActionGroup
{
    @NotNull
    protected BitSet getCurrentOptions(AnActionEvent event)
    {
        final BooleanOption[] children = getOptionClasses(event);
        final BitSet options = new BitSet(children.length);

        for (int i = 0; i < children.length; i++)
        {
            options.set(i, children[i].getOptionValue(event));
        }

        return options;
    }

    @NotNull
    protected BooleanOption[] getOptionClasses(AnActionEvent event)
    {
        final AnAction[] children = getChildren(event);
        final List<BooleanOption> optionClasses = new ArrayList<BooleanOption>();

        for (AnAction child : children)
        {
            if (child instanceof BooleanOption)
            {
                optionClasses.add((BooleanOption) child);
            }
            else
            {
                throw new IllegalArgumentException(getClass().getName() + " children must be of class " + BooleanOption.class.getName());
            }
        }
        
        return optionClasses.toArray(new BooleanOption[optionClasses.size()]);
    }
}
