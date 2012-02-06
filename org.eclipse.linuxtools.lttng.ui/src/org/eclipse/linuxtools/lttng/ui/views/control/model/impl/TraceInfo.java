/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.control.model.impl;

import org.eclipse.linuxtools.lttng.ui.views.control.model.ITraceInfo;

/**
 * <b><u>TraceInfo</u></b>
 * <p>
 * Implementation of the base trace information interface (ITraceInfo) to
 * store common data.
 * </p>
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
        } else {
            fName = String.valueOf(other.fName);
        }
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.ITraceInfo#getName()
     */
    @Override
    public String getName() {
        return fName;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.lttng.ui.views.control.model.ITraceInfo#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        fName = name;
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        if (fName == null) {
            return 17;
        }
        return fName.hashCode();
    }

    /*
     * (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof TraceInfo)) {
            return false;
        }

        TraceInfo otherInfo = (TraceInfo) other;
        return fName.equals(otherInfo.fName);
    }
    
    /*
     * (non-Javadoc)
     * @see java.lang.Object#toString()
     */
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