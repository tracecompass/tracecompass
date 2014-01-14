/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.lttng2.ust.ui.views.memusage;

import org.eclipse.osgi.util.NLS;

/**
 * Translatable strings for the ust memory usage view
 *
 * @author Geneviève Bastien
 */
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng2.ust.ui.views.memusage.messages"; //$NON-NLS-1$
    /** Title of the memory usage xy view */
    public static String MemoryUsageView_Title;

    /** Title of the memory viewer */
    public static String MemoryUsageViewer_Title;
    /** X axis caption */
    public static String MemoryUsageViewer_XAxis;
    /** Y axis caption */
    public static String MemoryUsageViewer_YAxis;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
