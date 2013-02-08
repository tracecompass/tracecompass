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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.ctfadaptor;

import org.eclipse.linuxtools.ctf.core.event.CTFCallsite;

/**
 * Callsite information
 * @since 2.0
 */
public class CtfTmfCallsite {

    private String eventName;
    private String fileName;
    private String functionName;
    private long lineNumber;
    private long ip;

    CtfTmfCallsite(CTFCallsite callsite) {
        eventName = callsite.getEventName();
        fileName = callsite.getFileName();
        functionName = callsite.getFunctionName();
        lineNumber = callsite.getLineNumber();
        ip = callsite.getIp();
    }

    /**
     * @return the event name
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * @return the file name
     */
    public String getFileName() {
        return fileName;
    }

    /**
     * @return the function name
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * @return the line number
     */
    public long getLineNumber() {
        return lineNumber;
    }

    /**
     * @return the ip
     */
    public long getIp() {
        return ip;
    }

    @Override
    public String toString() {
        return eventName + "@0x" + Long.toHexString(ip) + ": " + //$NON-NLS-1$ //$NON-NLS-2$
                fileName + ':' + Long.toString(lineNumber) + ' ' + functionName + "()"; //$NON-NLS-1$
    }
}
