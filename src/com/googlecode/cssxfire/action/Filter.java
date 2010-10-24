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

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;

import java.util.BitSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by IntelliJ IDEA.
 * User: Ronnie
 */
public class Filter extends AbstractIncomingChangesOptionGroup
{
    private static final String EMPTY_STRING = "";
    private static final String OPTIONS_SEPARATOR = ", ";

    @Override
    public void update(AnActionEvent event)
    {
        final Presentation presentation = event.getPresentation();
        final BitSet optionBits = getCurrentOptions(event);
        int numOptionsSelected = optionBits.cardinality();

        final String originalText = getTemplatePresentation().getText();
        final String originalDescription = getTemplatePresentation().getDescription();

        StringBuilder sb = new StringBuilder();
        String prepend = EMPTY_STRING;

        // Pretty description
        for (BooleanOption optionClass : getOptionClasses(event))
        {
            AtomicBoolean optionValue = optionClass.getOptionValue(event);
            if (optionValue != null && optionValue.get())
            {
                sb.append(prepend);
                sb.append(optionClass.getOptionName());
                prepend = OPTIONS_SEPARATOR;
            }
        }

        // Set text and description depending on current selection
        switch (numOptionsSelected)
        {
            case 0:
                presentation.setText(originalText + " (none active)");
                presentation.setDescription(originalDescription + ": none");
                break;
            case 1:
                presentation.setText(originalText + " (1 filter active)");
                presentation.setDescription(originalDescription + ": " + sb.toString());
                break;
            default:
                presentation.setText(originalText + " (" + numOptionsSelected + " filters active)");
                presentation.setDescription(originalDescription + ": " + sb.toString());
                break;
        }
    }
}
