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

package com.googlecode.cssxfire.ui;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Shamelessly copied from {@link com.intellij.openapi.diff.ex.DiffStatusBar} since it does not belong to the open-api.
 */
public class LegendDescriptorPanel extends JPanel
{
    private static final java.util.List<MyLegendTypeDescriptor> MY_TYPES =
            Arrays.asList(new MyLegendTypeDescriptor("Modified", Colors.MODIFIED_LEGEND),
                    new MyLegendTypeDescriptor("Added", Colors.ADDED_LEGEND),
                    new MyLegendTypeDescriptor("Invalid", Colors.INVALID_LEGEND));

    private final Collection<JComponent> myLabels = new ArrayList<JComponent>();

    private final JLabel myTextLabel = new JLabel("");
    private static final int COMP_HEIGHT = 30;

    public LegendDescriptorPanel()
    {
        for (MyLegendTypeDescriptor type : MY_TYPES)
        {
            addComponent(type);
        }
        initGui();
    }

    private void addComponent(final MyLegendTypeDescriptor diffType)
    {
        JComponent component = new JPanel()
        {
            public void paint(Graphics g)
            {
                setBackground(UIManager.getColor("Panel.background"));
                super.paint(g);
                FontMetrics metrics = getFontMetrics(getFont());

                g.setColor(diffType.getLegendColor());
                g.fill3DRect(10, (getHeight() - 10) / 2, 35, 10, true);

                Font font = g.getFont();
                if (font.getStyle() != Font.PLAIN)
                {
                    font = font.deriveFont(Font.PLAIN);
                }
                g.setFont(font);
                g.setColor(UIManager.getColor("Label.foreground"));
                int textBaseline = (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent();
                g.drawString(diffType.getDisplayName(), 67, textBaseline);
            }

            @Override
            public Dimension getPreferredSize()
            {
                FontMetrics metrics = getFontMetrics(getFont());
                return new Dimension((int) (70 + metrics.getStringBounds(diffType.getDisplayName(), getGraphics()).getWidth()), COMP_HEIGHT);
            }

            @Override
            public Dimension getMinimumSize()
            {
                return getPreferredSize();
            }
        };
        myLabels.add(component);
    }

    public Dimension getMinimumSize()
    {
        Dimension p = super.getPreferredSize();
        Dimension m = super.getMinimumSize();
        return new Dimension(m.width, p.height);
    }

    public Dimension getMaximumSize()
    {
        Dimension p = super.getPreferredSize();
        Dimension m = super.getMaximumSize();
        return new Dimension(m.width, p.height);
    }

    public void setText(String text)
    {
        myTextLabel.setText(text);
    }

    private void initGui()
    {
        JComponent filler = new JComponent()
        {
            @Override
            public Dimension getPreferredSize()
            {
                return myTextLabel.getPreferredSize();
            }
        };
        setLayout(new BorderLayout());

        add(myTextLabel, BorderLayout.WEST);
        Box box = Box.createHorizontalBox();
        box.add(Box.createHorizontalGlue());
        JPanel panel = new JPanel(new GridLayout(1, myLabels.size(), 0, 0));
        for (final JComponent myLabel : myLabels)
        {
            panel.add(myLabel);
        }
        panel.setMaximumSize(panel.getPreferredSize());
        box.add(panel);
        box.add(Box.createHorizontalGlue());
        add(box, BorderLayout.CENTER);

        add(filler, BorderLayout.EAST);
    }

    private static class MyLegendTypeDescriptor
    {
        private final String displayName;
        private final Color legendColor;

        private MyLegendTypeDescriptor(String displayName, Color legendColor)
        {
            this.displayName = displayName;
            this.legendColor = legendColor;
        }

        public String getDisplayName()
        {
            return displayName;
        }

        public Color getLegendColor()
        {
            return legendColor;
        }
    }
}
