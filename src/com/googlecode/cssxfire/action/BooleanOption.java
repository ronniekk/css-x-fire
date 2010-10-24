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

import com.googlecode.cssxfire.ui.Icons;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public abstract class BooleanOption extends AbstractIncomingChangesAction
{
    @Nullable
    protected abstract AtomicBoolean getOptionValue(AnActionEvent event);

    @NotNull
    protected abstract String getOptionName();

    @Override
    public void update(AnActionEvent event)
    {
        AtomicBoolean optionValue = getOptionValue(event);
        if (optionValue == null)
        {
            return;
        }
        // Set "check" icon if option is active
        event.getPresentation().setIcon(optionValue.get() ? Icons.CHECK : null);
    }
}
