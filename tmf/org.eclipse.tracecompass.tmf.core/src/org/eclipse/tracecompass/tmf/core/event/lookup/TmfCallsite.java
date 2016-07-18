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

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

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

    /** The line number. */
    private final @Nullable Long fLineNumber;

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
     * @deprecated Use {@link #TmfCallsite(String, Long)} instead.
     */
    @Deprecated
    public TmfCallsite(String fileName, String functionName, long lineNumber) {
        this(checkNotNull(fileName), lineNumber);
    }

    /**
     * Constructor
     *
     * @param fileName
     *            The source file's name
     * @param lineNumber
     *            The line number in the source file
     * @since 2.1
     */
    public TmfCallsite(@NonNull String fileName, @Nullable Long lineNumber) {
        fFileName = fileName;
        fLineNumber = lineNumber;
    }

    /**
     * Copy Constructor.
     *
     * @param other
     *            - An other call site implementation
     */
    public TmfCallsite(@NonNull ITmfCallsite other) {
        fFileName = other.getFileName();
        fLineNumber = other.getLineNo();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public @NonNull String getFileName() {
        return fFileName;
    }

    @Deprecated
    @Override
    public String getFunctionName() {
        return ""; //$NON-NLS-1$
    }

    @Deprecated
    @Override
    public long getLineNumber() {
        Long lineNumber = fLineNumber;
        return (lineNumber == null ? -1 : lineNumber.longValue());
    }


    /**
     * @since 2.1
     */
    @Override
    public @Nullable Long getLineNo() {
        return fLineNumber;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        return Objects.hash(fFileName, fLineNumber);
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

        if (fLineNumber != other.fLineNumber) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        Long lineNumber = fLineNumber;

        StringBuilder builder = new StringBuilder();
        builder.append(fFileName).append(':');
        builder.append(lineNumber == null ? "??" : Long.toString(lineNumber)); //$NON-NLS-1$
        return builder.toString();
    }
}
