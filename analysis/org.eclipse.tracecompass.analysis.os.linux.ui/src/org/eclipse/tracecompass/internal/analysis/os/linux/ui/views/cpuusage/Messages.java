/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.cpuusage;

import org.eclipse.osgi.util.NLS;

/**
 * Messages used in the LTTng kernel CPU usage view and viewers.
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.cpuusage.messages"; //$NON-NLS-1$

    public static String CpuUsageComposite_ColumnLegend;
    public static String CpuUsageView_Title;
    public static String CpuUsageXYViewer_CpuYAxis;
    public static String CpuUsageXYViewer_TimeXAxis;
    public static String CpuUsageXYViewer_Title;

    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
