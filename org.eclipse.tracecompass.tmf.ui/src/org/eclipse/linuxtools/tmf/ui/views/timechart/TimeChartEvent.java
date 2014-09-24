/*******************************************************************************
 * Copyright (c) 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.timechart;

import java.util.ArrayList;
import java.util.Iterator;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.ui.views.colors.ColorSettingsManager;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;

/**
 * Event in the time chart view
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public class TimeChartEvent implements ITimeEvent {

    private static final byte TIMESTAMP_SCALE = -9;

    private final TimeChartAnalysisEntry fParentEntry;
    private long fTime;
    private long fDuration;
    private long fFirstRank;
    private long fLastRank;
    private final RankRangeList fRankRangeList;
    private long fNbEvents;
    private int fColorSettingPriority;
    private boolean fIsBookmark;
    private boolean fIsVisible;
    private boolean fIsSearchMatch;
    private TimeChartAnalysisEntry fItemizedEntry;
    private boolean fItemizing;

    /**
     * Standard constructor
     *
     * @param parentEntry
     *            The parent entry
     * @param event
     *            The event from which this time chart event originates
     * @param rank
     *            The rank of the event in the trace
     * @param decorationProvider
     *            The decoration provider to use
     */
    public TimeChartEvent(TimeChartAnalysisEntry parentEntry, ITmfEvent event,
            long rank, TimeChartDecorationProvider decorationProvider) {
        fParentEntry = parentEntry;
        fTime = event.getTimestamp().normalize(0, TIMESTAMP_SCALE).getValue();
        fDuration = 0;
        fFirstRank = fLastRank = rank;
        fRankRangeList = new RankRangeList(rank);
        fNbEvents = 1;
        fColorSettingPriority = ColorSettingsManager.getColorSettingPriority(event);
        fIsBookmark = decorationProvider.isBookmark(rank);
        fIsVisible = decorationProvider.isVisible(event);
        fIsSearchMatch = decorationProvider.isSearchMatch(event);
    }

    @Override
    public ITimeGraphEntry getEntry() {
        return fParentEntry;
    }

    @Override
    public long getTime() {
        return fTime;
    }

    @Override
    public long getDuration() {
        return fDuration;
    }

    /**
     * Retrieve the rank of the trace event which started this time event.
     *
     * @return The rank of the beginning
     */
    public long getFirstRank() {
        return fFirstRank;
    }

    /**
     * Retrieve the rank of the trace event which *finished* this time event.
     *
     * @return The rank of the end
     */
    public long getLastRank() {
        return fLastRank;
    }

    /**
     * Get the list of rank ranges corresponding to this time event.
     *
     * @return The rank range list
     */
    public RankRangeList getRankRangeList() {
        return fRankRangeList;
    }

    /**
     * Merge another time event with this one.
     *
     * @param event
     *            The other event
     */
    public void merge(TimeChartEvent event) {
        mergeDecorations(event);
        if (fTime == event.getTime() && fDuration == event.getDuration()) {
            return;
        }
        long endTime = Math.max(fTime + fDuration, event.getTime() + event.getDuration());
        fTime = Math.min(fTime, event.getTime());
        fDuration = endTime - fTime;
        fFirstRank = Math.min(fFirstRank, event.fFirstRank);
        fLastRank = Math.max(fLastRank, event.fLastRank);
        fNbEvents += event.fNbEvents;
        fItemizedEntry = null;
        synchronized (fRankRangeList) {
            fRankRangeList.merge(event.getRankRangeList());
        }
    }

    /**
     * Merge the decorations of another time event with the decorations of this
     * one.
     *
     * @param event
     *            The other event
     */
    public void mergeDecorations(TimeChartEvent event) {
        fColorSettingPriority = Math.min(fColorSettingPriority, event.getColorSettingPriority());
        fIsBookmark |= event.fIsBookmark;
        fIsVisible |= event.fIsVisible;
        fIsSearchMatch |= event.fIsSearchMatch;
    }

    /**
     * Get the number of time events that have been merged with this one (starts
     * counting at 1 if no merge happened).
     *
     * @return The current number of events in the bath
     */
    public long getNbEvents() {
        return fNbEvents;
    }

    /**
     * Retrieve the color setting priority.
     *
     * @return The priority
     */
    public int getColorSettingPriority() {
        return fColorSettingPriority;
    }

    /**
     * Set the color setting priority.
     *
     * @param priority
     *            The priority to set
     */
    public void setColorSettingPriority(int priority) {
        fColorSettingPriority = priority;
    }

    /**
     * Check if this time event is bookmarked
     *
     * @return Y/N
     */
    public boolean isBookmarked() {
        return fIsBookmark;
    }

    /**
     * Set this time event to be bookmarked or not.
     *
     * @param isBookmarked
     *            Should time time event become a bookmark, or not
     */
    public void setIsBookmarked(boolean isBookmarked) {
        fIsBookmark = isBookmarked;
    }

    /**
     * Check if this time is currently visible or not.
     *
     * @return If the event is visible
     */
    public boolean isVisible() {
        return fIsVisible;
    }

    /**
     * Set this time event to visible (or to non-visible).
     *
     * @param isVisible The new status
     */
    public void setIsVisible(boolean isVisible) {
        fIsVisible = isVisible;
    }

    /**
     * Check if the time event matches the current search.
     *
     * @return If it matches, Y/N
     */
    public boolean isSearchMatch() {
        return fIsSearchMatch;
    }

    /**
     * Mark this event as matching (or non-matching) the current search.
     *
     * @param isSearchMatch
     *            The new matching status
     */
    public void setIsSearchMatch(boolean isSearchMatch) {
        fIsSearchMatch = isSearchMatch;
    }

    /**
     * Set this event's itemized entry.
     *
     * @param timeAnalysisEntry
     *            The entry to set
     */
    public void setItemizedEntry(TimeChartAnalysisEntry timeAnalysisEntry) {
        fItemizedEntry = timeAnalysisEntry;
    }

    /**
     * Retrieve this event's itemized entry.
     *
     * @return The itemized entry that was previously set
     */
    public TimeChartAnalysisEntry getItemizedEntry() {
        return fItemizedEntry;
    }

    /**
     * @return Has this time event been set to itemizing?
     */
    public boolean isItemizing() {
        return fItemizing;
    }

    /**
     * Set this event's itemizing flag to true or false.
     *
     * @param itemizing
     *            The new value
     */
    public void setItemizing(boolean itemizing) {
        fItemizing = itemizing;
    }

    /**
     * Inner class to define a range in terms of ranks in the trace.
     *
     * @version 1.0
     * @author Patrick Tasse
     */
    public class RankRange {
        private long firstRank;
        private long lastRank;

        /**
         * Standard constructor
         *
         * @param firstRank
         *            The first (earliest) rank of the range
         * @param lastRank
         *            The last (latest) rank of the range
         */
        public RankRange(long firstRank, long lastRank) {
            this.firstRank = firstRank;
            this.lastRank = lastRank;
        }

        /**
         * Retrieve the start rank of this range.
         *
         * @return The first rank
         */
        public long getFirstRank() {
            return firstRank;
        }

        /**
         * Retrieve the end rank of this range
         *
         * @return The end rank
         */
        public long getLastRank() {
            return lastRank;
        }

        /**
         * Calculate the minimal distance between two RankRange's
         *
         * @param range
         *            The other range
         * @return The distance, in "number of events" between the two ranges
         */
        public long distanceFrom(RankRange range) {
            if (range.lastRank < fFirstRank) {
                return fFirstRank - range.lastRank;
            } else if (range.firstRank > fLastRank) {
                return range.firstRank - fLastRank;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return "["+firstRank+","+lastRank+"]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        }
    }

    private class RankRangeList extends ArrayList<RankRange> {

        private static final long serialVersionUID = 6060485531208535986L;

        public RankRangeList(long rank) {
            super(1);
            add(new RankRange(rank, rank));
        }

        public void merge(RankRangeList rankRangeList) {
            long threshold = fParentEntry.getTrace().getCacheSize();
            for (RankRange newRange : rankRangeList) {
                boolean merged = false;
                for (RankRange oldRange : fRankRangeList) {
                    if (newRange.distanceFrom(oldRange) <= threshold) {
                        oldRange.firstRank = Math.min(oldRange.firstRank, newRange.firstRank);
                        oldRange.lastRank = Math.max(oldRange.lastRank, newRange.lastRank);
                        merged = true;
                        break;
                    }
                }
                if (!merged) {
                    add(newRange);
                }
            }
            Iterator<RankRange> iterator = fRankRangeList.iterator();
            RankRange previous = null;
            while (iterator.hasNext()) {
                RankRange range = iterator.next();
                if (previous != null && range.distanceFrom(previous) <= threshold) {
                    previous.firstRank = Math.min(previous.firstRank, range.firstRank);
                    previous.lastRank = Math.max(previous.lastRank, range.lastRank);
                    iterator.remove();
                }
                previous = range;
            }
        }
    }
}
