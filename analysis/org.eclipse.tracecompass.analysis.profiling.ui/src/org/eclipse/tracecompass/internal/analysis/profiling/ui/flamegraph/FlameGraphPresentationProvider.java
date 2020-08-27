/*******************************************************************************
 * Copyright (c) 2016, 2019 Ericsson
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
import java.util.Map;

import org.eclipse.swt.graphics.RGB;
import org.eclipse.tracecompass.common.core.format.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.AggregatedCalledFunctionStatistics;
import org.eclipse.tracecompass.tmf.core.presentation.IPaletteProvider;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.presentation.RotatingPaletteProvider;
import org.eclipse.tracecompass.tmf.ui.colors.RGBAUtil;
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
public class FlameGraphPresentationProvider extends TimeGraphPresentationProvider {
    /** Number of colors used for flameGraph events */
    public static final int NUM_COLORS = 360;

    private static final Format FORMATTER = SubSecondTimeWithUnitFormat.getInstance();

    private final StateItem[] fStateTable;
    private FlameGraphView fView;

    private IPaletteProvider fPalette = new RotatingPaletteProvider.Builder().setNbColors(NUM_COLORS).build();

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
        fStateTable = new StateItem[NUM_COLORS + 1];
        fStateTable[0] = new StateItem(State.MULTIPLE.rgb, State.MULTIPLE.toString());
        int i = 1;
        for (RGBAColor color : fPalette.get()) {
            fStateTable[i] = new StateItem(RGBAUtil.fromRGBAColor(color).rgb, color.toString());
            i++;
        }
    }

    @Override
    public StateItem[] getStateTable() {
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
            return Math.floorMod(flameGraphEvent.getValue(), fPalette.get().size()) + 1;
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
