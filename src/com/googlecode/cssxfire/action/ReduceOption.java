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

import com.googlecode.cssxfire.CssXFireConnector;
import com.googlecode.cssxfire.IncomingChangesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class ReduceOption extends BooleanOption
{
    @Override
    protected String getOptionName()
    {
        return "Reduce to single file filter";
    }

    @Override
    protected AtomicBoolean getOptionValue(AnActionEvent event)
    {
        IncomingChangesComponent changesComponent = getIncomingChangesComponent(event);
        if (changesComponent != null)
        {
            return changesComponent.getSmartReduce();
        }
        return null;
    }

    @Override
    public void actionPerformed(AnActionEvent event)
    {
        AtomicBoolean smartReduce = getOptionValue(event);
        if (smartReduce == null)
        {
            return;
        }

        // Flip value
        smartReduce.set(!smartReduce.get());

        // Store new value as default for new projects
        CssXFireConnector.getInstance().getState().setSmartReduce(smartReduce.get());
    }
}
