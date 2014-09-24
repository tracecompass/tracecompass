/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Bernd Hufmann - Implementation of new interfaces/listeners and support for
 *                   time stamp in any order
 *   Francois Chouinard - Moved from LTTng to TMF
 *   Francois Chouinard - Added support for empty initial buckets
 *   Patrick Tasse - Support selection range
 *   Jean-Christian Kouamé, Simon Delisle - Added support to manage lost events
 *   Xavier Raynaud - Support multi-trace coloring
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;

/**
 * Histogram-independent data model.
 *
 * It has the following characteristics:
 * <ul>
 * <li>The <i>basetime</i> is the timestamp of the first event
 * <li>There is a fixed number (<i>n</i>) of buckets of uniform duration
 * (<i>d</i>)
 * <li>The <i>timespan</i> of the model is thus: <i>n</i> * <i>d</i> time units
 * <li>Bucket <i>i</i> holds the number of events that occurred in time range:
 * [<i>basetime</i> + <i>i</i> * <i>d</i>, <i>basetime</i> + (<i>i</i> + 1) *
 * <i>d</i>)
 * </ul>
 * Initially, the bucket durations is set to 1ns. As the events are read, they
 * are tallied (using <i>countEvent()</i>) in the appropriate bucket (relative
 * to the <i>basetime</i>).
 * <p>
 * Eventually, an event will have a timestamp that exceeds the <i>timespan</i>
 * high end (determined by <i>n</i>, the number of buckets, and <i>d</i>, the
 * bucket duration). At this point, the histogram needs to be compacted. This is
 * done by simply merging adjacent buckets by pair, in effect doubling the
 * <i>timespan</i> (<i>timespan'</i> = <i>n</i> * <i>d'</i>, where <i>d'</i> =
 * 2<i>d</i>). This compaction happens as needed as the trace is read.
 * <p>
 * The model allows for timestamps in not increasing order. The timestamps can
 * be fed to the model in any order. If an event has a timestamp less than the
 * <i>basetime</i>, the buckets will be moved to the right to account for the
 * new smaller timestamp. The new <i>basetime</i> is a multiple of the bucket
 * duration smaller then the previous <i>basetime</i>. Note that the
 * <i>basetime</i> might no longer be the timestamp of an event. If necessary,
 * the buckets will be compacted before moving to the right. This might be
 * necessary to not lose any event counts at the end of the buckets array.
 * <p>
 * The mapping from the model to the UI is performed by the <i>scaleTo()</i>
 * method. By keeping the number of buckets <i>n</i> relatively large with
 * respect to to the number of pixels in the actual histogram, we should achieve
 * a nice result when visualizing the histogram.
 * <p>
 *
 * @version 2.0
 * @author Francois Chouinard
 */
public class HistogramDataModel implements IHistogramDataModel {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The default number of buckets
     */
    public static final int DEFAULT_NUMBER_OF_BUCKETS = 16 * 1000;

    /**
     * Number of events after which listeners will be notified.
     */
    public static final int REFRESH_FREQUENCY = DEFAULT_NUMBER_OF_BUCKETS;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // Trace management
    private ITmfTrace fTrace = null;
    private final Map<ITmfTrace, Integer> fTraceMap = new LinkedHashMap<>();

    // Bucket management
    private final int fNbBuckets;
    private final HistogramBucket[] fBuckets;
    private final long[] fLostEventsBuckets;
    private long fBucketDuration;
    private long fNbEvents;
    private int fLastBucket;

    // Timestamps
    private long fFirstBucketTime; // could be negative when analyzing events with descending order!!!
    private long fFirstEventTime;
    private long fEndTime;
    private long fSelectionBegin;
    private long fSelectionEnd;
    private long fTimeLimit;

    // Private listener lists
    private final ListenerList fModelListeners;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor with default number of buckets.
     */
    public HistogramDataModel() {
        this(0, DEFAULT_NUMBER_OF_BUCKETS);
    }

    /**
     * Default constructor with default number of buckets.
     *
     * @param startTime
     *            The histogram start time
     * @since 2.0
     */
    public HistogramDataModel(long startTime) {
        this(startTime, DEFAULT_NUMBER_OF_BUCKETS);
    }

    /**
     * Constructor with non-default number of buckets.
     *
     * @param nbBuckets
     *            A number of buckets.
     */
    public HistogramDataModel(int nbBuckets) {
        this(0, nbBuckets);
    }

    /**
     * Constructor with non-default number of buckets.
     *
     * @param startTime
     *            the histogram start time
     * @param nbBuckets
     *            A number of buckets.
     * @since 2.0
     */
    public HistogramDataModel(long startTime, int nbBuckets) {
        fFirstBucketTime = fFirstEventTime = fEndTime = startTime;
        fNbBuckets = nbBuckets;
        fBuckets = new HistogramBucket[nbBuckets];
        fLostEventsBuckets = new long[nbBuckets];
        fModelListeners = new ListenerList();
        clear();
    }

    /**
     * Copy constructor.
     *
     * @param other
     *            A model to copy.
     */
    public HistogramDataModel(HistogramDataModel other) {
        fNbBuckets = other.fNbBuckets;
        fBuckets = new HistogramBucket[fNbBuckets];
        for (int i = 0; i < fNbBuckets; i++) {
            fBuckets[i] = new HistogramBucket(other.fBuckets[i]);
        }
        fLostEventsBuckets = Arrays.copyOf(other.fLostEventsBuckets, fNbBuckets);
        fBucketDuration = Math.max(other.fBucketDuration, 1);
        fNbEvents = other.fNbEvents;
        fLastBucket = other.fLastBucket;
        fFirstBucketTime = other.fFirstBucketTime;
        fFirstEventTime = other.fFirstEventTime;
        fEndTime = other.fEndTime;
        fSelectionBegin = other.fSelectionBegin;
        fSelectionEnd = other.fSelectionEnd;
        fTimeLimit = other.fTimeLimit;
        fModelListeners = new ListenerList();
        Object[] listeners = other.fModelListeners.getListeners();
        for (Object listener : listeners) {
            fModelListeners.add(listener);
        }
    }


    /**
     * Disposes the data model
     * @since 3.0
     */
    public void dispose() {
        fTraceMap.clear();
        fTrace = null;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the number of events in the data model.
     *
     * @return number of events.
     */
    public long getNbEvents() {
        return fNbEvents;
    }

    /**
     * Returns the number of buckets in the model.
     *
     * @return number of buckets.
     */
    public int getNbBuckets() {
        return fNbBuckets;
    }

    /**
     * Returns the current bucket duration.
     *
     * @return bucket duration
     */
    public long getBucketDuration() {
        return fBucketDuration;
    }

    /**
     * Returns the time value of the first bucket in the model.
     *
     * @return time of first bucket.
     */
    public long getFirstBucketTime() {
        return fFirstBucketTime;
    }

    /**
     * Returns the time of the first event in the model.
     *
     * @return time of first event.
     */
    public long getStartTime() {
        return fFirstEventTime;
    }

    /**
     * Sets the trace of this model.
     * @param trace - a {@link ITmfTrace}
     * @since 3.0
     */
    public void setTrace(ITmfTrace trace) {
        this.fTrace = trace;
        fTraceMap.clear();
        ITmfTrace[] traces = TmfTraceManager.getTraceSet(fTrace);
        if (traces != null) {
            int i = 0;
            for (ITmfTrace tr : traces) {
                fTraceMap.put(tr, i);
                i++;
            }
        }
    }

    /**
     * Gets the trace of this model.
     * @return a {@link ITmfTrace}
     * @since 3.0
     */
    public ITmfTrace getTrace() {
        return this.fTrace;
    }

    /**
     * Gets the traces names of this model.
     * @return an array of trace names
     * @since 3.0
     */
    public String[] getTraceNames() {
        ITmfTrace[] traces = TmfTraceManager.getTraceSet(fTrace);
        if (traces == null) {
            return new String[0];
        }
        String[] traceNames = new String[traces.length];
        int i = 0;
        for (ITmfTrace tr : traces) {
            traceNames[i] = tr.getName();
            i++;
        }
        return traceNames;
    }

    /**
     * Gets the number of traces of this model.
     * @return the number of traces of this model.
     * @since 3.0
     */
    public int getNbTraces() {
        ITmfTrace[] traces = TmfTraceManager.getTraceSet(fTrace);
        if (traces == null) {
            return 1; //
        }
        return traces.length;
    }

    /**
     * Sets the model start time
     *
     * @param startTime
     *            the histogram range start time
     * @param endTime
     *            the histogram range end time
     * @since 2.0
     */
    public void setTimeRange(long startTime, long endTime) {
        fFirstBucketTime = fFirstEventTime = fEndTime = startTime;
        fBucketDuration = 1;
        updateEndTime();
        while (endTime >= fTimeLimit) {
            mergeBuckets();
        }
    }

    /**
     * Set the end time. Setting this ensures that the corresponding bucket is
     * displayed regardless of the event counts.
     *
     * @param endTime
     *            the time of the last used bucket
     * @since 2.2
     */
    public void setEndTime(long endTime) {
        fEndTime = endTime;
        fLastBucket = (int) ((endTime - fFirstBucketTime) / fBucketDuration);
    }

    /**
     * Returns the end time.
     *
     * @return the time of the last used bucket
     */
    public long getEndTime() {
        return fEndTime;
    }

    /**
     * Returns the begin time of the current selection in the model.
     *
     * @return the begin time of the current selection.
     * @since 2.1
     */
    public long getSelectionBegin() {
        return fSelectionBegin;
    }

    /**
     * Returns the end time of the current selection in the model.
     *
     * @return the end time of the current selection.
     * @since 2.1
     */
    public long getSelectionEnd() {
        return fSelectionEnd;
    }

    /**
     * Returns the time limit with is: start time + nbBuckets * bucketDuration
     *
     * @return the time limit.
     */
    public long getTimeLimit() {
        return fTimeLimit;
    }

    // ------------------------------------------------------------------------
    // Listener handling
    // ------------------------------------------------------------------------

    /**
     * Add a listener to the model to be informed about model changes.
     *
     * @param listener
     *            A listener to add.
     */
    public void addHistogramListener(IHistogramModelListener listener) {
        fModelListeners.add(listener);
    }

    /**
     * Remove a given model listener.
     *
     * @param listener
     *            A listener to remove.
     */
    public void removeHistogramListener(IHistogramModelListener listener) {
        fModelListeners.remove(listener);
    }

    // Notify listeners (always)
    private void fireModelUpdateNotification() {
        fireModelUpdateNotification(0);
    }

    // Notify listener on boundary
    private void fireModelUpdateNotification(long count) {
        if ((count % REFRESH_FREQUENCY) == 0) {
            Object[] listeners = fModelListeners.getListeners();
            for (Object listener2 : listeners) {
                IHistogramModelListener listener = (IHistogramModelListener) listener2;
                listener.modelUpdated();
            }
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void complete() {
        fireModelUpdateNotification();
    }

    /**
     * Clear the histogram model.
     *
     * @see org.eclipse.linuxtools.tmf.ui.views.distribution.model.IBaseDistributionModel#clear()
     */
    @Override
    public void clear() {
        Arrays.fill(fBuckets, null);
        Arrays.fill(fLostEventsBuckets, 0);
        fNbEvents = 0;
        fFirstBucketTime = 0;
        fEndTime = 0;
        fSelectionBegin = 0;
        fSelectionEnd = 0;
        fLastBucket = 0;
        fBucketDuration = 1;
        updateEndTime();
        fireModelUpdateNotification();
    }

    /**
     * Sets the current selection time range (no notification of listeners)
     *
     * @param beginTime
     *            The selection begin time.
     * @param endTime
     *            The selection end time.
     * @since 2.1
     */
    public void setSelection(long beginTime, long endTime) {
        fSelectionBegin = beginTime;
        fSelectionEnd = endTime;
    }

    /**
     * Sets the current selection time range with notification of listeners
     *
     * @param beginTime
     *            The selection begin time.
     * @param endTime
     *            The selection end time.
     * @since 2.1
     */
    public void setSelectionNotifyListeners(long beginTime, long endTime) {
        fSelectionBegin = beginTime;
        fSelectionEnd = endTime;
        fireModelUpdateNotification();
    }

    /**
     * Add event to the correct bucket, compacting the if needed.
     *
     * @param eventCount
     *            The current event Count (for notification purposes)
     * @param timestamp
     *            The timestamp of the event to count
     * @param trace
     *            The event trace
     * @since 3.0
     */
    @Override
    public void countEvent(long eventCount, long timestamp, ITmfTrace trace) {

        // Validate
        if (timestamp < 0) {
            return;
        }

        // Set the start/end time if not already done
        if ((fFirstBucketTime == 0) && (fLastBucket == 0) && (fBuckets[0] == null) && (timestamp > 0)) {
            fFirstBucketTime = timestamp;
            fFirstEventTime = timestamp;
            updateEndTime();
        }

        if (timestamp < fFirstEventTime) {
            fFirstEventTime = timestamp;
        }

        if (fEndTime < timestamp) {
            fEndTime = timestamp;
        }

        if (timestamp >= fFirstBucketTime) {

            // Compact as needed
            while (timestamp >= fTimeLimit) {
                mergeBuckets();
            }

        } else {

            // get offset for adjustment
            int offset = getOffset(timestamp);

            // Compact as needed
            while ((fLastBucket + offset) >= fNbBuckets) {
                mergeBuckets();
                offset = getOffset(timestamp);
            }

            moveBuckets(offset);

            fLastBucket = fLastBucket + offset;

            fFirstBucketTime = fFirstBucketTime - (offset * fBucketDuration);
            updateEndTime();
        }

        // Increment the right bucket
        int index = (int) ((timestamp - fFirstBucketTime) / fBucketDuration);
        if (fBuckets[index] == null) {
            fBuckets[index] = new HistogramBucket(getNbTraces());
        }
        Integer traceIndex = fTraceMap.get(trace);
        if (traceIndex == null) {
            traceIndex = 0;
        }
        fBuckets[index].addEvent(traceIndex);
        fNbEvents++;
        if (fLastBucket < index) {
            fLastBucket = index;
        }

        fireModelUpdateNotification(eventCount);
    }

    /**
     * Add lost event to the correct bucket, compacting the if needed.
     *
     * @param timeRange
     *            time range of a lost event
     * @param nbLostEvents
     *            the number of lost events
     * @param fullRange
     *            Full range or time range for histogram request
     * @since 2.2
     */
    public void countLostEvent(TmfTimeRange timeRange, long nbLostEvents, boolean fullRange) {

        // Validate
        if (timeRange.getStartTime().getValue() < 0 || timeRange.getEndTime().getValue() < 0) {
            return;
        }

        // Compact as needed
        if (fullRange) {
            while (timeRange.getEndTime().getValue() >= fTimeLimit) {
                mergeBuckets();
            }
        }

        int indexStart = (int) ((timeRange.getStartTime().getValue() - fFirstBucketTime) / fBucketDuration);
        int indexEnd = (int) ((timeRange.getEndTime().getValue() - fFirstBucketTime) / fBucketDuration);
        int nbBucketRange = (indexEnd - indexStart) + 1;

        int lostEventPerBucket = (int) Math.ceil((double) nbLostEvents / nbBucketRange);
        long lastLostCol = Math.max(1, nbLostEvents - lostEventPerBucket * (nbBucketRange - 1));

        // Increment the right bucket, bear in mind that ranges make it almost certain that some lost events are out of range
        for (int index = indexStart; index <= indexEnd && index < fLostEventsBuckets.length; index++) {
            if (index == (indexStart + nbBucketRange - 1)) {
                fLostEventsBuckets[index] += lastLostCol;
            } else {
                fLostEventsBuckets[index] += lostEventPerBucket;
            }
        }

        fNbEvents++;

        fireModelUpdateNotification(nbLostEvents);
    }

    /**
     * Scale the model data to the width, height and bar width requested.
     *
     * @param width
     *            A width of the histogram canvas
     * @param height
     *            A height of the histogram canvas
     * @param barWidth
     *            A width (in pixel) of a histogram bar
     * @return the result array of size [width] and where the highest value
     *         doesn't exceed [height]
     *
     * @see org.eclipse.linuxtools.tmf.ui.views.histogram.IHistogramDataModel#scaleTo(int,
     *      int, int)
     */
    @Override
    public HistogramScaledData scaleTo(int width, int height, int barWidth) {
        // Basic validation
        if ((width <= 0) || (height <= 0) || (barWidth <= 0))
        {
            throw new AssertionError("Invalid histogram dimensions (" + width + "x" + height + ", barWidth=" + barWidth + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
        }

        // The result structure
        HistogramScaledData result = new HistogramScaledData(width, height, barWidth);

        // Scale horizontally
        result.fMaxValue = 0;

        int nbBars = width / barWidth;
        int bucketsPerBar = (fLastBucket / nbBars) + 1;
        result.fBucketDuration = Math.max(bucketsPerBar * fBucketDuration, 1);
        for (int i = 0; i < nbBars; i++) {
            int count = 0;
            int countLostEvent = 0;
            result.fData[i] = new HistogramBucket(getNbTraces());
            for (int j = i * bucketsPerBar; j < ((i + 1) * bucketsPerBar); j++) {
                if (fNbBuckets <= j) {
                    break;
                }
                if (fBuckets[j] != null) {
                    count += fBuckets[j].getNbEvents();
                    result.fData[i].add(fBuckets[j]);
                }
                countLostEvent += fLostEventsBuckets[j];
            }
            result.fLostEventsData[i] = countLostEvent;
            result.fLastBucket = i;
            if (result.fMaxValue < count) {
                result.fMaxValue = count;
            }
            if (result.fMaxCombinedValue < count + countLostEvent) {
                result.fMaxCombinedValue = count + countLostEvent;
            }
        }

        // Scale vertically
        if (result.fMaxValue > 0) {
            result.fScalingFactor = (double) height / result.fMaxValue;
        }
        if (result.fMaxCombinedValue > 0) {
            result.fScalingFactorCombined = (double) height / result.fMaxCombinedValue;
        }

        fBucketDuration = Math.max(fBucketDuration, 1);
        // Set selection begin and end index in the scaled histogram
        result.fSelectionBeginBucket = (int) ((fSelectionBegin - fFirstBucketTime) / fBucketDuration) / bucketsPerBar;
        result.fSelectionEndBucket = (int) ((fSelectionEnd - fFirstBucketTime) / fBucketDuration) / bucketsPerBar;

        result.fFirstBucketTime = fFirstBucketTime;
        result.fFirstEventTime = fFirstEventTime;
        return result;
    }

    // ------------------------------------------------------------------------
    // Helper functions
    // ------------------------------------------------------------------------

    private void updateEndTime() {
        fTimeLimit = fFirstBucketTime + (fNbBuckets * fBucketDuration);
    }

    private void mergeBuckets() {
        for (int i = 0; i < (fNbBuckets / 2); i++) {
            fBuckets[i] = new HistogramBucket(fBuckets[2 * i], fBuckets[(2 * i) + 1]);
            fLostEventsBuckets[i] = fLostEventsBuckets[2 * i] + fLostEventsBuckets[(2 * i) + 1];
        }
        Arrays.fill(fBuckets, fNbBuckets / 2, fNbBuckets, null);
        Arrays.fill(fLostEventsBuckets, fNbBuckets / 2, fNbBuckets, 0);
        fBucketDuration *= 2;
        updateEndTime();
        fLastBucket = (fNbBuckets / 2) - 1;
    }

    private void moveBuckets(int offset) {
        for (int i = fNbBuckets - 1; i >= offset; i--) {
            fBuckets[i] = new HistogramBucket(fBuckets[i - offset]);
            fLostEventsBuckets[i] = fLostEventsBuckets[i - offset];
        }

        for (int i = 0; i < offset; i++) {
            fBuckets[i] = null;
            fLostEventsBuckets[i] = 0;
        }
    }

    private int getOffset(long timestamp) {
        int offset = (int) ((fFirstBucketTime - timestamp) / fBucketDuration);
        if (((fFirstBucketTime - timestamp) % fBucketDuration) != 0) {
            offset++;
        }
        return offset;
    }
}
