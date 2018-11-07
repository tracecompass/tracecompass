/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.common.core;

import org.eclipse.tracecompass.common.core.TraceCompassActivator;

/**
 * Plugin activator
 */
public class Activator extends TraceCompassActivator {

    private static final String PLUGIN_ID = "org.eclipse.tracecompass.common.core"; //$NON-NLS-1$

    /**
     * Return the singleton instance of this activator.
     *
     * @return The singleton instance
     */
    public static Activator instance() {
        return (Activator) TraceCompassActivator.getInstance(PLUGIN_ID);
    }

    /**
     * Constructor
     */
    public Activator() {
        super(PLUGIN_ID);
    }

    @Override
    protected void startActions() {
        // Do nothing
    }

    @Override
    protected void stopActions() {
        // Do nothing
    }

}
