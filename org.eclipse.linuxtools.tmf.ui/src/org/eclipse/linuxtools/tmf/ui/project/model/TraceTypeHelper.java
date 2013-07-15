/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.model;

import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * TraceTypeHelper, a helper that can link a few names to a configuation element
 * and a trace
 *
 * @author Matthew Khouzam
 * @since 2.0
 */
public class TraceTypeHelper {

    private final String fName;
    private final String fCategoryName;
    private final String fCanonicalName;
    private final ITmfTrace fTrace;

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
     */
    public TraceTypeHelper(String canonicalName, String categoryName, String name, ITmfTrace trace) {
        fName = name;
        fCategoryName = categoryName;
        fCanonicalName = canonicalName;
        fTrace = trace;
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
     * Get an object of the trace type
     * @return an object of the trace type
     * @since 2.1
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    private boolean standardValidate(String path) {
        final boolean valid = fTrace.validate(null, path).equals(Status.OK_STATUS);
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

    @Override
    public String toString() {
        return fName;
    }

}