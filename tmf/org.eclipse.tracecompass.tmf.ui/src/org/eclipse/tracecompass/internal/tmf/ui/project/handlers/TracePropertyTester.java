/*******************************************************************************
 * Copyright (c) 2012, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.project.handlers;

import java.util.Iterator;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfCommonProjectElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfProjectModelElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceElement;
import org.eclipse.tracecompass.tmf.ui.project.model.TmfTraceFolder;

/**
 * <b><u>TracePropertyTester</u></b>
 * <p>
 */
public class TracePropertyTester extends PropertyTester {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final String IS_IN_TRACE_FOLDER = "isInTraceFolder"; //$NON-NLS-1$
    private static final String IS_EXPERIMENT_TRACE = "isExperimentTrace"; //$NON-NLS-1$
    private static final String HAS_SUPPLEMENTARY_FILES = "hasSupplementaryFiles"; //$NON-NLS-1$
    private static final String TRACE_TYPE = "traceType"; //$NON-NLS-1$
    private static final String IS_SAME_PROJECT = "isSameProject"; //$NON-NLS-1$

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
            if (receiver instanceof IStructuredSelection) {
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
            if (receiver instanceof TmfTraceElement) {
                TmfTraceElement trace = (TmfTraceElement) receiver;
                return trace.getParent() instanceof TmfExperimentElement;
            }
            return false;
        }

        // Check if traces has supplementary files
        if (HAS_SUPPLEMENTARY_FILES.equals(property)) {
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
            if (receiver instanceof TmfCommonProjectElement) {
                TmfCommonProjectElement trace = (TmfCommonProjectElement) receiver;
                if (expectedValue instanceof String && expectedValue.equals(trace.getTraceType())) {
                    return true;
                }
            }
            return false;
        }

        // Check if the selected elements are all in the same project
        if (IS_SAME_PROJECT.equals(property)) {
            if (receiver instanceof IStructuredSelection) {
                IProject sameProject = null;
                for (Object element : ((IStructuredSelection) receiver).toList()) {
                    if (element instanceof TmfProjectModelElement) {
                        IProject project = ((TmfProjectModelElement) element).getProject().getResource();
                        if (sameProject == null) {
                            sameProject = project;
                        } else if (!sameProject.equals(project)) {
                            return false;
                        }
                    } else {
                        return false;
                    }
                }
                return true;
            }
        }

        return false;
    }

}
