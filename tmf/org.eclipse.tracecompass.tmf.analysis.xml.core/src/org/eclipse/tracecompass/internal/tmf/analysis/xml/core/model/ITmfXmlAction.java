/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Interface for an action behavior. An action is an output of the pattern.
 * Basically something that the pattern needs to do if it reaches a given state.
 *
 * @author Jean-Christian Kouame
 */
public interface ITmfXmlAction {

    /** The save stored fields action label */
    String SAVE_STORED_FIELDS_STRING = "saveStoredFields"; //$NON-NLS-1$

    /** The clear stored fields action label */
    String CLEAR_STORED_FIELDS_STRING = "clearStoredFields"; //$NON-NLS-1$

    /**
     * Execute the action
     *
     * @param event
     *            The active event
     * @param scenarioInfo
     *            The active scenario details. Or <code>null</code> if there is
     *            no scenario.
     */
    void execute(ITmfEvent event, TmfXmlScenarioInfo scenarioInfo);
}
