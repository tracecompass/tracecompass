/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.Activator;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlScenarioHistoryBuilder.ScenarioStatusType;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.pattern.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * This Class implements a Scenario in the XML-defined state system
 */
public class TmfXmlScenario {

    private final IXmlStateSystemContainer fContainer;
    private final TmfXmlFsm fFsm;
    private TmfXmlPatternEventHandler fPatternHandler;
    private TmfXmlScenarioInfo fScenarioInfo;
    TmfXmlScenarioHistoryBuilder fHistoryBuilder;

    /**
     * Constructor
     *
     * @param event
     *            The event at which this scenario is created
     * @param patternHandler
     *            The filter handler
     * @param fsmId
     *            the id of the fsm executed by this scenario
     * @param container
     *            The state system container this scenario belongs to
     * @param modelFactory
     *            The model factory
     */
    public TmfXmlScenario(@Nullable ITmfEvent event, TmfXmlPatternEventHandler patternHandler, String fsmId, IXmlStateSystemContainer container, ITmfXmlModelFactory modelFactory) {
        TmfXmlFsm fsm = patternHandler.getFsm(fsmId);
        if (fsm == null) {
            throw new IllegalArgumentException(fsmId + "has not been declared."); //$NON-NLS-1$
        }
        fFsm = fsm;
        fContainer = container;
        fHistoryBuilder = ((XmlPatternStateProvider) container).getHistoryBuilder();
        fPatternHandler = patternHandler;
        int quark = fHistoryBuilder.assignScenarioQuark(fContainer, fsmId);
        int statusQuark = fHistoryBuilder.getScenarioStatusQuark(fContainer, quark);
        fScenarioInfo = new TmfXmlScenarioInfo(fFsm.getInitialStateId(), ScenarioStatusType.PENDING, quark, statusQuark, fFsm);
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
        return fScenarioInfo.getStatus().equals(ScenarioStatusType.PENDING) || fScenarioInfo.getStatus().equals(ScenarioStatusType.IN_PROGRESS);
    }

    /**
     * Handle the ongoing event
     *
     * @param event
     *            The ongoing event
     */
    public void handleEvent(ITmfEvent event) {

        TmfXmlStateTransition out = fFsm.next(event, fPatternHandler.getTestMap(), fScenarioInfo);
        if (out == null) {
            return;
        }

        fFsm.setEventConsumed(true);
        // Processing the actions in the transition
        final List<String> actions = out.getAction();
        for (String actionId : actions) {
            ITmfXmlAction action = fPatternHandler.getActionMap().get(actionId);
            if (action != null) {
                action.execute(event, fScenarioInfo);
            } else {
                Activator.logError("Action " + actionId + " cannot be found."); //$NON-NLS-1$ //$NON-NLS-2$
                return;
            }
        }

        // Change the activeState
        final @NonNull String nextState = out.getTarget();
        if (fScenarioInfo.getStatus().equals(ScenarioStatusType.PENDING)) {
            fScenarioInfo.setStatus(ScenarioStatusType.IN_PROGRESS);
            fHistoryBuilder.startScenario(fContainer, fScenarioInfo, event);
        } else if (nextState.equals(fFsm.getAbandonStateId())) {
            fScenarioInfo.setStatus(ScenarioStatusType.ABANDONED);
            fHistoryBuilder.completeScenario(fContainer, fScenarioInfo, event);
        } else if (nextState.equals(fFsm.getFinalStateId())) {
            fScenarioInfo.setStatus(ScenarioStatusType.MATCHED);
            fHistoryBuilder.completeScenario(fContainer, fScenarioInfo, event);
        }
        fScenarioInfo.setActiveState(nextState);
        fHistoryBuilder.update(fContainer, fScenarioInfo, event);
    }

}
