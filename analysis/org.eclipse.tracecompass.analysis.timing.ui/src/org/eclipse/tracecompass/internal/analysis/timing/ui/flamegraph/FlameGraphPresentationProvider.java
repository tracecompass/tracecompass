/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.timing.ui.flamegraph;

import java.text.Format;
import java.text.NumberFormat;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProvider;
import org.eclipse.tracecompass.tmf.ui.symbols.SymbolProviderManager;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.StateItem;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.NullTimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils;

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

    private static final Format FORMATTER = new SubSecondTimeWithUnitFormat();

    private FlameGraphView fView;

    private Integer fAverageCharWidth;

    private enum State {
        MULTIPLE(new RGB(100, 100, 100)), EXEC(new RGB(0, 200, 0));

        private final RGB rgb;

        private State(RGB rgb) {
            this.rgb = rgb;
        }
    }

    /**
     * Constructor
     *
     */
    public FlameGraphPresentationProvider() {
    }

    @Override
    public StateItem[] getStateTable() {
        final float saturation = 0.6f;
        final float brightness = 0.6f;
        StateItem[] stateTable = new StateItem[NUM_COLORS + 1];
        stateTable[0] = new StateItem(State.MULTIPLE.rgb, State.MULTIPLE.toString());
        for (int i = 0; i < NUM_COLORS; i++) {
            RGB rgb = new RGB(i, saturation, brightness);
            stateTable[i + 1] = new StateItem(rgb, State.EXEC.toString());
        }
        return stateTable;
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
        return ImmutableMap.of(
                NonNullUtils.checkNotNull(Messages.FlameGraph_Duration), String.format("%s", FORMATTER.format(event.getDuration())), //$NON-NLS-1$
                NonNullUtils.checkNotNull(Messages.FlameGraph_SelfTime), String.format("%s", FORMATTER.format(((FlamegraphEvent) event).getSelfTime())), //$NON-NLS-1$
                NonNullUtils.checkNotNull(Messages.FlameGraph_NbCalls), NonNullUtils.checkNotNull(NumberFormat.getIntegerInstance().format(((FlamegraphEvent) event).getNbCalls())) // $NON-NLS-1$
        );
    }

    @Override
    public int getStateTableIndex(ITimeEvent event) {
        if (event instanceof FlamegraphEvent) {
            FlamegraphEvent flameGraphEvent = (FlamegraphEvent) event;
            return flameGraphEvent.getValue() + 1;
        } else if (event instanceof NullTimeEvent) {
            return INVISIBLE;
        }
        return State.MULTIPLE.ordinal();
    }

    /**
     * Get the event's symbol. It could be an address or a name.
     *
     * @param fGEvent
     *            An event
     * @param symbolProvider
     *            A symbol provider
     */
    private static String getFuntionSymbol(FlamegraphEvent event, ISymbolProvider symbolProvider) {
        String funcSymbol = ""; //$NON-NLS-1$
        if (event.getSymbol() instanceof TmfStateValue) {
            ITmfStateValue symbol = (ITmfStateValue) event.getSymbol();
            switch (symbol.getType()) {
            case LONG:
                Long longAddress = symbol.unboxLong();
                funcSymbol = symbolProvider.getSymbolText(longAddress);
                if (funcSymbol == null) {
                    return "0x" + Long.toHexString(longAddress); //$NON-NLS-1$
                }
                return funcSymbol;
            case STRING:
                return symbol.unboxStr();
            case INTEGER:
                Integer intAddress = symbol.unboxInt();
                funcSymbol = symbolProvider.getSymbolText(intAddress);
                if (funcSymbol == null) {
                    return "0x" + Integer.toHexString(intAddress); //$NON-NLS-1$
                }
                return funcSymbol;
            case CUSTOM:
            case DOUBLE:
            case NULL:
            default:
                break;
            }
        }
        return funcSymbol;
    }

    @Override
    public void postDrawEvent(ITimeEvent event, Rectangle bounds, GC gc) {
        if (fAverageCharWidth == null) {
            fAverageCharWidth = gc.getFontMetrics().getAverageCharWidth();
        }
        if (bounds.width <= fAverageCharWidth) {
            return;
        }
        if (!(event instanceof FlamegraphEvent)) {
            return;
        }
        String funcSymbol = ""; //$NON-NLS-1$
        ITmfTrace activeTrace = TmfTraceManager.getInstance().getActiveTrace();
        if (activeTrace != null) {
            FlamegraphEvent fgEvent = (FlamegraphEvent) event;
            ISymbolProvider symbolProvider = SymbolProviderManager.getInstance().getSymbolProvider(activeTrace);
            funcSymbol = getFuntionSymbol(fgEvent, symbolProvider);
        }
        gc.setForeground(gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
        Utils.drawText(gc, funcSymbol, bounds.x, bounds.y, bounds.width, bounds.height, true, true);
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
