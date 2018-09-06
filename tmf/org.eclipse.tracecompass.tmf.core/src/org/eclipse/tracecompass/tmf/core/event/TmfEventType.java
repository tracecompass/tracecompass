/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Event Model 1.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;

/**
 * A basic implementation of ITmfEventType.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfEvent
 * @see ITmfEventField
 */
public class TmfEventType implements ITmfEventType {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final @NonNull String fTypeId;
    private final ITmfEventField fRootField;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfEventType() {
        this(DEFAULT_TYPE_ID, null);
    }

    /**
     * Full constructor
     *
     * @param typeId the type name
     * @param root the root field
     */
    public TmfEventType(final @NonNull String typeId, final ITmfEventField root) {
        fTypeId = typeId;
        fRootField = root;
    }

    /**
     * Copy constructor
     *
     * @param type the other type
     */
    public TmfEventType(@NonNull ITmfEventType type) {
        fTypeId  = type.getName();
        fRootField = type.getRootField();
    }

    // ------------------------------------------------------------------------
    // ITmfEventType
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return fTypeId;
    }

    @Override
    public ITmfEventField getRootField() {
        return fRootField;
    }

    @Override
    public Collection<String> getFieldNames() {
        return (fRootField != null) ? fRootField.getFieldNames() : Collections.emptySet();
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fTypeId.hashCode();
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfEventType)) {
            return false;
        }
        final TmfEventType other = (TmfEventType) obj;
        if (!fTypeId.equals(other.fTypeId)) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfEventType [fTypeId=" + fTypeId + "]";
    }

}
