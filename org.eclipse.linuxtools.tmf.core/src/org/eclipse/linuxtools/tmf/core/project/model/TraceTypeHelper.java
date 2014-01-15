/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Bernd Hufmann - Handling of directory traces types
 *   Geneviève Bastien - Added support of experiment types
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.project.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.tmf.core.project.model.TmfTraceType.TraceElementType;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TraceValidationStatus;

/**
 * TraceTypeHelper, a helper that can link a few names to a configuration element
 * and a trace
 *
 * @author Matthew Khouzam
 * @since 3.0
 */
public class TraceTypeHelper {

    private final String fName;
    private final String fCategoryName;
    private final String fCanonicalName;
    private final TraceElementType fElementType;
    private final ITmfTrace fTrace;
    private final boolean fIsDirectory;

    /**
     * Constructor for a trace type helper. It is a link between a canonical
     * (hard to read) name, a category name, a name and a trace object. It is
     * used for trace validation.
     *
     * @param canonicalName
     *            The "path" of the tracetype
     * @param categoryName
     *            the category of the trace type
     * @param name
     *            the name of the trace
     * @param trace
     *            an object of the trace type
     * @param isDir
     *            flag indicating whether the trace type is for a directory or file trace
     * @param elementType
     *            True if this helper is for an experiment type
     */
    public TraceTypeHelper(String canonicalName, String categoryName, String name, ITmfTrace trace, boolean isDir, TraceElementType elementType) {
        fName = name;
        fCategoryName = categoryName;
        fCanonicalName = canonicalName;
        fTrace = trace;
        fIsDirectory = isDir;
        fElementType = elementType;
    }

    /**
     * Get the name
     *
     * @return the name
     */
    public String getName() {
        return fName;
    }

    /**
     * Get the category name
     *
     * @return the category name
     */
    public String getCategoryName() {
        return fCategoryName;
    }

    /**
     * Get the canonical name
     *
     * @return the canonical Name
     */
    public String getCanonicalName() {
        return fCanonicalName;
    }

    /**
     * Is the trace of this type?
     *
     * @param path
     *            the trace to validate
     * @return whether it passes the validation
     */
    public boolean validate(String path) {
        boolean valid = false;
        if (fTrace != null) {
            valid = standardValidate(path);
        }
        return valid;
    }

    /**
     * Validate a trace against this trace type with confidence level
     *
     * @param path
     *            the trace to validate
     * @return the confidence level (0 is lowest) or -1 if validation fails
     * @since 3.0
     */
    public int validateWithConfidence(String path) {
        int result = -1;
        if (fTrace != null) {
            IStatus status = fTrace.validate(null, path);
            if (status.isOK()) {
                result = 0;
                if (status instanceof TraceValidationStatus) {
                    result = ((TraceValidationStatus) status).getConfidence();
                }
            }
        }
        return result;
    }

    /**
     * Get an object of the trace type
     * @return an object of the trace type
     * @since 2.1
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Return whether this helper applies to a trace type or experiment type
     *
     * @return True if experiment type, false otherwise
     */
    public boolean isExperimentType() {
        return fElementType == TraceElementType.EXPERIMENT;
    }

    private boolean standardValidate(String path) {
        final boolean valid = fTrace.validate(null, path).isOK();
        return valid;
    }

    /**
     * Get the class associated with this trace type
     *
     * @return The trace class
     * @since 3.0
     */
    public Class<? extends ITmfTrace> getTraceClass() {
        return fTrace.getClass();
    }

    /**
     * Returns whether trace type is for a directory trace or a single file trace
     * @return <code>true</code> if trace type is for a directory trace else <code>false</code>
     */
    public boolean isDirectoryTraceType() {
        return fIsDirectory;
    }


    @Override
    public String toString() {
        return fName;
    }

}
