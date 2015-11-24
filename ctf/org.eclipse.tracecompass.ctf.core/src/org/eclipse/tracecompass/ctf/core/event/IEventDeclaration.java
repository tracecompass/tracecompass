/*******************************************************************************
 * Copyright (c) 2011-2014 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.io.BitBuffer;
import org.eclipse.tracecompass.ctf.core.event.types.StructDeclaration;
import org.eclipse.tracecompass.ctf.core.trace.CTFStream;
import org.eclipse.tracecompass.ctf.core.trace.CTFStreamInputReader;

/**
 * Representation of one type of event. A bit like "int" or "long" but for trace
 * events.
 *
 * @author Matthew Khouzam
 */
public interface IEventDeclaration {

    /**
     * Id of events when not set
     *
     * @since 1.0
     */
    public static final long UNSET_EVENT_ID = -2L;

    /**
     * Creates an instance of EventDefinition corresponding to this declaration.
     *
     * @param streamInputReader
     *            The StreamInputReader for which this definition is created.
     * @param input
     *            the bitbuffer input source
     * @param timestamp
     *            The timestamp when the event was taken
     * @return A new EventDefinition.
     * @throws CTFException
     *             As a bitbuffer is used to read, it could have wrapped
     *             IOExceptions.
     */
    EventDefinition createDefinition(CTFStreamInputReader streamInputReader, @NonNull BitBuffer input, long timestamp) throws CTFException;

    /**
     * Gets the name of an event declaration
     *
     * @return the name
     */
    String getName();

    /**
     * Gets the fields of an event declaration
     *
     * @return fields the fields in {@link StructDeclaration} format
     */
    StructDeclaration getFields();

    /**
     * Gets the context of an event declaration
     *
     * @return context the fields in {@link StructDeclaration} format
     */
    StructDeclaration getContext();

    /**
     * Gets the id of an event declaration
     *
     * @return The EventDeclaration ID
     */
    Long getId();

    /**
     * Gets the {@link CTFStream} of an event declaration
     *
     * @return the stream
     */
    CTFStream getStream();

    /**
     * What is the log level of this event?
     *
     * @return the log level.
     */
    long getLogLevel();

    /**
     * Get the {@link Set} of names of the custom CTF attributes.
     *
     * @return The set of custom attributes
     */
    @NonNull Set<@NonNull String> getCustomAttributes();

    /**
     * Get the value of a given CTF attribute.
     *
     * @param key
     *            The CTF attribute name
     * @return the CTF attribute
     */
    String getCustomAttribute(String key);

}
