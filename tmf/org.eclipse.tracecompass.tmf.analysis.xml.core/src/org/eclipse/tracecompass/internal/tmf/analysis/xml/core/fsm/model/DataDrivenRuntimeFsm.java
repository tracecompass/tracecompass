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
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlScenario;

/**
 * This class contains runtime data for a given FSM, like the scenarios, etc
 *
 * FIXME: Update TmfXmlScenario classes when legacy code is gone
 *
 * @author Geneviève Bastien
 */
public class DataDrivenRuntimeFsm {

    private int fScenarioCount = 0;
    private @Nullable TmfXmlScenario fPendingScenario = null;
    private List<TmfXmlScenario> fActiveScenarios = new ArrayList<>();

    /**
     * Get the number of active scenarios
     *
     * @return The number of active scenarios
     */
    public int getScenarioCount() {
        return fScenarioCount;
    }

    /**
     * Get the pending scenario
     *
     * @return The pending scenario or <code>null</code> if no scenario is
     *         pending
     */
    public @Nullable TmfXmlScenario getPendingScenario() {
        return fPendingScenario;
    }

    /**
     * Create a scenario that will be in the pending state. If there was a
     * previously pending scenario, it will be discarded without further
     * processing.
     *
     * @param scenario
     *            The initial state for this scenario
     */
    public void addPendingScenario(TmfXmlScenario scenario) {
        if (fPendingScenario == null) {
            fScenarioCount++;
        }
        fPendingScenario = scenario;
    }

    /**
     * Get the active scenarios
     *
     * @return The list of active scenarios
     */
    public List<TmfXmlScenario> getActiveScenarios() {
        return fActiveScenarios;
    }

    /**
     * Remove a scenario
     *
     * @param scenario
     *            The scenario to remove
     */
    public void removeScenario(TmfXmlScenario scenario) {
        if (fActiveScenarios.remove(scenario)) {
            fScenarioCount--;
        }
    }

    /**
     * Active the pending scenario. There will be no pending scenario after this
     */
    public synchronized void activatePending() {
        TmfXmlScenario pendingScenario = fPendingScenario;
        if (pendingScenario != null) {
            fPendingScenario = null;
            fActiveScenarios.add(pendingScenario);
        }
    }

}
