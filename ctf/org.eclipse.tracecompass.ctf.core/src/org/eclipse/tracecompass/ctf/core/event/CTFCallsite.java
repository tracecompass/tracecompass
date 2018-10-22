/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *  Matthew Khouzam - Initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.event;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * Callsite information to help with cdt integration
 *
 * @author Matthew Khouzam
 * @since 2.1
 */
@NonNullByDefault
public class CTFCallsite {
    /**
     * The event name
     */
    private final String fEventName;

    /**
     * the file name of the callsite
     */
    private final String fFileName;

    /**
     * the instruction pointer
     */
    private final long fIp;

    /**
     * the function name
     */
    private final String fFunctionName;

    /**
     * the line number of the callsite
     */
    private final long fLineNumber;

    /**
     * The callsite constructor
     *
     * @param en
     *            The event name
     * @param func
     *            the function name
     * @param ip
     *            the instruction pointer of the callsite
     * @param fn
     *            the file name of the callsite
     * @param line
     *            the line number of the callsite
     */
    public CTFCallsite(String en, String func, long ip, String fn, long line) {
        fEventName = en;
        fFileName = fn;
        fFunctionName = func;
        fIp = ip;
        fLineNumber = line;
    }

    /**
     * @return the eventName
     */
    public String getEventName() {
        return fEventName;
    }

    /**
     * @return the fileName
     */
    public String getFileName() {
        return fFileName;
    }

    /**
     * @return the ip
     */
    public long getIp() {
        return fIp;
    }

    /**
     * @return the functionName
     */
    public String getFunctionName() {
        return fFunctionName;
    }

    /**
     * @return the lineNumber
     */
    public long getLineNumber() {
        return fLineNumber;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fEventName, fFileName, fIp, fFunctionName, fLineNumber);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CTFCallsite other = (CTFCallsite) obj;
        if (!fEventName.equals(other.fEventName)) {
            return false;
        }
        if (!fFileName.equals(other.fFileName)) {
            return false;
        }
        if (!fFunctionName.equals(other.fFunctionName)) {
            return false;
        }
        if (fIp != other.fIp) {
            return false;
        }

        return (fLineNumber == other.fLineNumber);
    }

    @Override
    public String toString() {
        return fFileName + "/" + fFunctionName + ":" + fLineNumber; //$NON-NLS-1$ //$NON-NLS-2$
    }
}
