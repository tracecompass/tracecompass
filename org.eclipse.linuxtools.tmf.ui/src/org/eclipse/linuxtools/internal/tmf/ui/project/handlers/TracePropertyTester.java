/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.ui.project.handlers;

import java.util.Iterator;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceFolder;

/**
 * <b><u>TracePropertyTester</u></b>
 * <p>
 */
public class TracePropertyTester extends PropertyTester {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private final static String IS_IN_TRACE_FOLDER = "isInTraceFolder"; //$NON-NLS-1$
    private final static String IS_EXPERIMENT_TRACE = "isExperimentTrace"; //$NON-NLS-1$
    private final static String HAS_SUPPLEMENTARY_FILES = "hasSupplementaryFiles"; //$NON-NLS-1$
    private final static String TRACE_TYPE = "traceType"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TracePropertyTester() {
    }

    // ------------------------------------------------------------------------
    // IPropertyTester
    // ------------------------------------------------------------------------

    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

        // Check if the selected elements are in the trace folder
        if (IS_IN_TRACE_FOLDER.equals(property)) {
            if (receiver != null && receiver instanceof IStructuredSelection) {
                Iterator<?> iter = ((IStructuredSelection) receiver).iterator();
                while (iter.hasNext()) {
                    Object item = iter.next();
                    if (item instanceof TmfTraceElement) {
                        TmfTraceElement trace = (TmfTraceElement) item;
                        if (!(trace.getParent() instanceof TmfTraceFolder)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
                return true;
            }
        }

        // Check if the parent of a trace element is an experiment
        if (IS_EXPERIMENT_TRACE.equals(property)) {
            if (receiver != null && receiver instanceof TmfTraceElement) {
                TmfTraceElement trace = (TmfTraceElement) receiver;
                return trace.getParent() instanceof TmfExperimentElement;
            }
            return false;
        }

        // Check if traces has supplementary files
        if (HAS_SUPPLEMENTARY_FILES.equals(property)) {
            if (receiver == null) {
                return false;
            }

            if (receiver instanceof TmfTraceElement) {
                TmfTraceElement trace = (TmfTraceElement) receiver;
                return trace.hasSupplementaryResources();
            } else if (receiver instanceof TmfExperimentElement) {
                TmfExperimentElement trace = (TmfExperimentElement) receiver;
                boolean hasHistory = false;
                for (TmfTraceElement aTrace : trace.getTraces()) {
                    hasHistory |= aTrace.hasSupplementaryResources();
                }
                hasHistory |= trace.hasSupplementaryResources();
                return hasHistory;
            }
            return false;
        }

        // Check if the trace element is of a specific trace type
        if (TRACE_TYPE.equals(property)) {
            if (receiver != null && receiver instanceof TmfTraceElement) {
                TmfTraceElement trace = (TmfTraceElement) receiver;
                if (expectedValue instanceof String && expectedValue.equals(trace.getTraceType())) {
                    return true;
                }
            }
            return false;
        }

        return false;
    }

}
