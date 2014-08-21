/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.widgets.Utils.TimeFormat;

/**
 * Time Data Provider wrapper that converts nanoseconds to cycles.
 *
 * The user of the wrapper uses cycles, the wrapped provider uses nanoseconds.
 * @since 3.1
 *
 */
public class TimeDataProviderCyclesConverter implements ITimeDataProviderConverter {

    private static final long GIGAHERTZ = 1000000000L;

    private final @NonNull ITimeDataProvider fProvider;
    private final long fFreq;

    /**
     * Constructor
     *
     * @param provider
     *            the original time data provider
     * @param clockFrequency
     *            the clock frequency in Hz
     */
    public TimeDataProviderCyclesConverter(@NonNull ITimeDataProvider provider, long clockFrequency) {
        fProvider = provider;
        fFreq = clockFrequency;
    }

    /**
     * Convert nanoseconds to cycles
     *
     * @param nanos
     *            time in nanoseconds
     * @return time in cycles
     */
    public long toCycles(long nanos) {
        return Math.round(nanos * ((double) fFreq / GIGAHERTZ));
    }

    /**
     * Convert cycles to nanoseconds
     *
     * @param cycles
     *            time in cycles
     * @return time in nanoseconds
     */
    public long toNanos(long cycles) {
        return Math.round(cycles * ((double) GIGAHERTZ / fFreq));
    }

    @Override
    public long convertTime(long time) {
        return toCycles(time);
    }

    @Override
    public void setSelectionRangeNotify(long beginTime, long endTime) {
        fProvider.setSelectionRangeNotify(toNanos(beginTime), toNanos(endTime));
    }

    @Override
    public void setSelectionRange(long beginTime, long endTime) {
        fProvider.setSelectionRange(toNanos(beginTime), toNanos(endTime));
    }

    @Override
    public long getSelectionBegin() {
        return toCycles(fProvider.getSelectionBegin());
    }

    @Override
    public long getSelectionEnd() {
        return toCycles(fProvider.getSelectionEnd());
    }

    @Override
    public long getBeginTime() {
        return toCycles(fProvider.getBeginTime());
    }

    @Override
    public long getEndTime() {
        return toCycles(fProvider.getEndTime());
    }

    @Override
    public long getMinTime() {
        return toCycles(fProvider.getMinTime());
    }

    @Override
    public long getMaxTime() {
        return toCycles(fProvider.getMaxTime());
    }

    @Override
    public long getTime0() {
        return toCycles(fProvider.getTime0());
    }

    @Override
    public long getTime1() {
        return toCycles(fProvider.getTime1());
    }

    @Override
    public long getMinTimeInterval() {
        // do not convert: this is in integer units
        return fProvider.getMinTimeInterval();
    }

    @Override
    public void setStartFinishTimeNotify(long time0, long time1) {
        fProvider.setStartFinishTimeNotify(toNanos(time0), toNanos(time1));
    }

    @Override
    public void setStartFinishTime(long time0, long time1) {
        fProvider.setStartFinishTime(toNanos(time0), toNanos(time1));
    }

    @Override
    public void notifyStartFinishTime() {
        fProvider.notifyStartFinishTime();
    }

    @Override
    public void setSelectedTimeNotify(long time, boolean ensureVisible) {
        fProvider.setSelectedTimeNotify(toNanos(time), ensureVisible);
    }

    @Override
    public void setSelectedTime(long time, boolean ensureVisible) {
        fProvider.setSelectedTime(toNanos(time), ensureVisible);
    }

    @Override
    public void resetStartFinishTime() {
        fProvider.resetStartFinishTime();
    }

    @Override
    public int getNameSpace() {
        return fProvider.getNameSpace();
    }

    @Override
    public void setNameSpace(int width) {
        fProvider.setNameSpace(width);
    }

    @Override
    public int getTimeSpace() {
        return fProvider.getTimeSpace();
    }

    @Override
    public TimeFormat getTimeFormat() {
        return fProvider.getTimeFormat();
    }

}
