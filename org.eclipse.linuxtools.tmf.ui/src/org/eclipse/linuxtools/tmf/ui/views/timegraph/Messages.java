/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.timegraph;

import org.eclipse.osgi.util.NLS;

/**
 * Generic messages for the bar charts
 *
 * @since 2.1
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.tmf.ui.views.timegraph.messages"; //$NON-NLS-1$

    public static String AbstractTimeGraphtView_NextText;
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
