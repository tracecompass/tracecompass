/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model;

import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.model.runtime.DataDrivenScenarioInfo;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.module.IAnalysisDataContainer;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * A data-driven action base class
 *
 * @author Geneviève Bastien
 */
public interface DataDrivenAction extends IDataDrivenRuntimeObject {

    /**
     * An empty action
     */
    public static final DataDrivenAction NO_ACTION = (e, x, c) -> { /* Nothing to do */ };

    /**
     * Handle the event
     *
     * @param event
     *            The event to handle
     * @param scenarioInfo
     *            The scenario info
     * @param container
     *            The analysis data container
     */
    void eventHandle(ITmfEvent event, DataDrivenScenarioInfo scenarioInfo, IAnalysisDataContainer container);

}
