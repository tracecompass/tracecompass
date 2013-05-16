/*******************************************************************************
 * Copyright (c) 2011-2013 Ericsson, Ecole Polytechnique de Montreal and others
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors: Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.event;

import java.util.Set;

import org.eclipse.linuxtools.ctf.core.event.types.StructDeclaration;
import org.eclipse.linuxtools.ctf.core.trace.Stream;
import org.eclipse.linuxtools.ctf.core.trace.StreamInputReader;

/**
 * Representation of one type of event. A bit like "int" or "long" but for trace
 * events.
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public interface IEventDeclaration {

    /**
     * Creates an instance of EventDefinition corresponding to this declaration.
     *
     * @param streamInputReader
     *            The StreamInputReader for which this definition is created.
     * @return A new EventDefinition.
     */
    EventDefinition createDefinition(StreamInputReader streamInputReader);

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
     * Gets the {@link Stream} of an event declaration
     *
     * @return the stream
     * @since 2.0
     */
    Stream getStream();

    /**
     * What is the log level of this event?
     *
     * @return the log level.
     * @since 2.0
     */
    long getLogLevel();

    /**
     * Get the {@link Set} of names of the custom CTF attributes.
     *
     * @return The set of custom attributes
     * @since 2.0
     */
    Set<String> getCustomAttributes();

    /**
     * Get the value of a given CTF attribute.
     *
     * @param key
     *            The CTF attribute name
     * @return the CTF attribute
     * @since 2.0
     */
    String getCustomAttribute(String key);

}
