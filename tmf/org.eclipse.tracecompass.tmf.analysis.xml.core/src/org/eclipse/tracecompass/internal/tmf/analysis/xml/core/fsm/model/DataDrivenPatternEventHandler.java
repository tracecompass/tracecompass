/*******************************************************************************
 * Copyright (c) 2019 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenRuntimeData;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * The class responsible to handle events for pattern event handler
 *
 * @author Geneviève Bastien
 * @author Jean-Christian Kouamé
 */
public class DataDrivenPatternEventHandler implements IDataDrivenRuntimeObject {

    private final Collection<DataDrivenFsm> fFsms;
    private final Collection<DataDrivenFsm> fInitials;
    private final Set<DataDrivenFsm> fActiveFsmList = new HashSet<>();

    /**
     * Constructor
     *
     * @param fsms
     *            The finite state machines in this handler
     * @param initials
     *            The finite state machines to start at the beginning of the
     *            analysis
     */
    public DataDrivenPatternEventHandler(Collection<DataDrivenFsm> fsms, Collection<DataDrivenFsm> initials) {
        fFsms = fsms;
        fInitials = initials;
    }

    /**
     * Dispose of this fsm
     *
     * @param executionData
     *            The runtime data
     */
    public void dispose(DataDrivenRuntimeData executionData) {
        for (DataDrivenFsm fsm : fFsms) {
            fsm.dispose(executionData);
        }
    }

    /**
     * Handle the event
     *
     * @param event
     *            Event to handle
     * @param container
     *            The analysis container
     * @param runtimeData
     *            The runtime data for the current analysis execution
     */
    public void handleEvent(ITmfEvent event, IAnalysisDataContainer container, DataDrivenRuntimeData runtimeData) {
        final Set<DataDrivenFsm> activeFsmList = fActiveFsmList;
        if (activeFsmList.isEmpty()) {
            Collection<DataDrivenFsm> fsms = fInitials;
            if (fInitials.isEmpty()) {
                // No initial fsm specified: Add all FSMs to the list to create scenarios
                fsms = fFsms;
            }
            for (DataDrivenFsm fsm : fsms) {
                fActiveFsmList.add(fsm);
                fsm.createScenario(event, true, runtimeData, container);
            }
        } else {
            List<DataDrivenFsm> fsmToStart = new ArrayList<>();
            for (DataDrivenFsm fsm : fFsms) {
                if (fsm.isNewScenarioAllowed(runtimeData.getRuntimeForFsm(fsm))) {
                    fsmToStart.add(fsm);
                }
            }
            for (DataDrivenFsm fsm : fsmToStart) {
                fActiveFsmList.add(fsm);
                fsm.createScenario(event, false, runtimeData, container);
            }
        }
        for (DataDrivenFsm fsm : activeFsmList) {
            fsm.handleEvent(event, runtimeData, container);
        }
    }

}
