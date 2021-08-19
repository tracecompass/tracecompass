/*******************************************************************************
 * Copyright (c) 2016, 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.profiling.ui.flamegraph;

import java.text.Format;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.analysis.profiling.core.base.FlameDefaultPalette;
import org.eclipse.tracecompass.common.core.format.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.AggregatedCalledFunctionStatistics;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.widgets.timegraph.ITimeGraphStylePresentationProvider;
import org.eclipse.tracecompass.tmf.core.model.IOutputElement;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.ui.colors.ColorUtils;
import org.eclipse.tracecompass.tmf.ui.model.StyleManager;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;

import com.google.common.collect.ImmutableMap;

/**
 * Presentation provider for the flame graph view, based on the generic TMF
 * presentation provider.
 *
 * @author Sonia Farrah
 */
public class FlameGraphPresentationProvider extends TimeGraphPresentationProvider implements ITimeGraphStylePresentationProvider {

    private static final OutputElementStyle TRANSPARENT_STYLE = new OutputElementStyle(null, ImmutableMap.of());

    private static final Format FORMATTER = SubSecondTimeWithUnitFormat.getInstance();
    private StyleManager fStyleManager = new StyleManager(FlameDefaultPalette.getStyles());
    private Map<String, Integer> fKeyToIndex = new HashMap<>();

    /** Number of colors used for flameGraph events */
    public static final int NUM_COLORS = FlameDefaultPalette.getStyles().size() + 1;

    private final StateItem[] fStateTable;
    private FlameGraphView fView;

    private enum State {
        MULTIPLE(new RGB(100, 100, 100)), EXEC(new RGB(0, 200, 0));

        private final RGB rgb;

        private State(RGB rgb) {
            this.rgb = rgb;
        }
    }

    /**
     * Constructor
     */
    public FlameGraphPresentationProvider() {
        fStateTable = new StateItem[NUM_COLORS];
        fStateTable[0] = new StateItem(State.MULTIPLE.rgb, State.MULTIPLE.toString());
        getStateTable();
    }

    @Override
    public @NonNull StyleManager getStyleManager() {
        return fStyleManager;
    }

    @Override
    public @Nullable Object getStyle(OutputElementStyle elementStyle, String property) {
        return fStyleManager.getStyle(elementStyle, property);
    }

    @Override
    public @Nullable Float getFloatStyle(OutputElementStyle elementStyle, String property) {
        return fStyleManager.getFactorStyle(elementStyle, property);
    }

    @Override
    public @Nullable RGBAColor getColorStyle(OutputElementStyle elementStyle, String property) {
        return fStyleManager.getColorStyle(elementStyle, property);
    }


    @Override
    public @Nullable OutputElementStyle getElementStyle(ITimeEvent event) {
        if (event instanceof NullTimeEvent) {
            return null;
        }

        if (event instanceof FlamegraphEvent) {
            IOutputElement model = ((FlamegraphEvent) event).getModel();
            OutputElementStyle eventStyle = model.getStyle();
            if (eventStyle == null) {
                String name = event.getLabel();
                Object key = name == null ? model.getValue() : name;
                eventStyle = FlameDefaultPalette.getStyleFor(key);
            }
            return eventStyle;
        }
        return TRANSPARENT_STYLE;
    }

    @Override
    public StateItem[] getStateTable() {
        if (fStateTable[1] == null) {
            Map<@NonNull String, @NonNull OutputElementStyle> styles = FlameDefaultPalette.getStyles();
            if (styles.isEmpty()) {
                for (int i = 0; i < NUM_COLORS; i++) {
                    fStateTable[i + 1] = new StateItem(Collections.emptyMap());
                }
                return fStateTable;
            }
            int tableIndex = 1;
            for (Entry<@NonNull String, @NonNull OutputElementStyle> styleEntry : styles.entrySet()) {
                String styleKey = styleEntry.getKey();
                fKeyToIndex.put(styleKey, tableIndex);
                OutputElementStyle elementStyle = styleEntry.getValue();
                Map<String, Object> styleMap = new HashMap<>();
                RGBAColor bgColor = getColorStyle(elementStyle, StyleProperties.BACKGROUND_COLOR);
                if (bgColor != null) {
                    RGB rgb = new RGB(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue());
                    styleMap.put(StyleProperties.BACKGROUND_COLOR, ColorUtils.toHexColor(rgb));
                }
                RGBAColor color = getColorStyle(elementStyle, StyleProperties.COLOR);
                if (color != null) {
                    RGB rgb = new RGB(color.getRed(), color.getGreen(), color.getBlue());
                    styleMap.put(StyleProperties.COLOR, ColorUtils.toHexColor(rgb));
                }
                Object styleName = getStyle(elementStyle, StyleProperties.STYLE_NAME);
                if (styleName instanceof String) {
                    styleMap.put(StyleProperties.STYLE_NAME, styleName);
                } else {
                    styleMap.put(StyleProperties.STYLE_NAME, styleEntry.getKey());
                }
                Float height = getFloatStyle(elementStyle, StyleProperties.HEIGHT);
                if (height != null) {
                    styleMap.put(StyleProperties.HEIGHT, height);
                }
                Float width = getFloatStyle(elementStyle, StyleProperties.WIDTH);
                if (width != null) {
                    styleMap.put(StyleProperties.WIDTH, width.intValue());
                }
                Object symbolType = getStyle(elementStyle, StyleProperties.SYMBOL_TYPE);
                if (symbolType instanceof String) {
                    styleMap.put(StyleProperties.SYMBOL_TYPE, symbolType);
                }
                Object styleGroup = getStyle(elementStyle, StyleProperties.STYLE_GROUP);
                if (styleGroup != null) {
                    styleMap.put(StyleProperties.STYLE_GROUP, styleGroup);
                }
                fStateTable[tableIndex] = new StateItem(styleMap);
                tableIndex++;
            }
        }
        return fStateTable;
    }

    @Override
    public boolean displayTimesInTooltip() {
        return false;
    }

    @Override
    public String getStateTypeName() {
        return Messages.FlameGraph_Depth;
    }

    @Override
    public Map<String, String> getEventHoverToolTipInfo(ITimeEvent event, long hoverTime) {
        FlamegraphEvent fgEvent = (FlamegraphEvent) event;
        AggregatedCalledFunctionStatistics statistics = fgEvent.getStatistics();
        ImmutableMap.Builder<String, String> builder = new ImmutableMap.Builder<>();
        builder.put(Messages.FlameGraph_Symbol, fgEvent.getLabel());
        builder.put(Messages.FlameGraph_NbCalls, NumberFormat.getIntegerInstance().format(statistics.getDurationStatistics().getNbElements())); // $NON-NLS-1$
        builder.put(String.valueOf(Messages.FlameGraph_Durations), ""); //$NON-NLS-1$
        builder.put("\t" + Messages.FlameGraph_Duration, FORMATTER.format(event.getDuration())); //$NON-NLS-1$
        builder.put("\t" + Messages.FlameGraph_AverageDuration, FORMATTER.format(statistics.getDurationStatistics().getMean())); // $NON-NLS-1$ //$NON-NLS-1$
        builder.put("\t" + Messages.FlameGraph_MaxDuration, FORMATTER.format((statistics.getDurationStatistics().getMaxNumber()))); // $NON-NLS-1$ //$NON-NLS-1$
        builder.put("\t" + Messages.FlameGraph_MinDuration, FORMATTER.format(statistics.getDurationStatistics().getMinNumber())); // $NON-NLS-1$ //$NON-NLS-1$
        builder.put("\t" + Messages.FlameGraph_Deviation, FORMATTER.format(statistics.getDurationStatistics().getStdDev())); //$NON-NLS-1$
        builder.put(Messages.FlameGraph_SelfTimes, ""); //$NON-NLS-1$
        builder.put("\t" + Messages.FlameGraph_SelfTime, FORMATTER.format(fgEvent.getSelfTime())); //$NON-NLS-1$
        builder.put("\t" + Messages.FlameGraph_AverageSelfTime, FORMATTER.format(statistics.getSelfTimeStatistics().getMean())); // $NON-NLS-1$ //$NON-NLS-1$
        builder.put("\t" + Messages.FlameGraph_MaxSelfTime, FORMATTER.format(statistics.getSelfTimeStatistics().getMax())); // $NON-NLS-1$ //$NON-NLS-1$
        builder.put("\t" + Messages.FlameGraph_MinSelfTime, FORMATTER.format(statistics.getSelfTimeStatistics().getMin())); // $NON-NLS-1$ //$NON-NLS-1$
        builder.put("\t" + Messages.FlameGraph_SelfTimeDeviation, FORMATTER.format(statistics.getSelfTimeStatistics().getStdDev())); //$NON-NLS-1$
        return builder.build();

    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof FlamegraphEvent) {
            FlamegraphEvent flameGraphEvent = (FlamegraphEvent) event;
            return Math.floorMod(String.valueOf(flameGraphEvent.getValue()).hashCode(), NUM_COLORS) + 1;
        } else if (event instanceof NullTimeEvent) {
            return INVISIBLE;
        }
        return State.MULTIPLE.ordinal();
    }

    /**
     * The flame graph view
     *
     * @return The flame graph view
     */
    public FlameGraphView getView() {
        return fView;
    }

    /**
     * The flame graph view
     *
     * @param view
     *            The flame graph view
     */
    public void setView(FlameGraphView view) {
        fView = view;
    }
}
