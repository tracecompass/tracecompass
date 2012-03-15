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
package org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl;

import org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IProbeEventInfo;

/**
* <b><u>ProbleEventInfo</u></b>
* <p>
* Implementation of the trace event interface (IProbeEventInfo) to store probe event
* related data. 
* </p>
*/
public class ProbeEventInfo extends EventInfo implements IProbeEventInfo {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * The dynamic probe address (null if symbol is used).
     */
    private String fAddress;
    /**
     * The dynamic probe offset (if symbol is used).
     */
    private String fOffset;
    
    /**
     * The symbol name (null if address is used)
     */
    private String fSymbol;
    
    
    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor
     * @param name - name of event
     */
    public ProbeEventInfo(String name) {
        super(name);
    }
    
    /**
     * Copy constructor
     * @param other - the instance to copy
     */
    public ProbeEventInfo(ProbeEventInfo other) {
        super(other);
        fAddress = other.fAddress;
        fOffset = other.fOffset;
        fSymbol = other.fSymbol;
    }
    
    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IProbeEventInfo#getAddress()
     */
    @Override
    public String getAddress() {
        return fAddress;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IProbeEventInfo#setAddress(java.lang.String)
     */
    @Override
    public void setAddress(String address) {
        fAddress = address;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IProbeEventInfo#getOffset()
     */
    @Override
    public String getOffset() {
        return fOffset;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IProbeEventInfo#setOffset(java.lang.String)
     */
    @Override
    public void setOffset(String offset) {
        fOffset = offset;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IProbeEventInfo#getSymbol()
     */
    @Override
    public String getSymbol() {
        return fSymbol;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.IProbeEventInfo#setSymbol(java.lang.String)
     */
    @Override
    public void setSymbol(String symbol) {
        fSymbol = symbol;
    }

    // ------------------------------------------------------------------------
    // Operation
    // ------------------------------------------------------------------------
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.ITraceInfo#formatString()
     */
   @SuppressWarnings("nls")
   @Override
   public String formatString() {
       StringBuffer output = new StringBuffer();
       //    name (type: probe) [enabled]");
       //       address: 
       output.append(super.formatString());
       if (fAddress != null) {
           output.append("\n        addr: ");
           output.append(fAddress);
       } else {
           output.append("\n        offset: ");
           output.append(fOffset);
           output.append("\n");
           output.append("        symbol: ");
           output.append(fSymbol);
       }
       return output.toString();
   }
    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.BaseEventInfo#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fAddress == null) ? 0 : fAddress.hashCode());
        result = prime * result + ((fOffset == null) ? 0 : fOffset.hashCode());
        result = prime * result + ((fSymbol == null) ? 0 : fSymbol.hashCode());
        return result;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.BaseEventInfo#equals(java.lang.Object)
     */
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
        ProbeEventInfo other = (ProbeEventInfo) obj;
        if (fAddress == null) {
            if (other.fAddress != null) {
                return false;
            }
        } else if (!fAddress.equals(other.fAddress)) {
            return false;
        }
        if (fOffset == null) {
            if (other.fOffset != null) {
                return false;
            }
        } else if (!fOffset.equals(other.fOffset)) {
            return false;
        }
        if (fSymbol == null) {
            if (other.fSymbol != null) {
                return false;
            }
        } else if (!fSymbol.equals(other.fSymbol)) {
            return false;
        }
        return true;
    }

    
    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.internal.lttng2.ui.views.control.model.impl.EventInfo#toString()
     */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        StringBuffer output = new StringBuffer();
            output.append("[ProbeEventInfo(");
            output.append(super.toString());
            if (fAddress != null) {
                output.append(",fAddress=");
                output.append(fAddress);
            } else {
                output.append(",fOffset=");
                output.append(fOffset);
                output.append(",fSymbol=");
                output.append(fSymbol);
            }
            output.append(")]");
            return output.toString();
    }


}
