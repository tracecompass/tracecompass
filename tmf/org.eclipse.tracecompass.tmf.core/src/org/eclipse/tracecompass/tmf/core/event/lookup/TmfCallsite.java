/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.event.lookup;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;

/**
 * TMF call site information for source code lookup.
 *
 * @author Bernd Hufmann
 */
public class TmfCallsite implements ITmfCallsite {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** The file name string. */
    private final @NonNull String fFileName;

    /** The function name. */
    private final String fFunctionName;

    /** The line number. */
    private final long fLineNumber;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor.
     *
     * @param fileName
     *            - a file name
     * @param functionName
     *            - a function name
     * @param lineNumber
     *            - a line number
     */
    public TmfCallsite(String fileName, String functionName, long lineNumber) {
        if (fileName == null) {
            throw new IllegalArgumentException();
        }
        fFileName = fileName;
        fFunctionName = functionName;
        fLineNumber = lineNumber;
    }

    /**
     * Copy Constructor.
     *
     * @param other
     *            - An other call site implementation
     */
    public TmfCallsite(ITmfCallsite other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }
        fFileName = other.getFileName();
        fFunctionName = other.getFunctionName();
        fLineNumber = other.getLineNumber();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public @NonNull String getFileName() {
        return fFileName;
    }

    @Override
    public String getFunctionName() {
        return fFunctionName;
    }

    @Override
    public long getLineNumber() {
        return fLineNumber;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        // fFileName cannot be null!
        result = prime * result + fFileName.hashCode();
        result = prime * result + ((fFunctionName == null) ? 0 : fFunctionName.hashCode());
        result = prime * result + (int) (fLineNumber ^ (fLineNumber >>> 32));
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
        TmfCallsite other = (TmfCallsite) obj;

        // fFileName cannot be null!
        if (!fFileName.equals(other.fFileName)) {
            return false;
        }

        if (!Objects.equals(fFunctionName, other.fFunctionName)) {
            return false;
        }
        if (fLineNumber != other.fLineNumber) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(fFileName).append(':');
        builder.append(Long.toString(fLineNumber));
        if (fFunctionName != null) {
            builder.append(' ');
            builder.append(fFunctionName).append("()"); //$NON-NLS-1$
        }
        return builder.toString();
    }
}
