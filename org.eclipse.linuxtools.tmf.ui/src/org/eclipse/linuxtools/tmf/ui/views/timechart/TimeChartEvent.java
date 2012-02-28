/*******************************************************************************
 * Copyright (c) 2010 Ericsson
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
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITimeEvent;
import org.eclipse.linuxtools.tmf.ui.viewers.timeAnalysis.model.ITmfTimeAnalysisEntry;
import org.eclipse.linuxtools.tmf.ui.views.colors.ColorSettingsManager;

public class TimeChartEvent implements ITimeEvent {

    private static final byte TIMESTAMP_SCALE = -9;
    
    private TimeChartAnalysisEntry fParentEntry;
    private long fTime;
    private long fDuration;
    private long fFirstRank;
    private long fLastRank;
    private RankRangeList fRankRangeList;
    private long fNbEvents;
    private int fColorSettingPriority;
    private boolean fIsBookmark;
    private boolean fIsVisible;
    private boolean fIsSearchMatch;
    private TimeChartAnalysisEntry fItemizedEntry;
    private boolean fItemizing;

    public TimeChartEvent(TimeChartAnalysisEntry parentEntry, ITmfEvent event, long rank, TimeChartDecorationProvider decorationProvider) {
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
    public ITmfTimeAnalysisEntry getEntry() {
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

    public long getFirstRank() {
        return fFirstRank;
    }
    
    public long getLastRank() {
        return fLastRank;
    }
    
    public RankRangeList getRankRangeList() {
        return fRankRangeList;
    }
    
    public void merge(TimeChartEvent event) {
    	mergeDecorations(event);
        if (fTime == event.getTime() && fDuration == event.getDuration()) return;
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

    public void mergeDecorations(TimeChartEvent event) {
        fColorSettingPriority = Math.min(fColorSettingPriority, event.getColorSettingPriority());
        fIsBookmark |= event.fIsBookmark;
    	fIsVisible |= event.fIsVisible;
    	fIsSearchMatch |= event.fIsSearchMatch;
    }
    
    public long getNbEvents() {
        return fNbEvents;
    }

    public int getColorSettingPriority() {
    	return fColorSettingPriority;
    }
    
    public void setColorSettingPriority(int priority) {
    	fColorSettingPriority = priority;
    }
    
    public boolean isBookmarked() {
    	return fIsBookmark;
    }

    public void setIsBookmarked(boolean isBookmarked) {
    	fIsBookmark = isBookmarked;
    }
    
    public boolean isVisible() {
    	return fIsVisible;
    }

    public void setIsVisible(boolean isVisible) {
    	fIsVisible = isVisible;
    }
    
    public boolean isSearchMatch() {
    	return fIsSearchMatch;
    }

    public void setIsSearchMatch(boolean isSearchMatch) {
    	fIsSearchMatch = isSearchMatch;
    }
    
    public void setItemizedEntry(TimeChartAnalysisEntry timeAnalysisEntry) {
        fItemizedEntry = timeAnalysisEntry;
    }

    public TimeChartAnalysisEntry getItemizedEntry() {
        return fItemizedEntry;
    }

    public boolean isItemizing() {
        return fItemizing;
    }

    public void setItemizing(boolean itemizing) {
        fItemizing = itemizing;
    }

    public class RankRange {
        private long firstRank;
        private long lastRank;
        
        public RankRange(long firstRank, long lastRank) {
            this.firstRank = firstRank;
            this.lastRank = lastRank;
        }

        public long getFirstRank() {
            return firstRank;
        }

        public long getLastRank() {
            return lastRank;
        }

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
