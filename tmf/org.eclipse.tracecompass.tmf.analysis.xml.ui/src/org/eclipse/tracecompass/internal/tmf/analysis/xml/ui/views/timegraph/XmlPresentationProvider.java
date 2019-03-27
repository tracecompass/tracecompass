/*******************************************************************************
 * Copyright (c) 2014, 2018 École Polytechnique de Montréal and others.
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

package org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.timegraph;

import com.google.common.primitives.Ints;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenTimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlUtils;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;
import org.w3c.dom.Element;

/**
 * Presentation provider for the XML view, based on the generic TMF presentation
 * provider.
 *
 * TODO: This should support colors/states defined for each entry element in the
 * XML element.
 *
 * @author Florian Wininger
 */
public class XmlPresentationProvider extends TimeGraphPresentationProvider {

    private static final long[] COLOR_SEED = { 0x0000ff, 0xff0000, 0x00ff00,
            0xff00ff, 0x00ffff, 0xffff00, 0x000000, 0xf07300
    };

    private static final int COLOR_MASK = 0xffffff;

    private List<StateItem> stateValues = new ArrayList<>();
    /**
     * Average width of the characters used for state labels. Is computed in the
     * first call to postDrawEvent(). Is null before that.
     */
    private Integer fAverageCharacterWidth = null;
    /*
     * Maps the value of an event with the corresponding index in the
     * stateValues list
     */
    private Map<Integer, Integer> stateIndex = new HashMap<>();
    private StateItem[] stateTable = new StateItem[0];

    private final String fId;

    /**
     * Presentation provider constructor
     *
     * @param id
     *               the id of the generating analysis. This is needed since each
     *               xml view has the same presentation provider, so this can
     *               differentiate them.
     */
    public XmlPresentationProvider(String id) {
        fId = id;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof NullTimeEvent) {
            return INVISIBLE;
        }
        if (event instanceof TimeEvent && ((TimeEvent) event).hasValue()) {
            TimeEvent tcEvent = (TimeEvent) event;

            TimeGraphEntry entry = (TimeGraphEntry) event.getEntry();
            int value = tcEvent.getValue();

            ITimeGraphEntryModel model = entry.getModel();
            if (model instanceof DataDrivenTimeGraphEntryModel) {
                // Draw state only if state is already known
                Integer index = stateIndex.get(value);
                if (index != null) {
                    return index;
                }
            }
        }
        return TRANSPARENT;
    }

    @Override
    public StateItem[] getStateTable() {
        return stateTable;
    }

    @Override
    public String getEventName(ITimeEvent event) {
        if (event instanceof TimeEvent && ((TimeEvent) event).hasValue()) {
            TimeEvent tcEvent = (TimeEvent) event;

            DataDrivenTimeGraphEntryModel model = (DataDrivenTimeGraphEntryModel) ((TimeGraphEntry) event.getEntry()).getModel();
            int value = tcEvent.getValue();

            if (model.getDisplayQuark() >= 0) {
                Integer index = stateIndex.get(value);
                if (index != null) {
                    return stateValues.get(index.intValue()).getStateString();
                }
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
        return Collections.emptyMap();
    }

    /**
     * Returns the average character width, measured in pixels, of the font
     * described by the receiver.
     *
     * @param gc
     *            The graphic context
     * @return the average character width of the font
     */
    @Deprecated
    private static int getAverageCharWidth(GC gc) {
        return gc.getFontMetrics().getAverageCharWidth();
    }

    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc) {
        // Is there text to show
        DataDrivenTimeGraphEntryModel entry = (DataDrivenTimeGraphEntryModel) ((TimeGraphEntry) event.getEntry()).getModel();
        if (!entry.showText()) {
            return;
        }
        // See if the state is too short to show text
        if (fAverageCharacterWidth == null) {
            fAverageCharacterWidth = getAverageCharWidth(gc);
        }
        if (bounds.width <= fAverageCharacterWidth) {
            return;
        }
        String eventName = getEventName(event);
        if (eventName == null) {
            return;
        }

        Color stateColor = gc.getBackground();
        gc.setForeground(Utils.getDistinctColor(stateColor.getRGB()));
        Utils.drawText(gc, eventName, bounds.x, bounds.y, bounds.width, bounds.height, true, true);
    }

    @Override
    public void postDrawEntry(ITimeGraphEntry entry, Rectangle bounds, GC gc) {
        // Do nothing
    }

    /**
     * Loads the states from a {@link TmfXmlStrings#TIME_GRAPH_VIEW} XML
     * element
     *
     * @param viewElement
     *            The XML view element
     * @return A map of string values loaded and their corresponding numerical value
     */
    public synchronized Map<String, Integer> loadNewStates(@NonNull Element viewElement) {
        Map<String, Integer> map = new HashMap<>();
        stateValues.clear();
        stateIndex.clear();
        List<Element> states = TmfXmlUtils.getChildElements(viewElement, TmfXmlStrings.DEFINED_VALUE);

        for (Element state : states) {
            String valueStr = state.getAttribute(TmfXmlStrings.VALUE);
            Integer value = Ints.tryParse(valueStr);
            String name = state.getAttribute(TmfXmlStrings.NAME);
            if (value == null) {
                // find a numerical value for this one
                int innerVal = 10000;
                while (stateIndex.get(innerVal) != null) {
                    innerVal++;
                }
                value = innerVal;
                //  FIXME: We will use the value as the name, as this is how the colors work, when value is a string, the name attribute will be ignored.
                name = valueStr;
                map.put(name, value);
            }
            String color = state.getAttribute(TmfXmlStrings.COLOR);
            addOrUpdateState(value, name, color);
        }
        stateTable = stateValues.toArray(new StateItem[stateValues.size()]);
        Display.getDefault().asyncExec(this::fireColorSettingsChanged);
        return map;
    }

    /**
     * Add a new state in the time graph view. This allow to define at runtime
     * new states that cannot be known at the conception of this analysis.
     *
     * @param name
     *            The string associated with the state
     * @return the value for this state
     */
    public synchronized int addState(String name) {
        // Find a value for this name, start at 10000
        int value = 10000;
        while (stateIndex.get(value) != null) {
            value++;
        }
        addOrUpdateState(value, name, ""); //$NON-NLS-1$
        Display.getDefault().asyncExec(this::fireColorSettingsChanged);
        return value;
    }

    private synchronized void addOrUpdateState(int value, String name, String color) {
        // FIXME Allow this case
        if (value < 0) {
            return;
        }

        final RGB colorRGB = (color.startsWith(TmfXmlStrings.COLOR_PREFIX)) ? parseColor(color) : calcColor(name);

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
        stateTable = stateValues.toArray(new StateItem[stateValues.size()]);
    }

    private static RGB parseColor(String color) {
        RGB colorRGB;
        Integer hex = Integer.parseInt(color.substring(1), 16);
        int hex1 = hex.intValue() % 256;
        int hex2 = (hex.intValue() / 256) % 256;
        int hex3 = (hex.intValue() / (256 * 256)) % 256;
        colorRGB = new RGB(hex3, hex2, hex1);
        return colorRGB;
    }

    /*
     * This method will always return the same color for a same name, no matter
     * the value, so that different traces with the same XML analysis will
     * display identically states with the same name.
     */
    private static RGB calcColor(String name) {
        long hash = name.hashCode(); // hashcodes can be Integer.MIN_VALUE.
        long base = COLOR_SEED[(int) (Math.abs(hash) % COLOR_SEED.length)];
        int x = (int) ((hash & COLOR_MASK) ^ base);
        final int r = (x >> 16) & 0xff;
        final int g = (x >> 8) & 0xff;
        final int b = x & 0xff;
        return new RGB(r, g, b);
    }

    /**
     * Return whether an integer value has a corresponding index in the
     * available states
     *
     * @param status
     *            The numerical status of the event
     * @return <code>true</code> if the numerical value is an existing value in
     *         the available states
     */
    public boolean hasIndex(int status) {
        return stateIndex.containsKey(status);
    }

    @Override
    public String getPreferenceKey() {
        return fId + '.' + super.getPreferenceKey();
    }
}
