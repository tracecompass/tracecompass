/**********************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.views;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties.SymbolType;
import org.eclipse.tracecompass.tmf.ui.colors.ColorUtils;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry.DisplayStyle;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.MarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeLinkEvent;

import com.google.common.collect.ImmutableMap;

/**
 * Simple presentation provider to test the time graph widgets
 *
 * @author Matthew Khouzam
 */
class StubPresentationProvider extends TimeGraphPresentationProvider {

    private static final @NonNull String MARKER_COLOR_HEX = ColorUtils.toHexColor(160, 170, 200);
    private static final float OPACITY = (float) 80 / 255;

    @Override
    public String getPreferenceKey() {
        return "Stub";
    }

    private static final List<StateItem> SYMBOLS = Arrays.asList(
            new StateItem(ImmutableMap.of(StyleProperties.SYMBOL_TYPE, SymbolType.DIAMOND, StyleProperties.COLOR, MARKER_COLOR_HEX, StyleProperties.OPACITY, OPACITY, StyleProperties.HEIGHT, 1.0f)),
            new StateItem(ImmutableMap.of(StyleProperties.SYMBOL_TYPE, SymbolType.CIRCLE, StyleProperties.COLOR, MARKER_COLOR_HEX, StyleProperties.OPACITY, OPACITY, StyleProperties.HEIGHT, 1.0f)),
            new StateItem(ImmutableMap.of(StyleProperties.SYMBOL_TYPE, SymbolType.SQUARE, StyleProperties.COLOR, MARKER_COLOR_HEX, StyleProperties.OPACITY, OPACITY, StyleProperties.HEIGHT, 1.0f)),
            new StateItem(ImmutableMap.of(StyleProperties.SYMBOL_TYPE, SymbolType.TRIANGLE, StyleProperties.COLOR, MARKER_COLOR_HEX, StyleProperties.OPACITY, OPACITY, StyleProperties.HEIGHT, 1.0f)),
            new StateItem(ImmutableMap.of(StyleProperties.SYMBOL_TYPE, SymbolType.INVERTED_TRIANGLE, StyleProperties.COLOR, MARKER_COLOR_HEX, StyleProperties.OPACITY, OPACITY, StyleProperties.HEIGHT, 1.0f)),
            new StateItem(ImmutableMap.of(StyleProperties.SYMBOL_TYPE, SymbolType.CROSS, StyleProperties.COLOR, MARKER_COLOR_HEX, StyleProperties.OPACITY, OPACITY, StyleProperties.HEIGHT, 1.0f)),
            new StateItem(ImmutableMap.of(StyleProperties.SYMBOL_TYPE, SymbolType.PLUS, StyleProperties.COLOR, MARKER_COLOR_HEX, StyleProperties.OPACITY, OPACITY, StyleProperties.HEIGHT, 1.0f)));

    private static final StateItem LASER = new StateItem(
            ImmutableMap.of(
                    StyleProperties.STYLE_NAME, "\"LASER\"",
                    StyleProperties.HEIGHT, 0.1f,
                    StyleProperties.BACKGROUND_COLOR, "#ff0000",
                    StyleProperties.COLOR, "#ff0000"));

    private static final StateItem PULSE = new StateItem(
            ImmutableMap.of(StyleProperties.STYLE_NAME, "PULSE"));

    /**
     * States, visible since they are tested
     */
    public static final StateItem[] STATES = {
            new StateItem(new RGB(0, 255, 0), "HAT"),
            new StateItem(new RGB(128, 192, 255), "SKIN"),
            new StateItem(new RGB(0, 64, 128), "HAIR"),
            new StateItem(new RGB(0, 0, 255), "EYE"),
            new StateItem(new RGB(255, 64, 128), "PUCK"),
            LASER,
            PULSE
    };

    @Override
    public StateItem[] getStateTable() {
        return STATES;
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof TimeLinkEvent) {
            return 5;
        }
        if (event.getEntry() != null && DisplayStyle.LINE.equals(event.getEntry().getStyle())) {
            return 6;
        }

        if (event instanceof TimeEvent) {
            return ((TimeEvent) event).getValue();
        }
        return -1;
    }

    @Override
    public int getItemHeight(ITimeGraphEntry entry) {
        int itemHeight = super.getItemHeight(entry);
        return entry.getParent() == null ? itemHeight + 3 : itemHeight;
    }

    @Override
    public Map<String, Object> getSpecificEventStyle(ITimeEvent event) {
        if (event instanceof MarkerEvent && event.getDuration() == 0) {
            MarkerEvent markerEvent = (MarkerEvent) event;
            int value = markerEvent.getValue();
            return SYMBOLS.get(value).getStyleMap();
        }
        return super.getSpecificEventStyle(event);
    }

}
