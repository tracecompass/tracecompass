/*******************************************************************************
 * Copyright (c) 2012, 2018 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *   Geneviève Bastien - Move code to provide base classes for time graph view
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.internal.tmf.ui.widgets.timegraph.model.TimeGraphLineEntry;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IElementResolver;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * An entry for use in the time graph views
 */
public class TimeGraphEntry implements ITimeGraphEntry, IElementResolver {

    /**
     * Class to describe on which time range and resolution the zoomed entry list is
     * sampled.
     *
     * @author Loic Prieur-Drevon
     * @since 3.2
     */
    public static class Sampling {
        private final long fZoomStart;
        private final long fZoomEnd;
        private final long fResolution;
        private final @NonNull Map<@NonNull Integer, @NonNull Predicate<@NonNull Map<@NonNull String, @NonNull String>>> fPredicates;

        /**
         * Constructor for a zoom sampling object
         *
         * @param zoomStart
         *            the start time of the zoom
         * @param zoomEnd
         *            the end time of the zoom
         * @param resolution
         *            the resolution of the zoom
         */
        public Sampling(long zoomStart, long zoomEnd, long resolution) {
            fZoomStart = zoomStart;
            fZoomEnd = zoomEnd;
            fResolution = resolution;
            fPredicates = Collections.emptyMap();
        }

        /**
         * Constructor for a zoom sampling object
         *
         * @param zoomStart
         *            the start time of the zoom
         * @param zoomEnd
         *            the end time of the zoom
         * @param resolution
         *            the resolution of the zoom
         * @param predicates
         *            The active predicate applied to the view element
         * @since 4.3
         */
        public Sampling(long zoomStart, long zoomEnd, long resolution, @NonNull Map<@NonNull Integer, @NonNull Predicate<@NonNull Map<@NonNull String, @NonNull String>>> predicates) {
            fZoomStart = zoomStart;
            fZoomEnd = zoomEnd;
            fResolution = resolution;
            fPredicates = predicates;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fZoomStart, fZoomEnd, fResolution);
        }

        @Override
        public boolean equals(Object arg0) {
            if (arg0 == this) {
                return true;
            }
            if (arg0 == null) {
                return false;
            }
            if (arg0 instanceof Sampling) {
                Sampling other = (Sampling) arg0;
                return fZoomStart == other.fZoomStart && fZoomEnd == other.fZoomEnd && fResolution == other.fResolution && fPredicates.equals(other.fPredicates);
            }
            return false;
        }

    }

    /**
     * Comparator to validate that an event e2 is within the event e1
     *
     * @since 4.3
     */
    public static Comparator<ITimeEvent> WITHIN_COMPARATOR = (e1, e2) -> {
        int comp = Long.compare(e1.getTime(), e2.getTime());
        if (comp > 0) {
            return comp;
        }

        comp = Long.compare(e1.getTime() + e1.getDuration(), e2.getTime() + e2.getDuration());
        if (comp < 0) {
            return comp;
        }

        return 0;
    };

    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(TimeGraphEntry.class);
    /** Entry's parent */
    private TimeGraphEntry fParent = null;

    /** List of child entries */
    private final List<@NonNull TimeGraphEntry> fChildren = new CopyOnWriteArrayList<>();

    private @NonNull List<ITimeEvent> fEventList = new ArrayList<>();
    private @NonNull List<ITimeEvent> fZoomedEventList = new ArrayList<>();
    private Comparator<ITimeGraphEntry> fComparator;
    private Sampling fSampling;

    private @NonNull ITmfTreeDataModel fModel;
    private long fStartTime;
    private long fEndTime;

    /**
     * Entry factory
     *
     * @param model
     *            the model to build
     * @param style
     *            the style of the entry
     * @return the entry
     * @since 5.0
     */
    public static TimeGraphEntry create(TimeGraphEntryModel model, ITimeGraphEntry.DisplayStyle style) {
        switch (style) {
        case LINE:
            return new TimeGraphLineEntry(model);
        case STATE:
            return new TimeGraphEntry(model);
        default:
            throw new IllegalStateException("Invalid style " + style.toString()); //$NON-NLS-1$
        }
    }

    /**
     * Constructor
     *
     * @param name
     *            The name of this entry
     * @param startTime
     *            The start time of this entry
     * @param endTime
     *            The end time of this entry
     */
    public TimeGraphEntry(String name, long startTime, long endTime) {
        this(new TimeGraphEntryModel(-1, -1, Collections.singletonList(name), startTime, endTime));
    }

    /**
     * Constructor
     *
     * @param model
     *            Time graph model
     * @since 4.0
     */
    public TimeGraphEntry(@NonNull TimeGraphEntryModel model) {
        fModel = model;
        fStartTime = model.getStartTime();
        fEndTime = model.getEndTime();
    }

    /**
     * Constructor
     *
     * @param model
     *            Time graph model
     * @since 5.0
     */
    public TimeGraphEntry(@NonNull ITmfTreeDataModel model) {
        fModel = model;
        if (model instanceof TimeGraphEntryModel) {
            this.updateModel((TimeGraphEntryModel) model);
        } else {
            // Initialize the time range to positive longs
            fStartTime = 0;
            fEndTime = Long.MAX_VALUE;
        }
    }

    // ---------------------------------------------
    // Getters and setters
    // ---------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public TimeGraphEntry getParent() {
        return fParent;
    }

    /**
     * Sets the entry's parent
     *
     * @param entry The new parent entry
     * @since 2.0
     */
    public void setParent(TimeGraphEntry entry) {
        fParent = entry;
    }

    @Override
    public synchronized boolean hasChildren() {
        return !fChildren.isEmpty();
    }

    @Override
    public synchronized List<@NonNull TimeGraphEntry> getChildren() {
        return fChildren;
    }

    /**
     * Clear the children of the entry
     *
     * @since 2.0
     */
    public synchronized void clearChildren() {
        fChildren.clear();
    }

    @Override
    public String getName() {
        return getEntryModel().getName();
    }

    /**
     * Update the entry name
     *
     * @param name
     *            the updated entry name
     * @Deprecated The name should not be set through this method
     */
    @Deprecated
    public void setName(String name) {
        /*
         * Model is immutable, this is the only way to do this, consider not updating
         * name in the future?
         */
        fModel = new TimeGraphEntryModel(fModel.getId(), fModel.getParentId(), Collections.singletonList(name), getStartTime(), getEndTime(), fModel instanceof ITimeGraphEntryModel ? ((TimeGraphEntryModel) fModel).hasRowModel() : true);
    }

    @Override
    public long getStartTime() {
        return fStartTime;
    }

    @Override
    public long getEndTime() {
        return fEndTime;
    }

    /**
     * Updates the end time
     *
     * @param endTime
     *            the end time
     */
    public void updateEndTime(long endTime) {
        /*
         * Model is immutable, this is the only way to do this, consider not updating
         * end time in the future?
         */
        fEndTime = endTime;
    }

    @Override
    public boolean hasTimeEvents() {
        return fModel instanceof ITimeGraphEntryModel ? ((ITimeGraphEntryModel) fModel).hasRowModel() : true;
    }

    @Override
    public Iterator<@NonNull ITimeEvent> getTimeEventsIterator() {
        if (hasTimeEvents()) {
            return new EventIterator(fEventList, fZoomedEventList);
        }
        return null;
    }

    @Override
    public Iterator<@NonNull ITimeEvent> getTimeEventsIterator(long startTime, long stopTime, long visibleDuration) {
        if (!hasTimeEvents()) {
            return null;
        }
        return new EventIterator(fEventList, fZoomedEventList, startTime, stopTime);
    }

    /**
     * Add an event to this entry's event list. If necessary, update the start
     * and end time of the entry. If the event list's last event starts at the
     * same time as the event to add, it is replaced by the new event.
     *
     * @param event
     *            The time event to add
     */
    public void addEvent(ITimeEvent event) {
        int lastIndex = fEventList.size() - 1;
        if (lastIndex >= 0 && fEventList.get(lastIndex).getTime() == event.getTime()) {
            fEventList.set(lastIndex, event);
        } else {
            fEventList.add(event);
        }
        updateEntryBounds(event);
    }

    /**
     * Set the general event list of this entry. The list should be modifiable
     * but will only increase in size over time.
     *
     * @param eventList
     *            The modifiable list of time events, or null to clear the list
     */
    public void setEventList(List<ITimeEvent> eventList) {
        if (eventList != null) {
            fEventList = eventList;
        } else {
            fEventList = new ArrayList<>();
        }
    }

    /**
     * Set the zoomed event list of this entry. The list should be modifiable
     * but will only increase in size over time.
     *
     * @param eventList
     *            The modifiable list of time events, or null to clear the list
     */
    public void setZoomedEventList(List<ITimeEvent> eventList) {
        if (eventList != null) {
            fZoomedEventList = eventList;
        } else {
            fZoomedEventList = new ArrayList<>();
            fSampling = null;
        }
    }

    /**
     * Add an event to this entry's zoomed event list. If necessary, update the
     * start and end time of the entry. If the zoomed event list's last event
     * starts at the same time as the event to add, it is replaced by the new
     * event. If the new event starts before the zoomed event list's last event,
     * the new event is ignored and is assumed to be already part of the list.
     * If the new event starts before the zoomed event list's first event, the
     * list is assumed to be incomplete and is cleared, and the event is added.
     *
     * @param event
     *            The time event to add
     * @since 1.1
     */
    public void addZoomedEvent(ITimeEvent event) {
        long start = event.getTime();
        int lastIndex = fZoomedEventList.size() - 1;
        long lastStart = lastIndex >= 0 ? fZoomedEventList.get(lastIndex).getTime() : Long.MIN_VALUE;
        if (start > lastStart) {
            fZoomedEventList.add(event);
        } else if (start == lastStart) {
            fZoomedEventList.set(lastIndex, event);
        } else if (start < fZoomedEventList.get(0).getTime()) {
            fZoomedEventList.clear();
            fZoomedEventList.add(event);
        }
        updateEntryBounds(event);
    }

    /**
     * Update the list of zoomed time event. The update could insert time event
     * in the past. This kind of update is necessary for background search for
     * example where a previous list of zoomed event is computed using the
     * current resolution followed by an update to that zoomed event list using
     * resolution 1 to be able to search within data not available at the
     * current zoom level.
     *
     * @param event
     *            The time event to add to the zoomed event list
     *
     * @since 4.2
     */
    public void updateZoomedEvent(ITimeEvent event) {
        try (TraceCompassLogUtils.ScopeLog poc = new TraceCompassLogUtils.ScopeLog(LOGGER, Level.FINE, "UpdateZoomedEvent")) { //$NON-NLS-1$

            long start = getStartTime();
            long end = getEndTime();

            // If the entry has no time event, put a null time event to it
            if (fZoomedEventList.isEmpty()) {
                fZoomedEventList.add(new NullTimeEvent(this, start, end));
            }
            int index = Collections.binarySearch(fZoomedEventList, event, WITHIN_COMPARATOR);
            if (index >= 0) {
                ITimeEvent current = fZoomedEventList.get(index);
                if (!(current instanceof NullTimeEvent)) {
                    // The time event has been already added to the zoomed event list
                    return;
                }
                if (current.getTime() == event.getTime()) {
                    fZoomedEventList.set(index++, event);
                } else if (current.getTime() < event.getTime()) {
                    fZoomedEventList.set(index++, new NullTimeEvent(this, current.getTime(), event.getTime() - current.getTime()));
                    fZoomedEventList.add(index++, event);
                }
                long eventEndTime = (event.getTime() + event.getDuration());
                long currentEndTime = (current.getTime() + current.getDuration());
                if (eventEndTime < currentEndTime) {
                    fZoomedEventList.add(index, new NullTimeEvent(this, eventEndTime + 1, currentEndTime - eventEndTime));
                }
            } else {
                index = -index + 1;
                if (index < fZoomedEventList.size()) {
                    fZoomedEventList.add(index, event);
                } else {
                    fZoomedEventList.add(event);
                }
            }

            updateEntryBounds(event);

            // Put null time event at the beginning
            long first = fZoomedEventList.isEmpty() ? start : fZoomedEventList.get(0).getTime();
            if (start < first) {
                fZoomedEventList.add(0, new NullTimeEvent(this, start, first - start));
            }

            // Put null time event at the end
            long last = fZoomedEventList.isEmpty() ? end : fZoomedEventList.get(fZoomedEventList.size() - 1).getTime() + fZoomedEventList.get(fZoomedEventList.size() - 1).getDuration();
            if (end > last) {
                fZoomedEventList.add(new NullTimeEvent(this, last, end - last));
            }
        }
    }

    private void updateEntryBounds(ITimeEvent event) {
        if (event instanceof NullTimeEvent) {
            /* A NullTimeEvent should not affect the entry bounds */
            return;
        }
        long start = event.getTime();
        long newStart = getStartTime() == SWT.DEFAULT ? start : Long.min(start, getStartTime());

        long end = start + event.getDuration();
        long newEnd = getEndTime() == SWT.DEFAULT ? end : Long.max(end, getEndTime());

        /*
         * Model is immutable, this is the only way to do this, consider not updating
         * bounds in the future?
         */
        fStartTime = newStart;
        fEndTime = newEnd;
    }

    /**
     * Add a child entry to this one. If a comparator was previously set with
     * {@link #sortChildren(Comparator)}, the entry will be inserted in its
     * sort-order position. Otherwise it will be added to the end of the list.
     *
     * @param child
     *            The child entry
     */
    public synchronized void addChild(@NonNull TimeGraphEntry child) {
        if (fComparator == null) {
            addChild(fChildren.size(), child);
        } else {
            int i = Collections.binarySearch(fChildren, child, fComparator);
            if (i >= 0) {
                i ++;
                /*
                 * There might be several children which are equal in regards to the comparator,
                 * increment past the index of the last one.
                 */
                while (i < fChildren.size() && fComparator.compare(child, fChildren.get(i)) == 0) {
                    i++;
                }
            } else {
                /* Deal with negative insertion points from binarySearch */
                i = -i - 1;
            }
            addChild(i, child);
        }
    }

    /**
     * Add a child entry to this one at the specified position
     *
     * @param index
     *            Index at which the specified entry is to be inserted
     * @param child
     *            The child entry
     * @since 2.0
     */
    public synchronized void addChild(int index, @NonNull TimeGraphEntry child) {
        if (child.getParent() == this) {
            return;
        }
        if (child.getParent() != null) {
            child.getParent().removeChild(child);
        }
        child.setParent(this);
        fChildren.add(index, child);
    }

    /**
     * Remove a child entry from this one.
     *
     * @param child
     *            The child entry
     * @since 2.0
     */
    public synchronized void removeChild(@NonNull TimeGraphEntry child) {
        if (child.getParent() == this) {
            child.setParent(null);
        }
        fChildren.remove(child);
    }

    /**
     * Sort the children of this entry using the provided comparator. Subsequent
     * calls to {@link #addChild(TimeGraphEntry)} will use this comparator to
     * maintain the sort order.
     *
     * @param comparator
     *            The entry comparator
     */
    public synchronized void sortChildren(Comparator<ITimeGraphEntry> comparator) {
        fComparator = comparator;
        if (comparator == null || fChildren.isEmpty()) {
            return;
        }
        List<@NonNull TimeGraphEntry> copy = new ArrayList<>(fChildren);
        copy.sort(comparator);
        fChildren.clear();
        fChildren.addAll(copy);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + '(' + fModel.getLabels() + ')';
    }

    /**
     * @since 2.0
     */
    @Override
    public boolean matches(@NonNull Pattern pattern) {
        // Default implementation
        return pattern.matcher(fModel.getName()).find();
    }

    /**
     * Getter for the zoom sampling of this entry
     *
     * @return the zoom parameters of the current zoom event sampling.
     * @since 3.2
     */
    public Sampling getSampling() {
        return fSampling;
    }

    /**
     * Setter for the zoom sampling of this entry.
     *
     * @param sampling
     *            the sampling parameters used to compute the current zoomed event
     *            list.
     * @since 3.2
     */
    public void setSampling(Sampling sampling) {
        fSampling = sampling;
    }

    /**
     * Getter for the underlying time graph entry model
     *
     * @return The underlying time graph entry model
     * @since 4.0
     * @deprecated Use the {@link #getEntryModel()} instead
     */
    @Deprecated
    public ITimeGraphEntryModel getModel() {
        ITmfTreeDataModel model = fModel;
        return model instanceof ITimeGraphEntryModel ? (ITimeGraphEntryModel) model :
            new TimeGraphEntryModel(model.getId(), model.getParentId(), getName(), getStartTime(), getEndTime());
    }

    /**
     * Getter for the underlying time graph entry model
     *
     * @return The underlying time graph entry model
     * @since 5.0
     */
    public @NonNull ITmfTreeDataModel getEntryModel() {
        return fModel;
    }

    /**
     * Get the metadata for this data model. The keys are the names of the
     * metadata field or aspect. A field may have multiple values associated
     * with it.
     *
     * @return A map of field names to values
     * @since 4.1
     */
    @Override
    public @NonNull Multimap<@NonNull String, @NonNull Object> getMetadata() {
        if (fModel instanceof IElementResolver) {
            return ((IElementResolver) fModel).getMetadata();
        }
        return ImmutableMultimap.of();
    }

    /**
     * Update the underlying model.
     *
     * @param model
     *            new model for this TimeGraphEntry.
     * @since 4.0
     */
    public void updateModel(@NonNull TimeGraphEntryModel model) {
        if (fModel.getId() != model.getId()) {
            throw new IllegalArgumentException("TimeGraphEntry should be updated with a TimeGraphEntryModel with the same id."); //$NON-NLS-1$
        }
        fModel = model;
        fStartTime = model.getStartTime();
        fEndTime = model.getEndTime();
    }

    /**
     * Update the underlying model.
     *
     * @param model
     *            new model for this TimeGraphEntry.
     * @since 5.0
     */
    public void updateModel(@NonNull ITmfTreeDataModel model) {
        if (fModel.getId() != model.getId()) {
            throw new IllegalArgumentException("TimeGraphEntry should be updated with a TimeGraphEntryModel with the same id."); //$NON-NLS-1$
        }
        fModel = model;
    }

    /**
     * Tells whether there is actual zoomed event or not
     *
     * @return True if there is actual new zoomedEvents, false otherwise
     * @since 4.0
     */
    public boolean hasZoomedEvents() {
        /* Iterate to avoid concurrent modification exception */
        for (int i = 0; i < fZoomedEventList.size(); i++) {
            if (!(fZoomedEventList.get(i) instanceof NullTimeEvent)) {
                return true;
            }
        }
        return false;
    }

    @Deprecated
    @Override
    public @NonNull Map<@NonNull String, @NonNull String> computeData() {
        if (fModel instanceof IElementResolver) {
            return ((IElementResolver) fModel).computeData();
        }
        return Collections.emptyMap();
    }
}
