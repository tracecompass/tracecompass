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

import org.eclipse.linuxtools.internal.lttng2.control.core.model.ITraceInfo;

/**
 * <p>
 * Implementation of the base trace information interface (ITraceInfo) to
 * store common data.
 * </p>
 *
 * @author Bernd Hufmann
 */
public class TraceInfo implements ITraceInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The name of the element.
     */
    private String fName = null;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - name of trace element
     */
    public TraceInfo(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        fName = name;
    }

    /**
     * Copy constructor
     * @param other - the instance to copy
     */
    public TraceInfo(TraceInfo other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }
        fName = String.valueOf(other.fName);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public String getName() {
        return fName;
    }

    @Override
    public void setName(String name) {
        fName = name;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fName == null) ? 0 : fName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TraceInfo other = (TraceInfo) obj;
        if (fName == null) {
            if (other.fName != null) {
                return false;
            }
        } else if (!fName.equals(other.fName)) {
            return false;
        }
        return true;
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
            output.append("[TraceInfo(");
            output.append("Name=");
            output.append(getName());
            output.append(")]");
            return output.toString();
    }
}