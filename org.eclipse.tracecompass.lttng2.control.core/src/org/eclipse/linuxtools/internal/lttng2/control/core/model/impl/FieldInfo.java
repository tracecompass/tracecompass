/**********************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.core.model.impl;

import org.eclipse.linuxtools.internal.lttng2.control.core.model.IFieldInfo;

/**
* <p>
* Implementation of the basic trace event interface (IEventInfo) to store event
* related data.
* </p>
*
* @author Bernd Hufmann
*/
public class FieldInfo extends TraceInfo implements IFieldInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The trace event type.
     */
    private String fFieldType;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - name of base event
     */
    public FieldInfo(String name) {
        super(name);
    }

    /**
     * Copy constructor
     * @param other - the instance to copy
     */
    public FieldInfo(FieldInfo other) {
        super(other);
        fFieldType = other.fFieldType;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public String getFieldType() {
        return fFieldType;
    }

    @Override
    public void setFieldType(String fieldType) {
        fFieldType = fieldType.toLowerCase();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((fFieldType == null) ? 0 : fFieldType.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        FieldInfo other = (FieldInfo) obj;
        if (fFieldType == null) {
            if (other.fFieldType != null) {
                return false;
            }
        } else if (!fFieldType.equals(other.fFieldType)) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
            output.append("[FieldInfo(");
            output.append(super.toString());
            output.append(",type=");
            output.append(fFieldType);
            return output.toString();
    }
}