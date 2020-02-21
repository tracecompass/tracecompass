/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.control.ui.tests;

import org.eclipse.core.runtime.Plugin;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.service.LTTngControlServiceMI;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     *  The plug-in ID
     */
    public static final String PLUGIN_ID = "org.eclipse.tracecompass.lttng2.control.ui.tests";

    // The shared instance
    private static Activator fPlugin;

    /**
     * The constructor
     */
    public Activator() {
        setDefault(this);
    }

    /**
     * @return the shared instance
     */
    public static Activator getDefault() {
        return fPlugin;
    }

    /**
     * @param plugin the shared instance
     */
    private static void setDefault(Activator plugin) {
        fPlugin = plugin;
    }

    @Override
    public void start(BundleContext context) throws Exception {
        super.start(context);
        setDefault(this);
        String systemProperty = System.getProperty(LTTngControlServiceMI.MI_SCHEMA_VALIDATION_KEY);
        if (systemProperty == null) {
            System.setProperty(LTTngControlServiceMI.MI_SCHEMA_VALIDATION_KEY, Boolean.TRUE.toString());
        }
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        setDefault(null);
        super.stop(context);
    }

}
