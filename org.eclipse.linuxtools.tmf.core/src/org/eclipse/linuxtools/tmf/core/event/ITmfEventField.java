/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Removed arrays from the API
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;

/**
 * The generic event payload in TMF. Each field can be either a terminal or
 * further decomposed into subfields.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfEvent
 * @see ITmfEventType
 */
public interface ITmfEventField {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The root field id (the main container)
     */
    public static final @NonNull String ROOT_FIELD_ID = ":root:"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the field name
     */
    String getName();

    /**
     * @return the field value
     */
    Object getValue();

    /**
     * @return the value formatted as string
     * @since 2.0
     */
    String getFormattedValue();

    /**
     * Return the subfield names. The iteration order is the same as
     * {@link #getFields()}. The returned Collection is immutable.
     *
     * @return The subfield names (empty Collection if none)
     * @since 3.0
     */
    Collection<String> getFieldNames();

    /**
     * Return the subfield. The iteration order is the same as
     * {@link #getFieldNames()}. The returned Collection is immutable.
     *
     * @return The subfields (empty Collection if none)
     * @since 3.0
     */
    Collection<? extends ITmfEventField> getFields();

    /**
     * @param name The name of the field
     * @return a specific subfield by name (null if absent or inexistent)
     */
    ITmfEventField getField(String name);

    /**
     * Gets the a sub-field of this field, which may be multiple levels down.
     *
     * @param path
     *            Array of field names to recursively go through
     * @return The field at the end, or null if a field in the path cannot be
     *         found
     * @since 3.0
     */
    ITmfEventField getSubField(String... path);

}
