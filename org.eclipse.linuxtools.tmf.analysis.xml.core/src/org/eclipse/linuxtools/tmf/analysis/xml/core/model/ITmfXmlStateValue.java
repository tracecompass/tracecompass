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

package org.eclipse.linuxtools.tmf.analysis.xml.core.model;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;

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
     * @return the {@link ITmfStateValue}
     * @throws AttributeNotFoundException
     *             May be thrown by the state system during the query
     */
    ITmfStateValue getValue(@Nullable ITmfEvent event) throws AttributeNotFoundException;

    /**
     * Get the value of the event field that is the path of this state value
     *
     * @param event
     *            The current event
     * @return the value of the event field
     */
    ITmfStateValue getEventFieldValue(@NonNull ITmfEvent event);

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
     * @throws AttributeNotFoundException
     *             Pass through the exception it received
     * @throws TimeRangeException
     *             Pass through the exception it received
     * @throws StateValueTypeException
     *             Pass through the exception it received
     */
    void handleEvent(@NonNull ITmfEvent event) throws AttributeNotFoundException, StateValueTypeException, TimeRangeException;

}
