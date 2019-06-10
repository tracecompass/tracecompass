/*******************************************************************************
 * Copyright (c) 2015, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexis Cabana-Loriaux - Initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.piecharts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.dataviewers.piechart.PieChart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.piecharts.model.TmfPieChartStatisticsModel;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphColorScheme;

/**
 * Creates a viewer containing 2 pie charts, one for showing information about
 * the current selection, and the second one for showing information about the
 * current time-range selection. It follows the MVC pattern, being a view.
 *
 * This class is closely related with the IPieChartViewerState interface that
 * acts as a state machine for the general layout of the charts.
 *
 * @author Alexis Cabana-Loriaux
 * @since 2.0
 *
 */
public class TmfPieChartViewer extends Composite {

    /**
     * The pie chart containing global information about the trace
     */
    private PieChart fGlobalPC;

    /**
     * The name of the piechart containing the statistics about the global trace
     */
    private String fGlobalPCname;

    /**
     * The pie chart containing information about the current time-range
     * selection
     */
    private PieChart fTimeRangePC;

    /**
     * The name of the piechart containing the statistics about the current
     * selection
     */
    private String fTimeRangePCname;

    /**
     * The listener for the mouse movement event.
     */
    private Listener fMouseMoveListener;

    /**
     * The listener for the mouse right click event.
     */
    private MouseListener fMouseClickListener;

    /**
     * The list of listener to notify when an event type is selected
     */
    private ListenerList<Listener> fEventTypeSelectedListeners = new ListenerList<>(ListenerList.IDENTITY);

    /**
     * The name of the slice containing the too little slices
     */
    private String fOthersSliceName;

    /**
     * Implementation of the State design pattern to reorder the layout
     * depending on the selection. This variable holds the current state of the
     * layout.
     */
    private IPieChartViewerState fCurrentState;

    /**
     * Represents the minimum percentage a slice of pie must have in order to be
     * shown
     */
    private static final float MIN_PRECENTAGE_TO_SHOW_SLICE = 0.025F;// 2.5%

    /**
     * Represents the maximum number of slices of the pie charts. WE don't want
     * to pollute the viewer with too much slice entries.
     */
    private static final int NB_MAX_SLICES = 10;

    /**
     * The data that has to be presented by the pie charts
     */
    private TmfPieChartStatisticsModel fModel = null;

    /** The color scheme for the chart */
    private @NonNull TimeGraphColorScheme fColorScheme = new TimeGraphColorScheme();
    /**
     * @param parent
     *            The parent composite that will hold the viewer
     */
    public TmfPieChartViewer(Composite parent) {
        super(parent, SWT.NONE);
        fGlobalPCname = Messages.TmfStatisticsView_GlobalSelectionPieChartName;
        fTimeRangePCname = Messages.TmfStatisticsView_TimeRangeSelectionPieChartName;
        fOthersSliceName = Messages.TmfStatisticsView_PieChartOthersSliceName;
        parent.addDisposeListener(e -> {
            fColorScheme.dispose();
        });
        initContent();
    }

    // ------------------------------------------------------------------------
    // Class methods
    // ------------------------------------------------------------------------

    /**
     * Called by this class' constructor. Constructs the basic viewer containing
     * the charts, as well as their listeners
     */
    private synchronized void initContent() {
        setLayout(new FillLayout());

        fGlobalPC = null;
        fTimeRangePC = null;

        // Setup listeners for the tooltips
        fMouseMoveListener = event -> {
            PieChart pc = (PieChart) event.widget;
            switch (event.type) {
            /* Get tooltip information on the slice */
            case SWT.MouseMove:
                int sliceIndex = pc.getSliceIndexFromPosition(0, event.x, event.y);
                if (sliceIndex < 0) {
                    // mouse is outside the chart
                    pc.setToolTipText(null);
                    break;
                }
                float percOfSlice = (float) pc.getSlicePercent(0, sliceIndex);
                String percent = String.format("%.1f", percOfSlice); //$NON-NLS-1$
                Long nbEvents = Long.valueOf((long) pc.getSeriesSet().getSeries()[sliceIndex].getXSeries()[0]);

                String text = Messages.TmfStatisticsView_PieChartToolTipTextName + " = " + //$NON-NLS-1$
                        pc.getSeriesSet().getSeries()[sliceIndex].getId() + "\n"; //$NON-NLS-1$

                text += Messages.TmfStatisticsView_PieChartToolTipTextEventCount + " = "//$NON-NLS-1$
                        + nbEvents.toString() + " (" + percent + "%)"; //$NON-NLS-1$ //$NON-NLS-2$
                pc.setToolTipText(text);
                return;
            default:
            }
        };

        fMouseClickListener = new MouseListener() {

            @Override
            public void mouseUp(MouseEvent e) {
                // Do nothing
            }

            @Override
            public void mouseDown(MouseEvent e) {
                PieChart pc = (PieChart) e.widget;
                int slicenb = pc.getSliceIndexFromPosition(0, e.x, e.y);
                if (slicenb < 0 || slicenb >= pc.getSeriesSet().getSeries().length) {
                    // mouse is outside the chart
                    return;
                }
                Event selectionEvent = new Event();
                selectionEvent.text = pc.getSeriesSet().getSeries()[slicenb].getId();
                notifyEventTypeSelectionListener(selectionEvent);
            }

            @Override
            public void mouseDoubleClick(MouseEvent e) {
                // Do nothing
            }
        };

        // at creation no content is selected
        setCurrentState(new PieChartViewerStateNoContentSelected(this));
    }

    /**
     * Updates the data contained in the Global PieChart by using a Map.
     * Normally, this method is only called by the state machine.
     */
    synchronized void updateGlobalPieChart() {
        if (getGlobalPC() == null) {
            fGlobalPC = new PieChart(this, SWT.NONE);
            Color backgroundColor = fColorScheme.getColor(TimeGraphColorScheme.TOOL_BACKGROUND);
            Color foregroundColor = fColorScheme.getColor(TimeGraphColorScheme.TOOL_FOREGROUND);
            getGlobalPC().getTitle().setText(fGlobalPCname);
            getGlobalPC().getTitle().setForeground(foregroundColor);
            getGlobalPC().setBackground(backgroundColor);
            getGlobalPC().setForeground(foregroundColor);
            getGlobalPC().getAxisSet().getXAxis(0).getTitle().setText(""); //Hide the title over the legend //$NON-NLS-1$
            getGlobalPC().getAxisSet().getXAxis(0).getTitle().setForeground(foregroundColor);
            getGlobalPC().getLegend().setVisible(true);
            getGlobalPC().getLegend().setPosition(SWT.RIGHT);
            getGlobalPC().getLegend().setBackground(backgroundColor);
            getGlobalPC().getLegend().setForeground(foregroundColor);
            getGlobalPC().addListener(SWT.MouseMove, fMouseMoveListener);
            getGlobalPC().addMouseListener(fMouseClickListener);
        } else if (getGlobalPC().isDisposed() || fModel == null || fModel.getPieChartGlobalModel() == null) {
            return;
        }

        Map<String, Long> totalEventCountForChart = getTotalEventCountForChart(true);

        if (totalEventCountForChart == null) {
            return;
        }

        updatePieChartWithData(fGlobalPC, totalEventCountForChart, MIN_PRECENTAGE_TO_SHOW_SLICE, fOthersSliceName);
    }

    /**
     * Updates the data contained in the Time-Range PieChart by using a Map.
     * Normally, this method is only called by the state machine.
     */
    synchronized void updateTimeRangeSelectionPieChart() {
        if (getTimeRangePC() == null) {
            Color backgroundColor = fColorScheme.getColor(TimeGraphColorScheme.TOOL_BACKGROUND);
            Color foregroundColor = fColorScheme.getColor(TimeGraphColorScheme.TOOL_FOREGROUND);
            fTimeRangePC = new PieChart(this, SWT.NONE);
            fTimeRangePC.setBackground(backgroundColor);
            fTimeRangePC.setForeground(foregroundColor);
            getTimeRangePC().getTitle().setText(fTimeRangePCname);
            getTimeRangePC().getTitle().setForeground(foregroundColor);
            getTimeRangePC().getAxisSet().getXAxis(0).getTitle().setText(""); //Hide the title over the legend //$NON-NLS-1$
            getTimeRangePC().getAxisSet().getXAxis(0).getTitle().setForeground(foregroundColor);
            getTimeRangePC().getLegend().setPosition(SWT.BOTTOM);
            getTimeRangePC().getLegend().setVisible(true);
            getTimeRangePC().getLegend().setBackground(backgroundColor);
            getTimeRangePC().getLegend().setForeground(foregroundColor);
            getTimeRangePC().addListener(SWT.MouseMove, fMouseMoveListener);
            getTimeRangePC().addMouseListener(fMouseClickListener);
        }
        else if (getTimeRangePC().isDisposed()) {
            return;
        }

        Map<String, Long> totalEventCountForChart = getTotalEventCountForChart(false);

        if (totalEventCountForChart == null) {
            return;
        }

        updatePieChartWithData(fTimeRangePC, totalEventCountForChart, MIN_PRECENTAGE_TO_SHOW_SLICE, fOthersSliceName);
    }

    /* return the chart-friendly map given by the TmfPieChartStatisticsModel */
    private Map<String, Long> getTotalEventCountForChart(boolean isGlobal) {
        if (fModel == null) {
            return null;
        }
        Map<ITmfTrace, Map<String, Long>> chartModel;
        if (isGlobal) {
            chartModel = fModel.getPieChartGlobalModel();
        } else {
            chartModel = fModel.getPieChartSelectionModel();
        }
        if (chartModel == null) {
            return null;
        }

        Map<String, Long> totalEventCountForChart = new HashMap<>();
        for (Entry<ITmfTrace, Map<String, Long>> entry : chartModel.entrySet()) {
            Map<String, Long> traceEventCount = entry.getValue();
            if (traceEventCount == null) {
                continue;
            }
            for (Entry<String, Long> event : traceEventCount.entrySet()) {
                final Long value = totalEventCountForChart.get(event.getKey());
                if (value != null) {
                    totalEventCountForChart.put(event.getKey(), value + event.getValue());
                } else {
                    totalEventCountForChart.put(event.getKey(), event.getValue());
                }
            }
        }

        return totalEventCountForChart;
    }

    /**
     * Reinitializes the charts to their initial state, without any data
     */
    public synchronized void reinitializeCharts() {
        if (isDisposed()) {
            return;
        }

        if (getGlobalPC() != null && !getGlobalPC().isDisposed()) {
            getGlobalPC().dispose();
        }
        fGlobalPC = new PieChart(this, SWT.NONE);
        getGlobalPC().getTitle().setText(fGlobalPCname);
        getGlobalPC().getAxisSet().getXAxis(0).getTitle().setText(""); //Hide the title over the legend //$NON-NLS-1$
        if (getTimeRangePC() != null && !getTimeRangePC().isDisposed()) {
            getTimeRangePC().dispose();
            fTimeRangePC = null;
        }
        layout();
        setCurrentState(new PieChartViewerStateNoContentSelected(this));
    }

    /**
     * Function used to update or create the slices of a PieChart to match the
     * content of a Map passed in parameter. It also provides a facade to use
     * the PieChart API
     */
    private static void updatePieChartWithData(
            final PieChart chart,
            final Map<String, Long> slices,
            final float minimumSizeOfSlice,
            final String nameOfOthers) {

        List<EventOccurrenceObject> chartValues = new ArrayList<>();
        Long eventTotal = 0L;
        for (Entry<String, Long> entry : slices.entrySet()) {
            eventTotal += entry.getValue();
            chartValues.add(new EventOccurrenceObject(entry.getKey(), entry.getValue()));
        }

        // No events in the selection
        if (eventTotal == 0) {
            // clear the chart and show "NO DATA"

            return;
        }

        /*
         * filter out the event types taking too little space in the chart and
         * label the whole group together. The remaining slices will be showing
         */
        List<EventOccurrenceObject> filteredChartValues = new ArrayList<>();
        Long othersEntryCount = 0L;
        int nbSlices = 0;
        for (EventOccurrenceObject entry : chartValues) {
            if (entry.getNbOccurence() / eventTotal.floatValue() > minimumSizeOfSlice && nbSlices <= NB_MAX_SLICES) {
                filteredChartValues.add(entry);
                nbSlices++;
            } else {
                othersEntryCount += entry.getNbOccurence();
            }
        }

        Collections.sort(filteredChartValues);

        // Add the "Others" slice in the pie if its not empty
        if (othersEntryCount != 0) {
            filteredChartValues.add(new EventOccurrenceObject(nameOfOthers, othersEntryCount));
        }

        // put the entries in the chart and add their percentage
        double[][] tempValues = new double[filteredChartValues.size()][1];
        String[] tempNames = new String[filteredChartValues.size()];
        int index = 0;
        for (EventOccurrenceObject entry : filteredChartValues) {
            tempValues[index][0] = entry.getNbOccurence();
            tempNames[index] = entry.getName();
            index++;
        }

        chart.addPieChartSeries(tempNames, tempValues);
    }

    /**
     * Refresh this viewer
     *
     * @param refreshGlobal
     *            if we have to refresh the global piechart
     * @param refreshSelection
     *            if we have to refresh the selection piechart
     */
    public synchronized void refresh(boolean refreshGlobal, boolean refreshSelection) {
        if (fModel == null) {
            reinitializeCharts();
        } else {
            if (refreshGlobal) {
                /* will update the global pc */
                getCurrentState().newGlobalEntries(this);
            }

            if (refreshSelection) {
                // Check if the selection is empty
                int nbEventsType = 0;
                Map<String, Long> selectionModel = getTotalEventCountForChart(false);
                for (Long l : selectionModel.values()) {
                    if (l != 0) {
                        nbEventsType++;
                    }
                }

                // Check if the selection is empty or if
                // there is enough event types to show in the piecharts
                if (nbEventsType < 2) {
                    getCurrentState().newEmptySelection(this);
                } else {
                    getCurrentState().newSelection(this);
                }
            }
        }
    }

    /**
     * @param l
     *            the listener to add
     */
    public void addEventTypeSelectionListener(Listener l) {
        fEventTypeSelectedListeners.add(l);
    }

    /**
     * @param l
     *            the listener to remove
     */
    public void removeEventTypeSelectionListener(Listener l) {
        fEventTypeSelectedListeners.remove(l);
    }

    /* Notify all listeners that an event type has been selected */
    private void notifyEventTypeSelectionListener(Event e) {
        for (Object o : fEventTypeSelectedListeners.getListeners()) {
            ((Listener) o).handleEvent(e);
        }
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the global piechart
     */
    synchronized PieChart getGlobalPC() {
        return fGlobalPC;
    }

    /**
     * @return the time-range selection piechart
     */
    synchronized PieChart getTimeRangePC() {
        return fTimeRangePC;
    }

    /**
     * @return the current state of the viewer
     */
    synchronized IPieChartViewerState getCurrentState() {
        return fCurrentState;
    }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

    /**
     * @return the model
     */
    public TmfPieChartStatisticsModel getModel() {
        return fModel;
    }

    /**
     * @param model
     *            the model to set
     */
    public void setInput(TmfPieChartStatisticsModel model) {
        fModel = model;
    }

    /**
     * Normally, this method is only called by the state machine
     *
     * @param newChart
     *            the new PieChart
     */
    public synchronized void setTimeRangePC(PieChart newChart) {
        fTimeRangePC = newChart;
    }

    /**
     * Setter method for the state.
     *
     * @param newState
     *            The new state of the viewer Normally only called by classes
     *            implementing the IPieChartViewerState interface.
     */
    public synchronized void setCurrentState(final IPieChartViewerState newState) {
        fCurrentState = newState;
    }

    /**
     * Nested class used to handle and sort more easily the pair (Name, Number
     * of occurrences)
     *
     * @author Alexis Cabana-Loriaux
     */
    private static class EventOccurrenceObject implements Comparable<EventOccurrenceObject> {

        private String fName;

        private Long fNbOccurrences;

        EventOccurrenceObject(String name, Long nbOccurences) {
            this.fName = name;
            this.fNbOccurrences = nbOccurences;
        }

        @Override
        public int compareTo(EventOccurrenceObject other) {
            // descending order
            return Long.compare(other.getNbOccurence(), this.getNbOccurence());
        }

        public String getName() {
            return fName;
        }

        public Long getNbOccurence() {
            return fNbOccurrences;
        }
    }
}
