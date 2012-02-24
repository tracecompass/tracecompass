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

package org.eclipse.linuxtools.tmf.ui;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfExperimentElement;
import org.eclipse.linuxtools.tmf.ui.project.model.TmfTraceElement;

/**
 * <b><u>TracePropertyTester</u></b>
 * <p>
 */
public class TracePropertyTester extends PropertyTester {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    
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
        
        // Check if the parent of a trace element is an experiment
        if (receiver != null && receiver instanceof TmfTraceElement) {
            TmfTraceElement trace = (TmfTraceElement) receiver;
            if (isExperimentTrace.equals(property)) {
                return trace.getParent() instanceof TmfExperimentElement;
            }
        }

        return false;
    }

}
