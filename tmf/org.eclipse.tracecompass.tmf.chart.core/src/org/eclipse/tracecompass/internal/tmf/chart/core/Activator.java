/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.chart.core;

import org.eclipse.tracecompass.common.core.TraceCompassActivator;

/**
 * The activator class controls the plug-in life cycle
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public class Activator extends TraceCompassActivator {

    private static final String PLUGIN_ID = "org.eclipse.tracecompass.tmf.chart.core"; //$NON-NLS-1$

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
    }

    @Override
    protected void stopActions() {
    }
}
