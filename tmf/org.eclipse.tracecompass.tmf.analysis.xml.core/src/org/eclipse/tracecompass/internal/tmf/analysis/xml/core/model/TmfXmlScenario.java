/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsm;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsmState;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlScenarioHistoryBuilder.ScenarioStatusType;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * This Class implements a Scenario in the XML-defined state system
 */
public class TmfXmlScenario {

    private final IXmlStateSystemContainer fContainer;
    private final DataDrivenFsm fFsm;
    private TmfXmlScenarioInfo fScenarioInfo;
    TmfXmlScenarioHistoryBuilder fHistoryBuilder;

    /**
     * Constructor
     *
     * @param event
     *            The event at which this scenario is created
     * @param fsm
     *            the fsm executed by this scenario
     * @param initialState
     *            The initial state
     * @param container
     *            The state system container this scenario belongs to
     */
    public TmfXmlScenario(ITmfEvent event, DataDrivenFsm fsm, DataDrivenFsmState initialState, IXmlStateSystemContainer container) {
        fFsm = fsm;
        fContainer = container;
        fHistoryBuilder = ((XmlPatternStateProvider) container).getHistoryBuilder();
        int quark = fHistoryBuilder.assignScenarioQuark(fContainer, fsm);
        int statusQuark = fHistoryBuilder.getScenarioStatusQuark(fContainer, quark);
        fScenarioInfo = new TmfXmlScenarioInfo(initialState, ScenarioStatusType.PENDING, quark, statusQuark, fFsm);
        fHistoryBuilder.update(fContainer, fScenarioInfo, event);
    }

    /**
     * Get this scenario infos
     *
     * @return The scenario info
     */
    public TmfXmlScenarioInfo getScenarioInfos() {
        return fScenarioInfo;
    }

    /**
     * Cancel the execution of this scenario
     */
    public void cancel() {
        fScenarioInfo.setStatus(ScenarioStatusType.ABANDONED);
        if (fScenarioInfo.getStatus() != ScenarioStatusType.PENDING) {
            fHistoryBuilder.completeScenario(fContainer, fScenarioInfo, null);
        }
    }

    /**
     * Test if the scenario is active or not
     *
     * @return True if the scenario is active, false otherwise
     */
    public boolean isActive() {
        return fScenarioInfo.getStatus() == ScenarioStatusType.IN_PROGRESS;
    }

    /**
     * Test if the scenario is pending or not
     *
     * @return True if the scenario is pending, false otherwise
     */
    public boolean isPending() {
        return fScenarioInfo.getStatus() == ScenarioStatusType.PENDING;
    }

    /**
     * Handle the ongoing event
     *
     * @param event
     *            The ongoing event
     * @param container
     *            The data container
     * @return Whether or not the event was consumed by this scenario
     */
    public boolean handleEvent(ITmfEvent event, IAnalysisDataContainer container) {

        DataDrivenFsmState activeState = fScenarioInfo.getActiveState();

        DataDrivenFsmState nextState = activeState.takeTransition(event, fScenarioInfo, container);
        if (nextState == null) {
            return false;
        }

        if (fScenarioInfo.getStatus().equals(ScenarioStatusType.PENDING)) {
            fScenarioInfo.setStatus(ScenarioStatusType.IN_PROGRESS);
            fHistoryBuilder.startScenario(fContainer, fScenarioInfo, event);
        }
        if (nextState.isFinal()) {
            fScenarioInfo.setStatus(ScenarioStatusType.MATCHED);
            fHistoryBuilder.completeScenario(fContainer, fScenarioInfo, event);
        }
        fScenarioInfo.setActiveState(nextState);
        fHistoryBuilder.update(fContainer, fScenarioInfo, event);
        return true;
    }

}
