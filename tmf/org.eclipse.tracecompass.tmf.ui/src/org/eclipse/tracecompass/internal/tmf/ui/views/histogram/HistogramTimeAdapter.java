/*******************************************************************************
 * Copyright (c) 2018, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.views.histogram;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.ui.views.histogram.HistogramDataModel;
import org.eclipse.tracecompass.tmf.ui.views.histogram.HistogramScaledData;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.ITimeDataProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;

/**
 * HistogramScaledData wrapper to comply with Time data provider API.
 *
 * @author Simon Delisle
 */
public class HistogramTimeAdapter implements ITimeDataProvider {

    private final HistogramDataModel fHistogram;
    private int fTimeScale = 1;
    private TimeFormat fTimeFormat;

    /**
     * Constructor.
     *
     * @param histogram
     *            {@link HistogramScaledData} to wrap
     */
    public HistogramTimeAdapter(@NonNull HistogramDataModel histogram) {
        fHistogram = histogram;
    }

    @Override
    public void setSelectionRangeNotify(long beginTime, long endTime, boolean ensureVisible) {
        // Nothing to do
    }

    @Override
    public void setSelectionRange(long beginTime, long endTime, boolean ensureVisible) {
        // Nothing to do
    }

    @Override
    public long getSelectionBegin() {
        return fHistogram.getSelectionBegin();
    }

    @Override
    public long getSelectionEnd() {
        return fHistogram.getSelectionEnd();
    }

    @Override
    public long getBeginTime() {
        return fHistogram.getStartTime();
    }

    @Override
    public long getEndTime() {
        return fHistogram.getEndTime();
    }

    @Override
    public long getMinTime() {
        return fHistogram.getSelectionBegin();
    }

    @Override
    public long getMaxTime() {
        return getEndTime();
    }

    @Override
    public long getTime0() {
        return fHistogram.getStartTime();
    }

    @Override
    public long getTime1() {
        return getEndTime();
    }

    @Override
    public long getMinTimeInterval() {
        return fHistogram.getBucketDuration();
    }

    @Override
    public void setStartFinishTimeNotify(long time0, long time1) {
        // Nothing to do
    }

    @Override
    public void setStartFinishTime(long time0, long time1) {
        // Nothing to do
    }

    @Override
    public void notifyStartFinishTime() {
        // Nothing to do
    }

    @Override
    public void setSelectedTimeNotify(long time, boolean ensureVisible) {
        // Nothing to do
    }

    @Override
    public void setSelectedTime(long time, boolean ensureVisible) {
        // Nothing to do
    }

    @Override
    public int getNameSpace() {
        return 0;
    }

    @Override
    public void setNameSpace(int width) {
        // Nothing to do, no name space
    }

    @Override
    public int getTimeSpace() {
        return fTimeScale;
    }

    @Override
    public TimeFormat getTimeFormat() {
        return fTimeFormat;
    }

    /**
     * Set time space
     *
     * @param timeSpace
     *            the time space width
     */
    public void setTimeSpace(int timeSpace) {
        fTimeScale = timeSpace;
    }

    /**
     * Set the time format
     *
     * @param timeFormat
     *            the time format
     */
    public void setTimeFormat(org.eclipse.tracecompass.tmf.ui.views.FormatTimeUtils.TimeFormat timeFormat) {
        fTimeFormat = TimeFormat.convert(timeFormat);
    }
}
