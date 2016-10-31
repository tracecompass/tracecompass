/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.datastore.core.tests;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Plugin's activator
 */
public class BackendTestPlugin implements BundleActivator {

    /**
     * The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.tracecompass.datastore.core.tests";

    private static BundleContext fContext;

    /**
     * Gets the bundle of this plug-in.
     *
     * @return the bundle
     */
    public static Bundle getBundle() {
        if (fContext == null) {
            return null;
        }
        return fContext.getBundle();
    }

    @Override
    public void start(BundleContext context) throws Exception {
        fContext = context;
    }

    @Override
    public void stop(BundleContext context) throws Exception {

    }

}
