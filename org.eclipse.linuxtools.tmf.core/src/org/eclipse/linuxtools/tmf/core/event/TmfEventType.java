/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
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

package org.eclipse.linuxtools.tmf.core.event;

import java.util.Collection;
import java.util.Collections;

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

    private final String fContext;
    private final String fTypeId;
    private final ITmfEventField fRootField;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfEventType() {
        this(DEFAULT_CONTEXT_ID, DEFAULT_TYPE_ID, null);
    }

    /**
     * Full constructor
     *
     * @param context the type context
     * @param typeId the type name
     * @param root the root field
     */
    public TmfEventType(final String context, final String typeId, final ITmfEventField root) {
        if (context == null || typeId == null) {
            throw new IllegalArgumentException();
        }
        fContext = context;
        fTypeId = typeId;
        fRootField = root;

        // Register to the event type manager
        TmfEventTypeManager.getInstance().add(context, this);
    }

    /**
     * Copy constructor
     *
     * @param type the other type
     */
    public TmfEventType(final ITmfEventType type) {
        if (type == null) {
            throw new IllegalArgumentException();
        }
        fContext = type.getContext();
        fTypeId  = type.getName();
        fRootField = type.getRootField();
    }

    // ------------------------------------------------------------------------
    // ITmfEventType
    // ------------------------------------------------------------------------

    @Override
    public String getContext() {
        return fContext;
    }

    @Override
    public String getName() {
        return fTypeId;
    }

    @Override
    public ITmfEventField getRootField() {
        return fRootField;
    }

    /**
     * @since 3.0
     */
    @Override
    public Collection<String> getFieldNames() {
        return (fRootField != null) ? fRootField.getFieldNames() : Collections.EMPTY_SET;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fContext.hashCode();
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
        if (!fContext.equals(other.fContext)) {
            return false;
        }
        if (!fTypeId.equals(other.fTypeId)) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfEventType [fContext=" + fContext + ", fTypeId=" + fTypeId + "]";
    }

}
