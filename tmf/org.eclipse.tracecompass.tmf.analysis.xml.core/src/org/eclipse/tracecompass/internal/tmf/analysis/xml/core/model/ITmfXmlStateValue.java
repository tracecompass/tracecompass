/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.model;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;

/**
 * Interface that describe operations on a state value described in an XML
 * element
 *
 * @author Geneviève Bastien
 */
public interface ITmfXmlStateValue {

    /**
     * Get the current {@link ITmfStateValue} of this state value for an event.
     * It does not increment the value and does not any other processing of the
     * value.
     *
     * @param event
     *            The current event or <code>null</code> if no event is
     *            available.
     * @param scenarioInfo
     *            The active scenario details. Or <code>null</code> if there is
     *            no scenario.
     * @return the {@link ITmfStateValue}
     * @throws AttributeNotFoundException
     *             May be thrown by the state system during the query
     */
    ITmfStateValue getValue(@Nullable ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo) throws AttributeNotFoundException;

    /**
     * Get the value of the event field that is the path of this state value
     *
     * @param event
     *            The current event
     * @return the value of the event field
     */
    ITmfStateValue getEventFieldValue(ITmfEvent event);

    /**
     * Get the list of state attributes, the path to the state value
     *
     * @return the list of Attribute to have the path in the State System
     */
    List<ITmfXmlStateAttribute> getAttributes();

    /**
     * Handles an event, by setting the value of the attribute described by the
     * state attribute path in the state system.
     *
     * @param event
     *            The event to process
     * @param scenarioInfo
     *            The active scenario details. Or <code>null</code> if there is
     *            no scenario.
     * @throws AttributeNotFoundException
     *             Pass through the exception it received
     * @throws TimeRangeException
     *             Pass through the exception it received
     * @throws StateValueTypeException
     *             Pass through the exception it received
     */
    void handleEvent(ITmfEvent event, @Nullable TmfXmlScenarioInfo scenarioInfo) throws AttributeNotFoundException, StateValueTypeException, TimeRangeException;

}
