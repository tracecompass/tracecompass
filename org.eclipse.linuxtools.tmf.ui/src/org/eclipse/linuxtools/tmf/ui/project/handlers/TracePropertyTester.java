/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.project.handlers;

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
    
    private static String isInTraceFolder = "isInTraceFolder"; //$NON-NLS-1$
    private static String isExperimentTrace = "isExperimentTrace"; //$NON-NLS-1$
    
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

    /* (non-Javadoc)
     * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[], java.lang.Object)
     */
    @Override
    public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

        // Check if the selected elements are in the trace folder
        if (isInTraceFolder.equals(property)) {
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
        if (isExperimentTrace.equals(property)) {
            if (receiver != null && receiver instanceof TmfTraceElement) {
            TmfTraceElement trace = (TmfTraceElement) receiver;
                return trace.getParent() instanceof TmfExperimentElement;
            }
            return false;
        }

        return false;
    }

}
