/*******************************************************************************
 * Copyright (c) 2015 Ericsson
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

package org.eclipse.tracecompass.internal.tmf.ui.viewers.piecharts.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.tracecompass.internal.tmf.ui.viewers.piecharts.TmfPieChartViewer;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * This class contains the model shown by the {@link TmfPieChartViewer}.
 *
 * @author Alexis Cabana-Loriaux
 * @since 2.0
 */
public class TmfPieChartStatisticsModel {

    /**
     * The model for the PieChart viewer. For each chart, a trace has a group of
     * events and an associated count
     */
    private final Map<ITmfTrace, Map<String, Long>> fPieChartGlobalModel = new ConcurrentHashMap<>();
    private final Map<ITmfTrace, Map<String, Long>> fPieChartSelectionModel = new ConcurrentHashMap<>();

    /**
     * Default constructor
     */
    public TmfPieChartStatisticsModel() {
        // Do nothing
    }

    // ------------------------------------------------------------------------
    // Class Methods
    // ------------------------------------------------------------------------

    /**
     * Clean out the entire model
     */
    public void clear() {
        fPieChartGlobalModel.clear();
        fPieChartSelectionModel.clear();
    }

    // ------------------------------------------------------------------------
    // Getters and setter
    // ------------------------------------------------------------------------

    /**
     * @return the model to be applied to the global piechart
     */
    public Map<ITmfTrace, Map<String, Long>> getPieChartGlobalModel() {
        return fPieChartGlobalModel;
    }

    /**
     * @return the model to be applied to the global piechart
     */
    public Map<ITmfTrace, Map<String, Long>> getPieChartSelectionModel() {
        return fPieChartSelectionModel;
    }

    /**
     * Method used to set the model of one of the pie chart
     *
     * @param isGlobal if the model to update is global or selection piechart
     * @param jobTrace the trace
     * @param eventsPerType the map with pairs (Event, count)
     */
    public void setPieChartTypeCount(boolean isGlobal, ITmfTrace jobTrace, Map<String, Long> eventsPerType) {
        Map<ITmfTrace,Map<String, Long>> chartModel;
        if(isGlobal){
            chartModel = fPieChartGlobalModel;
        } else {
            chartModel = fPieChartSelectionModel;
        }

        chartModel.put(jobTrace, eventsPerType);
    }
}
