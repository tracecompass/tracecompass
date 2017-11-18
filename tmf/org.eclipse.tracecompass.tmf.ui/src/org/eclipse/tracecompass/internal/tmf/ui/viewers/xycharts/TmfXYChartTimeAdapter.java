/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.xycharts;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.ITmfChartTimeProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.ITimeDataProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;

/**
 * Tmf Chart data provider wrapper to comply with Time data provider API
 *
 * @author Matthew Khouzam
 */
public final class TmfXYChartTimeAdapter implements ITimeDataProvider {

    private final TmfXYChartViewer fTimeProvider;

    /**
     * Constructor, requires a {@link ITmfChartTimeProvider}
     *
     * @param provider
     *            the provider to wrap
     */
    public TmfXYChartTimeAdapter(@NonNull TmfXYChartViewer provider) {
        fTimeProvider = provider;
    }

    @Override
    public long getBeginTime() {
        return fTimeProvider.getStartTime();
    }

    @Override
    public long getEndTime() {
        return fTimeProvider.getEndTime();
    }

    @Override
    public long getMinTimeInterval() {
        return 1L;
    }

    @Override
    public int getNameSpace() {
        // charts have no namespace
        return 0;
    }

    @Override
    public long getSelectionBegin() {
        return fTimeProvider.getSelectionBeginTime();
    }

    @Override
    public long getSelectionEnd() {
        return fTimeProvider.getSelectionEndTime();
    }

    @Override
    public long getTime0() {
        return fTimeProvider.getWindowStartTime();
    }

    @Override
    public long getTime1() {
        return fTimeProvider.getWindowEndTime();
    }

    @Override
    public void setSelectionRangeNotify(long beginTime, long endTime, boolean ensureVisible) {
        // Do nothing
    }

    @Override
    public void setSelectionRange(long beginTime, long endTime, boolean ensureVisible) {
        // Do nothing
    }

    @Override
    public long getMinTime() {
        return fTimeProvider.getStartTime();
    }

    @Override
    public long getMaxTime() {
        return fTimeProvider.getEndTime();
    }

    @Override
    public TimeFormat getTimeFormat() {
        return TimeFormat.CALENDAR;
    }

    @Override
    public int getTimeSpace() {
        return getAxisWidth();
    }

    /**
     * Get the width of the axis
     *
     * @return the width of the axis
     */
    public int getAxisWidth() {
        return fTimeProvider.getSwtChart().getPlotArea().getBounds().width;
    }

    // -------------------------------------------------------------------------
    // Override rest if need be
    // -------------------------------------------------------------------------

    @Override
    public void setStartFinishTimeNotify(long time0, long time1) {
        fTimeProvider.updateWindow(time0, time1);
    }

    @Override
    public void setStartFinishTime(long time0, long time1) {
        // Do nothing
    }

    @Override
    public void notifyStartFinishTime() {
        // Do nothing
    }

    @Override
    public void setSelectedTimeNotify(long time, boolean ensureVisible) {
        // Do nothing
    }

    @Override
    public void setSelectedTime(long time, boolean ensureVisible) {
        // Do nothing
    }

    @Override
    public void setNameSpace(int width) {
        // Do nothing
    }

}
