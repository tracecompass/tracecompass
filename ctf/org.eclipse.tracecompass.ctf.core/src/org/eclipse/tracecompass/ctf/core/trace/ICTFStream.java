/*******************************************************************************
 * Copyright (c) 2011, 2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 * Contributors: Simon Marchi - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.trace;

import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.ctf.core.event.IEventDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.IDeclaration;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.internal.ctf.core.event.EventDeclaration;

/**
 * An _event stream_ can be divided into contiguous event packets of variable
 * size. An event packet can contain a certain amount of padding at the end. The
 * stream header is repeated at the beginning of each event packet. The
 * rationale for the event stream design choices is explained in
 * <a href="http://diamon.org/ctf/#specB" >Stream header rationale<a/>.
 * <p>
 *
 * The event stream header will therefore be referred to as the _event packet
 * header_ throughout the rest of this document.
 *
 * @author Matthew Khouzam
 * @author Efficios - Javadoc
 * @since 2.0
 */
public interface ICTFStream {

    /**
     * Gets the id of a stream
     *
     * @return id the id of a stream
     * @since 1.0
     */
    long getId();

    /**
     * Is the id of a stream set
     *
     * @return If the ID is set or not
     */
    boolean isIdSet();

    /**
     *
     * @return is the event header set (timestamp and stuff) (see Ctf Spec)
     */
    boolean isEventHeaderSet();

    /**
     *
     * @return is the event context set (pid and stuff) (see Ctf Spec)
     */
    boolean isEventContextSet();

    /**
     *
     * @return Is the packet context set (see Ctf Spec)
     */
    boolean isPacketContextSet();

    /**
     * Gets the event header declaration
     *
     * @return the event header declaration in declaration form
     */
    IDeclaration getEventHeaderDeclaration();

    /**
     *
     * @return the event context declaration in structdeclaration form
     */
    StructDeclaration getEventContextDecl();

    /**
     *
     * @return the packet context declaration in structdeclaration form
     */
    StructDeclaration getPacketContextDecl();

    /**
     *
     * @return the set of all stream inputs for this stream
     */
    Set<CTFStreamInput> getStreamInputs();

    /**
     *
     * @return the parent trace
     */
    CTFTrace getTrace();

    /**
     * Get all the event declarations in this stream.
     *
     * @return The event declarations for this stream
     * @since 2.0
     */
    @NonNull
    List<@Nullable IEventDeclaration> getEventDeclarations();

    /**
     * Get the event declaration for a given ID.
     *
     * @param eventId
     *            The ID, can be {@link EventDeclaration#UNSET_EVENT_ID}, or any
     *            positive value
     * @return The event declaration with the given ID for this stream, or
     *         'null' if there are no declaration with this ID
     * @throws IllegalArgumentException
     *             If the passed ID is invalid
     */
    @Nullable
    IEventDeclaration getEventDeclaration(int eventId);

}