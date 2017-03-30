/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
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
 *   Patrick Tasse - Renamed trace type id
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.project.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType.TraceElementType;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TraceValidationStatus;

/**
 * TraceTypeHelper, a helper that can link a few names to a configuration element
 * and a trace
 *
 * @author Matthew Khouzam
 */
public class TraceTypeHelper {

    private static final String SEP = " : "; //$NON-NLS-1$

    private final String fName;
    private final String fCategoryName;
    private final @NonNull String fTraceTypeId;
    private final TraceElementType fElementType;
    private final @NonNull ITmfTrace fTrace;
    private final boolean fIsDirectory;
    private boolean fEnable;

    /**
     * Constructor for a trace type helper. It is a link between a trace type
     * id, a category name, a name and a trace object.
     *
     * @param traceTypeId
     *            the trace type id
     * @param categoryName
     *            the category of the trace type
     * @param name
     *            the name of the trace type
     * @param trace
     *            an object of the trace type
     * @param isDir
     *            flag indicating whether the trace type is for a directory or
     *            file trace
     * @param elementType
     *            True if this helper is for an experiment type
     */
    public TraceTypeHelper(String traceTypeId, String categoryName, String name, @NonNull ITmfTrace trace, boolean isDir, TraceElementType elementType) {
        fName = name;
        fCategoryName = categoryName;
        fTraceTypeId = traceTypeId;
        fTrace = trace;
        fIsDirectory = isDir;
        fElementType = elementType;
        fEnable = true;
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
     * Get the trace type label "category : name".
     *
     * @return the trace type label
     */
    public String getLabel() {
        if (fCategoryName.isEmpty()) {
            return fName;
        }
        return fCategoryName + SEP + fName;
    }

    /**
     * Get the trace type id
     *
     * @return the trace type id
     */
    public @NonNull String getTraceTypeId() {
        return fTraceTypeId;
    }

    /**
     * Is the trace of this type?
     *
     * @param path
     *            the trace to validate
     * @return whether it passes the validation
     */
    public IStatus validate(String path) {
        return fTrace.validate(null, path);
    }

    /**
     * Validate a trace against this trace type with confidence level
     *
     * @param path
     *            the trace to validate
     * @return the confidence level (0 is lowest) or -1 if validation fails
     */
    public int validateWithConfidence(String path) {
        int result = -1;
        IStatus status = fTrace.validate(null, path);
        if (status.getSeverity() != IStatus.ERROR) {
            result = 0;
            if (status instanceof TraceValidationStatus) {
                result = ((TraceValidationStatus) status).getConfidence();
            }
        }
        return result;
    }

    /**
     * Get an object of the trace type
     * @return an object of the trace type
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

    /**
     * Get the class associated with this trace type
     *
     * @return The trace class
     */
    public Class<@NonNull ? extends ITmfTrace> getTraceClass() {
        return fTrace.getClass();
    }

    /**
     * Returns whether trace type is for a directory trace or a single file trace
     * @return <code>true</code> if trace type is for a directory trace else <code>false</code>
     */
    public boolean isDirectoryTraceType() {
        return fIsDirectory;
    }

    /**
     * Test whether the trace helper is enabled based on the trace type
     * preferences or not
     *
     * @return True if the trace helper is enabled, false otherwise
     * @since 2.4
     */
    public boolean isEnabled() {
        return fEnable;
    }

    /**
     * Enable/disable the trace type helper
     *
     * @param enable
     *            the new enable state
     * @since 2.4
     */
    public void setEnabled(boolean enable) {
        fEnable = enable;
    }

    @Override
    public String toString() {
        return fName;
    }

}
