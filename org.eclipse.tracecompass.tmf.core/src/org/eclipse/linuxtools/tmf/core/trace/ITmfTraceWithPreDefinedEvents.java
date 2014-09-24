/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import java.util.Set;

import org.eclipse.linuxtools.tmf.core.event.ITmfEventType;

/**
 * This interface should be implemented by all trace classes who have a way to
 * know in advance what events it may contain. It allows analyses and other
 * external components to ask the list of events for the trace might contain.
 *
 * The methods from this interface will typically be called to determine whether
 * or not it is worth reading a trace. If we can know in advance that a trace
 * does not contain the events required by an analysis, then the analysis will
 * not be run. So the response should not involve having to actually read the
 * trace.
 *
 * @author Geneviève Bastien
 * @author Matthew Khouzam
 * @since 3.0
 */
public interface ITmfTraceWithPreDefinedEvents {

    /**
     * Return a set of event types declared in the trace, without actually
     * reading the trace. This method can be called before reading a trace but
     * after it is initialized, in order to compare this set with a set of
     * events that a request handles, to determine whether or not it is worth
     * reading the trace.
     *
     * Some trace types have ways to determine the events that were traced
     * without having to read the whole trace and this is what this method will
     * query. The presence of an event in the returned set does not guarantee
     * that an event with this name actually happened during this trace, only
     * that it can be there.
     *
     * The set should be immutable. Destructive set operations should be
     * performed on a copy of this set.A helper class
     * {@link TmfEventTypeCollectionHelper} will provide ways of working with
     * this data structure.
     *
     * @return The set of events that might be present in the trace
     */
    Set<ITmfEventType> getContainedEventTypes();

}
