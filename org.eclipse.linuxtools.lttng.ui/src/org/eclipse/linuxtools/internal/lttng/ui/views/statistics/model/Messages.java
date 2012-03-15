package org.eclipse.linuxtools.internal.lttng.ui.views.statistics.model;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.eclipse.linuxtools.internal.lttng.ui.views.statistics.model.messages"; //$NON-NLS-1$
    public static String KernelStatisticsData_CPUs;
    public static String KernelStatisticsData_EventTypes;
    public static String KernelStatisticsData_Functions;
    public static String KernelStatisticsData_Modes;
    public static String KernelStatisticsData_Processes;
    public static String KernelStatisticsData_SubModes;
    public static String StatisticsData_UnknowProcess;
    static {
	// initialize resource bundle
	NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }
}
