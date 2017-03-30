/*******************************************************************************
 * Copyright (c) 2013, 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.timegraph;

import org.eclipse.osgi.util.NLS;

/**
 * Generic messages for the bar charts
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.tmf.ui.views.timegraph.messages"; //$NON-NLS-1$

    public static String AbstractTimeGraphtView_NextText;
    /**
     * Build Job title
     * @since 2.1
     */
    public static String AbstractTimeGraphView_BuildJob;

    /** @since 2.4*/
    public static String AbstractTimeGraphView_MarkerSetEditActionText;
    /** @since 2.4*/
    public static String AbstractTimeGraphView_MarkerSetMenuText;
    /** @since 2.4*/
    public static String AbstractTimeGraphView_MarkerSetNoneActionText;
    public static String AbstractTimeGraphView_NextTooltip;
    public static String AbstractTimeGraphView_PreviousText;
    public static String AbstractTimeGraphView_PreviousTooltip;
    public static String TimeGraphPresentationProvider_multipleStates;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
