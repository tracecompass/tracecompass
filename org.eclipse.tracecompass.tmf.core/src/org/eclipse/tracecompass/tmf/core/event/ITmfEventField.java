/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Removed arrays from the API
 *   Patrick Tasse - Use ellipsis for getField and remove getSubField
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event;

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
     */
    String getFormattedValue();

    /**
     * Return the subfield names. The iteration order is the same as
     * {@link #getFields()}. The returned Collection is immutable.
     *
     * @return The subfield names (empty Collection if none)
     */
    Collection<String> getFieldNames();

    /**
     * Return the subfields. The iteration order is the same as
     * {@link #getFieldNames()}. The returned Collection is immutable.
     *
     * @return The subfields (empty Collection if none)
     */
    Collection<? extends ITmfEventField> getFields();

    /**
     * Return a subfield by its path relative to this field.
     * If the path is empty, this field is returned.
     * @param path The path to the subfield
     * @return a specific subfield by path (null if inexistent)
     */
    ITmfEventField getField(@NonNull String... path);

}
