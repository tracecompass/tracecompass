/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsm;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenFsmState;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model.TmfXmlScenarioHistoryBuilder.ScenarioStatusType;

/**
 * This class gives basic details about a scenario (quark, scenarioName, ...)
 *
 * @author Jean-Christian Kouame
 */
public class TmfXmlScenarioInfo extends DataDrivenScenarioInfo {

    private final DataDrivenFsm fFsm;

    /**
     * Constructor
     *
     * @param activeState
     *            The active state
     * @param status
     *            The scenario status
     * @param quark
     *            The scenario quark
     * @param statusQuark
     *            The scenario status quark
     * @param fsm
     *            The FSM this scenario is part of
     */
    public TmfXmlScenarioInfo(DataDrivenFsmState activeState, ScenarioStatusType status, int quark, int statusQuark, DataDrivenFsm fsm) {
        super(activeState, status, quark, statusQuark);
        fFsm = fsm;
    }

    /**
     * Get the ID of the FSM this scenario is part of
     *
     * @return The ID of the FSM
     */
    public DataDrivenFsm getFsm() {
        return fFsm;
    }

}
