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

package org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.cpuusage;

import org.eclipse.osgi.util.NLS;

/**
 * Messages used in the LTTng kernel CPU usage view and viewers.
 *
 * @author Geneviève Bastien
 */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng2.kernel.ui.views.cpuusage.messages"; //$NON-NLS-1$
    public static String CpuUsageComposite_ColumnPercent;
    public static String CpuUsageComposite_ColumnProcess;
    public static String CpuUsageComposite_ColumnTID;
    public static String CpuUsageComposite_ColumnTime;
    public static String CpuUsageComposite_TextPercent;
    public static String CpuUsageComposite_TextTime;
    public static String CpuUsageView_Title;
    public static String CpuUsageXYViewer_CpuYAxis;
    public static String CpuUsageXYViewer_TimeXAxis;
    public static String CpuUsageXYViewer_Title;
    public static String CpuUsageXYViewer_Total;
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
