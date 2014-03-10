/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Florian Wininger - Initial API and implementation
 *   Geneviève Bastien - Review of the initial implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.analysis.xml.ui.views.timegraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.internal.tmf.analysis.xml.ui.TmfXmlUiStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.linuxtools.tmf.analysis.xml.core.stateprovider.TmfXmlStrings;
import org.eclipse.linuxtools.tmf.analysis.xml.ui.views.timegraph.XmlEntry.EntryDisplayType;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.w3c.dom.Element;

/**
 * Presentation provider for the XML view, based on the generic TMF presentation
 * provider.
 *
 * TODO: This should support colors/states defined for each entry element in the
 * XML element. Also, event values may not be integers only (for instance, this
 * wouldn't support yet the callstack view)
 *
 * @author Florian Wininger
 */
public class XmlPresentationProvider extends TimeGraphPresentationProvider {

    private List<StateItem> stateValues = new ArrayList<>();
    /*
     * Maps the value of an event with the corresponding index in the
     * stateValues list
     */
    private Map<Integer, Integer> stateIndex = new HashMap<>();

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof TimeEvent && ((TimeEvent) event).hasValue()) {
            TimeEvent tcEvent = (TimeEvent) event;

            XmlEntry entry = (XmlEntry) event.getEntry();
            int value = tcEvent.getValue();

            if (entry.getType() == EntryDisplayType.DISPLAY) {
                Integer index = stateIndex.get(value);
                if (index == null) {
                    /* Colors won't be refreshed yet, return something known */
                    index = TRANSPARENT;
                    stateIndex.put(value, stateValues.size());
                    StateItem item = new StateItem(calcColor(stateValues.size()), String.valueOf(value));
                    stateValues.add(item);
                    Display.getDefault().asyncExec(new Runnable() {
                        @Override
                        public void run() {
                            fireColorSettingsChanged();
                        }
                    });
                }
                return index;
            }
        }

        return INVISIBLE;
    }

    @Override
    public StateItem[] getStateTable() {
        return stateValues.toArray(new StateItem[stateValues.size()]);
    }

    @Override
    public String getEventName(ITimeEvent event) {
        if (event instanceof TimeEvent && ((TimeEvent) event).hasValue()) {
            TimeEvent tcEvent = (TimeEvent) event;

            XmlEntry entry = (XmlEntry) event.getEntry();
            int value = tcEvent.getValue();

            if (entry.getType() == EntryDisplayType.DISPLAY) {
                Integer index = stateIndex.get(value);
                String rgb = stateValues.get(index).getStateString();
                return rgb;
            }
            return null;
        }
        return Messages.XmlPresentationProvider_MultipleStates;
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime) {
        /*
         * TODO: Add the XML elements to support adding extra information in the
         * tooltips and implement this
         */
        return Collections.EMPTY_MAP;
    }

    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc) {
        /*
         * TODO Add the XML elements to support texts in intervals and implement
         * this
         */
    }

    @Override
    public void postDrawEntry(ITimeGraphEntry entry, Rectangle bounds, GC gc) {
    }

    /**
     * Loads the states from a {@link TmfXmlUiStrings#TIME_GRAPH_VIEW} XML
     * element
     *
     * @param viewElement
     *            The XML view element
     */
    public void loadNewStates(@NonNull Element viewElement) {
        stateValues.clear();
        stateIndex.clear();
        List<Element> states = XmlUtils.getChildElements(viewElement, TmfXmlStrings.DEFINED_VALUE);

        for (Element state : states) {
            int value = Integer.parseInt(state.getAttribute(TmfXmlStrings.VALUE));
            String name = state.getAttribute(TmfXmlStrings.NAME);
            String color = state.getAttribute(TmfXmlStrings.COLOR);

            // FIXME Allow this case
            if (value < 0) {
                return;
            }

            RGB colorRGB = new RGB(255, 0, 0);
            if (color.startsWith(TmfXmlStrings.COLOR_PREFIX)) {
                Integer hex = Integer.parseInt(color.substring(1), 16);
                int hex1 = hex.intValue() % 256;
                int hex2 = (hex.intValue() / 256) % 256;
                int hex3 = (hex.intValue() / (256 * 256)) % 256;
                colorRGB = new RGB(hex3, hex2, hex1);
            } else {
                colorRGB = calcColor(value);
            }

            StateItem item = new StateItem(colorRGB, name);

            Integer index = stateIndex.get(value);
            if (index == null) {
                /* Add the new state value */
                stateIndex.put(value, stateValues.size());
                stateValues.add(item);
            } else {
                /* Override a previous state value */
                stateValues.set(index, item);
            }
        }
        Display.getDefault().asyncExec(new Runnable() {
            @Override
            public void run() {
                fireColorSettingsChanged();
            }
        });
    }

    private static RGB calcColor(int value) {
        int x = (value * 97) % 1530;
        int r = 0, g = 0, b = 0;
        if (x >= 0 && x < 255) {
            r = 255;
            g = x;
            b = 0;
        }
        if (x >= 255 && x < 510) {
            r = 510 - x;
            g = 255;
            b = 0;
        }
        if (x >= 510 && x < 765) {
            r = 0;
            g = 255;
            b = x - 510;
        }
        if (x >= 765 && x < 1020) {
            r = 0;
            g = 1020 - x;
            b = 255;
        }
        if (x >= 1020 && x < 1275) {
            r = x - 1020;
            g = 0;
            b = 255;
        }
        if (x >= 1275 && x <= 1530) {
            r = 255;
            g = 0;
            b = 1530 - x;
        }
        return new RGB(r, g, b);
    }

}
