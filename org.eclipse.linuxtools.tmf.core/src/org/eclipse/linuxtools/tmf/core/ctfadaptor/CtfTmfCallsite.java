/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Patrick Tasse - Initial API and implementation
 *     Bernd Hufmann - Updated for new parent class
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.ctf.core.event.CTFCallsite;
import org.eclipse.linuxtools.tmf.core.event.lookup.TmfCallsite;

/**
 * CTF TMF call site information for source code lookup.
 *
 * @author Patrick Tasse
 * @since 2.0
 */
public class CtfTmfCallsite extends TmfCallsite {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** The event name. */
    final private String fEventName;

    /** The instruction pointer. */
    final private long fInstructionPointer;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard Constructor.
     *
     * @param callsite
     *              - a CTF call site
     */
    CtfTmfCallsite(CTFCallsite callsite) {
        super(callsite.getFileName(), callsite.getFunctionName(), callsite.getLineNumber());
        fEventName = callsite.getEventName();
        fInstructionPointer = callsite.getIp();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Returns the event name of the call site.
     * @return the event name
     */
    public String getEventName() {
        return fEventName;
    }

    /**
     * Returns the instruction pointer of the call site.
     * @return the instruction pointer
     */
    public long getIntructionPointer() {
        return fInstructionPointer;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fEventName == null) ? 0 : fEventName.hashCode());
        result = prime * result + (int) (fInstructionPointer ^ (fInstructionPointer >>> 32));
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
        CtfTmfCallsite other = (CtfTmfCallsite) obj;
        if (fEventName == null) {
            if (other.fEventName != null) {
                return false;
            }
        } else if (!fEventName.equals(other.fEventName)) {
            return false;
        }
        if (fInstructionPointer != other.fInstructionPointer) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return getEventName() + "@0x" + Long.toHexString(fInstructionPointer) + ": " + //$NON-NLS-1$ //$NON-NLS-2$
                getFileName() + ':' + Long.toString(getLineNumber()) + ' ' + getFileName() + "()"; //$NON-NLS-1$
    }
}
