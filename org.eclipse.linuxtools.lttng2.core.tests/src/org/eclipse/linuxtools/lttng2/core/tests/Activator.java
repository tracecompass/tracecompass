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

package org.eclipse.linuxtools.lttng2.core.tests;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
/**
 * <b><u>Activator</u></b>
 * <p>
 * The activator class controls the plug-in life cycle
 */
public class Activator implements BundleActivator {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.linuxtools.lttng2.core.tests"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    /**
     * The bundle context
     */
    private static BundleContext context;

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    static BundleContext getContext() {
        return context;
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
     */
    @Override
    public void start(BundleContext bundleContext) throws Exception {
        Activator.context = bundleContext;
    }

    /*
     * (non-Javadoc)
     * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
     */
    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        Activator.context = null;
    }

}
