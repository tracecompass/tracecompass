/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Determines a true or false value for a given input. The input is an event and
 * an optional scenarioInfo.
 *
 * @author Matthew Khouzam
 */
public interface ITmfXmlCondition {

    /**
     * Test the result of the condition for an event
     *
     * @param event
     *            The event on which to test the condition
     * @param scenarioInfo
     *            The active scenario details. Or <code>null</code> if there is
     *            no scenario.
     * @return Whether the condition is true or not
     */
    boolean test(ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo);

}