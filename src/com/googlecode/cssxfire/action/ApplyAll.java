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

import com.googlecode.cssxfire.IncomingChangesComponent;
import com.intellij.openapi.actionSystem.AnActionEvent;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class ApplyAll extends AbstractIncomingChangesAction
{
    public static final String ID = ApplyAll.class.getName();

    @Override
    public void actionPerformed(AnActionEvent event)
    {
        IncomingChangesComponent changesComponent = getIncomingChangesComponent(event);
        if (changesComponent == null)
        {
            return;
        }
        changesComponent.getTreeViewModel().applyPending();
    }
}
