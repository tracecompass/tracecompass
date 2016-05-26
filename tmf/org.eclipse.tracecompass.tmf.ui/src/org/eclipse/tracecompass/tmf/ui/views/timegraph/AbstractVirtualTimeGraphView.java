/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Loïc Prieur-Drevon - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.timegraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.ui.TmfUiRefreshHandler;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphPresentationProvider;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.TimeGraphViewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ILinkEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.IMarkerEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeEvent;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.VirtualTimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.VirtualTimeGraphEntry.Sampling;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;

import com.google.common.collect.Iterables;

/**
 * An abstract time graph view where only the visible elements are queried. This
 * largely reduces the amount of processing to do on views with large numbers of
 * entries.
 *
 * @since 3.1
 * @author Loïc Prieur-Drevon
 */
public abstract class AbstractVirtualTimeGraphView extends AbstractTimeGraphView {

    private static final int DEFAULT_BUFFER_SIZE = 10;
    private List<ILinkEvent> fLinkCache = null;
    private Sampling fLastSampling = null;

    /**
     * Set of visible entries to zoom on.
     */
    private @NonNull Set<ITimeGraphEntry> fVisibleEntries = Collections.emptySet();

    /**
     * Implementation of ZoomThread class to only zoom on items visible on screen.
     */
    public class ZoomThreadVisible extends ZoomThread {

        /**
         * Constructor for a Zoom thread
         *
         * @param startTime
         *            start time of the zoom area
         * @param endTime
         *            end time of the zoom area
         * @param resolution
         *            resolution to zoom on
         */
        public ZoomThreadVisible(long startTime, long endTime, long resolution) {
            super(startTime, endTime, resolution);
        }

        @Override
        public void doRun() {
            final @NonNull Sampling sampling = new Sampling(getZoomStartTime(), getZoomEndTime(), getResolution());
            Iterable<ITimeGraphEntry> newVisibleEntries = filterEntries(fVisibleEntries, sampling);
            Iterable<VirtualTimeGraphEntry> virtualGraphEntryList = Iterables.filter(newVisibleEntries, VirtualTimeGraphEntry.class);

            virtualGraphEntryList.forEach(v -> v.setZoomedEventList(null));
            zoomEntries(newVisibleEntries, getZoomStartTime(), getZoomEndTime(), getResolution(), getMonitor());
            completeZoom(virtualGraphEntryList, sampling);

            List<ILinkEvent> links = getLinks(sampling);
            /* Refresh the view-specific markers when zooming */
            List<IMarkerEvent> markers = getViewMarkerList(getZoomStartTime(), getZoomEndTime(), getResolution(), getMonitor());
            /* Refresh the trace-specific markers when zooming */
            markers.addAll(getTraceMarkerList(getZoomStartTime(), getZoomEndTime(), getResolution(), getMonitor()));
            applyResults(() -> {
                if (links != null) {
                    getTimeGraphViewer().setLinks(links);
                }
                getTimeGraphViewer().setMarkers(markers);
            });
            refresh();
        }

        /**
         * Filter the visible entries to keep only the {@link VirtualTimeGraphEntry}s
         * with an invalid {@link Sampling},
         *
         * @param visibleEntries
         *            list of entries to filter.
         * @param sampling
         *            zoom parameters
         */
        private @NonNull Iterable<ITimeGraphEntry> filterEntries(@NonNull Set<ITimeGraphEntry> visibleEntries, Sampling sampling) {
            List<ITimeGraphEntry> filter = new ArrayList<>(visibleEntries.size());
            for (VirtualTimeGraphEntry entry : Iterables.filter(visibleEntries, VirtualTimeGraphEntry.class)) {
                if (!sampling.equals(entry.getSampling())) {
                    filter.add(entry);
                }
            }
            return filter;
        }

        private void completeZoom(Iterable<VirtualTimeGraphEntry> entries, final Sampling sampling) {
            if (!getMonitor().isCanceled()) {
                for (VirtualTimeGraphEntry entry : entries) {
                    entry.fillZoomedEventList();
                    entry.setSampling(sampling);
                }
            } else {
                for (VirtualTimeGraphEntry entry : entries) {
                    entry.setSampling(null);
                }
            }
        }

        /**
         * Get the links from the cache if the sampling hasn't changed.
         *
         * @param sampling
         *            zoom parameters
         * @return list of links
         */
        private List<ILinkEvent> getLinks(@NonNull Sampling sampling) {
            if (Objects.equals(sampling, fLastSampling)) {
                return fLinkCache;
            }
            List<@NonNull ILinkEvent> links = getLinkList(getZoomStartTime(), getZoomEndTime(), getResolution(), getMonitor());
            /* Cache the links if the monitor is not cancelled */
            if (!getMonitor().isCanceled()) {
                fLinkCache = links;
                fLastSampling = sampling;
            }
            return links;
        }
    }

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructs a time graph view that contains either a time graph viewer or a
     * time graph combo.
     *
     * By default, the view uses a time graph viewer. To use a time graph combo, the
     * subclass constructor must call {@link #setTreeColumns(String[])} and
     * {@link #setTreeLabelProvider(TreeLabelProvider)}.
     *
     * @param id
     *            The id of the view
     * @param pres
     *            The presentation provider
     */
    public AbstractVirtualTimeGraphView(String id, TimeGraphPresentationProvider pres) {
        super(id, pres);
    }

    @Override
    public void createPartControl(Composite parent) {
        super.createPartControl(parent);
        getTimeGraphViewer().getTimeGraphControl().addPaintListener(new PaintListener() {

            /**
             * This paint control allows the virtual time graph refresh to occur on paint
             * events instead of just scrolling the time axis or zooming. To avoid
             * refreshing the model on every paint event, we use a TmfUiRefreshHandler to
             * coalesce requests and only execute the last one, we also check if the entries
             * have changed to avoid useless model refresh.
             *
             * @param e
             *            paint event on the visible area
             */
            @Override
            public void paintControl(PaintEvent e) {
                TmfUiRefreshHandler.getInstance().queueUpdate(this, () -> {
                    @NonNull Set<ITimeGraphEntry> newSet = getVisibleItems(DEFAULT_BUFFER_SIZE);
                    if (!fVisibleEntries.equals(newSet)) {
                        /*
                         * Start a zoom thread if the set of visible entries has changed. We do not use
                         * lists as the order is not important. We cannot use the start index / size of
                         * the visible entries as we can collapse / reorder events.
                         */
                        fVisibleEntries = newSet;
                        startZoomThread(getTimeGraphViewer().getTime0(), getTimeGraphViewer().getTime1());
                    }
                });
            }
        });
    }

    /**
     * Find which items are visible in the view
     *
     * @param buffer
     *            number of Items above and below border that we want to add to the
     *            list
     * @return a list of Items visible in the view with buffer above and below limit
     */
    private @NonNull Set<ITimeGraphEntry> getVisibleItems(int buffer) {
        TimeGraphViewer timeGraphViewer = getTimeGraphViewer();
        TimeGraphControl timeGraphControl = timeGraphViewer.getTimeGraphControl();

        int start = Math.max(0, timeGraphViewer.getTopIndex() - buffer);
        int end = Math.min(timeGraphViewer.getExpandedElementCount() - 1,
                timeGraphViewer.getTopIndex() + timeGraphControl.countPerPage() + buffer);

        Set<ITimeGraphEntry> visible = new HashSet<>(end - start + 1);
        for (int i = start; i <= end; i++) {
            /*
             * Use the getExpandedElement by index to avoid creating a copy of all the the
             * elements.
             */
            visible.add(timeGraphControl.getExpandedElement(i));
        }
        return visible;
    }

    @Override
    protected @Nullable ZoomThread createZoomThread(long startTime, long endTime, long resolution, boolean restart) {
        return new ZoomThreadVisible(startTime, endTime, resolution);
    }

    /**
     * Add events from the queried time range to the queried entries.
     * <p>
     * Called from the ZoomThread for every entry to update the zoomed event list.
     *
     * @param entries
     *            List of entries to zoom on.
     * @param zoomStartTime
     *            Start of the time range
     * @param zoomEndTime
     *            End of the time range
     * @param resolution
     *            The resolution
     * @param monitor
     *            The progress monitor object
     */
    protected abstract void zoomEntries(@NonNull Iterable<ITimeGraphEntry> entries,
            long zoomStartTime, long zoomEndTime, long resolution, @NonNull IProgressMonitor monitor);

    @Override
    protected @Nullable List<@NonNull ITimeEvent> getEventList(@NonNull TimeGraphEntry entry, long startTime, long endTime, long resolution, @NonNull IProgressMonitor monitor) {
        // implement the method to hide it from children classes.
        return Collections.emptyList();
    }

}
